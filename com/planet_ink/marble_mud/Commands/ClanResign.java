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
public class ClanResign extends StdCommand
{
	public ClanResign(){}

	private final String[] access={"CLANRESIGN"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase("")))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		if(!mob.isMonster())
		{
			Clan C=mob.getMyClan();
			try
			{
				String check=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
				if(check.equalsIgnoreCase("Y"))
				{
					if(C!=null)
						CMLib.clans().clanAnnounce(mob,"Member resigned from "+C.getGovernmentName()+" "+C.name()+": "+mob.Name());
					if(C!=null)
						C.delMember(mob);
					else
					{
						CMLib.database().DBUpdateClanMembership(mob.Name(), "", 0);
						mob.setClanID("");
						mob.setClanRole(0);
						CMLib.database().DBUpdateClanMembership(mob.Name(),"",0);
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
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
