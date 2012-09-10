package com.planet_ink.marble_mud.Items.Weapons;
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
public class StdNet extends StdWeapon
{
	public String ID(){	return "StdNet";}
	public StdNet()
	{
		super();
		setName("a net");
		setDisplayText("a net has been left here.");
		setDescription("Its a wide tangling net!");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(1);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		baseGoldValue=10;
		recoverPhyStats();
		minRange=1;
		maxRange=1;
		weaponType=Weapon.TYPE_NATURAL;
		material=RawMaterial.RESOURCE_HEMP;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(true);
	}


	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			return;
			//msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			unWear();
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
			msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.TYP_GENERAL,null));
		}
		else
		if((msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_GENERAL)
		&&(((MOB)msg.target()).isMine(this))
		&&(msg.sourceMessage()==null))
		{
			MOB M=(MOB)msg.target();
			Set<MOB> H=msg.source().getGroupMembers(new HashSet<MOB>());
			if(H.contains(M)) H.remove(M);

			for(int i=0;i<M.location().numInhabitants();i++)
			{
				MOB M2=M.location().fetchInhabitant(i);
				if((M2!=null)
				&&(M2!=msg.source())
				&&(!H.contains(M2))
				&&(M2.getVictim()==M.getVictim())
				&&(M2.rangeToTarget()==M.rangeToTarget()))
				{
					Ability A=CMClass.getAbility("Thief_Bind");
					if(A!=null)
					{
						A.setAffectedOne(this);
						A.invoke(msg.source(),M2,true,phyStats().level());
					}
				}
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
}
