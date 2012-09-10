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

public class Disease_Tetnus extends Disease
{
	public String ID() { return "Disease_Tetnus"; }
	public String name(){ return "Tetanus";}
	public String displayText(){ return "(Tetanus)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*6;}
	protected int DISEASE_DELAY(){return CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);}
	protected String DISEASE_DONE(){return "Your tetnus clears up!";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) ill.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> <S-IS-ARE> getting slower...";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	public int difficultyLevel(){return 0;}
	protected int dexDown=1;

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
			dexDown++;
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(dexDown<0) return;
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-dexDown);
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
		{
			dexDown=-1;
			CMLib.combat().postDeath(invoker(),affected,null);
		}
	}
}
