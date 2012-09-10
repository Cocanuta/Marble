package com.planet_ink.marble_mud.Common;
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
import com.planet_ink.marble_mud.MOBS.interfaces.Auctioneer.AuctionData;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;



import java.util.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;

/*
Copyright 2007-2012 Bo Zimmerman

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
public class AuctionCoffeeShop implements CoffeeShop
{
	public String ID(){return "AuctionCoffeeShop";}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public static final Vector emptyV=new Vector();
	public String auctionShop="";
	protected WeakReference<ShopKeeper> shopKeeper=null;
	
	public CMObject copyOf()
	{
		try
		{
			Object O=this.clone();
			return (CMObject)O;
		}
		catch(CloneNotSupportedException e)
		{
			return new AuctionCoffeeShop();
		}
	}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new AuctionCoffeeShop();}}
	public void initializeClass(){}
	
	
	public CoffeeShop build(ShopKeeper SK) {
		shopKeeper=new WeakReference(SK);
		return this;
	}
	
	public ShopKeeper shopKeeper(){ return (shopKeeper==null)?null:shopKeeper.get();}
	public boolean isSold(int code){ShopKeeper SK=shopKeeper(); return (SK==null)?false:SK.isSold(code);}
	
	public boolean inEnumerableInventory(Environmental thisThang)
	{
		return false;
	}

	public Environmental addStoreInventory(Environmental thisThang){ return addStoreInventory(thisThang,1,-1);}
	public int enumerableStockSize(){ return 0;}
	public int totalStockSize(){ return 0;}
	public Iterator<Environmental> getStoreInventory(){ return emptyV.iterator();}
	public Iterator<Environmental> getStoreInventory(String srchStr){ return emptyV.iterator();}
	public Iterator<Environmental> getEnumerableInventory(){ return emptyV.iterator();}
	
	public Environmental addStoreInventory(Environmental thisThang, 
										   int number, 
										   int price)
	{
		if(shopKeeper() instanceof Auctioneer)
			auctionShop=((Auctioneer)shopKeeper()).auctionHouse();
		return thisThang;
	}
	
	public int totalStockWeight(){return 0;}
	
	public int totalStockSizeIncludingDuplicates(){ return 0;}
	public void delAllStoreInventory(Environmental thisThang){}
	
	public boolean doIHaveThisInStock(String name, MOB mob){return getStock(name,mob)!=null;}

	public int stockPrice(Environmental likeThis)
	{
		return -1;
	}
	public int numberInStock(Environmental likeThis){ return 1;}
	public void resubmitInventory(List<Environmental> V){}
	
	public Environmental getStock(String name, MOB mob)
	{
		List<AuctionData> auctions=CMLib.coffeeShops().getAuctions(null,auctionShop);
		Vector auctionItems=new Vector();
		for(int a=0;a<auctions.size();a++)
		{
			Item I=((Auctioneer.AuctionData)auctions.get(a)).auctioningI;
			auctionItems.addElement(I);
		}
		for(int a=0;a<auctionItems.size();a++)
		{
			Item I=(Item)auctionItems.elementAt(a);
			I.setExpirationDate(CMLib.english().getContextNumber(auctionItems,I));
		}
		Environmental item=CMLib.english().fetchEnvironmental(auctionItems,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(auctionItems,name,false);
		return item;
	}


	public Environmental removeStock(String name, MOB mob)
	{
		return null;
	}
	
	public void emptyAllShelves(){}
	
	public List<Environmental> removeSellableProduct(String named, MOB mob)
	{
		return emptyV;
	}
	
	public String makeXML(){return "";}
	public void buildShopFromXML(String text){}
}
