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

public class Disease_Syphilis extends Disease
{
	public String ID() { return "Disease_Syphilis"; }
	public String name(){ return "Syphilis";}
	public String displayText(){ return "(Syphilis)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY );}
	protected String DISEASE_DONE(){return "Your syphilis clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) some uncomfortable red sores on <S-HIS-HER> privates.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIS-HER> privates.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}
	public int difficultyLevel(){return 0;}
	protected int conDown=0;
	private boolean norecurse=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if(CMLib.dice().rollPercentage()>50)
				conDown++;
			if(CMLib.dice().rollPercentage()<10)
			{
				Ability A=null;
				if(CMLib.dice().rollPercentage()>50)
					A=CMClass.getAbility("Disease_Cold");
				else
					A=CMClass.getAbility("Disease_Fever");
				if(A!=null)A.invoke(mob,mob,true,0);
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<=0) return;
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-conDown);
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
		int down=2;
		if(conDown>down) down=conDown;
		affectableState.setMovement(affectableState.getMovement()/down);
		affectableState.setMana(affectableState.getMana()/down);
		affectableState.setHitPoints(affectableState.getHitPoints()/down);
	}
}
