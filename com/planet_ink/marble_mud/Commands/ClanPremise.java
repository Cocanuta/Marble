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
public class ClanPremise extends StdCommand
{
	public ClanPremise(){}

	private final String[] access={"CLANPREMISE"};
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
			if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,false)))
			{
				msg.append("You aren't in the right position to set the premise to your "+C.getGovernmentName()+".");
			}
			else
			{
				try
				{
					String premise="";
					if((skipChecks)&&(commands.size()>1))
						premise=CMParms.combine(commands,1);
					else
					if(mob.session()!=null)
						premise=mob.session().prompt("Describe your "+C.getGovernmentName()+"'s Premise\n\r: ","");
					if(premise.length()>0)
					{
						commands.addElement(premise);
						if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,true))
						{
							C.setPremise(premise);
							C.update();
							CMLib.clans().clanAnnounce(mob,"The premise of "+C.getGovernmentName()+" "+C.clanID()+" has been changed.");
							return false;
						}
					}
					else
					{
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
