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
public class ActiveTicker extends StdBehavior
{
	public String ID(){return "ActiveTicker";}
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;}

	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;
	//protected short speed=1;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}



	public void setParms(String newParms)
	{
		parms=newParms;
		minTicks=CMParms.getParmInt(parms,"min",minTicks);
		maxTicks=CMParms.getParmInt(parms,"max",maxTicks);
		chance=CMParms.getParmInt(parms,"chance",chance);
		tickReset();
	}

	public String rebuildParms()
	{
		StringBuffer rebuilt=new StringBuffer("");
		rebuilt.append(" min="+minTicks);
		rebuilt.append(" max="+maxTicks);
		rebuilt.append(" chance="+chance);
		return rebuilt.toString();
	}
	
	public String getParmsNoTicks()
	{
		String parms=getParms();
		char c=';';
		int x=parms.indexOf(c);
		if(x<0){ c='/'; x=parms.indexOf(c);}
		if(x>0)
		{
			if((x+1)>parms.length())
				return "";
			parms=parms.substring(x+1);
		}
		else
		{
			return "";
		}
		return parms;
	}

	protected boolean canAct(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		||(tickID==Tickable.TICKID_ITEM_BEHAVIOR)
		||(tickID==Tickable.TICKID_ROOM_BEHAVIOR)
		||((tickID==Tickable.TICKID_AREA)&&(ticking instanceof Area)))
		{
			int a=CMLib.dice().rollPercentage();
			if((--tickDown)<1)
			{
				tickReset();
				if((ticking instanceof MOB)&&(!canActAtAll(ticking)))
					return false;
				if(a>chance)
					return false;
				return true;
			}
		}
		return false;
	}
}
