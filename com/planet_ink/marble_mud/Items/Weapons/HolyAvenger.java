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
public class HolyAvenger extends TwoHandedSword
{
	public String ID(){	return "HolyAvenger";}
	public HolyAvenger()
	{
		super();

		setName("the holy avenger");
		setDisplayText("a beautiful two-handed sword has been left here");
		setDescription("A two-handed sword crafted with a careful hand, and inscribed with several holy symbols.");
		secretIdentity="The Holy Avenger!  A good-only Paladin sword that casts dispel evil on its victims";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(20);
		basePhyStats().setWeight(25);
		basePhyStats().setAttackAdjustment(25);
		basePhyStats().setDamage(18);
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_GOOD|PhyStats.IS_BONUS);
		baseGoldValue=15500;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_MITHRIL;
		weaponType=TYPE_SLASHING;
		setRawLogicalAnd(true);
	}



	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_GET:
			if((!msg.source().charStats().getCurrentClass().ID().equals("Paladin"))
			||(!CMLib.flags().isGood(msg.source())))
			{
				unWear();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,name()+" flashes and flies out of <S-HIS-HER> hands!");
				if(msg.source().isMine(this))
					CMLib.commands().postDrop(msg.source(),this,true,false);
				return false;
			}
			break;
		default:
			break;
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source().location()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead())
		&&(CMLib.flags().isEvil((MOB)msg.target())))
		{
			CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),new HolyAvenger(),CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_UNDEAD,CMMsg.MSG_NOISYMOVEMENT,null);
			if(msg.source().location().okMessage(msg.source(),msg2))
			{
				msg.source().location().send(msg.source(), msg2);
				int damage=CMLib.dice().roll(1,15,0);
				if(msg.value()>0)
					damage=damage/2;
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),CMMsg.MSG_OK_ACTION,name()+" dispels evil within <T-NAME> and "+CMLib.combat().standardHitWord(Weapon.TYPE_BURSTING,damage)+" <T-HIM-HER>>!"));
				CMMsg msg3=CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_DAMAGE,CMMsg.NO_EFFECT,null);
				msg3.setValue(damage);
				msg.addTrailerMsg(msg3);
			}
		}
	}

}
