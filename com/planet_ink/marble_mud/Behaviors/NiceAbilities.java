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
public class NiceAbilities extends ActiveTicker
{
	public String ID(){return "NiceAbilities";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	
	public NiceAbilities()
	{
		super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "random benevolent skill using";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom==null) return true;

			double aChance=CMath.div(mob.curState().getMana(),mob.maxState().getMana());
			if((Math.random()>aChance)||(mob.curState().getMana()<50))
				return true;

			MOB target=thisRoom.fetchRandomInhabitant();
			int x=0;
			while(((target==null)||(target.getVictim()==mob)||(target==mob)||(target.isMonster()))&&((++x)<10))
				target=thisRoom.fetchRandomInhabitant();

			int tries=0;
			Ability tryThisOne=null;
			while((tryThisOne==null)&&(tries<100)&&((mob.numAllAbilities())>0))
			{
				tryThisOne=mob.fetchRandomAbility();
				if((tryThisOne!=null)
				   &&(mob.fetchEffect(tryThisOne.ID())==null)
				   &&(tryThisOne.castingQuality(mob,target)==Ability.QUALITY_BENEFICIAL_OTHERS))
				{
					if((tryThisOne.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					{
						if(!tryThisOne.appropriateToMyFactions(mob))
							tryThisOne=null;
					}
				}
				else
					tryThisOne=null;
				tries++;
			}
			if(tryThisOne!=null)
				if((target!=null)&&(target!=mob)&&(!target.isMonster()))
				{
					tryThisOne.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,tryThisOne.ID()));
					Vector V=new Vector();
					V.addElement(target.name());
					tryThisOne.invoke(mob,V,target,false,0);
				}
		}
		return true;
	}
}
