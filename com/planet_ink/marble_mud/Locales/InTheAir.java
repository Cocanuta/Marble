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
public class InTheAir extends StdRoom
{
	public String ID(){return "InTheAir";}
	public InTheAir()
	{
		super();
		basePhyStats.setWeight(1);
		name="the sky";
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_AIR;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		return isOkAirAffect(this,msg);
	}

	public static void makeFall(Physical P, Room room, int avg)
	{
		if((P==null)||(room==null)) return;

		if((avg==0)&&(room.getRoomInDir(Directions.DOWN)==null)) return;
		if((avg>0)&&(room.getRoomInDir(Directions.UP)==null)) return;

		if(((P instanceof MOB)&&(!CMLib.flags().isInFlight(P)))
		||((P instanceof Item)
				&&(((Item)P).container()==null)
				&&(!CMLib.flags().isFlying(((Item)P).ultimateContainer(null)))))
		{
			if(!CMLib.flags().isFalling(P))
			{
				Ability falling=CMClass.getAbility("Falling");
				if(falling!=null)
				{
					falling.setProficiency(avg);
					falling.setAffectedOne(room);
					falling.invoke(null,null,P,true,0);
				}
			}
		}
	}

	public static void airAffects(Room room, CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room)) return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector<Physical> needToFall=new Vector<Physical>();
		Vector<Physical> mightNeedAdjusting=new Vector<Physical>();
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if((mob!=null)
			&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room)))
			{
				Ability A=mob.fetchEffect("Falling");
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
					needToFall.addElement(mob);
			}
		}
		for(int i=0;i<room.numItems();i++)
		{
			Item item=room.getItem(i);
			if(item!=null)
			{
				Ability A=item.fetchEffect("Falling");
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
				if(item.container()==null)
					needToFall.addElement(item);
			}
		}
		int avg=((foundReversed)&&(!foundNormal))?100:0;
		for(Physical P : mightNeedAdjusting)
		{
			Ability A=P.fetchEffect("Falling");
			if(A!=null) A.setProficiency(avg);
		}
		for(Physical P : needToFall)
			makeFall(P,room,avg);
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		InTheAir.airAffects(this,msg);
	}

	public static boolean isOkAirAffect(Room room, CMMsg msg)
	{
		if(CMLib.flags().isSleeping(room))
			return true;
		if((msg.sourceMinor()==CMMsg.TYP_SIT)
		&&(!(msg.target() instanceof Exit))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			msg.source().tell("You can't sit here.");
			return false;
		}
		if((msg.sourceMinor()==CMMsg.TYP_SLEEP)&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			msg.source().tell("You can't sleep here.");
			return false;
		}
				
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.amITarget(room)))
		{
			MOB mob=msg.source();
			if((!CMLib.flags().isInFlight(mob))&&(!CMLib.flags().isFalling(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
			if(CMLib.dice().rollPercentage()>50)
			switch(room.getArea().getClimateObj().weatherType(room))
			{
			case Climate.WEATHER_BLIZZARD:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The swirling blizzard inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_HAIL:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The hail storm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_RAIN:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The rain storm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_SLEET:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The biting sleet inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_THUNDERSTORM:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The thunderstorm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_WINDY:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The hard winds inhibit <S-YOUPOSS> progress.");
				return false;
			}
		}
		InTheAir.airAffects(room,msg);
		return true;
	}
}
