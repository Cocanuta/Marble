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

public class Disease_Magepox extends Disease
{
	public String ID() { return "Disease_Magepox"; }
	public String name(){ return "Magepox";}
	public String displayText(){ return "(Magepox)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY );}
	protected int DISEASE_DELAY(){return 15;}
	protected String DISEASE_DONE(){return "Your magepox clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with the Magepox.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> watch(es) new mystical sores appear on <S-HIS-HER> body.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}
	public int difficultyLevel(){return 9;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,DISEASE_AFFECT());
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		int hitsLost=affected.maxState().getHitPoints()-affected.curState().getHitPoints();
		if(hitsLost<0) hitsLost=0;
		int movesLost=(affected.maxState().getMovement()-affected.curState().getMovement());
		if(movesLost<0) movesLost=0;
		int lostMana=hitsLost+movesLost;
		affectableState.setMana(affectableState.getMana()-lostMana);
		if(affectableState.getMana()<0)
			affectableState.setMana(0);
		if(affected.curState().getMana()>affectableState.getMana())
			affected.curState().setMana(affectableState.getMana());

	}
}
