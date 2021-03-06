package com.planet_ink.marble_mud.Commands;
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
@SuppressWarnings("rawtypes")
public class Weather extends StdCommand
{
	public Weather(){}

	private final String[] access={"WEATHER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room room=mob.location();
		if(room==null) return false;
		if((commands.size()>1)&&((room.domainType()&Room.INDOORS)==0)&&(((String)commands.elementAt(1)).equalsIgnoreCase("WORLD")))
		{
			StringBuffer tellMe=new StringBuffer("");
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
					tellMe.append(CMStrings.padRight(A.name(),20)+": "+A.getClimateObj().weatherDescription(room)+"\n\r");
			}
			mob.tell(tellMe.toString());
			return false;
		}
		mob.tell(room.getArea().getClimateObj().weatherDescription(room));
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
