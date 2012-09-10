package com.planet_ink.marble_mud.Abilities.Druid;
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
public class Chant_Treemorph extends Chant
{
	public String ID() { return "Chant_Treemorph"; }
	public String name(){ return "Treemorph";}
	public String displayText(){return "(Treemorph)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	Item tree=null;
	Race treeForm=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(tree!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((tree.owner()!=null)&&(tree.owner()!=mob.location()))
			{
				Room room=null;
				if(tree.owner() instanceof MOB)
					room=((MOB)tree.owner()).location();
				else
				if(tree.owner() instanceof Room)
					room=(Room)tree.owner();
				if((room!=null)&&(room!=mob.location()))
					room.bringMobHere(mob,false);
			}
		}
		return super.tick(ticking,tickID);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(treeForm!=null)
		{
			int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(treeForm);
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,treeForm.getAgingChart()[oldCat]);
		}
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.source().getVictim()==mob)
				msg.source().setVictim(null);
			if(mob.isInCombat())
			{
				MOB victim=mob.getVictim();
				if(victim!=null) victim.makePeace();
				mob.makePeace();
			}
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.curState().setHunger(1000);
			mob.curState().setThirst(1000);
			mob.recoverCharStats();
			mob.recoverPhyStats();

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if(msg.amISource(mob))
			{
				if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&(msg.sourceMajor()>0))
				{
					mob.tell("Trees can't do that.");
					return false;
				}
			}
		}
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.source().getVictim()==affected)
				msg.source().setVictim(null);
			if(mob.isInCombat())
			{
				MOB victim=mob.getVictim();
				if(victim!=null) victim.makePeace();
				mob.makePeace();
			}
		}
		return true;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		if((treeForm!=null)&&(affected instanceof MOB))
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName("a "+treeForm.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+treeForm.name());
			int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			treeForm.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);

			//affectableStats.setReplacementName("a tree of "+affected.name());
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SMELL);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
		}
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
			if(tree!=null) tree.destroy();
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer a tree.");
			mob.curState().setHitPoints(1);
			mob.curState().setMana(0);
			mob.curState().setMovement(0);
			mob.curState().setHunger(0);
			mob.curState().setThirst(0);
			CMLib.commands().postStand(mob,true);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		boolean success=proficiencyCheck(mob,-(levelDiff*10),auto);
		treeForm=CMClass.getRace("TreeGolem");
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int a=0;
					while(a<target.numEffects()) // personal effects
					{
						Ability A=target.fetchEffect(a);
						int s=target.numEffects();
						if(A!=null) A.unInvoke();
						if(target.numEffects()==s)
							a++;
					}
					target.makePeace();
					CMLib.commands().postStand(target,true);
					tree=CMClass.getItem("GenItem");
					tree.setName("a oak tree");
					tree.setDisplayText("an oak tree that reminds you of "+target.name()+" is growing here.");
					tree.setDescription("It`s a tall oak tree, which seems to remind you of "+target.name()+".");
					tree.setMaterial(RawMaterial.RESOURCE_OAK);
					tree.basePhyStats().setWeight(5000);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> turn(s) into a tree!!");
					success=maliciousAffect(mob,target,asLevel,(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)))*50,-1);
					Ability A=target.fetchEffect(ID());
					if(success&&(A!=null))
					{
						mob.location().addItem(tree);
						tree.addEffect(A);
						A.setAffectedOne(target);
						tree.recoverPhyStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");

		// return whether it worked
		return success;
	}
}
