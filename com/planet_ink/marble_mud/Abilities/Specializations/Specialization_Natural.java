package com.planet_ink.marble_mud.Abilities.Specializations;
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
public class Specialization_Natural extends Specialization_Weapon
{
	public String ID() { return "Specialization_Natural"; }
	public String name(){ return "Hand to hand combat";}
	public Specialization_Natural()
	{
		super();
		weaponClass=Weapon.CLASS_NATURAL;
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((activated)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(CMLib.dice().rollPercentage()<10)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&((!(msg.tool() instanceof Weapon))||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
			helpProficiency((MOB)affected, 0);
		super.executeMsg(myHost, msg);
	}


	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		activated=false;
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			activated=true;
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
					+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
					+(10*(getXLEVELLevel((MOB)affected))));
		}
	}
}
