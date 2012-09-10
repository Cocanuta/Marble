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
public class Chant_PlantBed extends Chant
{
	public String ID() { return "Chant_PlantBed"; }
	public String name(){ return "Plant Bed";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected Item peaPod=null;

	public void unInvoke()
	{
		super.unInvoke();
		if(peaPod!=null)
		{
			Room R=CMLib.map().roomLocation(peaPod);
			if(R!=null)
				R.showHappens(CMMsg.MSG_OK_VISUAL,"A pea-pod shrivels up!");
			Rideable RI=(Rideable)peaPod;
			for(int r=RI.numRiders()-1;r>=0;r--)
				RI.fetchRider(r).setRiding(null);
			peaPod.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((((mob.location().domainType()&Room.INDOORS)>0)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_DESERT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR))
		&&(!auto))
		{
			mob.tell("This chant will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=CMClass.getItem("GenBed");
				Rideable newRide=(Rideable)newItem;
				newItem.setName("a plant bed");
				newItem.setDisplayText("A enormously comfortable pea-pod looks ready to sleep in.");
				newItem.setDescription("The plant bed looks like a hollowed pea-pod with fern-like cushioning inside.  Looks like a nice place to take a nap in!");
				newRide.setRideBasis(Rideable.RIDEABLE_SLEEP);
				newRide.setRiderCapacity(1);
				newItem.setMaterial(RawMaterial.RESOURCE_HEMP);
				newItem.basePhyStats().setWeight(1000);
				newItem.setBaseValue(0);
				CMLib.flags().setGettable(newItem,false);
				Ability A=CMClass.getAbility("Prop_RideResister");
				A.setMiscText("disease poison");
				newItem.addNonUninvokableEffect(A);
				newItem.recoverPhyStats();
				newItem.setMiscText(newItem.text());
				peaPod=newItem;
				mob.location().addItem(newItem,ItemPossessor.Expire.Resource);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A comfortable pea-pod bed grows nearby.");
				mob.location().recoverPhyStats();
				beneficialAffect(mob,newItem,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
