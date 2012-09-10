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
@SuppressWarnings("rawtypes")
public class ResetWhole extends StdBehavior
{
	public String ID(){return "ResetWhole";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	protected long lastAccess=-1;

	public String accountForYourself()
	{ 
		return "periodic resetting";
	}

	public void executeMsg(Environmental E, CMMsg msg)
	{
		super.executeMsg(E,msg);
		if(!msg.source().isMonster())
		{
			if((E instanceof Area)
			&&(((Area)E).inMyMetroArea(msg.source().location().getArea())))
				lastAccess=System.currentTimeMillis();
			else
			if((E instanceof Room)
			&&(msg.source().location()==E))
				lastAccess=System.currentTimeMillis();
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(lastAccess<0) return true;

		long time=1800000;
		try
		{
			time=Long.parseLong(getParms());
			time=time*CMProps.getTickMillis();
		}
		catch(Exception e){}
		if((lastAccess+time)<System.currentTimeMillis())
		{
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(Enumeration<Behavior> e=R.behaviors();e.hasMoreElements();)
					{
						Behavior B=e.nextElement();
						if((B!=null)&&(B.ID().equals(ID())))
						{ R=null; break;}
					}
					if(R!=null)
						CMLib.map().resetRoom(R, true);
				}
			}
			else
			if(ticking instanceof Room)
				CMLib.map().resetRoom((Room)ticking, true);
			else
			{
				Room room=super.getBehaversRoom(ticking);
				if(room!=null)
					CMLib.map().resetRoom(room, true);
			}
			lastAccess=System.currentTimeMillis();
		}
		return true;
	}
}
