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
public class ClanQual extends StdCommand
{
	public ClanQual(){}

	private final String[] access={"CLANQUAL"};
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
				msg.append("You aren't in the right position to set the qualifications to your "+C.getGovernmentName()+".");
			}
			else
			{
				try
				{
					String premise="?";
					while(premise.equals("?"))
					{
						if((skipChecks)&&(commands.size()>1))
							premise=CMParms.combine(commands,1);
						else
						if(mob.session()!=null)
							premise=mob.session().prompt("Describe your "+C.getGovernmentName()+"'s Qualification Code (?)\n\r: ","");

						if(premise.equals("?"))
							mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
						else
						if(premise.length()>0)
						{
							mob.tell("Your qualifications will be as follows: "+CMLib.masking().maskDesc(premise)+"\n\r");
							if((mob.session()!=null)&&(mob.session().confirm("Is this correct (Y/n)?","Y")))
							{
								commands.addElement(premise);
								if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,true))
								{
									C.setAcceptanceSettings(premise);
									C.update();
									CMLib.clans().clanAnnounce(mob,"The qualifications of "+C.getGovernmentName()+" "+C.clanID()+" have been changed.");
									return false;
								}
							}
							else
								premise="?";
						}
						else
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
