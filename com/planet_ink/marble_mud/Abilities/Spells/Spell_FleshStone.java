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
public class Spell_FleshStone extends Spell
{
	public String ID() { return "Spell_FleshStone"; }
	public String name(){return "Flesh Stone";}
	public String displayText(){return "(Flesh to Stone)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public Item statue=null;
	protected boolean recurse=false;
	protected CharState prevState=null;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(statue!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			mob.makePeace();
			if((statue.owner()!=null)&&(statue.owner()!=mob.location()))
			{
				Room room=null;
				if(statue.owner() instanceof MOB)
					room=((MOB)statue.owner()).location();
				else
				if(statue.owner() instanceof Room)
					room=(Room)statue.owner();
				if((room!=null)&&(room!=mob.location()))
					room.bringMobHere(mob,false);
			}
		}
		return super.tick(ticking,tickID);
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
					mob.tell("Statues can't do that.");
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
		if(affected instanceof MOB)
		{
			//affectableStats.setReplacementName("a statue of "+affected.name());
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
		if((affected==null)||(!(affected instanceof MOB))||(recurse))
			return;
		recurse=true;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(statue!=null)
				statue.destroy();
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> flesh returns to normal.");
			if(prevState!=null) prevState.copyInto(mob.curState());
			CMLib.commands().postStand(mob,true);
		}
		recurse=false;
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!CMLib.flags().canBeHeardSpeakingBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		boolean success=proficiencyCheck(mob,-(levelDiff*5),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) at <T-NAMESELF>.^?");
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
					CMLib.commands().postStand(target,true);
					statue=CMClass.getItem("GenItem");
					String name=target.name();
					if(name.startsWith("A ")) name="a "+name.substring(2);
					if(name.startsWith("An ")) name="an "+name.substring(3);
					if(name.startsWith("The ")) name="the "+name.substring(4);
					statue.setName("a statue of "+name);
					statue.setDisplayText("a statue of "+name+" stands here.");
					statue.setDescription("It`s a hard granite statue, which looks exactly like "+name+".");
					statue.setMaterial(RawMaterial.RESOURCE_GRANITE);
					statue.basePhyStats().setWeight(2000);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> turn(s) into stone!!");
					success=maliciousAffect(mob,target,asLevel,(mob.phyStats().level()+(2*getXLEVELLevel(mob)))*10,-1);
					target.makePeace();
					if(mob.getVictim()==target) mob.setVictim(null);
					Ability A=target.fetchEffect(ID());
					if(success&&(A!=null))
					{
						mob.location().addItem(statue);
						statue.addEffect(A);
						A.setAffectedOne(target);
						statue.recoverPhyStats();
						((Spell_FleshStone)A).prevState=(CharState)target.curState().copyOf();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
