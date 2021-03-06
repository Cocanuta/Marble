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
public class HelpList extends StdCommand
{
	public HelpList(){}

	private final String[] access={"HELPLIST","HLIST"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		if(helpStr.length()==0)
		{
			mob.tell("You must enter a search pattern.  Use 'TOPICS' or 'COMMANDS' for an unfiltered list.");
			return false;
		}
		StringBuilder thisTag=
					CMLib.help().getHelpList(
					helpStr,
					CMLib.help().getHelpFile(),
					CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP)?CMLib.help().getArcHelpFile():null,
					mob);
		if((thisTag==null)||(thisTag.length()==0))
		{
			mob.tell("No help entries match '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.helpOut("Help",mob.Name()+" wanted help list match on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln("^xHelp File Matches:^.^?\n\r^N"+thisTag.toString().replace('_',' '));
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}

