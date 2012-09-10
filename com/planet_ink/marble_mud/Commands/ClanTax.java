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
@SuppressWarnings({"unchecked","rawtypes"})
public class ClanTax extends StdCommand
{
	public ClanTax(){}

	private final String[] access={"CLANTAX"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt(getAccessWords()[0],0);

		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(mob.getMyClan()==null))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		{
			Clan C=mob.getMyClan();
			if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.TAX,false)))
			{
				msg.append("You aren't in the right position to set the experience tax rate for your "+C.getGovernmentName()+".");
			}
			else
			{
				try
				{
					double newRate=0.0;
					if((skipChecks)&&(commands.size()>1))
						newRate=CMath.div(CMath.s_int(CMParms.combine(commands,1)),100);
					else
					if(mob.session()!=null)
					{
						String t=null;
						if((commands.size()<=1)||(!CMath.isNumber(CMParms.combine(commands,1))))
							t=mob.session().prompt("Enter your "+C.getGovernmentName()+"'s new tax rate (0-25)\n\r: ","");
						else
							t=CMParms.combine(commands,1);
						if(t.length()==0) return false;
						int intt=CMath.s_int(t);
						if((intt<0)||(intt>25)) 
						{
							mob.session().println("'"+t+"' is not a valid value.  Try 0-25.");
							return false;
						}
						commands.clear();
						commands.addElement("clantax");
						commands.addElement(t);
						newRate=CMath.div(CMath.s_int(t),100);
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.TAX,true))
					{
						C.setTaxes(newRate);
						C.update();
						CMLib.clans().clanAnnounce(mob,"The experience tax rate of "+C.getGovernmentName()+" "+C.clanID()+" has been changed to "+((int)Math.round(C.getTaxes()*100.0)+"%."));
						return false;
					}
				}
				catch(java.io.IOException e)
				{
				}
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
