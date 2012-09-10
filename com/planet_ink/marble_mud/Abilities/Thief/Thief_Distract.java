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
public class Thief_Distract extends ThiefSkill
{
	public String ID() { return "Thief_Distract"; }
	public String name(){ return "Distract";}
	public String displayText(){ return "(Distracted)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"DISTRACT"};
	public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE; }
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		float f=(float)0.05*(float)super.getXLEVELLevel(invoker());
		affectableStats.setArmor(affectableStats.armor()+(int)Math.round(CMath.div(affectableStats.armor(),2.0-f))+abilityCode());
		affectableStats.setAttackAdjustment((affectableStats.attackAdjustment()-(int)Math.round(CMath.div(affectableStats.attackAdjustment(),2.0-f)))-abilityCode());
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		MOB mob=(MOB)affected;
		if(invoker.location()!=mob.location())
			unInvoke();
		else
		{
			// preventing distracting player from doin anything else
			if(msg.amISource(invoker)
			&&(CMLib.dice().rollPercentage()>(mob.charStats().getStat(CharStats.STAT_WISDOM)*2))
			&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK))
			{
				invoker.location().show(invoker,mob,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> distract(s) <T-NAME>.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if((invoker!=null)&&(invoker.location()==mob.location())&&(!invoker.amDead()))
					invoker.tell(invoker,mob,null,"You are no longer distracting <T-NAMESELF>.");
				if((mob.location()!=null)&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer so distracted.");
			}
		}
		super.unInvoke();
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell("You are on the floor!");
			return false;
		}

		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to distract "+mob.getVictim().name()+"!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,auto?"<T-NAME> seem(s) distracted!":"<S-NAME> distract(s) <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,4);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to distract <T-NAMESELF>, but flub(s) it.");
		return success;
	}
}
