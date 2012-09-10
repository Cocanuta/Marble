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
public class Prop_TattooAdder extends Property
{
	public String ID() { return "Prop_TattooAdder"; }
	public String name(){ return "A TattooAdder";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS|Ability.CAN_EXITS;}
	int tattooCode=-1;

	public int tattooCode()
	{
		if(tattooCode>=0) return tattooCode;
		if(affected==null) return -1;
		if(affected instanceof Drink)
			tattooCode= CMMsg.TYP_DRINK;
		else
		if(affected instanceof Food)
			tattooCode= CMMsg.TYP_EAT;
		else
		if(affected instanceof MOB)
			tattooCode= CMMsg.TYP_DEATH;
		else
		if(affected instanceof Weapon)
			tattooCode= CMMsg.TYP_WEAPONATTACK;
		else
		if(affected instanceof Armor)
			tattooCode= CMMsg.TYP_WEAR;
		else
		if(affected instanceof Item)
			tattooCode= CMMsg.TYP_GET;
		else
		if(affected instanceof Room)
			tattooCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Area)
			tattooCode= CMMsg.TYP_ENTER;
		else
		if(affected instanceof Exit)
			tattooCode= CMMsg.TYP_ENTER;
		return tattooCode;
	}

	public void applyTattooCodes(MOB mob, boolean addOnly, boolean subOnly)
	{
		String tattooName=text();
		if(tattooName.length()==0) return;

		boolean tattooPlus=true;
		boolean tattooMinus=false;


		if(tattooName.startsWith("+-")||tattooName.startsWith("-+"))
		{
			tattooMinus=true;
			tattooName=tattooName.substring(2);
		}
		else
		if(tattooName.startsWith("+"))
			tattooName=tattooName.substring(1);
		else
		if(tattooName.startsWith("-"))
		{
			tattooPlus=false;
			tattooMinus=true;
			tattooName=tattooName.substring(1);
		}
		
		boolean silent=tattooName.startsWith("~");
		if(silent) tattooName=tattooName.substring(1);

		if(addOnly) tattooMinus=false;
		if(subOnly) tattooPlus=false;

		MOB.Tattoo pT=CMLib.database().parseTattoo(tattooName);
		MOB.Tattoo T = mob.findTattoo(pT.tattooName);
		if(T!=null)
		{
			if(tattooMinus)
			{
				if(!silent)
					mob.location().show(mob,affected,CMMsg.MSG_OK_ACTION,"<T-NAME> takes away the "+pT.tattooName.toLowerCase()+" tattoo from <S-NAME>.");
				mob.delTattoo(T);
			}
		}
		else
		{
			if(tattooPlus)
			{
				if(!silent)
					mob.location().show(mob,affected,CMMsg.MSG_OK_ACTION,"<T-NAME> gives <S-NAME> the "+pT.tattooName.toLowerCase()+" tattoo.");
				mob.addTattoo(pT);
			}
		}
	}


	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((tattooCode()==CMMsg.TYP_DEATH)&&(msg.sourceMinor()==tattooCode()))
		{
			if((msg.tool()==affected)&&(msg.source()!=affected))
				applyTattooCodes(msg.source(),false,true);
			else
			if((msg.source()==affected)
			&&(msg.tool() instanceof MOB)
			&&(msg.tool()!=affected))
				applyTattooCodes((MOB)msg.tool(),true,false);
		}
		else
		if(((msg.targetMinor()==tattooCode())||(msg.sourceMinor()==tattooCode()))
		&&(tattooCode()>=0)
		&&(msg.amITarget(affected)||(msg.tool()==affected)))
			applyTattooCodes(msg.source(),false,false);
		super.executeMsg(myHost,msg);
	}
}
