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
public class Prayer_Purify extends Prayer
{
	public String ID() { return "Prayer_Purify"; }
	public String name(){ return "Purify";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	public int classificationCode(){return ((affecting() instanceof Food)&&(!canBeUninvoked()))?Ability.ACODE_PROPERTY:Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}

	public void affectPhyStats(Physical affecting, PhyStats stats)
	{
		if((affecting instanceof Decayable)&&(((Decayable)affecting).decayTime()>0))
			((Decayable)affecting).setDecayTime(0);
		super.affectPhyStats(affecting,stats);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if((!(target instanceof Food))
			&&(!(target instanceof Drink)))
		{
			mob.tell("You cannot purify "+target.name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
									auto?"":"^S<S-NAME> purify <T-NAMESELF>"+inTheNameOf(mob)+".^?",
									auto?"":"^S<S-NAME> purifies <T-NAMESELF>"+inTheNameOf(mob)+".^?",
									auto?"":"^S<S-NAME> purifies <T-NAMESELF>"+inTheNameOf(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean doneSomething=false;
				if((target instanceof Drink)&&(((Drink)target).liquidType()!=RawMaterial.RESOURCE_FRESHWATER))
				{
					((Drink)target).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
					doneSomething=true;
					target.basePhyStats().setAbility(0);
					target.recoverPhyStats();
				}
				if(target.numEffects()>0) // personal affects
				{
					doneSomething=true;
					target.delAllEffects(true);
				}
				if((target instanceof Pill)
				&&(!((Pill)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Pill)target).setSpellList("Prayer_Sober");
				}
				if((target instanceof Potion)
				&&(!((Potion)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Potion)target).setSpellList("Prayer_Sober");
				}
				if(doneSomething)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" appears purified!");
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for purification, but nothing happens.");
		// return whether it worked
		return success;
	}
}
