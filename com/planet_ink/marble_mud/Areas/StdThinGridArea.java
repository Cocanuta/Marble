package com.planet_ink.marble_mud.Areas;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.Area.CompleteRoomEnumerator;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class StdThinGridArea extends StdGridArea
{
	public String ID(){	return "StdThinGridArea";}
	public long flags(){return Area.FLAG_THIN;}
	public RoomnumberSet myRoomSet=null;

	public void addProperRoom(Room R)
	{
		if(R!=null) R.setExpirationDate(WorldMap.ROOM_EXPIRATION_MILLIS);
		super.addProperRoom(R);
	}
	public Room getRoom(String roomID)
	{
		if(!isRoom(roomID)) return null;
		Room R=super.getRoom(roomID);
		if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
		{
			if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
				roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			R=CMLib.database().DBReadRoomObject(roomID,false);
			if(R!=null)
			{
				R.setArea(this);
				addProperRoom(R);
				TreeMap<String,Room> V=new TreeMap<String,Room>();
				V.put(roomID,R);
				CMLib.database().DBReadRoomExits(roomID,V,false);
				CMLib.database().DBReadContent(R,V,true);
				fillInAreaRoom(R);
				R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
			}
		}
		return R;
	}
	public boolean isRoom(String roomID)
	{
		return getProperRoomnumbers().contains(roomID);
	}
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		if(super.isRoom(R)) return true;
		if(R.roomID().length()==0) return false;
		return isRoom(R.roomID());
	}
	public Enumeration<Room> getProperMap(){return new IteratorEnumeration<Room>(properRooms.values().iterator());}
	public Enumeration<Room> getMetroMap()
	{
		int minimum=getProperRoomnumbers().roomCountAllAreas()/10;
		if(getCachedRoomnumbers().roomCountAllAreas()<minimum)
		{
			for(int r=0;r<minimum;r++)
				getRandomProperRoom();
		}
		MultiEnumeration<Room> multiEnumerator = new MultiEnumeration<Room>(new RoomIDEnumerator(this));
		for(final Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			multiEnumerator.addEnumeration(a.next().getMetroMap());
		return new CompleteRoomEnumerator(multiEnumerator);
	}
	public Enumeration<Room> getCompleteMap(){return new CompleteRoomEnumerator(new RoomIDEnumerator(this));}
}
