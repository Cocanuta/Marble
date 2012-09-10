package com.planet_ink.marble_mud.Commands;
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
@SuppressWarnings("rawtypes")
public class Split extends StdCommand
{
	public Split(){}

	private final String[] access={"SPLIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Split how much?");
			return false;
		}
		String itemID=CMParms.combine(commands,1);
		long numGold=CMLib.english().numPossibleGold(mob,itemID);
		if(numGold<0)
		{
			mob.tell("Split how much?!?");
			return false;
		}
		String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
		double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);

		int num=0;
		Set<MOB> H=mob.getGroupMembers(new SHashSet<MOB>());
		
		for(Iterator<MOB> i=H.iterator();i.hasNext();)
		{
			final MOB recipientM=i.next();
			if((!recipientM.isMonster())
			&&(recipientM!=mob)
			&&(recipientM.location()==mob.location())
			&&(mob.location().isInhabitant(recipientM)))
				num++;
			else
			{
				H.remove(recipientM);
			}
		}
		if(num==0)
		{
			mob.tell("No one appears to be eligible to receive any of your money.");
			return false;
		}

		double totalAbsoluteValue=CMath.mul(numGold,denom);
		totalAbsoluteValue=CMath.div(totalAbsoluteValue,num+1);
		if((totalAbsoluteValue*num)>CMLib.beanCounter().getTotalAbsoluteValue(mob,currency))
		{
			mob.tell("You don't have that much "+CMLib.beanCounter().getDenominationName(currency,denom)+".");
			return false;
		}
		List<Coins> V=CMLib.beanCounter().makeAllCurrency(currency,totalAbsoluteValue);
		CMLib.beanCounter().subtractMoney(mob,totalAbsoluteValue*num);
		for(Iterator e=H.iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			for(int v=0;v<V.size();v++)
			{
				Coins C=(Coins)V.get(v);
				C=(Coins)C.copyOf();
				mob.addItem(C);
				CMMsg newMsg=CMClass.getMsg(mob,recipient,C,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
				C.putCoinsBack();
			}
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	
}
