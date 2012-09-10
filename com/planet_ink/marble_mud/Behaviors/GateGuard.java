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
public class GateGuard extends StdBehavior
{
	public String ID(){return "GateGuard";}

	protected int noticeTock=4;
	protected boolean heardKnock=false;
	protected boolean keepLocked=false;
	protected boolean allnight=false;

	public String accountForYourself()
	{ 
		return "gate guarding";
	}

	public void setParms(String parm)
	{
		super.setParms(parm);
		keepLocked=false;
		allnight=false;
		Vector<String> V=CMParms.parse(parm);
		for(int v=0;v<V.size();v++)
		{
			if(((String)V.elementAt(v)).equalsIgnoreCase("keeplocked"))
			{
				keepLocked=true;
				V.removeElementAt(v);
				break;
			}
			else
			if(((String)V.elementAt(v)).equalsIgnoreCase("allnight"))
			{
				allnight=true;
				V.removeElementAt(v);
				break;
			}
		}
	}

	protected int findGate(MOB mob)
	{
		if(!CMLib.flags().isInTheGame(mob,false))
			return -1;
		Room R=mob.location();
		if(R!=null)
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(R.getRoomInDir(d)!=null)
			{
				Exit e=R.getExitInDir(d);
				if((e!=null)&&(e.hasADoor()))
					return d;
			}
		}
		return -1;
	}

	protected DoorKey getMyKeyTo(MOB mob, Exit e)
	{
		DoorKey key=null;
		String keyCode=e.keyName();
		for(int i=0;i<mob.numItems();i++)
		{
			Item item=mob.getItem(i);
			if((item instanceof DoorKey)&&(((DoorKey)item).getKey().equals(keyCode)))
			{
				key=(DoorKey)item;
				break;
			}
		}
		if(key==null)
		{
			key=(DoorKey)CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addItem(key);
		}
		return key;
	}

	protected int numValidPlayers(MOB mob, Room room)
	{
		if(room==null) return 0;
		int num=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)
			&&(!M.isMonster())
			&&(CMLib.flags().canBeSeenBy(M,mob))
			&&(CMLib.masking().maskCheck(getParms(),M,false)))
				num++;
		}
		return num;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			MOB mob=(MOB)host;
			if((msg.targetMinor()==CMMsg.TYP_KNOCK)
			&&(!msg.amISource(mob))
			&&(mob.location()!=null)
			&&(mob.location()!=msg.source().location())
			&&(!heardKnock)
			&&(CMLib.flags().canHear(mob))
			&&(canFreelyBehaveNormal(host)))
			{
				int dir=findGate(mob);
				if((dir>=0)
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),false)))
				{
					Exit e=mob.location().getExitInDir(dir);
					if(msg.amITarget(e))
						heardKnock=true;
				}
			}
		}
		super.executeMsg(host,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		MOB mob=(MOB)ticking;
		int dir=findGate(mob);
		if(dir<0) return true;
		Exit e=mob.location().getExitInDir(dir);
		int numPlayers=numValidPlayers(mob,mob.location());
		if(noticeTock==0)
		{
			if(heardKnock) numPlayers++;
			if((!allnight)&&(mob.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
			{
				if((!e.isLocked())&&(e.hasALock()))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_LOCK,"<S-NAME> lock(s) <T-NAME>.");
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
			}
			else
			{
				if((e.isLocked())&&((!keepLocked)||(numPlayers>0)))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_UNLOCK,"<S-NAME> unlock(s) <T-NAME>.");
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
				if((numPlayers>0)&&(!e.isOpen())&&(!e.isLocked()))
				{
					mob.doCommand(CMParms.parse("OPEN "+Directions.getDirectionName(dir)),Command.METAFLAG_FORCED);
				}
				if((numPlayers==0)&&(e.isOpen()))
				{
					mob.doCommand(CMParms.parse("CLOSE "+Directions.getDirectionName(dir)),Command.METAFLAG_FORCED);
				}
				if((numPlayers==0)&&(!e.isOpen())&&(!e.isLocked())&&(e.hasALock())&&(keepLocked))
				{
					if(getMyKeyTo(mob,e)!=null)
					{
						CMMsg msg=CMClass.getMsg(mob,e,CMMsg.MSG_LOCK,"<S-NAME> lock(s) <T-NAME>.");
						if(mob.location().okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,mob.location(),dir);
					}
				}
			}
			heardKnock=false;
			noticeTock--;
		}
		else
		if(noticeTock<0)
		{
			if(heardKnock) numPlayers++;
			if(mob.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_NIGHT)
				noticeTock=5;
			else
			if((e.isLocked())||((numPlayers==0)&&(e.isOpen())))
				noticeTock=3;
			else
			if((numPlayers>0)&&(!e.isOpen()))
				noticeTock=0;
		}
		else
			noticeTock--;
		return true;
	}
}
