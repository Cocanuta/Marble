package com.planet_ink.marble_mud.Items.MiscTech;
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
public class StdShipEngine extends StdElecItem
	implements ShipComponent.ShipEngine
{
	public String ID(){	return "StdShipEngine";}
	public StdShipEngine()
	{
		super();
		setName("a ships engine");
		basePhyStats.setWeight(500);
		setDisplayText("a ships engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setFuelType(RawMaterial.RESOURCE_PLASMA);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipEngine)) return false;
		return super.sameAs(E);
	}
	protected int maxThrust=1000;
	public int getMaxThrust(){return maxThrust;}
	public void setMaxThrust(int max){maxThrust=max;}
	protected int thrust=1000;
	public int getThrust(){return thrust;}
	public void setThrust(int current){thrust=current;}
}
