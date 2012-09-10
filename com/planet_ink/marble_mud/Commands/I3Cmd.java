package com.planet_ink.marble_mud.Commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.intermud.i3.packets.Intermud;
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
public class I3Cmd extends StdCommand
{
	public I3Cmd(){}

	private final String[] access={"I3"};
	public String[] getAccessWords(){return access;}

	public void i3Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3))
			mob.tell("Try I3 LIST, I3 CHANNELS, I3 ADD [CHANNEL], I3 DELETE [CHANNEL], I3 LISTEN [CHANNEL], I3 RESTART, or I3 INFO [MUD].");
		else
			mob.tell("Try I3 LIST, I3 LOCATE [NAME], or I3 INFO [MUD-NAME].");
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(!(CMLib.intermud().i3online()))
		{
			mob.tell("I3 is unavailable.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			i3Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMLib.intermud().i3online()))
			mob.tell("I3 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			CMLib.intermud().giveI3MudList(mob);
		else
		if(str.equalsIgnoreCase("add"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMLib.intermud().i3channelAdd(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("channels"))
			CMLib.intermud().giveI3ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("delete"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMLib.intermud().i3channelRemove(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("listen"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMLib.intermud().i3channelListen(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("ping"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			CMLib.intermud().i3pingRouter(mob);
		}
		else
		if(str.equalsIgnoreCase("restart"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			try {
				mob.tell(CMLib.hosts().get(0).executeCommand("START I3"));
			}catch(Exception e){ Log.errOut("I3Cmd",e);}
		}
		else
		if(str.equalsIgnoreCase("locate"))
		{
			if(commands.size()<2)
			{
				mob.tell("You did not specify a name!");
				return false;
			}
			CMLib.intermud().i3locate(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("silence"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.I3)){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMLib.intermud().i3channelSilence(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("info"))
			CMLib.intermud().i3mudInfo(mob,CMParms.combine(commands,1));
		else
			i3Error(mob);

		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
