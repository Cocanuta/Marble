package com.planet_ink.marble_mud.Abilities.Properties;
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
public class Prop_Weather extends Property
{
	public String ID() { return "Prop_Weather"; }
	public String name(){ return "Weather Setter";}
	protected int canAffectCode(){return Ability.CAN_AREAS;}

	int code=-1;
	
	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		if((code<0)&&(text().length()>0))
		{
			for(int i=0;i<Climate.WEATHER_DESCS.length;i++)
				if(Climate.WEATHER_DESCS[i].equalsIgnoreCase(text()))
					code=i;
		}
		if(code>=0)
		{
			if(affected instanceof Room)
			{
				((Room)affected).getArea().getClimateObj().setCurrentWeatherType(code);
				((Room)affected).getArea().getClimateObj().setNextWeatherType(code);
			}
			else
			if(affected instanceof Area)
			{
				((Area)affected).getClimateObj().setCurrentWeatherType(code);
				((Area)affected).getClimateObj().setNextWeatherType(code);
			}
		}
	}
	
}
