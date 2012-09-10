package com.planet_ink.marble_mud.Abilities.Spells;
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
public class Spell_IronGrip extends Spell
{
	public String ID() { return "Spell_IronGrip"; }
	public String name(){return "Iron Grip";}
	public String displayText(){return "(Iron Grip)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> weapon hand becomes flesh again.");

		super.unInvoke();

	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().toUpperCase().indexOf("DISARM")>=0))
			{
				mob.location().show(msg.source(),mob,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to disarm <T-NAME>, but the grip is too strong!");
				return false;
			}
			else
			if((msg.amISource(mob))
			&&(msg.targetMinor()==CMMsg.TYP_REMOVE)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item)
			&&(mob.isMine(msg.target()))
			&&(((Item)msg.target()).amWearingAt(Wearable.WORN_WIELD)))
			{
				mob.location().show(mob,null,msg.target(),CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to let go of <O-NAME>, but <S-HIS-HER> grip is too strong!");
				if((!mob.isInCombat())&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTODRAW)))
				{
					mob.tell("** Autodraw has been turned OFF. **");
					mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_AUTODRAW));
				}
				return false;
			}
			else
			if((msg.amISource(mob))
			&&((msg.targetMinor()==CMMsg.TYP_DROP)
				||(msg.targetMinor()==CMMsg.TYP_GET))
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item)
			&&(mob.isMine(msg.target()))
			&&(((Item)msg.target()).amWearingAt(Wearable.WORN_WIELD)))
			{
				mob.location().show(mob,null,msg.target(),CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to let go of <O-NAME>, but <S-HIS-HER> grip is too strong!");
				return false;
			}
			else
			if((msg.amISource(mob))
			&&(msg.sourceMinor()==CMMsg.TYP_THROW)
			&&(msg.tool() instanceof Item)
			&&(!((Item)msg.tool()).amWearingAt(Wearable.IN_INVENTORY))
			&&(mob.isMine(msg.tool())))
			{
				mob.location().show(mob,null,msg.tool(),CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to let go of <O-NAME>, but <S-HIS-HER> grip is too strong!");
				return false;
			}
		}
		return true;
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> watch(es) <T-HIS-HER> weapon hand turn to iron!":"^S<S-NAME> invoke(s) a spell on <T-NAMESELF> and <T-HIS-HER> weapon hand turns into iron!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s).");

		return success;
	}
}
