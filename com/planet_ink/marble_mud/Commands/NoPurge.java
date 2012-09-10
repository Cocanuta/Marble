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
public class NoPurge extends StdCommand
{
	public NoPurge(){}

	private final String[] access={"NOPURGE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		String protectMe=CMParms.combine(commands,0);
		if(protectMe.length()==0)
		{
			mob.tell("Protect whom?  Enter a player name to protect from autopurge.");
			return false;
		}
		if((!CMLib.players().playerExists(protectMe))&&(!CMLib.players().accountExists(protectMe))&&(CMLib.clans().getClan(protectMe)==null))
		{
			mob.tell("Protect whom?  '"+protectMe+"' is not a known player.");
			return false;
		}
		List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		for(int b=0;b<protectedOnes.size();b++)
		{
			String B=(String)protectedOnes.get(b);
			if(B.equalsIgnoreCase(protectMe))
			{
				mob.tell("That player already protected.  Do LIST NOPURGE and check out #"+(b+1)+".");
				return false;
			}
		}
		mob.tell("The player '"+protectMe+"' is now protected from autopurge.");
		StringBuffer str=Resources.getFileResource("protectedplayers.ini",false);
		if(protectMe.trim().length()>0) str.append(protectMe+"\n");
		Resources.updateFileResource("::protectedplayers.ini",str);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE);}

	
}
