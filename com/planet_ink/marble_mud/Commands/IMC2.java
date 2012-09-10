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
public class IMC2 extends StdCommand
{
	public IMC2(){}

	private final String[] access={"IMC2"};
	public String[] getAccessWords(){return access;}

	public void IMC2Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMC2))
			mob.tell("Try IMC2 LIST, IMC2 INFO [MUD], IMC2 LOCATE, IMC2 RESTART, or IMC2 CHANNELS.");
		else
			mob.tell("Try IMC2 LIST, IMC2 INFO [MUD], IMC2 LOCATE");
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(!(CMLib.intermud().imc2online()))
		{
			mob.tell("IMC2 is unavailable.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			IMC2Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMLib.intermud().imc2online()))
			mob.tell("IMC2 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			CMLib.intermud().giveIMC2MudList(mob);
		else
		if(str.equalsIgnoreCase("locate"))
			CMLib.intermud().i3locate(mob,CMParms.combine(commands,1));
		else
		if(str.equalsIgnoreCase("channels") && CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMC2))
			CMLib.intermud().giveIMC2ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("info"))
			CMLib.intermud().imc2mudInfo(mob,CMParms.combine(commands,1));
		else
		if(str.equalsIgnoreCase("restart") && CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMC2))
		{
			try {
				mob.tell(CMLib.hosts().get(0).executeCommand("START IMC2"));
			}catch(Exception e){ Log.errOut("IMC2Cmd",e);}
		}
		else
			IMC2Error(mob);

		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
