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
public class Spell_SpiderClimb extends Spell
{
	public String ID() { return "Spell_SpiderClimb"; }
	public String name(){return "Spider Climb";}
	public String displayText(){return "(Spider Climb)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			if(CMLib.flags().isStanding((MOB)affected))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLIMBING);
		}
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		Room room=((MOB)affected).location();
		if((canBeUninvoked())&&(!mob.amDead()))
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> no longer <S-HAS-HAVE> a spidery gait.");
		super.unInvoke();
		if(canBeUninvoked())
			room.recoverRoomStats();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> spidery magic.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"^S<S-NAME> attains a climbers stance!":"^S<S-NAME> invoke(s) a spidery spell upon <S-HIM-HERSELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,10);
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),"<S-NAME> attempt(s) to invoke a spell, but fail(s).");

		return success;
	}
}
