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
public class EndlessOcean extends StdGrid
{
	public String ID(){return "EndlessOcean";}
	public EndlessOcean()
	{
		super();
		name="the ocean";
		basePhyStats.setWeight(2);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_WATERSURFACE;}
	public int domainConditions(){return Room.CONDITION_WET;}

	public String getGridChildLocaleID(){return "SaltWaterSurface";}
	public List<Integer> resourceChoices(){return UnderSaltWater.roomResources;}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(WaterSurface.isOkWaterSurfaceAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}
	
	public void buildGrid()
	{
		super.buildGrid();
		if(subMap!=null)
		{
			Exit ox=CMClass.getExit("Open");
			if(rawDoors()[Directions.NORTH]==null)
				for(int i=0;i<subMap.length;i++)
					if(subMap[i][0]!=null)
						linkRoom(subMap[i][0],subMap[i][yGridSize()/2],Directions.NORTH,ox,ox);
			if(rawDoors()[Directions.SOUTH]==null)
				for(int i=0;i<subMap.length;i++)
					if(subMap[i][yGridSize()-1]!=null)
						linkRoom(subMap[i][yGridSize()-1],subMap[i][yGridSize()/2],Directions.SOUTH,ox,ox);
			if(rawDoors()[Directions.EAST]==null)
				for(int i=0;i<subMap[0].length;i++)
					if(subMap[xGridSize()-1][i]!=null)
						linkRoom(subMap[xGridSize()-1][i],subMap[xGridSize()/2][i],Directions.EAST,ox,ox);
			if(rawDoors()[Directions.WEST]==null)
				for(int i=0;i<subMap[0].length;i++)
					if(subMap[0][i]!=null)
						linkRoom(subMap[0][i],subMap[xGridSize()/2][i],Directions.WEST,ox,ox);
			if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS())
			{
				if(rawDoors()[Directions.NORTHEAST]==null)
				{
					for(int i=0;i<subMap.length;i++)
						if(subMap[i][0]!=null)
							linkRoom(subMap[i][0],subMap[xGridSize()/2][yGridSize()/2],Directions.NORTHEAST,ox,ox);
					for(int i=0;i<subMap[0].length;i++)
						if(subMap[subMap.length-1][i]!=null)
							linkRoom(subMap[subMap.length-1][i],subMap[xGridSize()/2][yGridSize()/2],Directions.NORTHEAST,ox,ox);
				}
				if(rawDoors()[Directions.NORTHWEST]==null)
				{
					for(int i=0;i<subMap.length;i++)
						if(subMap[i][0]!=null)
							linkRoom(subMap[i][0],subMap[xGridSize()/2][yGridSize()/2],Directions.NORTHWEST,ox,ox);
					for(int i=0;i<subMap[0].length;i++)
						if(subMap[0][i]!=null)
							linkRoom(subMap[0][i],subMap[xGridSize()/2][yGridSize()/2],Directions.NORTHWEST,ox,ox);
				}
				if(rawDoors()[Directions.SOUTHWEST]==null)
				{
					for(int i=0;i<subMap.length;i++)
						if(subMap[i][yGridSize()-1]!=null)
							linkRoom(subMap[i][yGridSize()-1],subMap[xGridSize()/2][yGridSize()/2],Directions.SOUTHWEST,ox,ox);
					for(int i=0;i<subMap[0].length;i++)
						if(subMap[0][i]!=null)
							linkRoom(subMap[0][i],subMap[xGridSize()/2][yGridSize()/2],Directions.SOUTHWEST,ox,ox);
				}
				if(rawDoors()[Directions.SOUTHEAST]==null)
				{
					for(int i=0;i<subMap.length;i++)
						if(subMap[i][yGridSize()-1]!=null)
							linkRoom(subMap[i][yGridSize()-1],subMap[xGridSize()/2][yGridSize()/2],Directions.SOUTHEAST,ox,ox);
					for(int i=0;i<subMap[0].length;i++)
						if(subMap[subMap.length-1][i]!=null)
							linkRoom(subMap[subMap.length-1][i],subMap[xGridSize()/2][yGridSize()/2],Directions.NORTHEAST,ox,ox);
				}
			}
		}
	}
}
