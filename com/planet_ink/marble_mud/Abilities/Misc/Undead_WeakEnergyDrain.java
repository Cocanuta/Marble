package com.planet_ink.marble_mud.Abilities.Misc;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class Undead_WeakEnergyDrain extends StdAbility
{
	public String ID() { return "Undead_WeakEnergyDrain"; }
	public String name(){ return "Weak Energy Drain";}
	public String displayText(){ return "(Drained of Energy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"DRAINWEAKENERGY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public int levelsDown=1;

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(levelsDown<0) return;
		int attacklevel=affectableStats.attackAdjustment()/affectableStats.level();
		affectableStats.setLevel(affectableStats.level()-levelsDown);
		if(affectableStats.level()<=0)
		{
			levelsDown=-1;
			CMLib.combat().postDeath(invoker(),(MOB)affected,null);
		}
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(attacklevel*levelsDown));
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null) return;
		int hplevel=affectableState.getHitPoints()/affected.basePhyStats().level();
		affectableState.setHitPoints(affectableState.getHitPoints()-(hplevel*levelsDown));
		int manalevel=affectableState.getMana()/affected.basePhyStats().level();
		affectableState.setMana(affectableState.getMana()-(manalevel*levelsDown));
		int movelevel=affectableState.getMovement()/affected.basePhyStats().level();
		affectableState.setMovement(affectableState.getMovement()-(movelevel*levelsDown));
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setClassLevel(affectableStats.getCurrentClass(),affected.basePhyStats().level()-levelsDown-affectableStats.combinedSubLevels());
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if((canBeUninvoked())
		&&(ID().equals("Undead_WeakEnergyDrain")))
			mob.tell("The energy drain is lifted.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=null;
		Ability reAffect=null;
		if(mob.isInCombat())
		{
			if(mob.rangeToTarget()>0)
			{
				mob.tell("You are too far away to touch!");
				return false;
			}
			MOB victim=mob.getVictim();
				reAffect=victim.fetchEffect("Undead_WeakEnergyDrain");
			if(reAffect==null)
				reAffect=victim.fetchEffect("Undead_EnergyDrain");
			if(reAffect!=null) target=victim;
		}
		if(target==null)
			target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=auto?"":"^S<S-NAME> extend(s) an energy draining hand to <T-NAMESELF>!^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_ALWAYS:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> drained!");
					if(reAffect!=null)
					{
						if(reAffect instanceof Undead_WeakEnergyDrain)
							((Undead_WeakEnergyDrain)reAffect).levelsDown++;
						((StdAbility)reAffect).setTickDownRemaining(((StdAbility)reAffect).getTickDownRemaining()+5);
						mob.recoverPhyStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
					}
					else
						success=maliciousAffect(mob,target,asLevel,10,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to drain <T-NAMESELF>, but fail(s).");

		return success;
	}
}
