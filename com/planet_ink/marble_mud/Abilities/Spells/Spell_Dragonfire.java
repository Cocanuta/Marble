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
public class Spell_Dragonfire extends Spell
{
	public String ID() { return "Spell_Dragonfire"; }
	public String name(){return "Dragonfire";}
	public String displayText(){return "(Dragonfire)";}
	public int maxRange(){return adjustedMaxInvokerRange(3);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	public long flags(){return Ability.FLAG_FIREBASED;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth burning.");
			return false;
		}
		
		if(!CMLib.flags().canBreathe(mob))
		{
			mob.tell("You can't breathe!");
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

			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"A blast of flames erupt!":"^S<S-NAME> blast(s) flames from <S-HIS-HER> mouth!^?")+CMProps.msp("fireball.wav",40)))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_ALWAYS:0),null);
				if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int maxDie = adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob));
					int damage = CMLib.dice().roll(maxDie,4,maxDie);
					if((msg.value()>0)||(msg2.value()>0))
						damage = (int)Math.round(CMath.div(damage,2.0));
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The dragonfire <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.");


		// return whether it worked
		return success;
	}
}
