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
public class Prop_TicketTaker extends Property
{
	public String ID() { return "Prop_TicketTaker"; }
	public String name(){ return "Ticket Taker";}
	public String displayText() {return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{
		return "one who acts as a ticket taker";
	}

	protected double cost(){
		int amount=CMath.s_int(text());
		if(amount==0) amount=10;
		return (double)amount;
	}

	protected boolean isMine(Environmental host, Rideable R)
	{
		if(host instanceof Rider)
		{
			Rider mob=(Rider)host;
			if(R==mob) return true;
			if(mob.riding()==null) return false;
			if(mob.riding()==R) return true;
			if((R instanceof Rider)&&(((Rider)R).riding()==mob.riding()))
				return true;
			if((mob.riding() instanceof Rider)&&(((Rider)mob.riding()).riding()==R))
				return true;
		}
		else
		if(host instanceof Rideable)
			return host==R;
		return false;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_MOUNT:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				{
					String currency=CMLib.beanCounter().getCurrency(affected);
					if(currency.length()==0)
						currency=CMLib.beanCounter().getCurrency(mob);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)>=cost())
					{
						String costStr=CMLib.beanCounter().nameCurrencyShort(currency,cost());
						mob.location().show(mob,myHost,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> give(s) "+costStr+" to <T-NAME>.");
						CMLib.beanCounter().subtractMoney(mob,currency,cost());
					}
				}
				break;
				}
			}
		}
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_MOUNT:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				{
					String currency=CMLib.beanCounter().getCurrency(affected);
					if(currency.length()==0)
						currency=CMLib.beanCounter().getCurrency(mob);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<cost())
					{
						String costStr=CMLib.beanCounter().nameCurrencyLong(currency,cost());
						if(myHost instanceof MOB)
							CMLib.commands().postSay((MOB)myHost,mob,"You'll need "+costStr+" to board.",false,false);
						else
							mob.tell("You'll need "+costStr+" to board.");
						return false;
					}
					break;
				}
				default:
					break;
				}
		}
		return true;
	}
}
