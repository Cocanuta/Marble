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
public class Spell_FakeWeapon extends Spell
{
	public String ID() { return "Spell_FakeWeapon"; }
	public String name(){return "Fake Weapon";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void unInvoke()
	{
		Item item=null;
		if(affected instanceof Item)
			item=(Item)affected;
		super.unInvoke();
		if(item != null)
			item.destroy();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof Item))
		{
			if((msg.tool()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
				msg.setValue(0);
			else
			if((msg.target()!=null)
			&&((msg.target()==affected)
				||(msg.target()==((Item)affected).container())
				||(msg.target()==((Item)affected).ultimateContainer(null))))
			{
				if(((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
				||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC))))
				{
					Room room=null;
					if((msg.source()!=null)
					&&(msg.source().location()!=null))
						room=msg.source().location();
					if(room==null) room=CMLib.map().roomLocation(affected);
					if(room!=null)
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles around "+affected.Name()+" and is absorbed into the air.");
					return false;
				}
				else
				if(msg.tool() instanceof Ability)
				{
					msg.source().tell("That doesn't appear to work on "+affected.name());
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String weaponName=CMParms.combine(commands,0);
		String[] choices={"sword","dagger","mace","staff","axe","hammer", "flail"};
		int choice=-1;
		for(int i=0;i<choices.length;i++)
		{
			if(choices[i].equalsIgnoreCase(weaponName))
				choice=i;
		}
		if(choice<0)
		{
			mob.tell("You must specify what kind of weapon to create: sword, dagger, mace, flail, staff, axe, or hammer.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Weapon weapon=(Weapon)CMClass.getItem("GenWeapon");
				weapon.basePhyStats().setAttackAdjustment(100 +(10 * super.getXLEVELLevel(mob)));
				weapon.basePhyStats().setDamage(75+(3 * super.getXLEVELLevel(mob)));
				weapon.basePhyStats().setDisposition(weapon.basePhyStats().disposition()|PhyStats.IS_BONUS);
				weapon.setMaterial(RawMaterial.RESOURCE_COTTON);
				switch(choice)
				{
				case 0:
					weapon.setName("a fancy sword");
					weapon.setDisplayText("a fancy sword sits here");
					weapon.setDescription("looks fit to cut something up!");
					weapon.setWeaponClassification(Weapon.CLASS_SWORD);
					weapon.setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case 1:
					weapon.setName("a sharp dagger");
					weapon.setDisplayText("a sharp dagger sits here");
					weapon.setDescription("looks fit to cut something up!");
					weapon.setWeaponClassification(Weapon.CLASS_DAGGER);
					weapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
				case 2:
					weapon.setName("a large mace");
					weapon.setDisplayText("a large mace sits here");
					weapon.setDescription("looks fit to whomp on something with!");
					weapon.setWeaponClassification(Weapon.CLASS_BLUNT);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 3:
					weapon.setName("a quarterstaff");
					weapon.setDisplayText("a quarterstaff sits here");
					weapon.setDescription("looks like a reliable weapon");
					weapon.setWeaponClassification(Weapon.CLASS_STAFF);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 4:
					weapon.setName("a deadly axe");
					weapon.setDisplayText("a deadly axe sits here");
					weapon.setDescription("looks fit to shop something up!");
					weapon.setWeaponClassification(Weapon.CLASS_AXE);
					weapon.setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case 5:
					weapon.setName("a large hammer");
					weapon.setDisplayText("a large hammer sits here");
					weapon.setDescription("looks fit to pound something into a pulp!");
					weapon.setWeaponClassification(Weapon.CLASS_HAMMER);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case 6:
					weapon.setName("a large flail");
					weapon.setDisplayText("a large flail sits here");
					weapon.setDescription("looks fit to pound something into a pulp!");
					weapon.setWeaponClassification(Weapon.CLASS_FLAILED);
					weapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				weapon.basePhyStats().setWeight(0);
				weapon.setBaseValue(0);
				weapon.recoverPhyStats();
				mob.addItem(weapon);
				mob.location().show(mob,null,weapon,CMMsg.MSG_OK_ACTION,"Suddenly, <S-NAME> own(s) <O-NAME>!");
				beneficialAffect(mob,weapon,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically wave(s) <S-HIS-HER> arms around, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
