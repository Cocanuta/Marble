package com.planet_ink.marble_mud.Abilities.Skills;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Skill_Subdue extends StdSkill
{
	public String ID() { return "Skill_Subdue"; }
	public String name(){ return "Subdue";}
	public String displayText(){ return "(Subdueing "+whom+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"SUBDUE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_EVASIVE;}
	public int usageType(){return USAGE_MOVEMENT;}
	protected MOB whom=null;
	protected int whomDamage=0;
	protected int asLevel=0;

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.attackAdjustment() - 10 + super.getXLEVELLevel(invoker()));
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if(canBeUninvoked()&&
			(M.amDead()||(!CMLib.flags().isInTheGame(M, false))||(!M.amActive())||M.amDestroyed()||(M.getVictim()!=whom)))
			{
				unInvoke();
				return true;
			}
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(affected !=null)
			&&(msg.source()==affected)
			&&(msg.target()==whom))
			{
				whomDamage+=msg.value();
				msg.setValue(1);
			}
		}
		return true;
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source()==affected)
			&&(msg.target()==whom)
			&&(msg.targetMinor()==CMMsg.TYP_EXAMINE)
			&&(CMLib.flags().canBeSeenBy(whom, msg.source())))
			{
				double actualHitPct = CMath.div(whom.curState().getHitPoints()-whomDamage,whom.baseState().getHitPoints());
				msg.source().tell(msg.source(),whom,null,"<T-NAME> is "+CMath.toPct(actualHitPct)+" health away from being overcome.");
			}
				
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(affected !=null)
			&&(msg.source()==affected)
			&&(msg.target()==whom)
			&&(whom.curState().getHitPoints() - whomDamage)<=0)
			{
				Ability sap=CMClass.getAbility("Skill_ArrestingSap");
				if(sap!=null) sap.invoke(whom,new XVector(new Object[]{"SAFELY",Integer.toString(adjustedLevel(msg.source(),asLevel))}),whom,true,0);
				whom.makePeace();
				msg.source().makePeace();
				unInvoke();
			}
		}
	}
	
	public void unInvoke()
	{
		if((canBeUninvoked())&&(affected instanceof MOB))
			((MOB)affected).tell("You are no longer trying to subdue "+whom.name());
		super.unInvoke();
	}
	
	public int castingQuality(MOB mob, Physical target)
	{
		return Ability.QUALITY_INDIFFERENT;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Ability A=mob.fetchEffect(ID());
		if(A!=null)
			A.unInvoke();
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":"^F^<FIGHT^><S-NAME> attempt(s) to subdue <T-NAMESELF>!^</FIGHT^>^?");
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
				Skill_Subdue SK=(Skill_Subdue)mob.fetchEffect(ID());
				if(SK!=null)
				{
					SK.whom=target;
					SK.asLevel=asLevel;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to subdue <T-NAMESELF>, but fails.");
		return success;
	}
}
