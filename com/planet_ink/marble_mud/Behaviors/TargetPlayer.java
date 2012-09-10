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

/**
 * <p>Title: False Realities Flavored marblemud</p>
 * <p>Description: The False Realities Version of marblemud</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
@SuppressWarnings("rawtypes")
public class TargetPlayer extends ActiveTicker
{
	public String ID(){return "TargetPlayer";}
	protected int canImproveCode() {return Behavior.CAN_MOBS;}

	public TargetPlayer() 
	{
		super();
		minTicks=3; maxTicks=12; chance=100;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "hero targeting";
	}

	public boolean tick(Tickable ticking, int tickID) 
	{
		if(canAct(ticking,tickID))
		{
			MOB mob = (MOB) ticking;
			if (mob.getVictim() != null) 
			{
				Set<MOB> theBadGuys = mob.getVictim().getGroupMembers(new HashSet<MOB>());
				MOB shouldFight = null;
				for (Iterator e = theBadGuys.iterator(); e.hasNext(); ) 
				{
					MOB consider = (MOB) e.next();
					if (consider.isMonster())
						continue;
					if (shouldFight == null) 
					{
						shouldFight = consider;
					}
					else 
					{
						if (((shouldFight.phyStats()!=null)&&(consider.phyStats()!=null)) 
						&&(shouldFight.phyStats().level() > consider.phyStats().level()))
							shouldFight = consider;
					}
				}
				if(shouldFight!=null) 
				{
					if(shouldFight.equals(mob.getVictim()))
						return true;
					else 
					if(CMLib.flags().canBeSeenBy(shouldFight,mob))
					{
						mob.setVictim(shouldFight);
					}
				}
			}
			return true;
		}
		return true;
	}
}
