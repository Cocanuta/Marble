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
public class GiantAmphibian extends GreatAmphibian
{
	public String ID(){	return "GiantAmphibian"; }
	public String name(){ return "Giant Amphibian"; }
	public int shortestMale(){return 50;}
	public int shortestFemale(){return 55;}
	public int heightVariance(){return 20;}
	public int lightestWeight(){return 1955;}
	public int weightVariance(){return 405;}
	public long forbiddenWornBits(){return ~(Wearable.WORN_EYES);}
	public String racialCategory(){return "Amphibian";}
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();

	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<25;i++)
				resources.addElement(makeResource
				("some "+name().toLowerCase(),RawMaterial.RESOURCE_FISH));
				for(int i=0;i<15;i++)
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",RawMaterial.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
