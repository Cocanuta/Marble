package com.planet_ink.marble_mud.Items.ClanItems;
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
public class StdClanApron extends StdClanItem
{
	public String ID(){ return "StdClanApron";}

	public StdClanApron()
	{
		super();

		setName("a clan apron");
		basePhyStats.setWeight(1);
		setDisplayText("an apron belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_SPECIALAPRON);
		material=RawMaterial.RESOURCE_COTTON;
		setRawProperLocationBitmap(Wearable.WORN_WAIST|Wearable.WORN_ABOUT_BODY);
		setRawLogicalAnd(false);
		recoverPhyStats();
	}
	
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(owner() instanceof MOB)
		if(msg.amITarget(owner()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_BID:
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_LIST:
				if((clanID().length()>0)
				&&(msg.source()!=owner())
				&&(!msg.source().getClanID().equals(clanID())))
				{
					Clan C=CMLib.clans().getClan(clanID());
					int state=Clan.REL_NEUTRAL;
					if(C!=null) state=C.getClanRelations(msg.source().getClanID());
					if((state!=Clan.REL_NEUTRAL)
					&&(state!=Clan.REL_ALLY)
					&&(state!=Clan.REL_FRIENDLY))
					{
						msg.source().tell(((MOB)owner()),null,null,"<S-NAME> seem(s) to be ignoring you.");
						return false;
					}
				}
				break;
			}
		}
		return super.okMessage(affecting,msg);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(fetchEffect("Merchant")==null)
		{
			Ability A=CMClass.getAbility("Merchant");
			if(A!=null) addNonUninvokableEffect(A);
		}
		return true;
	}
}
