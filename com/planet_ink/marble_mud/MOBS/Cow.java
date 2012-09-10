package com.planet_ink.marble_mud.MOBS;
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
public class Cow extends StdMOB implements Drink
{
	public String ID(){return "Cow";}
	public Cow()
	{
		super();
		username="a cow";
		setDescription("A large lumbering beast that looks too slow to get out of your way.");
		setDisplayText("A fat happy cow wanders around here.");
		CMLib.factions().setAlignment(this,Faction.ALIGN_NEUTRAL);
		setMoney(0);
		setWimpHitPoint(0);

		basePhyStats().setDamage(1);
		basePhyStats().setSpeed(1.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(2);
		basePhyStats().setArmor(90);
		baseCharStats().setMyRace(CMClass.getRace("Cow"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}
	public long decayTime(){return 0;}
	public void setDecayTime(long time){}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
			return true;
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
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
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_FILL)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Container)
		&&(((Container)msg.target()).capacity()>0))
		{
			Container container=(Container)msg.target();
			Item I=CMClass.getItem("GenLiquidResource");
			I.setName("some milk");
			I.setDisplayText("some milk has been left here.");
			I.setDescription("It looks like milk");
			I.setMaterial(RawMaterial.RESOURCE_MILK);
			I.setBaseValue(RawMaterial.CODES.VALUE(RawMaterial.RESOURCE_MILK));
			I.basePhyStats().setWeight(1);
			CMLib.materials().addEffectsToResource(I);
			I.recoverPhyStats();
			I.setContainer(container);
			if(container.owner()!=null)
				if(container.owner() instanceof MOB)
					((MOB)container.owner()).addItem(I);
				else
				if(container.owner() instanceof Room)
					((Room)container.owner()).addItem(I,ItemPossessor.Expire.Resource);
		}
	}
	public int thirstQuenched(){return 100;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return RawMaterial.RESOURCE_MILK;}
	public boolean disappearsAfterDrinking(){return false;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public int amountTakenToFillMe(Drink theSource){return 0;}
}
