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
public class FasterRecovery extends StdBehavior
{
	public String ID(){return "FasterRecovery";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS|Behavior.CAN_ITEMS;}
	
	public String accountForYourself()
	{ 
		return "faster recovering";
	}

	public static int getVal(String text, String key, int defaultValue)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	
	public void doBe(MOB M, int burst, int health, int hits, int mana, int move)
	{
		if(M==null) return;
		for(int i2=0;i2<burst;i2++)
			M.tick(M,Tickable.TICKID_MOB);
		for(int i2=0;i2<health;i2++)
			M.curState().recoverTick(M,M.maxState());
		if(hits!=0)
		{
			int oldMana=M.curState().getMana();
			int oldMove=M.curState().getMovement();
			for(int i2=0;i2<mana;i2++)
				M.curState().recoverTick(M,M.maxState());
			M.curState().setMana(oldMana);
			M.curState().setMovement(oldMove);
		}
		if(mana!=0)
		{
			int oldHP=M.curState().getHitPoints();
			int oldMove=M.curState().getMovement();
			for(int i2=0;i2<mana;i2++)
				M.curState().recoverTick(M,M.maxState());
			M.curState().setHitPoints(oldHP);
			M.curState().setMovement(oldMove);
		}
		if(move!=0)
		{
			int oldMana=M.curState().getMana();
			int oldHP=M.curState().getHitPoints();
			for(int i2=0;i2<mana;i2++)
				M.curState().recoverTick(M,M.maxState());
			M.curState().setMana(oldMana);
			M.curState().setHitPoints(oldHP);
		}
	}
	public void doBe(Room room, int burst, int health, int hits, int mana, int move)
	{
		if(room==null) return;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if(M!=null)
				doBe(M,burst,health,hits,mana,move);
		}
	}
	public void doBe(Area area, int burst, int health, int hits, int mana, int move)
	{
		if(area==null) return;
		for(Enumeration r=area.getMetroMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			doBe(R,burst,health,hits,mana,move);
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		int burst=getVal(getParms(),"BURST",0)-1;
		int health=getVal(getParms(),"HEALTH",0)-1;
		int hits=getVal(getParms(),"HITS",0)-1;
		int mana=getVal(getParms(),"MANA",0)-1;
		int move=getVal(getParms(),"MOVE",0)-1;
		if(ticking instanceof Room)
			doBe((Room)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Area)
			doBe((Area)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Rideable)
		{
			Rider R=null;
			for(int r=0;r<((Rideable)ticking).numRiders();r++)
			{
				R=((Rideable)ticking).fetchRider(r);
				if(R instanceof MOB)
					doBe((MOB)R,burst,health,hits,mana,move);
			}
		}
		else
		if(ticking instanceof MOB)
			doBe((MOB)ticking,burst,health,hits,mana,move);
		else
		if(ticking instanceof Item)
		{
			if(CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof MOB)
			&&(!((Item)ticking).amWearingAt(Wearable.IN_INVENTORY)))
				doBe((MOB)((Item)ticking).owner(),burst,health,hits,mana,move);
			else
			if(!CMLib.flags().isGettable((Item)ticking)
			&&(((Item)ticking).owner() instanceof Room))
				doBe((Room)((Item)ticking).owner(),burst,health,hits,mana,move);
		}
		return true;
	}
}
