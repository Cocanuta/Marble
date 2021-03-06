package com.planet_ink.marble_mud.Abilities.Diseases;
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

public class Disease_Malaria extends Disease
{
	public String ID() { return "Disease_Malaria"; }
	public String name(){ return "Malaria";}
	public String displayText(){ return "(Malaria)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 9*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your malaria clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with malaria.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> ache(s) and sneeze(s). AAAAAAAAAAAAAACHOOO!!!!";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_PROXIMITY|DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}
	public int difficultyLevel(){return 1;}
	private boolean norecurse=false;
	protected int conDown=0;
	protected int tickUp=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((!mob.amDead())&&((++tickUp)==CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)))
		{
			tickUp=0;
			conDown++;
			if((CMLib.dice().rollPercentage()<20)
			&&(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_DISEASE)))
			{
				unInvoke();
				return false;
			}
		}
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			int damage=CMLib.dice().roll(2,diseaser.phyStats().level()+1,1);
			CMLib.combat().postDamage(diseaser,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_DISEASE,-1,null);
			catchIt(mob);
			if(CMLib.dice().rollPercentage()==1)
			{
				Ability A=CMClass.getAbility("Disease_Fever");
				if(A!=null) A.invoke(diseaser,mob,true,0);
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-5);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-(5+conDown));
		if((affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)&&(!norecurse))
		{
			conDown=-1;
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=affected;
			norecurse=true;
			CMLib.combat().postDeath(diseaser,affected,null);
			norecurse=false;
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		affectableState.setMovement(affectableState.getMovement()/2);
		affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/3));
		affectableState.setHitPoints(affectableState.getHitPoints()-affected.phyStats().level());
	}
}
