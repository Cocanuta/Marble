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
public class ClanDeclare extends StdCommand
{
	public ClanDeclare(){}

	private final String[] access={"CLANDECLARE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());

		commands.setElementAt(getAccessWords()[0],0);
		if(commands.size()<3)
		{
			mob.tell("You must specify the clans name, and a new relationship.");
			return false;
		}
		String rel=((String)commands.lastElement()).toUpperCase();
		commands.removeElementAt(commands.size()-1);
		String clan=CMParms.combine(commands,1).toUpperCase();
		commands.addElement(rel);
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		if((clan.length()>0)&&(rel.length()>0))
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=mob.getMyClan();
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.DECLARE,false))
				{
					int newRole=-1;
					for(int i=0;i<Clan.REL_DESCS.length;i++)
						if(rel.equalsIgnoreCase(Clan.REL_DESCS[i]))
							newRole=i;
					if(newRole<0)
					{
						mob.tell("'"+rel+"' is not a valid relationship. Try WAR, HOSTILE, NEUTRAL, FRIENDLY, or ALLY.");
						return false;
					}
					Clan C2=CMLib.clans().findClan(clan);
					if(C2==null)
					{
						mob.tell(clan+" isn't valid clan.");
						return false;
					}
					if(C2==C)
					{
						mob.tell("You can't do that.");
						return false;
					}
					if(C.getClanRelations(C2.clanID())==newRole)
					{
						mob.tell("You are already in that state with "+C2.clanID()+".");
						return false;

					}
					long last=C.getLastRelationChange(C2.clanID());
					if(last>(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)*CMProps.getTickMillis()))
					{
						last=last+(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)*CMProps.getTickMillis());
						if(System.currentTimeMillis()<last)
						{
							mob.tell("You must wait at least 1 mud month between relation changes.");
							return false;
						}
					}
					if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.DECLARE,true))
					{
						CMLib.clans().clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has declared "+CMStrings.capitalizeAndLower(Clan.REL_STATES[newRole].toLowerCase())+" "+C2.name()+".");
						C.setClanRelations(C2.clanID(),newRole,System.currentTimeMillis());
						C.update();
						return false;
					}
				}
				else
				{
					msg.append("You aren't in the right position to declare relationships with your "+C.getGovernmentName()+".");
				}
			}
		}
		else
		{
			mob.tell("You must specify the clans name, and a new relationship.");
			return false;
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
