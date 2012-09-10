package com.planet_ink.marble_mud.Abilities.Fighter;
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

public class Fighter_BlindFighting extends FighterSkill
{
	public String ID() { return "Fighter_BlindFighting"; }
	public String name(){ return "Blind Fighting";}
	public String displayText(){ return "";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	protected boolean seeEnabled = false;

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		if(seeEnabled)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_VICTIM);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		seeEnabled = false;
		if(!(ticking instanceof MOB)) return true;
		MOB mob=(MOB)ticking;
		if(!mob.isInCombat()) return true;
		if((!CMLib.flags().canBeSeenBy(mob.getVictim(),mob))
		&&(CMLib.flags().canBeHeardMovingBy(mob.getVictim(),mob))
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false)))
		{
			seeEnabled=true;
			if(CMLib.dice().rollPercentage()<10)
				helpProficiency(mob, 0);
		}
		return true;
	}
}
