package com.planet_ink.marble_mud.Abilities.Prayers;
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
public class Prayer_MassParalyze extends Prayer
{
	public String ID() { return "Prayer_MassParalyze"; }
	public String name(){ return "Mass Paralyze";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_PARALYZING;}
	public String displayText(){ return "(Paralyzed)";}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The paralysis eases out of your muscles.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null) return false;

		boolean success=proficiencyCheck(mob,0,auto);
		boolean nothingDone=true;
		if(success)
		{
			for(Iterator e=h.iterator();e.hasNext();)
			{
				MOB target=(MOB)e.next();
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> "+prayForWord(mob)+" an unholy paralysis upon <T-NAMESELF>.^?");
				CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.TYP_PARALYZE|(auto?CMMsg.MASK_ALWAYS:0),null);
				if((target!=mob)&&(mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
				{
					int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
					if(levelDiff<0) levelDiff=0;
					if(levelDiff>6) levelDiff=6;
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((msg.value()<=0)&&(msg2.value()<=0))
					{
						success=maliciousAffect(mob,target,asLevel,8-levelDiff,-1);
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can't move!");
					}
					nothingDone=false;
				}
			}
		}

		if(nothingDone)
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to paralyze everyone, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
