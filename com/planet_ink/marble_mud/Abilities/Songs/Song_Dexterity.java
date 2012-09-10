package com.planet_ink.marble_mud.Abilities.Songs;
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
@SuppressWarnings("rawtypes")
public class Song_Dexterity extends Song
{
	public String ID() { return "Song_Dexterity"; }
	public String name(){ return "Dexterity";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int amount=0;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-amount);
		else
			affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+(amount+super.getXLEVELLevel(invoker())));
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(mob.getGroupMembers(new HashSet<MOB>()).size()==0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		amount=CMath.s_int(CMParms.combine(commands,0));

		if(amount<=0)
		{
			if(mob.isMonster())
				amount=mob.charStats().getStat(CharStats.STAT_DEXTERITY)/2;
			else
			{
				mob.tell("Sing about how much dexterity?");
				return false;
			}
		}

		if(amount>=mob.charStats().getStat(CharStats.STAT_DEXTERITY))
		{
			mob.tell("You can't sing away that much dexterity.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
