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
public class Spell_IceSheet extends Spell
{
	public String ID() { return "Spell_IceSheet"; }
	public String name(){return "Ice Sheet";}
	public String displayText(){return "(Ice Sheet spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The ice sheet melts.");
		super.unInvoke();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
		   return false;
		Room room=(Room)affected;
		if(msg.source().location()==room)
		{
			MOB mob=msg.source();
			if(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			{
				if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER))
				{
					mob.tell("You are frozen in the ice sheet and can't even blink.");
					return false;
				}
				else
				if((msg.sourceMajor(CMMsg.MASK_MOVE)))
				{
					if((!CMLib.flags().isInFlight(mob))
					&&(CMLib.dice().rollPercentage()>((msg.source().charStats().getStat(CharStats.STAT_DEXTERITY)*3)+25)))
					{
						int oldDisposition=mob.basePhyStats().disposition();
						oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING));
						mob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
						mob.recoverPhyStats();
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> slip(s) on the ice.");
						return false;
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Room)))
		   return;
		super.executeMsg(myHost,msg);
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			MOB mob=msg.source();
			Room room=(Room)affected;
			msg.addTrailerMsg(CMClass.getMsg(mob,room,null,CMMsg.MSG_OK_VISUAL,"\n\r<T-NAME> is covered in ice.",null,null));
		}
	}
	
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// sleeping for a room disables any special characteristic (as of water)
		if(affected instanceof Room)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,"An Ice Sheet is already here!");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			String msgStr="the ground becomes covered in ice!";
			if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
				msgStr="the water freezes over!";
			if(auto)msgStr=Character.toUpperCase(msgStr.charAt(0))+msgStr.substring(1);
			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto),(auto?"":"^S<S-NAME> speak(s) and gesture(s) and ")+msgStr+"^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) about the cold, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
