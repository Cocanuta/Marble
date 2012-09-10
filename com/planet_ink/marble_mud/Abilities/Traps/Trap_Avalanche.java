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
public class Trap_Avalanche extends StdTrap
{
	public String ID() { return "Trap_Avalanche"; }
	public String name(){ return "avalanche";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 25;}
	public String requiresToSet(){return "100 pounds of stone";}

	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),100);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	public List<Item> getTrapComponents() {
		Vector V=new Vector();
		for(int i=0;i<100;i++)
			V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_STONE));
		return V;
	}
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<100))
			{
				mob.tell("You'll need to set down at least 100 pounds of stone first.");
				return false;
			}
		}
		if(P instanceof Room)
		{
			Room R=(Room)P;
			if(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
			{
				if(mob!=null)
					mob.tell("You can only set this trap in the mountains.");
				return false;
			}
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((doesSaveVsTraps(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off an avalanche!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> trigger(s) an avalanche!"))
			{
				super.spring(target);
				if((affected!=null)
				&&(affected instanceof Room))
				{
					Room R=(Room)affected;
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=invoker()))
							if(invoker().mayIFight(M))
							{
								int damage=CMLib.dice().roll(trapLevel()+abilityCode(),20,1);
								CMLib.combat().postDamage(invoker(),M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"The avalanche <DAMAGE> <T-NAME>!");
							}
					}
				}
			}
		}
	}
}
