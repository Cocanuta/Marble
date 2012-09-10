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

public class Poison_Slumberall extends Poison
{
	public String ID() { return "Poison_Slumberall"; }
	public String name(){ return "Slumberall";}
	private static final String[] triggerStrings = {"POISONSLEEP"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "You don't feel so drowsy anymore.";}
	protected String POISON_START(){return null;}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	protected boolean fallenYet=false;

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			CMLib.commands().postStand(mob,true);
		}
		super.unInvoke();
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amITarget(mob)&&(fallenYet)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.sourceMajor()>0))
		{
			mob.tell("You are way too drowsy.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if((!fallenYet)&&(mob.location()!=null))
		{
			fallenYet=true;
			mob.location().show(mob,null,CMMsg.MSG_SLEEP,"<S-NAME> fall(s) asleep!");
			mob.recoverPhyStats();
		}
		return true;
	}
}
