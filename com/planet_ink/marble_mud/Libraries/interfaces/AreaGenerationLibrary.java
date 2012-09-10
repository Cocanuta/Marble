package com.planet_ink.marble_mud.Libraries.interfaces;
import com.planet_ink.marble_mud.core.exceptions.CMException;
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
import com.planet_ink.marble_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.marble_mud.Libraries.layouts.AbstractLayout;
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
public interface AreaGenerationLibrary extends CMLibrary
{
	public void buildDefinedIDSet(List<XMLpiece> xmlRoot, Map<String,Object> defined);
	public List<Item> findItems(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public List<MOB> findMobs(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public String findString(String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public Room buildRoom(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException;
	public void checkRequirements(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public Map<String,String> getUnfilledRequirements(Map<String,Object> defined, XMLLibrary.XMLpiece piece);
	public Area buildArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, int direction) throws CMException;
	public boolean fillInArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Area A, int direction) throws CMException;
	public LayoutManager getLayoutManager(String named);
	
	public static interface LayoutManager
	{
		public String name();
		public Vector<LayoutNode> generate(int num, int dir);
	}
	
	public static interface LayoutNode 
	{
		public void crossLink(LayoutNode to);
		public void delLink(LayoutNode linkNode);
		public LayoutNode getLink(int d);
		public Hashtable<Integer,LayoutNode> links();
		public Hashtable<LayoutTags,String> tags();
		public long[] coord();
		public boolean isStreetLike();
		public void deLink();
		public void flag(LayoutFlags flag);
		public void flagRun(LayoutRuns dirs);
		public boolean isFlagged(LayoutFlags flag);
		public LayoutRuns getFlagRuns();
		public LayoutTypes type();
		public void setExits(int[] dirs);
		public void flagGateExit(int dir);
		public void reType(LayoutTypes type);
		public String getColorRepresentation(int line);
		public Room room();
		public void setRoom(Room room);
	}
	
	public enum LayoutTags { NODERUN, NODEFLAGS, NODETYPE, NODEEXITS, NODEGATEEXIT}
	public enum LayoutTypes { surround, leaf, street, square, interior }
	public enum LayoutFlags { corner, gate, intersection, tee, offleaf }
	public enum LayoutRuns { ew,ns,ud,nesw,nwse }
}
