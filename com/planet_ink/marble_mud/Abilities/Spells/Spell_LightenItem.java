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
public class Spell_LightenItem extends Spell
{
	public String ID() { return "Spell_LightenItem"; }
	public String name(){return "Lighten Item";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		if((affected!=null)&&(affected instanceof Item))
		{
			Item item=(Item)affected;
			if((item.owner()!=null)
			&&(item.owner() instanceof MOB)
			&&(((MOB)item.owner()).isMine(item)))
			{
				MOB mob=(MOB)item.owner();
				mob.tell(item.name()+" grows heavy again.");
				if((mob.phyStats().weight()+item.basePhyStats().weight())>mob.maxCarry())
				{
					if(!item.amWearingAt(Wearable.IN_INVENTORY))
						CMLib.commands().postRemove(mob,item,false);
					if(item.amWearingAt(Wearable.IN_INVENTORY))
						CMLib.commands().postDrop(mob,item,false,false);
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
		{
			String str=CMParms.combine(commands,0).toUpperCase();
			if(str.equals("MONEY")||str.equals("GOLD")||str.equals("COINS"))
				mob.tell("You can't cast this spell on your own coins.");
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already light!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> grow(s) much lighter.");
				beneficialAffect(mob,target,asLevel,100);
				target.recoverPhyStats();
				mob.recoverPhyStats();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}
