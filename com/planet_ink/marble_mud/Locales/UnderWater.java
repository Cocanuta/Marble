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
public class UnderWater extends StdRoom implements Drink
{
	public String ID(){return "UnderWater";}
	public UnderWater()
	{
		super();
		name="the water";
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
		basePhyStats.setWeight(3);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_UNDERWATER;}
	public int domainConditions(){return Room.CONDITION_WET;}
	protected int baseThirst(){return 0;}
	public long decayTime(){return 0;}
	public void setDecayTime(long time){}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
		}
	}

	public static void makeSink(Physical P, Room room, int avg)
	{
		if((P==null)||(room==null)) return;

		Room R=room.getRoomInDir(Directions.DOWN);
		if(avg>0) R=room.getRoomInDir(Directions.UP);
		if((R==null)
		||((R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
			return;

		if(((P instanceof MOB)&&(!CMLib.flags().isWaterWorthy(P))&&(!CMLib.flags().isInFlight(P))&&(P.phyStats().weight()>=1))
		||((P instanceof Item)&&(!CMLib.flags().isInFlight(((Item)P).ultimateContainer(null)))&&(!CMLib.flags().isWaterWorthy(((Item)P).ultimateContainer(null)))))
			if(P.fetchEffect("Sinking")==null)
			{
				Ability sinking=CMClass.getAbility("Sinking");
				if(sinking!=null)
				{
					sinking.setProficiency(avg);
					sinking.setAffectedOne(room);
					sinking.invoke(null,null,P,true,0);
				}
			}
	}

	public static void sinkAffects(Room room, CMMsg msg)
	{
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			MOB mob=msg.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(((Drink)room).thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
		
		CMLib.commands().handleHygenicMessage(msg, 100, PlayerStats.HYGIENE_WATERCLEAN);

		if(CMLib.flags().isSleeping(room))
			return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector<Physical> needToSink=new Vector<Physical>();
		Vector<Physical> mightNeedAdjusting=new Vector<Physical>();

		if((room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(room.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob=room.fetchInhabitant(i);
				if((mob!=null)
				&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room)))
				{
					Ability A=mob.fetchEffect("Sinking");
					if(A!=null)
					{
						if(A.proficiency()>=100)
						{
							foundReversed=true;
							mightNeedAdjusting.addElement(mob);
						}
						foundNormal=foundNormal||(A.proficiency()<=0);
					}
					else
					if((!CMath.bset(mob.basePhyStats().disposition(),PhyStats.IS_SWIMMING))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Amphibian"))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Fish")))
						needToSink.addElement(mob);
				}
			}
		for(int i=0;i<room.numItems();i++)
		{
			Item item=room.getItem(i);
			if(item!=null)
			{
				Ability A=item.fetchEffect("Sinking");
				if(A!=null)
				{
					if(A.proficiency()>=100)
					{
						foundReversed=true;
						mightNeedAdjusting.addElement(item);
					}
					foundNormal=foundNormal||(A.proficiency()<=0);
				}
				else
					needToSink.addElement(item);
			}
		}
		int avg=((foundReversed)&&(!foundNormal))?100:0;
		for(Physical P : mightNeedAdjusting)
		{
			Ability A=P.fetchEffect("Sinking");
			if(A!=null) A.setProficiency(avg);
		}
		for(Physical P : needToSink)
			makeSink(P,room,avg);
	}

	public static int isOkUnderWaterAffect(Room room, CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room))
			return 0;

		if((msg.source()==msg.target()) // suffocation is a valid gas attack
		&&(msg.sourceMinor()==msg.targetMinor())
		&&(msg.sourceMinor()==CMMsg.TYP_GAS))
		{
			return 0;
		}
		
		if((msg.targetMinor()==CMMsg.TYP_FIRE)
		||(msg.targetMinor()==CMMsg.TYP_GAS)
		||(msg.sourceMinor()==CMMsg.TYP_FIRE)
		||(msg.sourceMinor()==CMMsg.TYP_GAS))
		{
			msg.source().tell("That won't work underwater.");
			return -1;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon))
		{
			Weapon w=(Weapon)msg.tool();
			if((w.weaponType()==Weapon.TYPE_SLASHING)
			||(w.weaponType()==Weapon.TYPE_BASHING))
			{
				int damage=msg.value();
				damage=damage/3;
				damage=damage*2;
				msg.setValue(msg.value()-damage);
			}
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell("You don't want to be drinking saltwater.");
				return -1;
			}
			return 1;
		}
		return 0;
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(UnderWater.isOkUnderWaterAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
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
	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_SEAWEED),
		Integer.valueOf(RawMaterial.RESOURCE_FISH),
		Integer.valueOf(RawMaterial.RESOURCE_CATFISH),
		Integer.valueOf(RawMaterial.RESOURCE_SALMON),
		Integer.valueOf(RawMaterial.RESOURCE_CARP),
		Integer.valueOf(RawMaterial.RESOURCE_TROUT),
		Integer.valueOf(RawMaterial.RESOURCE_SAND),
		Integer.valueOf(RawMaterial.RESOURCE_CLAY),
		Integer.valueOf(RawMaterial.RESOURCE_LIMESTONE)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public List<Integer> resourceChoices(){return UnderWater.roomResources;}
}
