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
public class Prayer_Behemoth extends Prayer
{
	public String ID() { return "Prayer_Behemoth"; }
	public String name(){ return "Behemoth";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){return "(Behemoth)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your behemoth size has subsided.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STAT_STRENGTH,affectedStats.getStat(CharStats.STAT_STRENGTH)+5);
		affectedStats.setStat(CharStats.STAT_DEXTERITY,affectedStats.getStat(CharStats.STAT_DEXTERITY)-5);
		affectedStats.setStat(CharStats.STAT_WEIGHTADJ,
				affectedStats.getStat(CharStats.STAT_WEIGHTADJ)
				+(affectedMOB.basePhyStats().weight()*4));
	}

	public void affectPhyStats(Physical affected, PhyStats affectedStats)
	{
		super.affectPhyStats(affected,affectedStats);
		affectedStats.setHeight(affectedStats.height()*3);
		affectedStats.setName("A BEHEMOTH "+affected.name().toUpperCase());
		if(!(affected instanceof MOB))
			affectedStats.setWeight(affectedStats.weight()*4);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already BEHEMOTH in size.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) to BEHEMOTH size!");
				beneficialAffect(mob,target,asLevel,0);
				if(target instanceof MOB)
					CMLib.utensils().confirmWearability((MOB)target);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
