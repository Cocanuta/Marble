package com.planet_ink.marble_mud.Common;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.marble_mud.core.threads.ServiceEngine;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Function;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.marble_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.marble_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.StdRace;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
Copyright 2012 Ben Cherrington

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
/**
 * Abstract collection of item objects, complete with some 
 * finders and various accessors.  Also, the copyOf method
 * does a deep copy.  Also, this is a weak item collection,
 * so as items get destroyed, they disappear from here.
 */
public class WeakItemCollection implements ItemCollection, CMCommon
{
	private SVector<WeakReference<Item>> innerContents=new SVector<WeakReference<Item>>(0); 
	private ConvertingList<WeakReference<Item>,Item> contents 
				= new ConvertingList<WeakReference<Item>,Item>(innerContents,
						new Converter<WeakReference<Item>,Item>(){
					public Item convert(WeakReference<Item> obj) { return obj.get();}
				});
	public String ID() { return "WeakItemCollection"; }
	
	public CMObject copyOf() 
	{
		WeakItemCollection c=(WeakItemCollection)newInstance();
		for(int i=0;i<contents.size();i++)
		{
			Item I=contents.get(i);
			if(I!=null)
			{
				Item I2=(Item)I.copyOf();
				I2.setOwner(I.owner());
				c.innerContents.add(new WeakReference<Item>(I2));
			}
		}
		for(int i=0;i<contents.size();i++)
		{
			Item I=contents.get(i);
			Item I2=c.contents.get(i);
			if((I!=null)&&(I2!=null))
			{
				if(I.container() != null)
					for(int i2=0;i2<contents.size();i2++)
						if(I.container() == contents.get(i2))
						{
							I2.setContainer((Container)c.contents.get(i2));
							break;
						}
			}
		}
		return c;
	}
	
	public void initializeClass() {}
	public CMObject newInstance() { return new WeakItemCollection(); }
	public int compareTo(CMObject o) { return o==this?0:1; }
	
	public Item findItem(String itemID)
	{
		Item item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,true);
		if(item==null) item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,false);
		return item;
	}
	public Enumeration<Item> items() { return new IteratorEnumeration<Item>(contents.iterator()); }
	
	public Item findItem(Item goodLocation, String itemID)
	{
		Item item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(item==null) item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return item;
	}
	
	public List<Item> findItems(Item goodLocation, String itemID)
	{
		List<Item> items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(items.size()==0)
			items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return items;
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public List<Item> findItems(String itemID)
	{
		List items=CMLib.english().fetchEnvironmentals(contents,itemID,true);
		if(items.size()==0)
			items=CMLib.english().fetchEnvironmentals(contents,itemID, false);
		return items;
	}
	
	public void addItem(Item item)
	{
		if((item!=null)&&(!item.amDestroyed()))
			innerContents.addElement(new WeakReference<Item>(item));
	}
	
	public void delItem(Item item)
	{
		for(int i=contents.size()-1;i>=0;i--)
		{
			Item I=getItem(i);
			if(I==item)
			{
				innerContents.remove(i);
				break;
			}
			else
			if(I==null)
				try { innerContents.remove(i); }catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
	}
	public void delAllItems(boolean destroy)
	{
		if(destroy)
			for(int i=numItems()-1;i>=0;i--)
			{
				Item I=getItem(i);
				if(I!=null) I.destroy();
			}
		innerContents.clear();
		contents.clear();
	}

	public int numItems()
	{
		return contents.size();
	}
	
	public boolean isContent(Item item)
	{
		for(int i=contents.size()-1;i>=0;i--)
		{
			Item I=getItem(i);
			if(I==item) 
				return true;
			if(I==null) 
				try { innerContents.remove(i); }catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return false;
	}
	
	public Item getItem(int i)
	{
		try
		{
			Item I=contents.get(i);
			if(I==null) 
				innerContents.remove(i);
			else
				return I;
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public void eachItem(final EachApplicable<Item> applier)
	{
		final List<Item> contents=this.contents;
		if(contents!=null)
		try{
    		for(int a=0;a<contents.size();a++)
    		{
    			final Item I=contents.get(a);
    			if(I!=null) 
    				applier.apply(I);
    			else
    				try { innerContents.remove(a); }catch(java.lang.ArrayIndexOutOfBoundsException x){}
    		}
    	} catch(ArrayIndexOutOfBoundsException e){}
	}
    public Item getRandomItem()
    {
    	if(numItems()==0) return null;
    	return getItem(CMLib.dice().roll(1,numItems(),-1));
    }
}
