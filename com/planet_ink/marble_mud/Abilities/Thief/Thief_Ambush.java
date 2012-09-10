package com.planet_ink.marble_mud.Abilities.Thief;
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
public class Thief_Ambush extends ThiefSkill
{
	public String ID() { return "Thief_Ambush"; }
	public String name(){ return "Ambush";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"AMBUSH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect("Thief_Hide")!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
		if(!H.contains(mob)) H.add(mob);
		int numBesidesMe=0;
		for(Iterator e=H.iterator();e.hasNext();)
		{
			MOB M=(MOB)e.next();
			if((M!=mob)&&(mob.location().isInhabitant(M)))
				numBesidesMe++;
		}
		if(numBesidesMe==0)
		{
			mob.tell("You need a group to set up an ambush!");
			return false;
		}
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&(M!=mob)&&(!H.contains(M))&&(CMLib.flags().canSee(M)))
			{
				mob.tell(M,null,null,"<S-NAME> is watching you too closely.");
				return false;
			}
		}
		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to set up an ambush, but fail(s).");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),"<S-NAME> set(s) up an ambush, directing everyone to hiding places.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability hide=CMClass.getAbility("Thief_Hide");
				for(Iterator e=H.iterator();e.hasNext();)
				{
					MOB M=(MOB)e.next();
					hide.invoke(M,M,true,adjustedLevel(mob,asLevel));
				}
			}
			else
				success=false;
		}
		return success;
	}
}
