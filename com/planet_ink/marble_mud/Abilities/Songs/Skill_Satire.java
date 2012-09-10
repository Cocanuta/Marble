package com.planet_ink.marble_mud.Abilities.Songs;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;


import java.util.*;

/* 


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings({"unchecked","rawtypes"})
public class Skill_Satire extends BardSkill
{
	public String ID() { return "Skill_Satire"; }
	public String name(){ return "Satire";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"SATIRE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;}

	public void criminalFail(LegalBehavior B, Area A2, MOB mob, MOB witness)
	{
		String crime="disrespect for the law";
		String desc="Everyone should respect the law.";
		String crimeLocs="";
		String crimeFlags="!witness";
		String sentence=Law.PUNISHMENT_DESCS[0];
		B.addWarrant(A2,mob,witness,crimeLocs,crimeFlags,crime,sentence,desc);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob==target)
		{
			mob.tell("Mock whom?!");
			return false;
		}
		LegalBehavior B=null;
		Area A2=null;
		Vector<MOB> forgivables=new Vector();
		final Room room=mob.location();
		if(room==null)
		{
			return false;
		}
		B=CMLib.law().getLegalBehavior(room);
		A2=CMLib.law().getLegalObject(room);
		if((B==null)||((!B.isAnyOfficer(A2, target))&&(!B.isJudge(A2, target))))
		{
			mob.tell(mob,target,null,"<T-NAME> is not an officer here.");
			return false;
		}
		Set<MOB> group = mob.getGroupMembers(new HashSet<MOB>());
		if(B!=null)
			for(MOB M : group)
			{
				if((M.location()==room)
				&&(M!=mob)
				&& B.hasWarrant(A2,M))
				{
					forgivables.add(M);
				}
			}

		if(!CMLib.flags().canBeHeardSpeakingBy(mob, target))
		{
			mob.tell(mob,target,null,"<T-NAME> can't hear you.");
			return false;
		}
		
		if(forgivables.size()==0)
		{
			mob.tell("Noone you know is wanted for anything here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		
		boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(!success)
		{
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to mock <T-NAME>, but <S-IS-ARE> not funny.");
			if(CMLib.dice().rollPercentage()>mob.charStats().getStat(CharStats.STAT_CHARISMA))
				criminalFail(B,A2,mob,target);
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_JUSTICE,"<S-NAME> mock(s) <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(MOB M : forgivables)
			{
				if(B.aquit(A2, M, null))
				{
					room.show(M, target, CMMsg.MSG_OK_VISUAL, "<T-NAME>, smiling, forget(s) <S-YOUPOSS> crime.");
					return false;
				}
			}
			if((msg.value()>0)||(CMLib.dice().rollPercentage()<(25-mob.charStats().getStat(CharStats.STAT_CHARISMA))))
			{
				criminalFail(B,A2,mob,target);
			}
		}
		return success;
	}

}
