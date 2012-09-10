package com.planet_ink.marble_mud.Abilities.Spells;
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
public class Spell_Permanency extends Spell
{
	public String ID() { return "Spell_Permanency"; }
	public String name(){return "Permanency";}
	protected int canAffectCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overridemana(){return Ability.COST_ALL;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

		if((mob.baseState().getMana()<100)||(mob.maxState().getMana()<100))
		{
			mob.tell("You aren't powerful enough to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StdAbility theOne=null;
				for(int a=target.numEffects()-1;a>=0;a--) // personal effects
				{
					Ability A=target.fetchEffect(a);
					if((A.invoker()==mob)
					 &&(!A.isAutoInvoked())
					 &&(A.canBeUninvoked())
					 &&(A instanceof StdAbility)
					 &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
					{
						theOne=(StdAbility)A;
						break;
					}
				}
				if(theOne==null)
				{
					mob.tell("There does not appear to be any of your spells on "+target.name()+" which can be made permanent.");
					return false;
				}
				else
				if(((target instanceof Room)||(target instanceof Exit))
				&&(theOne.enchantQuality()==Ability.QUALITY_MALICIOUS)
				&&(!CMLib.law().doesOwnThisProperty(mob,mob.location())))
				{
					mob.tell("You can not make "+theOne.name()+" permanent here.");
					return false;
				}
				else
				{
					theOne.makeNonUninvokable();
					theOne.setSavable(true);
					mob.baseState().setMana(mob.baseState().getMana()-100);
					mob.maxState().setMana(mob.maxState().getMana()-100);
					target.text();
					if((target instanceof Room)
					&&(CMLib.law().doesOwnThisProperty(mob,(Room)target)))
						CMLib.database().DBUpdateRoom((Room)target);
					else
					if(target instanceof Exit)
					{
						Room R=mob.location();
						Room R2=null;
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
							if(R.getExitInDir(d)==target)
							{ R2=R.getRoomInDir(d); break;}
						if((CMLib.law().doesOwnThisProperty(mob,R))
						||((R2!=null)&&(CMLib.law().doesOwnThisProperty(mob,R2))))
							CMLib.database().DBUpdateExits(R);
					}
					mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"The quality of "+theOne.name()+" inside <T-NAME> glows!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but lose(s) patience.");


		// return whether it worked
		return success;
	}
}
