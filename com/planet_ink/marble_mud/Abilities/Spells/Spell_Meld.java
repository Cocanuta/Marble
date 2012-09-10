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
public class Spell_Meld extends Spell
{
	public String ID() { return "Spell_Meld"; }
	public String name(){return "Meld";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean shinBone(Item one, Item two, long locationOne, long locationTwo)
	{
		if((one.fitsOn(locationOne)&&two.fitsOn(locationTwo))
		   &&(!one.fitsOn(locationTwo))
		   &&(!two.fitsOn(locationOne)))
			return true;
		else
		if((two.fitsOn(locationOne)&&one.fitsOn(locationTwo))
		   &&(!two.fitsOn(locationTwo))
		   &&(!one.fitsOn(locationOne)))
			return true;
		return false;
	}
	int[] heiarchy={RawMaterial.MATERIAL_FLESH,
					RawMaterial.MATERIAL_PAPER,
					RawMaterial.MATERIAL_CLOTH,
					RawMaterial.MATERIAL_LEATHER,
					RawMaterial.MATERIAL_VEGETATION,
					RawMaterial.MATERIAL_WOODEN,
					RawMaterial.MATERIAL_SYNTHETIC,
					RawMaterial.MATERIAL_METAL,
					RawMaterial.MATERIAL_ROCK,
					RawMaterial.MATERIAL_PRECIOUS,
					RawMaterial.MATERIAL_ENERGY,
					RawMaterial.MATERIAL_MITHRIL,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};

	protected int getHeiarchy(int material)
	{
		for(int i=0;i<heiarchy.length;i++)
			if(heiarchy[i]==material) return i;
		return 99;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		// add something to disable traps
		//
		if(commands.size()<2)
		{
			mob.tell("Meld what and what else together?");
			return false;
		}
		Item itemOne=mob.findItem(null,(String)commands.elementAt(0));
		if((itemOne==null)||(!CMLib.flags().canBeSeenBy(itemOne,mob)))
		{
			mob.tell("You don't seem to have a '"+((String)commands.elementAt(0))+"'.");
			return false;
		}
		Item itemTwo=mob.findItem(null,CMParms.combine(commands,1));
		if((itemTwo==null)||(!CMLib.flags().canBeSeenBy(itemTwo,mob)))
		{
			mob.tell("You don't seem to have a '"+CMParms.combine(commands,1)+"'.");
			return false;
		}

		Item melded=null;

		if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
		{
			Armor armorOne=(Armor)itemOne;
			Armor armorTwo=(Armor)itemTwo;
			if(armorOne.getClothingLayer()!=armorTwo.getClothingLayer())
			{
				mob.tell("This spell can only be cast on items worn at the same layer.");
				return false;
			}
			if(armorOne.getLayerAttributes()!=armorTwo.getLayerAttributes())
			{
				mob.tell("Those items are too different to meld together.");
				return false;
			}
			
			if(shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_NECK)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_EARS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_EYES)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_TORSO)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_NECK,Wearable.WORN_TORSO)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_ARMS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_WAIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_WAIST,Wearable.WORN_LEGS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_ARMS,Wearable.WORN_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_ARMS,Wearable.WORN_HANDS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HANDS,Wearable.WORN_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HANDS,Wearable.WORN_RIGHT_FINGER)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_LEGS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_LEGS,Wearable.WORN_FEET))
			{

			}
			else
			{
				mob.tell(itemOne.name()+" and "+itemTwo.name()+" aren't worn in compatible places, and thus can't be melded.");
				return false;
			}
		}
		else
		if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
		{
			if(!itemOne.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(itemOne.name()+" can't be held, and thus can't be melded with "+itemTwo.name()+".");
				return false;
			}
			if(!itemTwo.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(itemTwo.name()+" can't be held, and thus can't be melded with "+itemOne.name()+".");
				return false;
			}
			if(itemOne.rawLogicalAnd())
			{
				mob.tell(itemOne.name()+" is two handed, and thus can't be melded with "+itemTwo.name()+".");
				return false;
			}
			if(itemTwo.rawLogicalAnd())
			{
				mob.tell(itemTwo.name()+" is two handed, and thus can't be melded with "+itemOne.name()+".");
				return false;
			}
		}
		else
		if((itemOne instanceof Container)&&(itemTwo instanceof Container))
		{

		}
		else
		{
			mob.tell("You can't meld those together.");
			return false;
		}

		if(itemOne==itemTwo)
		{
			mob.tell("You can't meld something to itself.");
			return false;
		}

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
			CMMsg msg=CMClass.getMsg(mob,itemOne,this,verbalCastCode(mob,itemOne,auto),"^S<S-NAME> meld(s) "+itemOne.name()+" and "+itemTwo.name()+".^?");
			CMMsg msg2=CMClass.getMsg(mob,itemTwo,this,verbalCastCode(mob,itemOne,auto),null);
			if(mob.location().okMessage(mob,msg)&&mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);

				String itemOneName=itemOne.Name();
				String itemTwoName=itemTwo.Name();
				int x=itemOneName.indexOf("melded together");
				if(x>0) itemOneName=itemOneName.substring(0,x).trim();
				x=itemTwoName.indexOf("melded together");
				if(x>0) itemTwoName=itemTwoName.substring(0,x).trim();

				String newName=itemOneName+" and "+itemTwoName+" melded together";
				if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
				{
					int material=((Armor)itemOne).material();
					if(getHeiarchy(material&RawMaterial.MATERIAL_MASK)<getHeiarchy(((Armor)itemTwo).material()&RawMaterial.MATERIAL_MASK))
						material=((Armor)itemTwo).material();

					long wornLocation=itemOne.rawProperLocationBitmap()|itemTwo.rawProperLocationBitmap();
					if((wornLocation&Wearable.WORN_HELD)==(Wearable.WORN_HELD))
						wornLocation-=Wearable.WORN_HELD;
					if(((wornLocation&Wearable.WORN_LEFT_FINGER)==(Wearable.WORN_LEFT_FINGER))
					   &&((wornLocation&Wearable.WORN_RIGHT_FINGER)==(Wearable.WORN_RIGHT_FINGER)))
					{
						if(((wornLocation&Wearable.WORN_LEFT_WRIST)==(Wearable.WORN_LEFT_WRIST))
						&&((wornLocation&Wearable.WORN_RIGHT_WRIST)==0))
						   wornLocation-=Wearable.WORN_RIGHT_FINGER;
						else
						if(((wornLocation&Wearable.WORN_RIGHT_WRIST)==(Wearable.WORN_RIGHT_WRIST))
						&&((wornLocation&Wearable.WORN_LEFT_WRIST)==0))
						   wornLocation-=Wearable.WORN_LEFT_FINGER;
						else
						{
							if(CMLib.dice().rollPercentage()>50)
								wornLocation-=Wearable.WORN_RIGHT_FINGER;
							else
								wornLocation-=Wearable.WORN_LEFT_FINGER;
						}
					}

					if(((wornLocation&Wearable.WORN_LEFT_WRIST)==(Wearable.WORN_LEFT_WRIST))
					   &&((wornLocation&Wearable.WORN_RIGHT_WRIST)==(Wearable.WORN_RIGHT_WRIST)))
					{
						if(((wornLocation&Wearable.WORN_LEFT_FINGER)==(Wearable.WORN_LEFT_FINGER))
						&&((wornLocation&Wearable.WORN_RIGHT_FINGER)==0))
						   wornLocation-=Wearable.WORN_RIGHT_WRIST;
						else
						if(((wornLocation&Wearable.WORN_RIGHT_FINGER)==(Wearable.WORN_RIGHT_FINGER))
						&&((wornLocation&Wearable.WORN_LEFT_FINGER)==0))
						   wornLocation-=Wearable.WORN_LEFT_WRIST;
						else
						{
							if(CMLib.dice().rollPercentage()>50)
								wornLocation-=Wearable.WORN_RIGHT_WRIST;
							else
								wornLocation-=Wearable.WORN_LEFT_WRIST;
						}
					}


					Armor gc=CMClass.getArmor("GenArmor");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.basePhyStats().setArmor((itemOne.basePhyStats().armor()+itemTwo.basePhyStats().armor())/2);
					gc.setMaterial(material);
					gc.setCapacity(0);
					if(itemOne instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemOne).capacity());
					if(itemTwo instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemTwo).capacity());
					gc.setRawLogicalAnd(true);
					gc.setRawProperLocationBitmap(wornLocation);

					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility((itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability())/2);
					melded=gc;
					mob.addItem(gc);
				}
				else
				if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
				{
					Weapon gc=CMClass.getWeapon("GenWeapon");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.basePhyStats().setAttackAdjustment((itemOne.basePhyStats().attackAdjustment()+itemTwo.basePhyStats().attackAdjustment())/2);
					gc.basePhyStats().setDamage((itemOne.basePhyStats().damage()+itemTwo.basePhyStats().damage())/2);
					if(itemOne instanceof Weapon)
						gc.setAmmoCapacity(((Weapon)itemOne).ammunitionCapacity());
					if(itemTwo instanceof Weapon)
						gc.setAmmoCapacity(((Weapon)itemTwo).ammunitionCapacity() + gc.ammunitionCapacity());
					if((itemOne instanceof Weapon)&&(((Weapon)itemOne).ammunitionType().length()>0))
						gc.setAmmunitionType(((Weapon)itemOne).ammunitionType());
					if((itemTwo instanceof Weapon)&&(((Weapon)itemTwo).ammunitionType().length()>0))
						gc.setAmmunitionType(((Weapon)itemTwo).ammunitionType());
					if(itemOne instanceof Weapon)
						gc.setWeaponType(((Weapon)itemOne).weaponType());
					else
						gc.setWeaponType(((Weapon)itemTwo).weaponType());
					if(itemTwo instanceof Weapon)
						gc.setWeaponClassification(((Weapon)itemTwo).weaponClassification());
					else
						gc.setWeaponClassification(((Weapon)itemOne).weaponClassification());
					gc.setRawLogicalAnd(true);
					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility((itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability())/2);
					melded=gc;
					mob.addItem(gc);
				}
				else
				if((itemOne instanceof Container)&&(itemTwo instanceof Container))
				{
					boolean isLocked=((Container)itemOne).hasALock();
					String keyName=((Container)itemOne).keyName();
					if(!isLocked)
					{
						isLocked=((Container)itemTwo).hasALock();
						keyName=((Container)itemTwo).keyName();
					}
					Container gc=(Container)CMClass.getItem("GenContainer");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					CMLib.flags().setGettable(gc,CMLib.flags().isGettable(itemOne)&&CMLib.flags().isGettable(itemTwo));
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.setCapacity(((Container)itemOne).capacity()+((Container)itemTwo).capacity());
					gc.setLidsNLocks((((Container)itemOne).hasALid()||((Container)itemTwo).hasALid()),true,isLocked,false);
					gc.setKeyName(keyName);

					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility(itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability());
					melded=gc;
					mob.addItem(gc);
				}
				if(melded!=null)
				{
					for(final Enumeration<Ability> a=itemOne.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(melded.fetchEffect(A.ID())==null))
							melded.addEffect((Ability)A.copyOf());
					}
					for(final Enumeration<Ability> a=itemTwo.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(melded.fetchEffect(A.ID())==null))
							melded.addEffect((Ability)A.copyOf());
					}
					for(Enumeration<Behavior> e=itemOne.behaviors();e.hasMoreElements();)
					{
						Behavior B=e.nextElement();
						if(B!=null)	
							melded.addBehavior((Behavior)B.copyOf());
					}
					for(Enumeration<Behavior> e=itemTwo.behaviors();e.hasMoreElements();)
					{
						Behavior B=e.nextElement();
						if(B!=null)	
							melded.addBehavior((Behavior)B.copyOf());
					}
					melded.recoverPhyStats();
				}
				itemOne.destroy();
				itemTwo.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) "+itemOne.name()+" and "+itemTwo.name()+", but fail(s).");

		// return whether it worked
		return success;
	}
}
