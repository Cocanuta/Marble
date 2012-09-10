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
public class Spell_Forget extends Spell
{
	public String ID() { return "Spell_Forget"; }
	public String name(){return "Forget";}
	public String displayText(){return "(Forgetful)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public HashSet<Ability> forgotten=new HashSet<Ability>();
	public HashSet<Ability> remember=new HashSet<Ability>();

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.tool() instanceof Ability))
		{
			if(remember.contains(msg.tool()))
				return true;
			if(forgotten.contains(msg.tool()))
			{
				mob.tell("You still can't remember "+msg.tool().name()+"!");
				return false;
			}
			if(mob.fetchAbility(msg.tool().ID())==msg.tool())
			{
				if(CMLib.dice().rollPercentage()>(mob.charStats().getSave(CharStats.STAT_SAVE_MIND)+10))
				{
					forgotten.add((Ability)msg.tool());
					mob.tell("You can't remember "+msg.tool().name()+"!");
					return false;
				}
				else
					remember.add((Ability)msg.tool());
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You start remembering things again.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,-((target.charStats().getStat(CharStats.STAT_INTELLIGENCE)*2)+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"^S<S-NAME> incant(s) confusingly at <T-NAMESELF>^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),str);
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					success=maliciousAffect(mob,target,asLevel,-levelDiff,-1);
					if(success)
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) forgetful!");
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) confusingly at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
