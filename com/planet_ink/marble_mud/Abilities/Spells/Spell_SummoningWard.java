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
public class Spell_SummoningWard extends Spell
{
	public String ID() { return "Spell_SummoningWard"; }
	public String name(){return "Summoning Ward";}
	public String displayText(){return "(Summoning Ward)";}
	protected int quality=Ability.QUALITY_INDIFFERENT;
	public int abstractQuality(){ return quality;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your summoning ward dissipates.");

		super.unInvoke();

	}


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(!msg.amISource(mob))
			&&(mob.location()!=msg.source().location())
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING))
			&&(!mob.amDead()))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				return false;
			}
		}
		else
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING)))
			{
				Ability A=(Ability)msg.tool();
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
				{
					if((msg.source().location()!=null)&&(msg.source().location()!=R))
						msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
					R.showHappens(CMMsg.MSG_OK_VISUAL,"Magical energy fizzles and is absorbed into the air!");
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=null;
		if(commands.size()>0)
		{
			String s=CMParms.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(CMLib.english().containsString(mob.location().ID(),s)
			||CMLib.english().containsString(mob.location().name(),s)
			||CMLib.english().containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		if(target==null)
			target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell("This place is already under a summoning ward.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> seem(s) magically protected.":"^S<S-NAME> invoke(s) a summoning ward upon <T-NAMESELF>.^?");
			if(target instanceof Room) quality=Ability.QUALITY_MALICIOUS;
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((target instanceof Room)
				&&((CMLib.law().doesOwnThisProperty(mob,((Room)target)))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
				{
					beneficialAffect(mob,target,asLevel,0);
					if(target instanceof Room)
					{
						Spell_SummoningWard A=(Spell_SummoningWard)target.fetchEffect(ID());
						if(A!=null) A.quality=Ability.QUALITY_MALICIOUS;
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a summoning ward, but fail(s).");
		quality=Ability.QUALITY_INDIFFERENT;

		return success;
	}
}
