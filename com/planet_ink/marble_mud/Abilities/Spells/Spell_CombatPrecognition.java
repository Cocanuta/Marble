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
public class Spell_CombatPrecognition extends Spell
{
	public String ID() { return "Spell_CombatPrecognition"; }
	public String name(){return "Combat Precognition";}
	public String displayText(){return "(Combat Precognition)";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int overrideMana(){return 100;}
	boolean lastTime=false;
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(mob.location()!=null)
		   &&(CMLib.flags().aliveAwakeMobile(mob,true)))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				CMMsg msg2=CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> avoid(s) the attack by <T-NAME>!");
				if((proficiencyCheck(null,mob.charStats().getStat(CharStats.STAT_DEXTERITY)-60,false))
				&&(!lastTime)
				&&(msg.source().getVictim()==mob)
				&&(msg.source().rangeToTarget()==0)
				&&(mob.location().okMessage(mob,msg2)))
				{
					lastTime=true;
					mob.location().send(mob,msg2);
					helpProficiency(mob, 0);
					return false;
				}
				lastTime=false;
			}
			else
			if((msg.value()<=0)
			   &&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			   &&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,mob.charStats().getStat(CharStats.STAT_DEXTERITY)-50,false)))
			{
				String tool=null;
				if((msg.tool()!=null)&&(msg.tool() instanceof Ability))
					tool=((Ability)msg.tool()).name();
				CMMsg msg2=null;
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_JUSTICE:
					if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
					&&(tool!=null))
						msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"physical":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_GAS:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"noxious fumes":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_COLD:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"cold blast":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_ELECTRIC:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"electrical attack":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_FIRE:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"blast of heat":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_WATER:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"weat blast":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_ACID:
					msg2=CMClass.getMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"acid attack":tool)+" from <T-NAME>.");
					break;
				}
				if((msg2!=null)&&(mob.location()!=null)&&(mob.location().okMessage(mob,msg2)))
				{
					mob.location().send(mob,msg2);
					return false;
				}
			}
		}
		return true;
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("Your combat precognition fades away.");
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> the sight.");
			return false;
		}

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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"<T-NAME> shout(s) combatively!":"^S<S-NAME> shout(s) a combative spell!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> shout(s) combatively, but nothing more happens.");
		// return whether it worked
		return success;
	}
}
