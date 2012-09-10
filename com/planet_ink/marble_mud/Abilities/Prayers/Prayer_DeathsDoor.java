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
public class Prayer_DeathsDoor extends Prayer
{
	public String ID() { return "Prayer_DeathsDoor"; }
	public String name(){ return "Deaths Door";}
	public String displayText(){ return "(Deaths Door)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			final Room startRoom=mob.getStartRoom();
			if(msg.amISource(mob)
			&&(msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(startRoom!=null))
			{
				if(mob.fetchAbility("Dueling")!=null)
					return super.okMessage(host,msg);
				final Room oldRoom=mob.location();
				mob.resetToMaxState();
				oldRoom.show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> pulled back from death's door!");
				startRoom.bringMobHere(mob,false);
				unInvoke();
				for(int a=mob.numEffects()-1;a>=0;a--) // personal effects
				{
					Ability A=mob.fetchEffect(a);
					if(A!=null) A.unInvoke();
				}
				if((oldRoom!=startRoom) && oldRoom.isInhabitant(mob) && startRoom.isInhabitant(mob))
					oldRoom.delInhabitant(mob); // hopefully unnecessary
				return false;
			}
		}
		return super.okMessage(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your deaths door protection fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) guarded at deaths door!":"^S<S-NAME> "+prayWord(mob)+" for <T-NAME> to be guarded at deaths door!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but there is no answer.");


		// return whether it worked
		return success;
	}
}
