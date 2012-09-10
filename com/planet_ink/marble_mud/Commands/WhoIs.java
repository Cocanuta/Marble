package com.planet_ink.marble_mud.Commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.intermud.i3.packets.Intermud;
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
public class WhoIs extends Who
{
	public WhoIs(){}

	private final String[] access={"WHOIS"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String mobName=CMParms.combine(commands,1);
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("whois whom?");
			return false;
		}

		int x=mobName.indexOf("@");
		if(x>=0)
		{
			if((!(CMLib.intermud().i3online()))
			&&(!CMLib.intermud().imc2online()))
				mob.tell("Intermud is unavailable.");
			else
			if(x==0)
				CMLib.intermud().i3who(mob,mobName.substring(1));
			else
			{
				String mudName=mobName.substring(x+1);
				mobName=mobName.substring(0,x);
				if(Intermud.isAPossibleMUDName(mudName))
				{
					mudName=Intermud.translateName(mudName);
					if(!Intermud.isUp(mudName))
					{
						mob.tell(mudName+" is not available.");
						return false;
					}
				}
				CMLib.intermud().i3finger(mob,mobName,mudName);
			}
			return false;
		}

		StringBuffer msg=new StringBuffer("");
		for(Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)
			&&(((mob2.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
				||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))
					&&(mob.phyStats().level()>=mob2.phyStats().level())))
			&&(mob2.phyStats().level()>0)
			&&(mob2.name().toUpperCase().startsWith(mobName.toUpperCase())))
				msg.append(showWhoShort(mob2));
		}
		if(msg.length()==0)
			mob.tell("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(CMStrings.padRight("Race",12)+" ");
			head.append(CMStrings.padRight("Class",12)+" ");
			head.append(CMStrings.padRight("Level",7));
			head.append("] Character name^.^N\n\r");
			mob.tell(head.toString()+msg.toString());
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
