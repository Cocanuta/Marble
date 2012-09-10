package com.planet_ink.marble_mud.Behaviors;
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
public class ClanHelper extends StdBehavior
{
	public String ID(){return "ClanHelper";}

	public String accountForYourself()
	{ 
		if(parms.length()>0)
			return "fellow '"+parms+"' protecting";
		else
			return "fellow clan members protecting";
	}
	
	protected boolean mobKiller=false;
	public void startBehavior(PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
			{
				((MOB)forMe).setClanID(parms.trim());
				Clan C=CMLib.clans().getClan(parms.trim());
				if(C==null)
					C=CMLib.clans().findClan(parms.trim());
				if(C!=null)
					((MOB)forMe).setClanRole(C.getGovernment().getAcceptPos());
				else
					((MOB)forMe).setClanRole(0);
			}
		}
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB))) return;
		MOB source=msg.source();
		MOB observer=(MOB)affecting;
		MOB target=(MOB)msg.target();

		if((target==null)||(observer==null)) return;
		if((source!=observer)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(target!=observer)
		&&(source!=target)
		&&(observer.getClanID().length()>0)
		&&(!observer.isInCombat())
		&&(CMLib.flags().canBeSeenBy(source,observer))
		&&(CMLib.flags().canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer,false)))
		{
			if(observer.getClanID().equalsIgnoreCase(target.getClanID()))
			{
				String reason="WE ARE UNDER ATTACK!! CHARGE!!";
				if((observer.getClanID().equals(target.getClanID()))
				&&(!observer.getClanID().equals(source.getClanID())))
					reason=observer.getClanID().toUpperCase()+"S UNITE! CHARGE!";
				Aggressive.startFight(observer,source,true,false,reason);
			}
		}
	}
}
