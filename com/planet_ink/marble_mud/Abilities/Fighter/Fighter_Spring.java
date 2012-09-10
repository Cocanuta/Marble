package com.planet_ink.marble_mud.Abilities.Fighter;
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
public class Fighter_Spring extends FighterSkill
{
	public String ID() { return "Fighter_Spring"; }
	public String name(){ return "Spring Attack";}
	private static final String[] triggerStrings = {"SPRINGATTACK","SPRING","SATTACK"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;}
	public int usageType(){return USAGE_MOVEMENT;}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.curState().getMovement()<50)
				return Ability.QUALITY_INDIFFERENT;
			if(mob.rangeToTarget()>=mob.location().maxRange())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to make a spring attack!");
			return false;
		}
		if(mob.curState().getMovement()<50)
		{
			mob.tell("You are too tired to make a spring attack.");
			return false;
		}
		if(mob.rangeToTarget()>=mob.location().maxRange())
		{
			mob.tell("There is no more room to spring back!");
			return false;
		}

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
				if(mob.getVictim()==target)
				{
					msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_RETREAT,"^F^<FIGHT^><S-NAME> spring(s) back!^</FIGHT^>^?");
					CMLib.color().fixSourceFightColor(msg);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(mob.rangeToTarget()<mob.location().maxRange())
						{
							msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_RETREAT,null);
							if(mob.location().okMessage(mob,msg))
								mob.location().send(mob,msg);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to spring attack <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
