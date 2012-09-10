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
public class Spell_Frenzy extends Spell
{
	public String ID() { return "Spell_Frenzy"; }
	public String name(){return "Frenzy";}
	public String displayText(){return "(Frenzy spell)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int hpAdjustment=0;
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
			invoker=(MOB)affected;
		int xlvl=super.getXLEVELLevel(invoker());
		float f=(float)0.1*(float)xlvl;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(CMath.div(affectableStats.damage(),6.0-f)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(CMath.div(affectableStats.attackAdjustment(),6.0-f)));
		affectableStats.setArmor(affectableStats.armor()+20+(2*xlvl));
	}

	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(mob.curState().getHitPoints()<=hpAdjustment)
				mob.curState().setHitPoints(1);
			else
				mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
			mob.tell("You feel calmer.");
			mob.recoverMaxState();
		}
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		Room R=CMLib.map().roomLocation(target);
		if(R==null) R=mob.location();

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> scream(s) at <T-NAMESELF>!^?");
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(target.location()==R)
				{
					R.show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> go(es) wild!");
					hpAdjustment=(int)Math.round(CMath.div(target.maxState().getHitPoints(),5.0));
					beneficialAffect(mob,target,asLevel,0);
					target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
					target.recoverMaxState();
					target.recoverPhyStats();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> scream(s) wildly at <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
