package com.planet_ink.marble_mud.Items.interfaces;
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
public interface Electronics extends Item, Technical
{
	public int fuelType();
	public void setFuelType(int resource);
	
	public long powerCapacity();
	public void setPowerCapacity(long capacity);
	
	public long powerRemaining();
	public void setPowerRemaining(long remaining);
	
	public boolean activated();
	public void activate(boolean truefalse);
	
	public interface PowerSource extends Electronics
	{
	}
	
	public interface PowerGenerator extends PowerSource
	{
		public int[] getConsumedFuelTypes();
		public void setConsumedFuelType(int[] resources);
		public int getGeneratedAmountPerTick();
		public void setGenerationAmountPerTick(int amt);
	}
	
	public interface ElecPanel extends Electronics
	{
		public static enum ElecPanelType
		{
			ANY,WEAPON,ENGINE,SENSOR,POWER,COMPUTER,ENVIRO_CONTROL,GENERATOR
		}
		
		public ElecPanelType panelType();
		public void setPanelType(ElecPanelType type);
	}
	
}
