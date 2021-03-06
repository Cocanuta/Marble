package com.planet_ink.marble_mud.Races;
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
public class Harpy extends GreatBird
{
	public String ID(){	return "Harpy"; }
	public String name(){ return "Harpy"; }
	public int shortestMale(){return 59;}
	public int shortestFemale(){return 59;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 160;}
	public int weightVariance(){return 80;}
	public long forbiddenWornBits(){return Wearable.WORN_HELD|Wearable.WORN_WIELD|Wearable.WORN_FEET;}
	public String racialCategory(){return "Avian";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,4,20,50,75,100,110,120};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{ affectableStats.setStat(CharStats.STAT_GENDER,'F');}

	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some greasy "+name().toLowerCase()+" claws",RawMaterial.RESOURCE_BONE));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("some dirty "+name().toLowerCase()+" feathers",RawMaterial.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
