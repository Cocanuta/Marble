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
public class ClanWho extends Who
{
	public ClanWho(){}

	private final String[] access={"CLANWHO","CLWH"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(mob.getMyClan()==null))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		StringBuffer msg=new StringBuffer("");
		for(Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();

			if((mob2!=null)
			&&(!S.isStopped())
			&&((((mob2.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
					||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))&&(mob.phyStats().level()>=mob2.phyStats().level()))))
			&&(mob2.getClanID().equals(mob.getClanID()))
			&&(CMLib.flags().isInTheGame(mob2,true))
			&&(mob2.phyStats().level()>0))
				msg.append(showWhoShort(mob2));
		}
		mob.tell(shortHead+msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
