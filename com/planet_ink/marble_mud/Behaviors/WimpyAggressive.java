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
public class WimpyAggressive extends Aggressive
{
	public String ID(){return "WimpyAggressive";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}

	public WimpyAggressive()
	{
		super();

		tickWait = 0;
		tickDown = 0;
	}

	public String accountForYourself()
	{ 
		if(getParms().trim().length()>0)
			return "wimpy aggression against "+CMLib.masking().maskDesc(getParms(),true).toLowerCase();
		else
			return "wimpy aggressiveness";
	}
	
	public boolean grantsAggressivenessTo(MOB M)
	{
		return ((M!=null)&&(CMLib.flags().isSleeping(M)))&&
			CMLib.masking().maskCheck(getParms(),M,false);
	}
	
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}

	public static void pickAWimpyFight(MOB observer, boolean mobKiller, boolean misBehave, String attackMsg, String zapStr)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		Room R=observer.location();
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB mob=R.fetchInhabitant(i);
			if((mob!=null)
			&&(mob!=observer)
			&&(CMLib.flags().isSleeping(mob))
			&&(CMLib.masking().maskCheck(zapStr,observer,false)))
			{
				startFight(observer,mob,mobKiller,misBehave,attackMsg);
				if(observer.isInCombat()) break;
			}
		}
	}

	public static void tickWimpyAggressively(Tickable ticking, boolean mobKiller, boolean misBehave, int tickID, String attackMsg, String zapStr)
	{
		if(tickID!=Tickable.TICKID_MOB) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		pickAWimpyFight((MOB)ticking,mobKiller,misBehave,attackMsg,zapStr);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Tickable.TICKID_MOB) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickWimpyAggressively(ticking,mobkill,misbehave,tickID,attackMessage,getParms());
		}
		return true;
	}
}
