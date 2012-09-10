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
public class Prop_LimitedItems extends Property
{
	public String ID() { return "Prop_LimitedItems"; }
	public String name(){ return "Limited Item";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public static Map<String,List<Item>> instances=new Hashtable<String,List<Item>>();
	public static boolean[] playersLoaded=new boolean[1];
	protected boolean norecurse=false;
	protected boolean destroy=false;

	public String accountForYourself()
	{
		if(CMath.s_int(text())<=0)
			return "Only 1 may exist";
		return "Only "+CMath.s_int(text())+" may exist.";
	}

	protected void countIfNecessary(Item I)
	{
		if(CMLib.flags().isInTheGame(I,false))
		{
			int max=CMath.s_int(text());
			List<Item> myInstances=null;
			synchronized(instances)
			{
				if(!instances.containsKey(I.Name()))
				{
					myInstances=new Vector();
					instances.put(I.Name(),myInstances);
					myInstances.add(I);
				}
			}
			myInstances=instances.get(I.Name());
			if(myInstances!=null)
			{
				synchronized(myInstances)
				{
					if(!myInstances.contains(I))
					{
						if(max==0) max++;
						int num=0;
						for(int i=myInstances.size()-1;i>=0;i--)
							if(!((Item)myInstances.get(i)).amDestroyed())
								num++;
							else
								myInstances.remove(i);
						if(num>=max)
						{
							I.destroy();
							destroy=true;
						}
						else
							myInstances.add(I);
					}
				}
			}
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		&&(affected instanceof Item)
		&&((!(((Item)affected).owner() instanceof MOB))
		   ||((MOB)((Item)affected).owner()).playerStats()==null))
			countIfNecessary((Item)affected);
	}
	
	public void affectPhyStats(Physical E, PhyStats affectableStats)
	{
		super.affectPhyStats(E,affectableStats);
		
		if((!(E instanceof Item))||(((Item)E).owner()==null))
			return;
		
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return;
		
		if(norecurse) return;
		norecurse=true;
		
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_UNLOCATABLE);
		
		synchronized(playersLoaded)
		{
			if(!playersLoaded[0])
			{
				playersLoaded[0]=true;
				Log.sysOut("Prop_LimitedItems","Checking player inventories");
				List<String> V=CMLib.database().getUserList();
				for(String name : V)
				{
					MOB M=CMLib.players().getLoadPlayer(name);
					if((M!=null)&&(M.location()!=null)&&(M.location().isInhabitant(M)))
						Log.sysOut("Prop_LimitedItems",M.name()+" is in the Game!!!");
				}
				Log.sysOut("Prop_LimitedItems","Done checking player inventories");
			}
		}
		if((((Item)affected).owner() instanceof MOB)
		&&(((MOB)((Item)affected).owner()).playerStats()!=null))
			countIfNecessary((Item)affected);
		if(destroy) ((Item)affected).destroy();
		norecurse=false;
	}
}
