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
public class ShallowWater extends StdRoom implements Drink
{
	public String ID(){return "ShallowWater";}
	public ShallowWater()
	{
		super();
		name="the water";
		basePhyStats.setWeight(2);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_WATERSURFACE;}
	public int domainConditions(){return Room.CONDITION_WET;}
	protected int baseThirst(){return 0;}
	public long decayTime(){return 0;}
	public void setDecayTime(long time){}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			if(liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell("You don't want to be drinking saltwater.");
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		
		CMLib.commands().handleHygenicMessage(msg, 100, PlayerStats.HYGIENE_WATERCLEAN);
		
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			MOB mob=msg.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
	}

	public int thirstQuenched(){return 500;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return RawMaterial.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean disappearsAfterDrinking(){return false;}
	public boolean containsDrink(){return true;}
	public int amountTakenToFillMe(Drink theSource){return 0;}
	public List<Integer> resourceChoices(){return UnderWater.roomResources;}
}
