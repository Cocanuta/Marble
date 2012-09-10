package com.planet_ink.marble_mud.Abilities.Traps;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Trap_Spark extends StdTrap
{
	public String ID() { return "Trap_Spark"; }
	public String name(){ return "sparking trap";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 19;}
	public String requiresToSet(){return "10 pounds of metal";}

	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),10);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	public List<Item> getTrapComponents() {
		Vector V=new Vector();
		for(int i=0;i<10;i++)
			V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON));
		return V;
	}
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<10))
			{
				mob.tell("You'll need to set down at least 10 pounds of metal first.");
				return false;
			}
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a sparking trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> set(s) off an sparking trap!"))
			{
				super.spring(target);
				CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(trapLevel()+abilityCode(),8,1),CMMsg.MASK_ALWAYS|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The sparks <DAMAGE> <T-NAME>!"+CMProps.msp("shock.wav",30));
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
