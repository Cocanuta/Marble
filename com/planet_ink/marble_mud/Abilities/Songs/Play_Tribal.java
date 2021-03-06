package com.planet_ink.marble_mud.Abilities.Songs;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class Play_Tribal extends Play
{
	public String ID() { return "Play_Tribal"; }
	public String name(){ return "Tribal";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected String songOf(){return name()+" Music";}
	protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.isInCombat())&&(mob.isMonster()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		Room R=CMLib.map().roomLocation(affected);
		if(R!=null)
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB mob=R.fetchInhabitant(m);
			if(mob!=null)
			for(int i=0;i<mob.numEffects();i++) // personal
			{
				Ability A=mob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof StdAbility)
				&&(A.abstractQuality()!=Ability.QUALITY_MALICIOUS)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&(((StdAbility)A).getTickDownRemaining()==1))
					((StdAbility)A).setTickDownRemaining(2);
			}
		}
		return true;
	}
}
