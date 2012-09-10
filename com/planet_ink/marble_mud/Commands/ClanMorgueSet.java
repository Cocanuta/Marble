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
public class ClanMorgueSet extends StdCommand
{
	public ClanMorgueSet(){}

	private final String[] access={"CLANMORGUESET"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		Room R=mob.location();
		if(skipChecks)
		{
			commands.setElementAt(getAccessWords()[0],0);
			R=CMLib.map().getRoom(CMParms.combine(commands,1));
		}
		else
		{
			commands.clear();
			commands.addElement(getAccessWords()[0]);
			commands.addElement(CMLib.map().getExtendedRoomID(R));
		}

		if((mob.getClanID()==null)||(R==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		Clan C=mob.getMyClan();
		if(C==null)
		{
			mob.tell("There is no longer a clan called "+mob.getClanID()+".");
			return false;
		}
		if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
		{
			mob.tell("You cannot set a morgue.  Your "+C.getGovernmentName()+" does not have enough members to be considered active.");
			return false;
		}
		if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,false))
		{
			if(!CMLib.law().doesOwnThisProperty(C.clanID(),R))
			{
				mob.tell("Your "+C.getGovernmentName()+" does not own this room.");
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,true))
			{
				C.setMorgue(CMLib.map().getExtendedRoomID(R));
				C.update();
				mob.tell("Your "+C.getGovernmentName()+" morgue is now set to "+R.roomTitle(mob)+".");
				CMLib.clans().clanAnnounce(mob, "The morgue of "+C.getGovernmentName()+" "+C.clanID()+" is now set to "+R.roomTitle(mob)+".");
				return true;
			}
		}
		else
		{
			mob.tell("You aren't in the right position to set your "+C.getGovernmentName()+"'s morgue.");
			return false;
		}
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
