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
public class Prayer_Cannibalism extends Prayer
{
	public String ID() { return "Prayer_Cannibalism"; }
	public String name(){ return "Inflict Cannibalism";}
	public String displayText(){ return "(Cannibalism)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(CMLib.flags().canSee(mob)))
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell("Your cannibalistic hunger fades.");
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof Food)
		&&(msg.target() instanceof Item))
		{
			if(((((Item)msg.target()).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
			&&(CMLib.english().containsString(msg.target().Name(),msg.source().charStats().getMyRace().name())))
			{
				msg.source().curState().adjHunger(+((Food)msg.target()).nourishment(),msg.source().maxState().maxHunger(msg.source().baseWeight()));
				msg.source().curState().adjThirst(+((Food)msg.target()).nourishment()*2,msg.source().maxState().maxThirst(msg.source().baseWeight()));
			}
			else
				msg.source().curState().adjHunger(-((Food)msg.target()).nourishment(),msg.source().maxState().maxHunger(msg.source().baseWeight()));
		}
		else
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(msg.target() instanceof Drink))
			msg.source().curState().adjThirst(-((Drink)msg.target()).thirstQuenched(),msg.source().maxState().maxThirst(msg.source().baseWeight()));
	}

	public boolean raceWithBlood(Race R)
	{
		List<RawMaterial> V=R.myResources();
		if(V!=null)
		{
			for(int i2=0;i2<V.size();i2++)
			{
				Item I2=(Item)V.get(i2);
				if((I2.material()==RawMaterial.RESOURCE_BLOOD)
				&&(I2 instanceof Drink))
					return true;
			}
		}
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return true;
		MOB M=(MOB)affected;
		if((M.location()!=null)&&(!CMLib.flags().isSleeping(M)))
		{
			M.curState().adjThirst(-(M.location().thirstPerRound(M)*2),M.maxState().maxThirst(M.baseWeight()));
			M.curState().adjHunger(-2,M.maxState().maxHunger(M.baseWeight()));
			if((M.isMonster())
			&&((M.curState().getThirst()<=0)||(M.curState().getHunger()<=0))
			&&(M.fetchEffect("Butchering")==null)
			&&(CMLib.flags().aliveAwakeMobileUnbound(M,true)))
			{
				DeadBody B=null;
				Food F=null;
				for(int i=0;i<M.location().numItems();i++)
				{
					Item I=M.location().getItem(i);
					if((I!=null)
					&&(I instanceof DeadBody)
					&&(I.container()==null)
					&&(((DeadBody)I).charStats()!=null)
					&&(((DeadBody)I).charStats().getMyRace()==M.charStats().getMyRace()))
						B=(DeadBody)I;
					else
					if((I!=null)
					&&(I instanceof Food)
					&&(I.container()==null)
					&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
					&&(CMLib.english().containsString(I.Name(),M.charStats().getMyRace().name())))
						F=(Food)I;
				}
				if(F!=null)
				{
					CMLib.commands().postGet(M,null,F,false);
					if(M.isMine(F))
					{
						M.doCommand(CMParms.parse("EAT "+F.Name()),Command.METAFLAG_FORCED);
						if(M.isMine(F))
							((Item)F).destroy();
					}
					else
						((Item)F).destroy();
				}
				else
				if(B!=null)
				{
					Ability A=CMClass.getAbility("Butchering");
					if(A!=null) A.invoke(M,CMParms.parse(B.Name()),B,true,0);
				}
				else
				if(CMLib.dice().rollPercentage()<10)
				{
					MOB M2=M.location().fetchRandomInhabitant();
					if((M2!=null)&&(M2!=M)&&(M.charStats().getMyRace()==M2.charStats().getMyRace()))
						M.setVictim(M2);
				}
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;



		boolean success=proficiencyCheck(mob,-((target.charStats().getStat(CharStats.STAT_WISDOM)*2)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> invoke(s) a cannibalistic hunger upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> inflicted with cannibalistic urges!");
					target.curState().setHunger(0);
					target.curState().setThirst(0);
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inflict cannibalistic urges upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
