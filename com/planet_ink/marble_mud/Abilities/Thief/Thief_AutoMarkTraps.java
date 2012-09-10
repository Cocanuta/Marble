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
@SuppressWarnings("rawtypes")
public class Thief_AutoMarkTraps extends Thief_AutoDetectTraps
{
	public String ID() { return "Thief_AutoMarkTraps"; }
	public String displayText() {return "(Automarking traps)";}
	public String name(){ return "AutoMark Traps";}
	private static final String[] triggerStrings = {"AUTOMARKTRAPS"};
	public String[] triggerStrings(){return triggerStrings;}
	protected String skillName(){return "mark";}

	public void dropem(MOB mob, Physical P)
	{
		Ability A=mob.fetchAbility("Thief_DetectTraps");
		if(A==null)
		{
			A=CMClass.getAbility("Thief_DetectTraps");
			A.setProficiency(100);
		}
		CharState savedState=(CharState)mob.curState().copyOf();
		if(A.invoke(mob,P,false,0))
		{
			A=mob.fetchAbility("Thief_MarkTraps");
			if(A==null)
			{
				A=CMClass.getAbility("Thief_MarkTraps");
				A.setProficiency(100);
			}
			A.invoke(mob,P,false,0);
		}
		mob.curState().setMana(savedState.getMana());
		mob.curState().setHitPoints(savedState.getHitPoints());
		mob.curState().setMana(savedState.getMovement());
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		if((!auto)&&(target.fetchAbility("Thief_MarkTraps")==null))
		{
			target.tell("You don't know how to mark traps yet!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
