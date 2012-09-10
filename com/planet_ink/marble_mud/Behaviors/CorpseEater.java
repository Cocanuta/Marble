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
public class CorpseEater extends ActiveTicker
{
	public String ID(){return "CorpseEater";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private boolean EatItems=false;
	public CorpseEater()
	{
		super();
		minTicks=5; maxTicks=20; chance=75;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "corpse eating";
	}
	
	public void setParms(String newParms) 
	{
		super.setParms(newParms);
		EatItems=(newParms.toUpperCase().indexOf("EATITEMS") > 0);
	}


	public static MOB makeMOBfromCorpse(DeadBody corpse, String type)
	{
		if((type==null)||(type.length()==0))
			type="StdMOB";
		MOB mob=CMClass.getMOB(type);
		if(corpse!=null)
		{
			mob.setName(corpse.name());
			mob.setDisplayText(corpse.displayText());
			mob.setDescription(corpse.description());
			mob.setBaseCharStats((CharStats)corpse.charStats().copyOf());
			mob.setBasePhyStats((PhyStats)corpse.basePhyStats().copyOf());
			mob.recoverCharStats();
			mob.recoverPhyStats();
			int level=mob.basePhyStats().level();
			mob.baseState().setHitPoints(CMLib.dice().rollHP(level,mob.basePhyStats().ability()));
			mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
			mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		return mob;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;
			for(int i=0;i<thisRoom.numItems();i++)
			{
				Item I=thisRoom.getItem(i);
				if((I!=null)
				&&(I instanceof DeadBody)
				&&(CMLib.flags().canBeSeenBy(I,mob)||CMLib.flags().canSmell(mob)))
				{
					if(getParms().length()>0)
					{
						if(((DeadBody)I).playerCorpse())
						{
							if(getParms().toUpperCase().indexOf("+PLAYER")<0)
								continue;
						}
						else
						if((getParms().toUpperCase().indexOf("-NPC")>=0)
						||(getParms().toUpperCase().indexOf("-MOB")>=0))
							continue;
						MOB mob2=makeMOBfromCorpse((DeadBody)I,null);
						if(!CMLib.masking().maskCheck(getParms(),mob2,false))
						{
							mob2.destroy();
							continue;
						}
						mob2.destroy();
					}
					else
					if(((DeadBody)I).playerCorpse())
						continue;
						
					if((I instanceof Container)&&(!EatItems))
						((Container)I).emptyPlease();
					thisRoom.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> eat(s) <O-NAME>.");
					I.destroy();
					return true;
				}
			}
		}
		return true;
	}
}
