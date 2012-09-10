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
@SuppressWarnings({"unchecked","rawtypes"})
public class Mobile extends ActiveTicker implements MobileBehavior
{
	public String ID(){return "Mobile";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}
	
	protected boolean wander=false;
	protected boolean dooropen=false;
	protected int leash=0;
	protected Hashtable leashHash=null;
	protected Vector restrictedLocales=null;
	protected long[] altStatusTaker=null;
	protected long tickStatus=Tickable.STATUS_NOT;
	protected int ticksSuspended=0;
	
	public String accountForYourself()
	{ 
		return "wandering";
	}
	
	public long getTickStatus()
	{
		long[] o=altStatusTaker;
		if((o!=null)&&(o[0]!=Tickable.STATUS_NOT))
			return o[0];
		return tickStatus;
	}

	public Mobile()
	{
		super();
		minTicks=20; maxTicks=60; chance=100;
		leash=0;
		wander=false;
		dooropen=false;
		restrictedLocales=null;
		tickReset();
	}


	public boolean okRoomForMe(Room currentRoom, Room newRoom)
	{
		if(newRoom==null) return false;
		if(leash>0)
		{
			if(currentRoom==null) return false;
			if(leashHash==null)    leashHash=new Hashtable();
			Integer DISTNOW=(Integer)leashHash.get(currentRoom);
			Integer DISTLATER=(Integer)leashHash.get(newRoom);
			if(DISTNOW==null)
			{
				DISTNOW=Integer.valueOf(0);
				leashHash.put(currentRoom,DISTNOW);
			}
			if(DISTLATER==null)
			{
				DISTLATER=Integer.valueOf(DISTNOW.intValue()+1);
				leashHash.put(newRoom,DISTLATER);
			}
			if(DISTLATER.intValue()>(DISTNOW.intValue()+1))
			{
				DISTLATER=Integer.valueOf(DISTNOW.intValue()+1);
				leashHash.remove(newRoom);
				leashHash.put(newRoom,DISTLATER);
			}
			if(DISTLATER.intValue()>leash)
				return false;
		}
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(Integer.valueOf(newRoom.domainType()));
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		wander=false;
		dooropen=false;
		leash=0;
		leashHash=null;
		restrictedLocales=null;
		leash=CMParms.getParmInt(newParms,"LEASH",0);
		Vector<String> V=CMParms.parse(newParms);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("WANDER"))
				wander=true;
			else
			if(s.equalsIgnoreCase("OPENDOORS"))
				dooropen=true;
			else
			if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
			{
				if(restrictedLocales==null)
					restrictedLocales=new Vector();
				if(s.equalsIgnoreCase("+ALL"))
					restrictedLocales.clear();
				else
				if(s.equalsIgnoreCase("-ALL"))
				{
					restrictedLocales.clear();
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						restrictedLocales.addElement(Integer.valueOf(Room.INDOORS+i));
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						restrictedLocales.addElement(Integer.valueOf(i));
				}
				else
				{
					char c=s.charAt(0);
					s=s.substring(1).toUpperCase().trim();
					int code=-1;
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(Room.indoorDomainDescs[i].startsWith(s))
							code=Room.INDOORS+i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.removeElement(Integer.valueOf(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.addElement(Integer.valueOf(code));
					}
					code=-1;
					for(int i=0;i<Room.outdoorDomainDescs.length;i++)
						if(Room.outdoorDomainDescs[i].startsWith(s))
							code=i;
					if(code>=0)
					{
						if((c=='+')&&(restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.removeElement(Integer.valueOf(code));
						else
						if((c=='-')&&(!restrictedLocales.contains(Integer.valueOf(code))))
							restrictedLocales.addElement(Integer.valueOf(code));
					}

				}
			}
		}
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_MISC2+0;
		super.tick(ticking,tickID);
		if(ticksSuspended>0)
		{
			ticksSuspended--;
			return true;
		}
		if((ticking instanceof MOB)
		&&(!((MOB)ticking).isInCombat())
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBILITY))
		&&(canAct(ticking,tickID)))
		{
			Vector objections=null;
			final MOB mob=(MOB)ticking;
			final Room room=mob.location();
			if(room==null) return true;
			
			if((room.getArea()!=null)
			&&(room.getArea().getAreaState()!=Area.State.ACTIVE))
				return true;
			
			if((!CMLib.flags().canWorkOnSomething(mob)) && (CMLib.dice().roll(1,100,0)>1))
			{
				tickDown=0;
				return true;
			}
			
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Room R=room.getRoomInDir(d);
				if((R!=null)&&(!okRoomForMe(room,R)))
				{
					if(objections==null) objections=new Vector();
					objections.addElement(R);
				}
			}
			tickStatus=Tickable.STATUS_MISC2+16;
			altStatusTaker=new long[1];
			CMLib.tracking().beMobile((MOB)ticking,dooropen,wander,false,objections!=null,altStatusTaker,objections);
			if(mob.location()==room)
				tickDown=0;
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
	@Override
	public void suspendMobility(int numTicks) { ticksSuspended=numTicks;}
}
