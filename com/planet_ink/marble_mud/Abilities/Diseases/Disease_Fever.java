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

public class Disease_Fever extends Disease
{
	public String ID() { return "Disease_Fever"; }
	public String name(){ return "Fever";}
	public String displayText(){ return "(Fever)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 1;}

	protected int DISEASE_TICKS(){return 15;}
	protected int DISEASE_DELAY(){return 3;}
	protected String DISEASE_DONE(){return "You head stops hurting.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a fever.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			MOB newvictim=mob.location().fetchRandomInhabitant();
			if(newvictim!=mob) mob.setVictim(newvictim);
		}
		else
		if(CMLib.flags().aliveAwakeMobile(mob,false)
		&&(CMLib.flags().canSee(mob))
		&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1: mob.tell("You think you just saw your mother swim by."); break;
			case 2: mob.tell("A pink elephant just attacked you!"); break;
			case 3: mob.tell("A horse just asked you a question."); break;
			case 4: mob.tell("Your hands look very green."); break;
			case 5: mob.tell("You think you just saw your father float by."); break;
			case 6: mob.tell("A large piece of bread swings at you and misses!"); break;
			case 7: mob.tell("Oh, the pretty colors!"); break;
			case 8: mob.tell("You think you just saw something, but aren't sure."); break;
			case 9: mob.tell("Hundreds of little rainbow bees buzz around your head."); break;
			case 10: mob.tell("Everything looks upside-down."); break;
			}
		}
		return super.tick(ticking,tickID);
	}

}
