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
public class Prayer_DispelUndead extends Prayer
{
	public String ID() { return "Prayer_DispelUndead"; }
	public String name(){ return "Dispel Undead";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(target.charStats().getMyRace().racialCategory().equals("Undead")))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"The evil inside <T-NAME> is exorcised!":"^S<S-NAME> "+prayForWord(mob)+" to dispel the coldness inside <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)+15,adjustedLevel(mob,asLevel)/2);
				if(msg.value()>0)
					harming=(int)Math.round(CMath.div(harming,2.0));
				CMLib.combat().postDamage(mob,target,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BURSTING,"The holy spell <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
