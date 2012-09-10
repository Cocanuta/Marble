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
public class Spell_FaerieFog extends Spell
{
	public String ID() { return "Spell_FaerieFog"; }
	public String name(){return "Faerie Fog";}
	public String displayText(){return "(Faerie Fog)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode() {	return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	private Room theRoom=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if(canBeUninvoked())
		{
			Room room=(Room)affected;
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The faerie fog starts to clear out.");
		}
		super.unInvoke();
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)||(affected instanceof Item))
		{
			Room R=CMLib.map().roomLocation(affected);
			if((R!=null)&&(R==theRoom)&&(!unInvoked)&&(R.fetchEffect(ID())==this))
			{
				if((affectableStats.disposition()&PhyStats.IS_INVISIBLE)==PhyStats.IS_INVISIBLE)
					affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_INVISIBLE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GLOWING);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
				affectableStats.setArmor(affectableStats.armor()+10);
			}
			else
			{
				affected.delEffect(this);
				affected.recoverPhyStats();
			}
		}
		else
		if((affected instanceof Room)&&(!unInvoked))
		{
			Room R=(Room)affected;
			theRoom=R;
			MOB M=null;
			for(int i=0;i<R.numInhabitants();i++)
			{
				M=R.fetchInhabitant(i);
				if((M!=null)&&(M.fetchEffect(ID())==null))
				{
					M.addEffect(this);
					setAffectedOne(R);
				}
			}
			Item I=null;
			for(int i=0;i<R.numItems();i++)
			{
				I=R.getItem(i);
				if((I!=null)&&(I.fetchEffect(ID())==null))
				{
					I.addEffect(this);
					setAffectedOne(R);
				}
			}
		}
				
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!CMLib.flags().isInvisible(target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
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
			mob.tell(mob,null,null,"A faerie fog is already here.");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto),(auto?"A ":"^S<S-NAME> speak(s) and gesture(s) and a ")+"sparkling fog envelopes the area.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> mutter(s) about a faerie fog, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
