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
public class Mageness extends CombatAbilities
{
	public String ID(){return "Mageness";}

	public String accountForYourself()
	{ 
		return "magliness";
	}

	protected void getSomeMoreMageAbilities(MOB mob)
	{
		for(int a=0;a<((mob.basePhyStats().level())+5);a++)
		{
			Ability addThis=null;
			int tries=0;
			while((addThis==null)&&((++tries)<10))
			{
				addThis=CMClass.randomAbility();
				if((CMLib.ableMapper().qualifyingLevel(mob,addThis)<0)
				||(!CMLib.ableMapper().qualifiesByLevel(mob,addThis))
				||(((addThis.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)&&(!addThis.appropriateToMyFactions(mob)))
				||(mob.fetchAbility(addThis.ID())!=null)
				||((addThis.abstractQuality()!=Ability.QUALITY_MALICIOUS)
				   &&(addThis.abstractQuality()!=Ability.QUALITY_BENEFICIAL_SELF)
				   &&(addThis.abstractQuality()!=Ability.QUALITY_BENEFICIAL_OTHERS)))
					addThis=null;
			}
			if(addThis!=null)
			{
				addThis=(Ability)addThis.newInstance();
				addThis.setSavable(false);
				addThis.setProficiency(CMLib.ableMapper().getMaxProficiency(addThis.ID())/2);
				mob.addAbility(addThis);
				addThis.autoInvocation(mob);
			}
		}
	}
	
	public void startBehavior(PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		combatMode=COMBAT_RANDOM;
		makeClass(mob,getParmsMinusCombatMode(),"Mage");
		newCharacter(mob);
		getSomeMoreMageAbilities(mob);
		//%%%%%att,armor,damage,hp,mana,move
		if((preCastSet==Integer.MAX_VALUE)||(preCastSet<=0))
			setCombatStats(mob,-50,-50,-25,-45,50,-50);
	}
}
