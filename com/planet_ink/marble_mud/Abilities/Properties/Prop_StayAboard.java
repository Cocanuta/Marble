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
public class Prop_StayAboard extends Property
{
	public String ID() { return "Prop_StayAboard"; }
	public String name(){ return "Stays on mounted thing";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	protected Rideable rideable=null;
	public String accountForYourself() { return "Stays on anything mounted to.";}
	protected boolean noRepeat=false;
	
	public void setAffectedOne(Physical P)
	{
		super.setAffectedOne(P);
		if(P instanceof Rider) {
			rideable = ((Rider)P).riding();
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		synchronized(this)
		{
			if(noRepeat) return true;
			try {
				noRepeat=true;
				if((tickID==Tickable.TICKID_MOB)
				&&(ticking instanceof Rider)
				&&(ticking instanceof MOB)
				&&(rideable!=null))
					stayAboard((Rider)ticking);
			} finally {
				noRepeat=false;
			}
		}
		return true;
	}
	
	public void stayAboard(Rider R)
	{
		Room rideR=CMLib.map().roomLocation(rideable);
		if((rideR!=null)
		&&((CMLib.map().roomLocation(R)!=rideR)
			||(R.riding()!=rideable)))
		{
			if(R.riding()!=null)
				R.setRiding(null);
			if(CMLib.map().roomLocation(R)!=rideR)
				if(R instanceof Item)
					rideR.moveItemTo((Item)R,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				else
				if(R instanceof MOB)
					rideR.bringMobHere((MOB)R,true);
			R.setRiding(rideable);
		}
	}
	
	public void affectPhyStats(Physical E, PhyStats affectableStats)
	{
		super.affectPhyStats(E, affectableStats);
		synchronized(this)
		{
			if(noRepeat) return;
			try {
				noRepeat=true;
				if(E instanceof Rider)
					if(rideable==null)
						rideable=((Rider)E).riding();
					else
					if(!CMLib.flags().isInTheGame(rideable,true))
						rideable=null;
					else
					if(E instanceof Item)
						stayAboard((Rider)E);
			} finally {
				noRepeat=false;
			}
		}
	}
}
