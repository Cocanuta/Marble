package com.planet_ink.marble_mud.Libraries.interfaces;
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
import com.planet_ink.marble_mud.Libraries.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.io.IOException;
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
public interface CatalogLibrary extends CMLibrary
{
	public String[] getCatalogItemNames();
	public String[] getCatalogMobNames();
	public Item[] getCatalogItems();
	public MOB[] getCatalogMobs();
	public boolean isCatalogObj(Environmental E);
	public boolean isCatalogObj(String name);
	public Item getCatalogItem(String name);
	public MOB getCatalogMob(String name);
	public Physical getCatalogObj(Physical P);
	public CataData getCatalogItemData(String name);
	public CataData getCatalogMobData(String name);
	public CataData getCatalogData(Physical P);
	public void delCatalog(Physical P);
	public void addCatalog(Physical PA);
	public void submitToCatalog(Physical P);
	public void updateCatalog(Physical modelP);
	public StringBuffer checkCatalogIntegrity(Physical P);
	public void updateCatalogIntegrity(Physical P);
	public void changeCatalogUsage(Physical P, boolean add);
	public Item getDropItem(MOB M, boolean live);
	public CataData sampleCataData(String xml);
	public Vector<RoomContent> roomContent(Room R);
	public void updateRoomContent(String roomID, Vector<RoomContent> content);
	public void newInstance(Physical P);
	public void bumpDeathPickup(Physical P);
	
	public static interface RoomContent
	{
		public Physical P();
		public Environmental holder();
		public boolean isDirty();
		public void flagDirty();
		public boolean deleted();
	}
	
	public static interface CataData 
	{
		public MaskingLibrary.CompiledZapperMask getMaskV();
		public String getMaskStr();
		public boolean getWhenLive();
		public double getRate();
		public void setMaskStr(String s);
		public void setWhenLive(boolean l);
		public void setRate(double r);
		public Enumeration<Physical> enumeration();
		public void addReference(Physical P);
		public boolean isReference(Physical P);
		public void delReference(Physical P);
		public int numReferences();
		public String mostPopularArea();
		public String randomRoom();
		public void cleanHouse();
		public Physical getLiveReference();
		public int getDeathsPicksups();
		public void bumpDeathPickup();
		
		public String data();
		
		public void build(String catadata);
	}
}
