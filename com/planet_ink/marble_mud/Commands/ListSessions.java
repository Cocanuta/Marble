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
public class ListSessions extends StdCommand
{
	public ListSessions(){}

	private final String[] access={"SESSIONS"};
	public String[] getAccessWords(){return access;}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String sort="";
		if((commands!=null)&&(commands.size()>1))
			sort=CMParms.combine(commands,1).trim().toUpperCase();
		StringBuffer lines=new StringBuffer("\n\r^x");
		lines.append(CMStrings.padRight("#",3)+"| ");
		lines.append(CMStrings.padRight("Status",9)+"| ");
		lines.append(CMStrings.padRight("Valid",5)+"| ");
		lines.append(CMStrings.padRight("Name",17)+"| ");
		lines.append(CMStrings.padRight("IP",17)+"| ");
		lines.append(CMStrings.padRight("Idle",17)+"^.^N\n\r");
		Vector broken=new Vector();
		for(Session S : CMLib.sessions().allIterable())
		{
			String[] set=new String[6];
			set[0]=CMStrings.padRight(""+broken.size(),3)+"| ";
			set[1]=(S.isStopped()?"^H":"")+CMStrings.padRight(Session.STATUS_STR[S.getStatus()],9)+(S.isStopped()?"^?":"")+"| ";
			if (S.mob() != null)
			{
				set[2]=CMStrings.padRight(((S.mob().session()==S)?"Yes":"^HNO!^?"),5)+"| ";
				set[3]="^!"+CMStrings.padRight("^<LSTUSER^>"+S.mob().Name()+"^</LSTUSER^>",17)+"^?| ";
			}
			else
			{
				set[2]=CMStrings.padRight("N/A",5)+"| ";
				set[3]=CMStrings.padRight("NAMELESS",17)+"| ";
			}
			set[4]=CMStrings.padRight(S.getAddress(),17)+"| ";
			set[5]=CMStrings.padRight(CMLib.english().returnTime(S.getIdleMillis(),0)+"",17);
			broken.addElement(set);
		}
		Vector sorted=null;
		int sortNum=-1;
		if(sort.length()>0)
		{
			if("STATUS".startsWith(sort))
				sortNum=1;
			else
			if("VALID".startsWith(sort))
				sortNum=2;
			else
			if(("NAME".startsWith(sort))||("PLAYER".startsWith(sort)))
				sortNum=3;
			else
			if(("IP".startsWith(sort))||("ADDRESS".startsWith(sort)))
				sortNum=4;
			else
			if(("IDLE".startsWith(sort))||("MILLISECONDS".startsWith(sort)))
				sortNum=5;
		}
		if(sortNum<0)
			sorted=broken;
		else
		{
			sorted=new Vector();
			while(broken.size()>0)
			{
				int selected=0;
				for(int s=1;s<broken.size();s++)
				{
					String[] S=(String[])broken.elementAt(s);
					if(S[sortNum].compareToIgnoreCase(((String[])broken.elementAt(selected))[sortNum])<0)
					   selected=s;
				}
				sorted.addElement(broken.elementAt(selected));
				broken.removeElementAt(selected);
			}
		}
		for(int s=0;s<sorted.size();s++)
		{
			String[] S=(String[])sorted.elementAt(s);
			for(int i=0;i<S.length;i++)
				lines.append(S[i]);
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SESSIONS);}

	
}
