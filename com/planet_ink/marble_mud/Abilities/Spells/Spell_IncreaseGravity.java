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
public class Spell_IncreaseGravity extends Spell
{
	public String ID() { return "Spell_IncreaseGravity"; }
	public String name(){return "Increase Gravity";}
	public String displayText(){return "(Gravity is Increased)";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS|CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected Room theGravityRoom=null;
	protected Room gravityRoom()
	{
		if(theGravityRoom!=null)
			return theGravityRoom;
		if(affected instanceof Room)
			theGravityRoom=(Room)affected;
		return theGravityRoom;
	}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		else
		if((affected!=null)&&(affected instanceof Room)&&(invoker!=null))
		{
			Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if(inhab.fetchEffect(ID())==null)
				{
					Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					A.startTickDown(invoker,inhab,tickDown);
				}
				if(inhab.isInCombat())
					inhab.curState().adjMovement(-1,inhab.maxState());
			}
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(CMMsg.MSG_OK_VISUAL, "Gravity returns to normal...");
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if((mob.location()!=null)&&(mob.location()!=gravityRoom()))
					mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, "Your weight returns to normal..");
			}
		}
		super.unInvoke();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((((MOB)affected).location()!=gravityRoom())
			||((gravityRoom()!=null)&&(gravityRoom().fetchEffect(ID())==null)))
			{
				unInvoke();
				return true;
			}
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ADVANCE:
			{
				msg.source().tell("You feel too heavy to advance.");
				return false;
			}
		case CMMsg.TYP_RETREAT:
			{
				msg.source().tell("You feel too heavy to retreat.");
				return false;
			}
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			{
				msg.source().tell("You feel too heavy to leave.");
				return false;
			}
		}
		return true;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(((MOB)affected).location()!=gravityRoom())
			unInvoke();
		else
		{
			if((affectableStats.disposition()&PhyStats.IS_FLYING)>0)
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_FLYING);
			affectableStats.setWeight(affectableStats.weight()*2);
		}
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
			mob.tell(mob,null,null,"Gravity has already been increased here!");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), (auto?"G":"^S<S-NAME> speak(s) and wave(s) and g")+"ravity begins to increase!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				theGravityRoom=mob.location();
				beneficialAffect(mob,mob.location(),asLevel,adjustedLevel(mob,asLevel));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) heavily, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
