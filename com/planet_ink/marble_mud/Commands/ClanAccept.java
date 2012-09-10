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
import com.planet_ink.marble_mud.Common.interfaces.Clan.MemberRecord;
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
public class ClanAccept extends StdCommand
{
	public ClanAccept(){}

	private final String[] access={"CLANACCEPT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());

		commands.setElementAt(getAccessWords()[0],0);
		String qual=CMParms.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
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
				if(C.getGovernment().getAutoRole() == C.getGovernment().getAcceptPos())
				{
					mob.tell("Everyone is already accepted.");
					return false;
				}
				if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ACCEPT,false))
				{
					List<MemberRecord> apps=C.getMemberList(C.getGovernment().getAutoRole());
					if(apps.size()<1)
					{
						mob.tell("There are no applicants to your "+C.getGovernmentName()+".");
						return false;
					}
					qual=CMStrings.capitalizeAndLower(qual);
					for(MemberRecord member : apps)
					{
						if(member.name.equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMLib.players().getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not add to "+C.getGovernmentName()+".");
							return false;
						}
						if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.ACCEPT,true))
						{
							C.addMember(M,C.getGovernment().getAcceptPos());
							CMLib.clans().clanAnnounce(mob,M.Name()+" is now a new member of "+C.getGovernmentName()+" "+C.name()+".");
							mob.tell(M.Name()+" has been accepted into "+C.getGovernmentName()+" '"+C.clanID()+"'.");
							if((M.session()!=null)&&(M.session().mob()==M))
								M.tell(mob.Name()+" has accepted you as a member of "+C.getGovernmentName()+" '"+C.clanID()+"'.");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't an applicant of your "+C.getGovernmentName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to accept members into your "+C.getGovernmentName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which applicant you are accepting.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
