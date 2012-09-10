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
public class Chant_AntTrain extends Chant
{
	public String ID() { return "Chant_AntTrain"; }
	public String name(){return "Ant Train";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}

	boolean wasntMine=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		if(invoker==null)
			return;

		MOB mob=invoker;
		Item item=(Item)affected;
		super.unInvoke();


		if(canBeUninvoked())
		{
			if(item.amWearingAt(Wearable.WORN_FLOATING_NEARBY))
			{
				if(wasntMine)
					mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME>, is left behind by a departing train of ants.");
				else
					mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME>, is carried back into <S-HIS-HER> hands by a departing train of ants.");
				item.unWear();
			}
			if(wasntMine)
				CMLib.commands().postDrop(mob,item,true,false);
			wasntMine=false;

			item.recoverPhyStats();
			mob.recoverMaxState();
			mob.recoverCharStats();
			mob.recoverPhyStats();
		}
	}


	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_REMOVE))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if((!(target instanceof Item))
		||(!CMLib.flags().isGettable(((Item)target))))
		{
			mob.tell("The ants can't carry "+target.name()+"!");
			return false;
		}

		if(mob.freeWearPositions(Wearable.WORN_FLOATING_NEARBY,(short)0,(short)0)==0)
		{
			mob.tell("There is no more room around you to float anything!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			wasntMine=false;
			if(!mob.isMine(target))
			{
				target.addNonUninvokableEffect(this);
				target.recoverPhyStats();
				wasntMine=true;
				if(target instanceof Coins)
				{
					mob.location().delItem((Item)target);
					mob.addItem((Item)target);
				}
				else
				if(!CMLib.commands().postGet(mob,null,(Item)target,true))
				{
					target.delEffect(this);
					target.recoverPhyStats();
					return false;
				}
				target.delEffect(this);
				target.recoverPhyStats();
			}
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> begin(s) to float around.":"^S<S-NAME> chant(s), and a train of ants appears to carry <T-NAMESELF> for <S-HIM-HER>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				long properWornCode=((Item)target).rawProperLocationBitmap();
				boolean properWornLogical=((Item)target).rawLogicalAnd();
				((Item)target).setRawLogicalAnd(false);
				((Item)target).setRawProperLocationBitmap(Wearable.WORN_FLOATING_NEARBY);
				((Item)target).wearAt(Wearable.WORN_FLOATING_NEARBY);
				((Item)target).setRawLogicalAnd(properWornLogical);
				((Item)target).setRawProperLocationBitmap(properWornCode);
				((Item)target).recoverPhyStats();
				beneficialAffect(mob,target,asLevel,(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)))*10);
				mob.recoverPhyStats();
				mob.recoverMaxState();
				mob.recoverCharStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for ants, but fail(s).");



		// return whether it worked
		return success;
	}
}
