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
public class Chant_VineWeave extends Chant
{
	public String ID() { return "Chant_VineWeave"; }
	public String name(){ return "Vine Weave";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.location().resourceChoices()==null)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if(((mob.location().myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
		&&((mob.location().myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_COTTON)))
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_SILK)))
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_HEMP)))
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_VINE)))
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_WHEAT)))
		&&(!mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_SEAWEED))))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=RawMaterial.RESOURCE_VINE;
		if(mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_VINE)))
			material=RawMaterial.RESOURCE_VINE;
		else
		if(mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_SILK)))
			material=RawMaterial.RESOURCE_SILK;
		else
		if(mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_HEMP)))
			material=RawMaterial.RESOURCE_HEMP;
		else
		if(mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_WHEAT)))
			material=RawMaterial.RESOURCE_WHEAT;
		else
		if(mob.location().resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_SEAWEED)))
			material=RawMaterial.RESOURCE_SEAWEED;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the plants.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				ItemCraftor A=(ItemCraftor)CMClass.getAbility("Weaving");
				ItemCraftor.ItemKeyPair pair=null;
				if(A!=null) pair=A.craftAnyItem(material);
				if(pair==null)
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}
				Item building=pair.item;
				Item key=pair.key;
				mob.location().addItem(building,ItemPossessor.Expire.Resource);
				if(key!=null) mob.location().addItem(key,ItemPossessor.Expire.Resource);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" twists out of some vines and grows still.");
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the plants, but nothing happens.");

		// return whether it worked
		return success;
	}
}
