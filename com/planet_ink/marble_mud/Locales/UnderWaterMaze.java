package com.planet_ink.marble_mud.Locales;
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
@SuppressWarnings("unchecked")
public class UnderWaterMaze extends StdMaze
{
	public String ID(){return "UnderWaterMaze";}
	public UnderWaterMaze()
	{
		super();
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
		basePhyStats.setWeight(3);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_UNDERWATER;}
	public int domainConditions(){return Room.CONDITION_WET;}
	protected int baseThirst(){return 0;}


	public String getGridChildLocaleID(){return "UnderWater";}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(UnderWater.isOkUnderWaterAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}
	public List<Integer> resourceChoices(){return UnderWater.roomResources;}
}
