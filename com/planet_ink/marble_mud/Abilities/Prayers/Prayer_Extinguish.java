package com.planet_ink.marble_mud.Abilities.Prayers;
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
public class Prayer_Extinguish extends Prayer
{
	public String ID() { return "Prayer_Extinguish"; }
	public String name(){ return "Extinguish";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget, Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" to extinguish <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.utensils().extinguish(mob,target,false);
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" to extinguish <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
