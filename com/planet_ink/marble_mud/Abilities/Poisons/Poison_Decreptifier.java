package com.planet_ink.marble_mud.Abilities.Poisons;
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

public class Poison_Decreptifier extends Poison
{
	public String ID() { return "Poison_Decreptifier"; }
	public String name(){ return "Decreptifier";}
	private static final String[] triggerStrings = {"POISONDECREPT"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 25;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 5;}
	protected String POISON_DONE(){return "The poison runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> seem(s) weakened!^?";}
	protected String POISON_AFFECT(){return "^G<S-NAME> shiver(s) weakly.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?CMLib.dice().roll(1,3,1):0;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-10);
		if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-10);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-10);
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
	}
}
