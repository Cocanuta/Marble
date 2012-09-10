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

public class Poison_Firebreather extends Poison_Liquor
{
	public String ID() { return "Poison_Firebreather"; }
	public String name(){ return "Firebreather";}
	private static final String[] triggerStrings = {"LIQUORFIRE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int POISON_TICKS(){return 35;}

	protected int alchoholContribution(){return 3;} 
	protected int level(){return 3;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;
		Room room=mob.location();
		if((CMLib.dice().rollPercentage()<drunkness)&&(CMLib.flags().aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(CMLib.dice().rollPercentage()<40)
			{
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) fire!"+CMProps.msp("fireball.wav",20));
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,null);
					if((mob!=target)&&(mob.mayPhysicallyAttack(target))&&(room.okMessage(mob,msg)))
					{
						room.send(mob,msg);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.phyStats().level();
						if (maxDie > 10)
							maxDie = 10;
						damage += CMLib.dice().roll(maxDie,6,1);
						if(msg.value()>0)
							damage = (int)Math.round(CMath.div(damage,2.0));
						CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"^F^<FIGHT^>The fire <DAMAGE> <T-NAME>!^</FIGHT^>^?");
					}
				}
			}
			else
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) smoke!");
			disableHappiness=true;
		}
		return super.tick(ticking,tickID);
	}
}
