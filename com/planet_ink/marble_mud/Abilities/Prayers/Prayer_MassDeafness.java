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
public class Prayer_MassDeafness extends Prayer
{
	public String ID() { return "Prayer_MassDeafness"; }
	public String name(){ return "Mass Deafness";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Deafness)";}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!CMLib.flags().canHear((MOB)target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(CMLib.flags().canHear(mob)))
			mob.tell("Your hearing returns.");
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
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> "+prayForWord(mob)+" an unholy deafness upon <T-NAMESELF>.^?");
				if((target!=mob)&&(mob.location().okMessage(mob,msg)))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						success=maliciousAffect(mob,target,asLevel,0,-1);
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> go(es) deaf!!");
					}
					nothingDone=false;
				}
			}
		}

		if(nothingDone)
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to deafen everyone, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
