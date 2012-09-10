package com.planet_ink.marble_mud.Abilities.Druid;
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

import java.util.Vector;


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
public class Chant_DeepDarkness extends Chant
{
	public String ID() { return "Chant_DeepDarkness"; }
	public String name(){return "Deep Darkness";}
	public String displayText(){return "(Deep Darkness spell)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			room.recoverRoomStats();
			room.recoverRoomStats();
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The deep darkness starts to lift.");
		}
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_SPELL_AFFECT)
		&&(affected instanceof Room)
		&&(affected.fetchEffect(ID())==this)
		&&(affected.fetchEffect(affected.numEffects()-1)!=this))
		{
			affected.delEffect(this);
			affected.addEffect(this);
			((Room)affected).recoverRoomStats();
			((Room)affected).recoverRoomStats();
		}
		return super.tick(ticking,tickID);
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(CMLib.flags().isGlowing(affected))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_GLOWING);
		if(CMLib.flags().isLightSource(affected))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_LIGHTSOURCE);
		affectableStats.setDisposition(affectableStats.disposition() |  PhyStats.IS_DARK);
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
			mob.tell(mob,null,null,"Deep Darkness is already been here!");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target,this,verbalCastCode(mob,target,auto), (auto?"D":"^S<S-NAME> chant(s) deeply and d")+"arkness descends.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) deeply, but nothing happens.");

		// return whether it worked
		return success;
	}
}
