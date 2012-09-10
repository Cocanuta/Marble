package com.planet_ink.marble_mud.Abilities.Properties;
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
public class Prop_ItemTransporter extends Property
{
	public String ID() { return "Prop_ItemTransporter"; }
	public String name(){ return "Item Transporter";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	protected Room roomDestination=null;
	protected MOB mobDestination=null;
	protected Container nextDestination=null;
	protected static Map<String,List<PhysicalAgent>> possiblePossibilities=new Hashtable<String,List<PhysicalAgent>>();
	protected static Map<String,Integer> lastLooks=new Hashtable<String,Integer>();

	public String accountForYourself()
	{ return "Item Transporter";	}

	public Item ultimateParent(Item item)
	{
		if(item==null) return null;
		if(item.container()==null) return item;
		if(item.container().container()==item)
			item.container().setContainer(null);
		if(item.container()==item)
			item.setContainer(null);
		return ultimateParent(item.container());
	}

	private synchronized boolean setDestination()
	{
		List<PhysicalAgent> possibilities=(List<PhysicalAgent>)possiblePossibilities.get(text());
		Integer lastLook=lastLooks.get(text());
		if((possibilities==null)||(lastLook==null)||(lastLook.intValue()<0))
		{
			possibilities=new Vector();
			possiblePossibilities.put(text(),possibilities);
			lastLook=Integer.valueOf(10);
			lastLooks.put(text(),lastLook);
		}
		else
			lastLooks.put(text(),Integer.valueOf(lastLook.intValue()-1));
		if(possibilities.size()==0)
		{
			roomDestination=null;
			mobDestination=null;
			nextDestination=null;
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					Ability A=room.fetchEffect("Prop_ItemTransReceiver");
					if((A!=null)&&(A.text().equalsIgnoreCase(text())))
						possibilities.add(room);
					for(int i=0;i<room.numItems();i++)
					{
						Item item=room.getItem(i);
						if((item!=null)&&(item!=affected))
						{
							A=item.fetchEffect("Prop_ItemTransReceiver");
							if((A!=null)&&(A.text().equalsIgnoreCase(text())))
								possibilities.add(item);
						}
					}
					for(int m=0;m<room.numInhabitants();m++)
					{
						MOB mob=room.fetchInhabitant(m);
						if((mob!=null)&&(mob!=affected))
						{
							A=mob.fetchEffect("Prop_ItemTransReceiver");
							if((A!=null)&&(A.text().equalsIgnoreCase(text())))
								possibilities.add(mob);
							for(int i=0;i<mob.numItems();i++)
							{
								Item item=mob.getItem(i);
								if((item!=null)&&(item!=affected))
								{
									A=item.fetchEffect("Prop_ItemTransReceiver");
									if((A!=null)&&(A.text().equalsIgnoreCase(text())))
										possibilities.add(item);
								}
							}
						}
					}
				}
			}catch(NoSuchElementException e){}
		}
		if(possibilities.size()>0)
		{
			PhysicalAgent P=possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1));
			nextDestination=null;
			if(P instanceof Room)
				roomDestination=(Room)P;
			else
			if(P instanceof MOB)
				mobDestination=(MOB)P;
			else
			if(P instanceof Container)
			{
				nextDestination=(Container)P;
				if((nextDestination!=null)&&(nextDestination.owner()!=null))
				{
					if(nextDestination.owner() instanceof Room)
						roomDestination=(Room)nextDestination.owner();
					else
					if(nextDestination.owner() instanceof MOB)
						mobDestination=(MOB)nextDestination.owner();
				}
				else
					nextDestination=null;
			}
			else
			if(P instanceof Item)
			{
				nextDestination=null;
				Item I = (Item)P;
				if((I!=null)&&(I.owner()!=null))
				{
					if(I.owner() instanceof Room)
						roomDestination=(Room)I.owner();
					else
					if(I.owner() instanceof MOB)
						mobDestination=(MOB)I.owner();
				}
			}
		}
		if((mobDestination==null)&&(roomDestination==null))
			return false;
		return true;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null) return true;

		if(((msg.amITarget(affected))
			&&((msg.targetMinor()==CMMsg.TYP_PUT)
			   ||(msg.targetMinor()==CMMsg.TYP_GIVE))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Item))
		||((affected instanceof MOB)
			&&(msg.amISource((MOB)affected))
			&&(msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.target() !=null)
			&&(msg.target() instanceof Item))
		||((affected instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_DROP)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item))
		||((affected instanceof Room)
			&&(msg.sourceMinor()==CMMsg.TYP_THROW)
			&&(affected==CMLib.map().roomLocation(msg.target()))
			&&(msg.tool() instanceof Item)))
		{
			if(!setDestination())
			{
				msg.source().tell("The transporter has no possible ItemTransReceiver with the code '"+text()+"'.");
				return false;
			}
		}
		return true;
	}

	public synchronized void tryToMoveStuff()
	{
		if((mobDestination!=null)||(roomDestination!=null))
		{
			Room room=roomDestination;
			MOB mob=mobDestination;
			Room roomMover=null;
			MOB mobMover=null;
			Item container=null;
			if(affected==null) return;
			if(affected instanceof Room)
				roomMover=(Room)affected;
			else
			if(affected instanceof MOB)
				mobMover=(MOB)affected;
			else
			if(affected instanceof Item)
			{
				container=(Item)affected;
				if((container.owner()!=null)&&(container.owner() instanceof Room))
					roomMover=(Room)container.owner();
				else
				if((container.owner()!=null)&&(container.owner() instanceof MOB))
					mobMover=(MOB)container.owner();
			}
			Vector itemsToMove=new Vector();
			if(roomMover!=null)
			{
				for(int i=0;i<roomMover.numItems();i++)
				{
					Item item=roomMover.getItem(i);
					if((item!=null)
					   &&(item!=container)
					   &&(item.amWearingAt(Wearable.IN_INVENTORY))
					   &&((item.container()==container)||(ultimateParent(item)==container)))
					   itemsToMove.addElement(item);
				}
				for(int i=0;i<itemsToMove.size();i++)
					roomMover.delItem((Item)itemsToMove.elementAt(i));
			}
			else
			if(mobMover!=null)
			{
				int oldNum=itemsToMove.size();
				for(int i=0;i<mobMover.numItems();i++)
				{
					Item item=mobMover.getItem(i);
					if((item!=null)
					   &&(item!=container)
					   &&(item.amWearingAt(Wearable.IN_INVENTORY))
					   &&((item.container()==container)||(ultimateParent(item)==container)))
					   itemsToMove.addElement(item);
				}
				for(int i=oldNum;i<itemsToMove.size();i++)
					mobMover.delItem((Item)itemsToMove.elementAt(i));
			}
			if(itemsToMove.size()>0)
			{
				mobDestination=null;
				roomDestination=null;
				if(room!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						room.addItem(item,ItemPossessor.Expire.Player_Drop);
					}
				if(mob!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						if(mob instanceof ShopKeeper)
							((ShopKeeper)mob).getShop().addStoreInventory(item);
						else
							mob.addItem(item);
					}
				if(room!=null) room.recoverRoomStats();
				if(mob!=null){
					mob.recoverCharStats();
					mob.recoverPhyStats();
					mob.recoverMaxState();
				}
			}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			tryToMoveStuff();
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		// amazingly important that this happens first!
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_GET)
		||(msg.targetMinor()==CMMsg.TYP_GIVE)
		||(msg.targetMinor()==CMMsg.TYP_PUT)
		||(msg.sourceMinor()==CMMsg.TYP_THROW)
		||(msg.targetMinor()==CMMsg.TYP_DROP))
			tryToMoveStuff();
	}
}
