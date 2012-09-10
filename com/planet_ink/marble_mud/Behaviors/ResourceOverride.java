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
public class ResourceOverride extends StdBehavior
{
	public String ID(){return "ResourceOverride";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	private int tickDown=1;
	private Vector rscs=new Vector();
	private Vector roomTypes=new Vector();
	
	public String accountForYourself()
	{ 
		return "resource overriding";
	}
	
	public void setParms(String newStr)
	{
		super.setParms(newStr);
		rscs.clear();
		roomTypes.clear();
		Vector<String> V=CMParms.parse(getParms());
		if(V.size()==0) return;
		for(int v=0;v<V.size();v++)
		{
			// first try for a real one
			int code=-1;
			String which=((String)V.elementAt(v)).toUpperCase().trim();
			if((CMath.s_int(which)>0)||(which.equalsIgnoreCase("0")))
			   code=CMath.s_int(which);
	
			if(code<0) code = RawMaterial.CODES.FIND_IgnoreCase(which);
			if(code<0)
				for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
				{
					if(RawMaterial.MATERIAL_DESCS[i].equalsIgnoreCase(which))
					{ code=RawMaterial.CODES.COMPOSE_RESOURCES(i).get(0).intValue(); break;}
				}
			if(code<0) code = RawMaterial.CODES.FIND_StartsWith(which);
			if(code<0)
				for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
				{
					if(RawMaterial.MATERIAL_DESCS[i].startsWith(which))
					{ code=RawMaterial.CODES.COMPOSE_RESOURCES(i).get(0).intValue(); break;}
				}
			if(code>=0)
			{
				if(!rscs.contains(Integer.valueOf(code)))
					rscs.add(Integer.valueOf(code));
			}
			else
			{
				for(int i=0;i<Room.outdoorDomainDescs.length;i++)
					if(which.equalsIgnoreCase(Room.outdoorDomainDescs[i]))
					{ code=i; break;}
				if(code<0)
					for(int i=0;i<Room.indoorDomainDescs.length;i++)
						if(which.equalsIgnoreCase(Room.indoorDomainDescs[i]))
						{ code=Room.INDOORS|i; break;}
				if(!roomTypes.contains(Integer.valueOf(code)))
					roomTypes.add(Integer.valueOf(code));
			}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(rscs.size()==0) return true;
		if((--tickDown)>0) return true;
		if((tickID==Tickable.TICKID_ROOM_BEHAVIOR)
		&&(ticking instanceof Room))
		{
			tickDown=2;
			Room R=(Room)ticking;
			if(!rscs.contains(Integer.valueOf(R.myResource())))
				R.setResource(((Integer)rscs.elementAt(CMLib.dice().roll(1,rscs.size(),-1))).intValue());
		}
		else
		if((tickID==Tickable.TICKID_AREA)
		&&(ticking instanceof Area))
		{
			tickDown=5;
			Area A=(Area)ticking;
			Room R=null;
			for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
			{
				R=(Room)e.nextElement();
				if(((roomTypes.size()==0)||(!roomTypes.contains(Integer.valueOf(R.domainType()))))
				&&(!rscs.contains(Integer.valueOf(R.myResource()))))
					R.setResource(((Integer)rscs.elementAt(CMLib.dice().roll(1,rscs.size(),-1))).intValue());
			}
		}
		return true;
	}
}
