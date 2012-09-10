package com.planet_ink.marble_mud.Items.Basic;
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
public class StdCage extends StdContainer
{
	public String ID(){	return "StdCage";}
	public StdCage()
	{
		super();
		setName("a cage");
		setDisplayText("a cage sits here.");
		setDescription("It\\`s of solid wood construction with metal bracings.  The door has a key hole.");
		capacity=1000;
		setContainTypes(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED);
		material=RawMaterial.RESOURCE_OAK;
		baseGoldValue=15;
		basePhyStats().setWeight(25);
		recoverPhyStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_EXIT_REOPEN)&&(isOpen()))
		{
			Room R=CMLib.map().roomLocation(this);
			if((R!=null)&&(owner() instanceof Room)&&(CMLib.flags().isInTheGame(this,true)))
			{
				List<Item> mobContents=getContents();
				for(Iterator<Item> e=mobContents.iterator();e.hasNext();)
				{
					Environmental E=(Environmental)e.next();
					if(E instanceof CagedAnimal)
					{
						MOB M=((CagedAnimal)E).unCageMe();
						if(M!=null)
							M.bringToLife(R,true);
						R.show(M,null,this,CMMsg.MSG_OK_ACTION,"<S-NAME> escapes from <O-NAME>!");
						E.destroy();
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
				if((hasALid)&&(isOpen))
				{
					if(CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN))
						CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_REOPEN);
				}
				break;
			case CMMsg.TYP_OPEN:
				if((hasALid)&&(!isOpen)&&(!isLocked))
				{
					if((owner() instanceof Room)
					&&(!CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN)))
						CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,30);
				}
				break;
			case CMMsg.TYP_LOOK: case CMMsg.TYP_EXAMINE:
			{
				synchronized(this)
				{
					if(!isOpen)
					{
						isOpen=true;
						super.executeMsg(myHost,msg);
						isOpen=false;
						return;
					}
				}
				break;
			}
			}
		}
		super.executeMsg(myHost,msg);
	}
}
