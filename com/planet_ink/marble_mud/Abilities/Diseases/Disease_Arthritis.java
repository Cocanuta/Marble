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

public class Disease_Arthritis extends Disease
{
	public String ID() { return "Disease_Arthritis"; }
	public String name(){ return "Arthritis";}
	public String displayText(){ return "(Arthritis)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 4;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your arthritis clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-IS-ARE> in pain.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-3);
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
	}

}
