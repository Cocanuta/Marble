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
public class After extends StdCommand implements Tickable
{
	public String name(){return "SysOpSkills";} // for tickables use
	public long getTickStatus(){return Tickable.STATUS_NOT;}

	public List<AfterCommand> afterCmds=new Vector<AfterCommand>();

	public After(){}
	
	private static class AfterCommand
	{
		long start=0;
		long duration=0;
		boolean every=false;
		MOB M=null;
		Vector command=null;
		int metaFlags=0;
	}

	private final String[] access={"AFTER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean every=false;
		commands.removeElementAt(0);

		String afterErr="format: after (every) [X] [TICKS/MINUTES/SECONDS/HOURS] [COMMAND]";
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("stop"))
		{
			afterCmds.clear();
			CMLib.threads().deleteTick(this,Tickable.TICKID_AREA);
			mob.tell("Ok.");
			return false;
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("list"))
		{
			//afterCmds.clear();
			int s=0;
			StringBuffer str=new StringBuffer("^xCurrently scheduled AFTERs: ^?^.^?\n\r");
			str.append(CMStrings.padRight("Next run",20)+" "+CMStrings.padRight(" Interval",20)+" "+CMStrings.padRight("Who",10)+" Command\n\r");
			while(s<afterCmds.size())
			{
				AfterCommand V=afterCmds.get(s);
				every=V.every;
				str.append(CMStrings.padRight(CMLib.time().date2String(V.start+V.duration),20)+" ");
				str.append((every?"*":" ")+CMStrings.padRight(CMLib.english().returnTime(V.duration,0),20)+" ");
				str.append(CMStrings.padRight(V.M.Name(),10)+" ");
				str.append(CMStrings.limit(CMParms.combine(V.command,0),25)+"\n\r");
				s++;
			}
			mob.tell(str.toString());
			return false;
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("every"))
		{ every=true; commands.removeElementAt(0);}
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		long time=CMath.s_long((String)commands.elementAt(0));
		if(time==0) { mob.tell("Time may not be 0."+afterErr); return false;}
		commands.removeElementAt(0);
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		String s=(String)commands.elementAt(0);
		long multiplier=CMLib.english().getMillisMultiplierByName(s);
		if(multiplier<0)
		{
			mob.tell("'"+s+" Time may not be 0. "+afterErr);
			return false;
		}
		else
			time=time*multiplier;
		commands.removeElementAt(0);
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		AfterCommand V=new AfterCommand();
		V.start=System.currentTimeMillis();
		V.duration=time;
		V.every=every;
		V.M=mob;
		V.command=commands;
		V.metaFlags=metaFlags;
		afterCmds.add(V);
		CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		mob.tell("Ok.");
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AFTER);}



	public boolean tick(Tickable ticking, int tickID)
	{
		if(afterCmds.size()==0) return false;
		int s=0;
		while(s<afterCmds.size())
		{
			AfterCommand cmd=afterCmds.get(s);
			if(System.currentTimeMillis()>(cmd.start+cmd.duration))
			{
				boolean every=cmd.every;
				if(every)
				{
					cmd.start=System.currentTimeMillis();
					s++;
				}
				else
					afterCmds.remove(s);
				cmd.M.doCommand((Vector)cmd.command.clone(),cmd.metaFlags);
			}
			else
				s++;
		}
		return true;
	}
}
