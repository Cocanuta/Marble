package com.planet_ink.marble_mud.Abilities.SuperPowers;
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
public class Power_OctoGrapple extends SuperPower
{
	public String ID() { return "Power_OctoGrapple"; }
	public String name(){ return "Octo-Grapple";}
	public String displayText()
	{
		if(affected==invoker)
			return "(Grappling)";
		return "(Grappled)";
	}
	private static final String[] triggerStrings = {"GRAPPLE"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SKILL;}
	public long flags(){return Ability.FLAG_BINDING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(mob!=invoker))
		{
			if((msg.sourceMajor(CMMsg.MASK_EYES))
			||(msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
				{
					if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the grappling arms."))
					{
						if(CMLib.dice().rollPercentage()<mob.charStats().getStat(CharStats.STAT_STRENGTH))
						{
							unInvoke();
							if((mob.fetchEffect(ID())==null)&&(invoker!=null)&&(invoker!=mob))
							{
								Ability A=mob.fetchEffect(ID());
								if(A!=null) A.unInvoke();
							}
						}
					}
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.sensesMask()|PhyStats.IS_BOUND);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==invoker)
			affectableStats.alterBodypart(Race.BODY_ARM,-2);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((!mob.amDead())&&(CMLib.flags().isInTheGame(mob,false)))
			{
				if(mob==invoker)
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> release(s) <S-HIS-HER> grapple.");
					else
						mob.tell("You release your grapple.");
				}
				else
				{
					if(mob.location()!=null)
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> <S-IS-ARE> released from the grapple");
					else
						mob.tell("You are released from the grapple.");
				}
				CMLib.commands().postStand(mob,true);
			}
		}
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!auto)&&(mob.baseWeight()<(target.baseWeight()-200)))
		{
			mob.tell(target.name()+" is too big to grapple!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;
		// now see if it worked
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-(((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH))*5))),auto)&&(hit);
		success=success&&(target.charStats().getBodyPart(Race.BODY_ARM)>2);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> get(s) grappled!":"^F^<FIGHT^><S-NAME> grab(s) <T-NAMESELF> with <S-HIS-HER> huge metallic arms!^</FIGHT^>^?");
			CMLib.color().fixSourceFightColor(msg);
			if((mob.location().okMessage(mob,msg))&&(msg.value()<=0))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,asLevel,10,-1);
				success=maliciousAffect(mob,mob,asLevel,10,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to grab <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
