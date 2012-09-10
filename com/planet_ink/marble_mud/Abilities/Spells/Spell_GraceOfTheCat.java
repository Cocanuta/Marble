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
public class Spell_GraceOfTheCat extends Spell
{
	public String ID() { return "Spell_GraceOfTheCat"; }
	public String name(){return "Grace Of The Cat";}
	public String displayText(){return "(Grace Of The Cat)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}
	int increase = -1;
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(increase <= 0)
		{
			increase = 1;
			if (affectableStats.getCurrentClass().baseClass().equals("Thief"))
				increase = 2;
			if (affectableStats.getCurrentClass().baseClass().equals("Bard"))
				increase = 2;
			if (affectableStats.getCurrentClass().baseClass().equals("Fighter"))
				increase = 3;
			if (affectableStats.getCurrentClass().baseClass().equals("Mage"))
				increase = 2;
			if (affectableStats.getCurrentClass().baseClass().equals("Cleric"))
				increase = 1;
			if (affectableStats.getCurrentClass().baseClass().equals("Druid"))
				increase = 1;
			increase += (getXLEVELLevel(invoker())+2)/3;
		}
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY) + increase);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You begin to feel more like your regular clumsy self.");
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> speak(s) and gesture(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> move(s) more gracefully!");
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) gracefully to <T-NAMESELF>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
