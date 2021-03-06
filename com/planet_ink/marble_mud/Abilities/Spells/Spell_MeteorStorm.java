package com.planet_ink.marble_mud.Abilities.Spells;
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
public class Spell_MeteorStorm extends Spell
{
	public String ID() { return "Spell_MeteorStorm"; }
	public String name(){return "Meteor Storm";}
	public int maxRange(){return adjustedMaxInvokerRange(5);}
	public int minRange(){return 1;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth storming at.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"A devastating meteor shower erupts!":"^S<S-NAME> conjur(s) up a devastating meteor shower!^?")+CMProps.msp("meteor.wav",40)))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					invoker=mob;

					int damage = 0;
					int maxDie=(int)Math.round(CMath.div(adjustedLevel(mob,asLevel),3.0));
					damage = CMLib.dice().roll(maxDie,6,maxDie);
					if(msg.value()<=0)
						damage = (int)Math.round(CMath.div(damage,2.0));
					if(target.location()==mob.location())
						CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BASHING,"The meteors <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
