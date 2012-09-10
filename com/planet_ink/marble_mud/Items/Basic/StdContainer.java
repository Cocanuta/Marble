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
public class StdContainer extends StdItem implements Container
{
	public String ID(){	return "StdContainer";}
	protected boolean isLocked=false;
	protected boolean hasALock=false;
	protected boolean isOpen=true;
	protected boolean hasALid=false;
	protected int capacity=0;
	protected long containType=0;

	public StdContainer()
	{
		super();
		setName("a container");
		setDisplayText("a nondescript container sits here.");
		setDescription("I`ll bet you could put stuff in it!");
		capacity=25;
		baseGoldValue=10;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_COTTON;
	}

	public int capacity()
	{
		return capacity;
	}
	public void setCapacity(int newValue)
	{
		capacity=newValue;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					if(!CMLib.flags().isDroppable((Item)msg.tool()))
					{
						mob.tell("You can't seem to let go of "+msg.tool().name()+".");
						return false;
					}
					Item newitem=(Item)msg.tool();
					if(hasALid()&&(!isOpen()))
					{
						mob.tell(name()+" is closed.");
						return false;
					}
					else
					if(newitem.amWearingAt(Wearable.WORN_WIELD))
					{
						mob.tell("You are already wielding that!");
						return false;
					}
					else
					if(newitem.amWearingAt(Wearable.WORN_HELD))
					{
						mob.tell("You are holding that!");
						return false;
					}
					else
					if(!newitem.amWearingAt(Wearable.IN_INVENTORY))
					{
						mob.tell("You are wearing that!");
						return false;
					}
					else
					if(capacity<=0)
					{
						mob.tell("You can't put anything in "+name()+"!");
						return false;
					}
					else
					{
						if(!canContain(newitem))
						{
							mob.tell("You can't put "+newitem.name()+" in "+name()+".");
							return false;
						}
						else
						if(newitem.phyStats().weight()>capacity)
						{
							mob.tell(newitem.name()+" won't fit in "+name()+".");
							return false;
						}
						else
						if((recursiveWeight()+newitem.phyStats().weight())>capacity)
						{
							if(getContents().size()==0)
								mob.tell(name()+" is too small.");
							else
								mob.tell(name()+" is full.");
							return false;
						}
						if((!msg.source().isMine(this))&&(msg.source().isMine(newitem)))
							if(!CMLib.commands().postDrop(msg.source(),newitem,true,true))
								return false;
						return true;
					}
				}
				break;
			case CMMsg.TYP_GET:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!(CMLib.flags().canBeSeenBy(newitem,mob)||(newitem instanceof Light)))
						&&(amWearingAt(Wearable.IN_INVENTORY))
						&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
						{
							mob.tell(mob,newitem,this,"You can't see <T-NAME> in <O-NAME>.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(name()+" is closed.");
							return false;
						}
						else
						if((mob.phyStats().level()<newitem.phyStats().level()-(10+(mob.phyStats().level()/5)))
						&&(!(mob instanceof ShopKeeper))
						&&(!mob.charStats().getMyRace().leveless())
						&&(!mob.charStats().getCurrentClass().leveless()))
						{
							mob.tell(newitem.name()+" is too powerful to endure possessing it.");
							return false;
						}
						else
						if((newitem.recursiveWeight()>(mob.maxCarry()-mob.phyStats().weight()))&&(!mob.isMine(this)))
						{
							mob.tell(newitem.name()+" is too heavy.");
							return false;
						}
						else
						if((newitem.numberOfItems()>(mob.maxItems()-mob.numItems()))&&(!mob.isMine(this)))
						{
							mob.tell("You can't carry that many items.");
							return false;
						}
						else
						if(!CMLib.flags().isGettable(newitem))
						{
							mob.tell("You can't get "+newitem.name()+".");
							return false;
						}
						return true;
					}
					mob.tell(mob,newitem,this,"You can't see <T-NAME> in <O-NAME>.");
					return false;
				}
				else
				if((recursiveWeight()>(mob.maxCarry()-mob.phyStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				else
				if(((numberOfItems()>mob.maxItems()-mob.numItems()))&&(!mob.isMine(this)))
				{
					mob.tell("You can't carry that many items.");
					return false;
				}
				break;
			case CMMsg.TYP_REMOVE:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!CMLib.flags().canBeSeenBy(newitem,mob))
						&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
						{
							mob.tell("You can't see that.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(name()+" is closed.");
							return false;
						}
						else
							return true;
					}
					mob.tell("You don't see that here.");
					return false;
				}
				break;
			case CMMsg.TYP_CLOSE:
				if(isOpen)
				{
					if(!hasALid)
					{
						mob.tell("There is nothing to close on "+name()+".");
						return false;
					}
					return true;
				}
				mob.tell(name()+" is already closed.");
				return false;
			case CMMsg.TYP_OPEN:
				if(!hasALid)
				{
					mob.tell("There is nothing to open on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is already open!");
					return false;
				}
				if(isLocked)
				{
					mob.tell(name()+" is locked.");
					return false;
				}
				return true;
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
				if(!hasALid)
				{
					mob.tell("There is nothing to lock or unlock on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is open!");
					return false;
				}
				else
				if(!hasALock)
				{
					mob.tell("There is no lock!");
					return false;
				}
				else
				{
					if((!isLocked)&&(msg.targetMinor()==CMMsg.TYP_UNLOCK))
					{
						mob.tell(name()+" is not locked.");
						return false;
					}
					else
					if((isLocked)&&(msg.targetMinor()==CMMsg.TYP_LOCK))
					{
						mob.tell(name()+" is already locked.");
						return false;
					}
					else
					{
						for(int i=0;i<mob.numItems();i++)
						{
							Item item=mob.getItem(i);
							if((item!=null)
							&&(item instanceof DoorKey)
							&&((DoorKey)item).getKey().equals(keyName())
							&&((item.container()==null)
							   ||((item.container().container()==null)
								  &&(item.container() instanceof Container)
								  &&((((Container)item.container()).containTypes()&Container.CONTAIN_KEYS)>0)))
							&&(CMLib.flags().canBeSeenBy(item,mob)))
								return true;
						}
						mob.tell("You don't have the key.");
						return false;
					}
				}
				//break;
				default:
					break;
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_EXIT_REOPEN)
		{
			setLidsNLocks(hasALid,!hasALid,hasALock,hasALock);
			return false;
		}
		return super.tick(ticking,tickID);
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					newitem.setContainer(this);
					if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					{
						mob.location().recoverRoomStats();
						newitem.recoverPhyStats();
					}
				}
				break;
			case CMMsg.TYP_CLOSE:
				if((!hasALid)||(!isOpen)) return;
				isOpen=false;
				break;
			case CMMsg.TYP_OPEN:
				if((!hasALid)||(isOpen)||(isLocked)) return;
				if((owner() instanceof Room)
				&&(!CMLib.flags().isGettable(this))
				&&(!CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN)))
					CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,30);
				isLocked=false;
				isOpen=true;
				break;
			case CMMsg.TYP_LOCK:
				if((!hasALid)||(!hasALock)||(isLocked)) return;
				isOpen=false;
				isLocked=true;
				break;
			case CMMsg.TYP_UNLOCK:
				if((!hasALid)||(!hasALock)||(isOpen)||(!isLocked))
					return;
				if((owner() instanceof Room)
				&&(!CMLib.flags().isGettable(this))
				&&(!CMLib.threads().isTicking(this,Tickable.TICKID_EXIT_REOPEN)))
					CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,30);
				isLocked=false;
				break;
			default:
				break;
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.sourceMinor()==CMMsg.TYP_THROW)
		&&(msg.source()!=null)
		&&(msg.source().isMine(this)))
		{
			setContainer(null);
			Room R=CMLib.map().roomLocation(msg.target());
			if(R!=null)
			{
				CMLib.utensils().recursiveDropMOB(msg.source(),R,this,this instanceof DeadBody);
				if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_OPTIMIZE))
				{
					msg.source().location().recoverRoomStats();
					if(msg.source().location()!=R)
						R.recoverRoomStats();
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	public long containTypes(){return containType;}

	public void setContainTypes(long containTypes){containType=containTypes;}

	public boolean canContain(Environmental E)
	{
		if (!(E instanceof Item)) return false;
		if(containType==0) return true;
		for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
			if(CMath.isSet((int)containType,i))
				switch((int)CMath.pow(2,i))
				{
				case CONTAIN_LIQUID:
					if((((Item)E).material()&RawMaterial.MATERIAL_LIQUID)>0)
						return true;
					break;
				case CONTAIN_COINS:
					if(E instanceof Coins)
						return true;
					break;
				case CONTAIN_SWORDS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_SWORD))
						return true;
					break;
				case CONTAIN_DAGGERS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_KEYS:
					if(E instanceof DoorKey)
						return true;
					break;
				case CONTAIN_DRINKABLES:
					if((E instanceof Drink)&&(E instanceof Item))
						return true;
					break;
				case CONTAIN_CLOTHES:
					if((E instanceof Armor)
					&&(((Armor)E).fitsOn(Wearable.WORN_ABOUT_BODY)
					   ||((Armor)E).fitsOn(Wearable.WORN_ARMS)
					   ||((Armor)E).fitsOn(Wearable.WORN_LEGS)
					   ||((Armor)E).fitsOn(Wearable.WORN_HEAD)
					   ||((Armor)E).fitsOn(Wearable.WORN_TORSO)
					   ||((Armor)E).fitsOn(Wearable.WORN_WAIST)))
						return true;
					break;
				case CONTAIN_FOOTWEAR:
					if((E instanceof Armor)
					&&(((Armor)E).fitsOn(Wearable.WORN_FEET)))
						return true;
					break;
				case CONTAIN_RAWMATERIALS:
					return (E instanceof RawMaterial);
				case CONTAIN_OTHERWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_SWORD)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_ONEHANDWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).rawLogicalAnd()==false))
						return true;
					break;
				case CONTAIN_BODIES:
					if(E instanceof DeadBody)
						return true;
					break;
				case CONTAIN_SMOKEABLES:
					if(E instanceof Item)
					{
						if((((Item)E).material()==RawMaterial.RESOURCE_PIPEWEED)
						||(((Item)E).material()==RawMaterial.RESOURCE_HERBS))
							return true;
					}
					break;
				case CONTAIN_CAGED:
					if(E instanceof CagedAnimal)
						return true;
					break;
				case CONTAIN_READABLES:
					if((E instanceof Item)
					&&(((Item)E).isReadable()))
						return true;
					break;
				case CONTAIN_SCROLLS:
					if(E instanceof Scroll)
						return true;
					break;
				case CONTAIN_SSCOMPONENTS:
					if(E instanceof ShipComponent)
						return true;
					break;
				}
		return false;
	}




	public boolean isLocked(){return isLocked;}
	public boolean hasALock(){return hasALock;}
	public boolean isOpen(){return isOpen;}
	public boolean hasALid(){return hasALid;}

	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked)
	{
		hasALid=newHasALid;
		isOpen=newIsOpen;
		hasALock=newHasALock;
		isLocked=newIsLocked;
	}

	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
		if(!isGeneric()) setKeyName(miscText);
	}
	public String keyName()
	{
		return miscText;
	}
	public void setKeyName(String newKeyName)
	{
		miscText=newKeyName;
	}
	public void emptyPlease()
	{
		List<Item> V=getContents();
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.get(v);
			I.setContainer(null);
		}
	}
	public boolean isInside(Item I)
	{
		if(I.container()==null) return false;
		if(I.container()==this) return true;
		return isInside(I.container());
	}
	
	@Override
	public int numberOfItems()
	{
		return getContents().size()+1;
	}
	
	@Override
	public int recursiveWeight()
	{
		int weight=phyStats().weight();
		if(owner()==null) return weight;
		if(owner() instanceof MOB)
		{
			MOB M=(MOB)owner();
			for(int i=0;i<M.numItems();i++)
			{
				Item thisItem=M.getItem(i);
				if((thisItem!=null)&&(thisItem.ultimateContainer(this)==this))
					weight+=thisItem.phyStats().weight();
			}
		}
		else
		if(owner() instanceof Room)
		{
			Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				Item thisItem=R.getItem(i);
				if((thisItem!=null)&&(thisItem.ultimateContainer(this)==this))
					weight+=thisItem.phyStats().weight();
			}
		}
		return weight;
	}
	
	public ReadOnlyList<Item> getContents()
	{
		List<Item> V=new Vector<Item>();
		if(owner()!=null)
		{
			Item I;
			for(Enumeration<Item> e = owner().items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if(I==null) continue;
				if(isInside(I))
					V.add(I);
			}
		}
		return new ReadOnlyList<Item>(V);
	}
}
