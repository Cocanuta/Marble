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
public class Spell_Nightmare extends Spell
{
	public String ID() { return "Spell_Nightmare"; }
	public String name(){return "Nightmare";}
	public String displayText(){return "(Having a nightmare)";}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}

	public int amountRemaining=0;
	boolean notAgainThisRound=false;

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep

		if(msg.amISource(mob))
		{
			if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&((msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			{
				if(!notAgainThisRound)
				{
					Room R=mob.location();
					Item I=null;
					MOB M=null;
					if(R!=null)
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
						I=mob.fetchWieldedItem();
						if(I!=null) mob.tell(mob,I,null,"<T-NAME> rips away your flesh.");
						break;
					case 2:
						I=mob.fetchWieldedItem();
						if(I!=null) mob.tell(mob,I,null,"<T-NAME> seems to wrap itself around you.");
						break;
					case 3:
						I=mob.fetchWieldedItem();
						if(I!=null) mob.tell(mob,I,null,"<T-NAME> seems to bend around your hands.");
						break;
					case 4:
						mob.tell("You see your flesh melting away in large chunks.");
						break;
					case 5:
						M=R.fetchRandomInhabitant();
						if(M!=null) mob.tell(mob,M,null,"<T-NAME> glare(s) at you, taking on a horrifying form.");
						break;
					case 6:
						M=R.fetchRandomInhabitant();
						if(M!=null) mob.tell(mob,M,null,"<T-NAME> rip(s) open <T-HIS-HER> jaws and stuff(s) you in it.");
						break;
					case 7:
						M=R.fetchRandomInhabitant();
						if(M!=null) mob.tell(mob,M,null,"<T-NAME> rip(s) up <T-HIS-HER> flesh in front of you.");
						break;
					case 8:
						M=R.fetchRandomInhabitant();
						if(M!=null) mob.tell(mob,M,null,"<T-NAME> become(s) a horrifying image of terror.");
						break;
					case 9:
						mob.tell(mob,null,null,"The nightmare consumes your mind, taking you into madness.");
						break;
					case 10:
						M=R.fetchRandomInhabitant();
						if(M!=null) mob.tell(mob,M,null,"<T-NAME> <T-IS-ARE> trying to take control of your mind.");
						break;
					}
					notAgainThisRound=true;
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> struggle(s) with an imaginary foe."); break;
					case 2:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> scream(s) in horror!"); break;
					case 3:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> beg(s) for mercy."); break;
					case 4:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> grab(s) <S-HIS-HER> head and cr(ys)."); break;
					case 5:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> whimper(s)."); break;
					case 6:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> look(s) terrified!"); break;
					case 7:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> swipe(s) at <S-HIS-HER> feet and arms."); break;
					case 8:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> claw(s) at the air."); break;
					case 9:	mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> shiver(s) in fear."); break;
					case 10:mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,
						"<S-NAME> shake(s) in anticipation of horror!"); break;
					}
					amountRemaining-=(int)Math.round(CMath.mul(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE),2.5));
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		if((!mob.amDead())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to wake up from <S-HIS-HER> nightmare.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			notAgainThisRound=false;
		return super.tick(ticking,tickID);
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


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF>.^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))||(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					amountRemaining=100;
					maliciousAffect(mob,target,asLevel,10,-1);
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> go(es) into the throes of a horrendous nightmare!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
