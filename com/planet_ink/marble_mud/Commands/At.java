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
public class At extends StdCommand
{
	public At(){}

	private final String[] access={"AT"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		Room room=CMLib.map().findWorldRoomLiberally(mob,cmd,"APMIR",100,120000);
		if(room==null)
		{
			if(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.AT))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.AT))
		{
			mob.tell("You aren't powerful enough to do that there.");
			return false;
		}
		Room R=mob.location();
		if(R!=room)	room.bringMobHere(mob,false);
		mob.doCommand(commands,metaFlags);
		if(mob.location()!=R) R.bringMobHere(mob,false);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.AT);}

	
}
