package com.planet_ink.marble_mud.Abilities.Properties;
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
import com.planet_ink.marble_mud.Libraries.interfaces.TimeManager;
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
public class Prop_AddDamage extends Property implements TriggeredAffect
{
	public String ID() { return "Prop_AddDamage"; }
	public String name(){ return "Additional Damage";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	int weaponType=Weapon.TYPE_NATURAL;
	int typeOfEffect=CMMsg.TYP_WEAPONATTACK;
	double pctDamage=0.0;
	int bonusDamage=0;
	volatile boolean norecurse=false;

	public int triggerMask()
	{ 
		return TriggeredAffect.TRIGGER_HITTING_WITH;
	}

	public String accountForYourself()
	{
		String id="Does extra damage of the following amount and types: "+text();
		return id;
	}

	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		final List<String> parms=CMParms.parse(newMiscText.toUpperCase());
		for(String s : parms)
		{
			if(s.startsWith("+")) s=s.substring(1);
			if(CMath.isPct(s))
				pctDamage=CMath.s_pct(s);
			else
			if(CMath.isInteger(s))
				bonusDamage=CMath.s_int(s);
			else
			{
				boolean done=false;
				for(int i=0;i<Weapon.TYPE_DESCS.length;i++)
				{
					final String type=Weapon.TYPE_DESCS[i];
					if(type.equals(s))
					{
						weaponType=i;
						done=true;
						break;
					}
				}
				if(!done)
				for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
				{
					final String type=CMMsg.TYPE_DESCS[i];
					if(type.equals(s))
					{
						typeOfEffect=i;
						done=true;
						break;
					}
				}
				if(!done)
				for(int i=0;i<Weapon.TYPE_DESCS.length;i++)
				{
					final String type=Weapon.TYPE_DESCS[i];
					if(type.startsWith(s))
					{
						weaponType=i;
						done=true;
						break;
					}
				}
				if(!done)
				for(int i=0;i<Weapon.TYPE_DESCS.length;i++)
				{
					final String type=Weapon.TYPE_DESCS[i];
					if(type.startsWith(s))
					{
						typeOfEffect=i;
						done=true;
						break;
					}
				}
				if(!done)
				for(int i=0;i<RawMaterial.CODES.NAMES().length;i++)
				{
					final String type=RawMaterial.CODES.NAMES()[i];
					if(type.equals(s))
					{
						done=true; // just eat it
						break;
					}
				}
				if((!done)&&(!s.equals("ALL")))
					Log.errOut("Prop_AddDamage","Unknown weapon type/attack: "+s+" in "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(affected)));
			}
		}
	}

	protected final int getDamage(final CMMsg msg)
	{
		final int dmg = (int)CMath.round(CMath.mul(msg.value(), pctDamage)) + bonusDamage;
		if(dmg < 0) return 0;
		return dmg;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		MOB mob=null;
		if(affected instanceof Item)
		{
			if(((Item)affected).owner() instanceof MOB)
			{
				mob=(MOB)((Item)affected).owner();
			}
			else
				return;
		}
		else
		if(affected instanceof MOB)
			mob=(MOB)affected;
		else
			return;
		if((msg.source()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead())
		&&(msg.tool()!=this)
		&&(msg.source().location()!=null))
		{
			if(((affected instanceof Armor)||(affected instanceof Shield))
			&&(msg.amITarget(mob))
			&&(!msg.amISource(mob))
			&&(CMLib.dice().rollPercentage()>32+msg.source().charStats().getStat(CharStats.STAT_DEXTERITY))
			&&(msg.source().rangeToTarget()==0)
			&&((msg.targetMajor(CMMsg.MASK_HANDS))||(msg.targetMajor(CMMsg.MASK_MOVE))))
			{
				CMMsg msg2=CMClass.getMsg(mob,msg.source(),this,CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,null);
				if(msg.source().location().okMessage(msg.source(),msg2))
				{
					msg.source().location().send(msg.source(),msg2);
					if(msg2.value()<=0)
					{
						final int damage=getDamage(msg);
						CMLib.combat().postDamage(mob,msg.source(),affected,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,
							 "^F^<FIGHT^><S-YOUPOSS> <O-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?");
					}
				}
			}
			else
			if((msg.tool()==affected)
			&&(!msg.amITarget(mob))
			&&(msg.amISource(mob))
			&&(!(msg.tool() instanceof Wand)))
			{
				final int damage=getDamage(msg);
				String str="^F^<FIGHT^><S-YOUPOSS> <O-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?";
				synchronized(this)
				{
					if(!norecurse)
					{
						norecurse=true;
						try
						{
							CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),affected,(int)Math.round(damage),
							CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,str);
						}
						finally
						{
							norecurse=false;
						}
					}
				}
			}
			else
			if((mob==affected)
			&&(!msg.amITarget(mob))
			&&(msg.amISource(mob))
			&&(msg.tool() instanceof Weapon)
			&&(!(msg.tool() instanceof Wand)))
			{
				final int damage=getDamage(msg);
				String str="^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?";
				CMLib.combat().postDamage(mob,(MOB)msg.target(),this,(int)Math.round(damage),
					  CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,str);
			}
		}
	}
}
