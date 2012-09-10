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
public class Trap_BearTrap extends StdTrap
{
	public String ID() { return "Trap_BearTrap"; }
	public String name(){ return "bear trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 16;}
	public String requiresToSet(){return "30 pounds of metal";}
	public int baseRejuvTime(int level){ return 35;}

	protected int amountRemaining=250;
	protected MOB trapped=null;

	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
			if(I==null) I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_MITHRIL);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),30);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	public List<Item> getTrapComponents() {
		Vector V=new Vector();
		for(int i=0;i<30;i++)
			V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON));
		return V;
	}
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
			if(I==null)	I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_MITHRIL);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<30))
			{
				mob.tell("You'll need to set down at least 30 pounds of metal first.");
				return false;
			}
		}
		return true;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((sprung)
		&&(trapped!=null)
		&&(affected!=null)
		&&(msg.amISource(trapped))
		&&(trapped.location()!=null))
		{
			if((((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_FLEE))
				&&(msg.amITarget(affected))
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
			{
				if(trapped.location().show(trapped,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> struggle(s) to get out of the bear trap."))
				{
					amountRemaining-=trapped.charStats().getStat(CharStats.STAT_STRENGTH);
					amountRemaining-=trapped.phyStats().level();
					if(amountRemaining<=0)
					{
						trapped.location().show(trapped,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> pull(s) free of the bear trap.");
						trapped=null;
					}
					else
						return false;
				}
				else
					return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void spring(MOB target)
	{
		trapped=null;
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(CMLib.flags().isInFlight(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) a bear trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> step(s) on a bear trap!"))
			{
				super.spring(target);
				int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
				trapped=target;
				amountRemaining=250+((trapLevel()+abilityCode())*10);
				CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"The bear trap <DAMAGE> <T-NAME>!");
			}
		}
	}
}
