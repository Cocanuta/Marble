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
@SuppressWarnings("rawtypes")
public class Skill_FalseArrest extends BardSkill
{
	public String ID() { return "Skill_FalseArrest"; }
	public String name(){ return "False Arrest";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"FALSEARREST"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 50;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob==target)
		{
			mob.tell("Arrest whom?!");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		LegalBehavior B=null;
		Area A2=null;
		if(mob.location()!=null)
		{
			B=CMLib.law().getLegalBehavior(mob.location());
			if((B==null)||(!B.hasWarrant(CMLib.law().getLegalObject(mob.location()),target)))
				B=null;
			else
				A2=CMLib.law().getLegalObject(mob.location());
		}

		if(B==null)
		for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			if(CMLib.flags().canAccess(mob,A))
			{
				B=CMLib.law().getLegalBehavior(A);
				if((B!=null)
				&&(B.hasWarrant(CMLib.law().getLegalObject(A),target)))
				{
					A2=CMLib.law().getLegalObject(A);
					break;
				}
			}
			B=null;
			A2=null;
		}

		if(B==null)
		{
			mob.tell(target.name()+" is not wanted for anything, anywhere.");
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
			beneficialWordsFizzle(mob,target,"<S-NAME> frown(s) at <T-NAMESELF>, but lose(s) the nerve.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> frown(s) at <T-NAMESELF>.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(!B.arrest(A2,mob,target))
			{
				mob.tell("You are not able to arrest "+target.name()+" at this time.");
				return false;
			}
		}
		return success;
	}

}
