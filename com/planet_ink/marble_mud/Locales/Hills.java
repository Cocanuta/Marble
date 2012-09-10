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
@SuppressWarnings({"unchecked","rawtypes"})
public class Hills extends StdRoom
{
	public String ID(){return "Hills";}
	public Hills()
	{
		super();
		name="the hills";
		basePhyStats.setWeight(3);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_HILLS;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_GRAPES),
		Integer.valueOf(RawMaterial.RESOURCE_BERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_BLUEBERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_BLACKBERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_STRAWBERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_RASPBERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_BOYSENBERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_GREENS),
		Integer.valueOf(RawMaterial.RESOURCE_OLIVES),
		Integer.valueOf(RawMaterial.RESOURCE_BEANS),
		Integer.valueOf(RawMaterial.RESOURCE_RICE),
		Integer.valueOf(RawMaterial.RESOURCE_LEATHER),
		Integer.valueOf(RawMaterial.RESOURCE_FEATHERS),
		Integer.valueOf(RawMaterial.RESOURCE_MESQUITE),
		Integer.valueOf(RawMaterial.RESOURCE_EGGS),
		Integer.valueOf(RawMaterial.RESOURCE_HERBS),
		Integer.valueOf(RawMaterial.RESOURCE_POTATOES)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public List<Integer> resourceChoices(){return Hills.roomResources;}
}
