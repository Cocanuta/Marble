package com.planet_ink.marble_mud.Abilities.Thief;
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
import com.planet_ink.marble_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Thief_DazzlingCaltrops extends Thief_Caltrops
{
	public String ID() { return "Thief_DazzlingCaltrops"; }
	public String name(){ return "Dazzling Caltrops";}
	private static final String[] triggerStrings = {"DAZZLINGCALTROPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public String caltropTypeName(){return "dazzling ";}

	public void spring(MOB mob)
	{
		if((!invoker().mayIFight(mob))
		||(invoker().getGroupMembers(new HashSet<MOB>()).contains(mob))
		||(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) looking at some "+caltropTypeName()+"caltrops on the floor.");
		else
		if(mob.curState().getMana()>6)
		{
			mob.curState().adjMana(-CMLib.dice().roll(3+getX1Level(mob),6,3),mob.maxState());
			mob.location().show(invoker(),mob,this,CMMsg.MSG_OK_ACTION,"The "+caltropTypeName()+"caltrops on the ground sparkle and confuse <T-NAME>");
		}
		// does not set sprung flag -- as this trap never goes out of use
	}
}
