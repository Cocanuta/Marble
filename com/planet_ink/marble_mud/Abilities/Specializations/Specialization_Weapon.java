package com.planet_ink.marble_mud.Abilities.Specializations;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class Specialization_Weapon extends StdAbility
{
	public String ID() { return "Specialization_Weapon"; }
	public String name(){ return "Weapon Specialization";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	protected boolean activated=false;
	protected int weaponClass=-1;
	protected int secondWeaponClass=-1;
	
	protected short[] bonuses=null;
	protected int numExpertises=-1;

	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;}

	protected int getDamageBonus(int dmgType)
	{
		switch(dmgType)
		{
		case Weapon.TYPE_SLASHING: return bonuses[0];
		case Weapon.TYPE_PIERCING: return bonuses[1];
		case Weapon.TYPE_BASHING: return bonuses[2];
		case Weapon.TYPE_SHOOT: return bonuses[1];
		default:
			return 0;
		}
	}
	protected int getDamageBonus(MOB mob, int dmgType)
	{
		if(mob==null) return 0;
		if((numExpertises==mob.numExpertises())&&(bonuses!=null))
			return getDamageBonus(dmgType);
		if(bonuses==null) bonuses=new short[3];
		bonuses[0]=(short)getX1Level(mob);
		bonuses[1]=(short)getX2Level(mob);
		bonuses[2]=(short)getX3Level(mob);
		numExpertises=mob.numExpertises();
		return getDamageBonus(dmgType);
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((activated)
		&&(msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Weapon))
		{
			Weapon w=(Weapon)msg.tool();
			if((w.weaponClassification()==weaponClass)
			||(weaponClass<0)
			||(w.weaponClassification()==secondWeaponClass))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)&&(CMLib.dice().rollPercentage()<10))
					helpProficiency((MOB)affected, 0);
				else
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
				&&((w.weaponClassification()==Weapon.CLASS_NATURAL)||(!w.amWearingAt(Wearable.IN_INVENTORY)))
				&&(msg.value()>0))
					msg.setValue(msg.value()+(this.getDamageBonus(msg.source(),w.weaponType())));
			}
		}
	}


	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon instanceof Weapon)
			&&((((Weapon)myWeapon).weaponClassification()==weaponClass)
 			 ||(weaponClass<0)
			 ||(((Weapon)myWeapon).weaponClassification()==secondWeaponClass)))
			{
				activated=true;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
						+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
						+(10*(getXLEVELLevel((MOB)affected))));
					
			}
		}
	}
}
