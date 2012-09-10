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
public class Prayer_Nullification extends Prayer
{
	public String ID() { return "Prayer_Nullification"; }
	public String name(){ return "Nullification";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
					&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
					   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
					{
						if((A.invoker()!=null)&&((A.invoker().phyStats().level()<=(mob.phyStats().level()+(2*super.getXLEVELLevel(mob))))))
							if((mob==target)&&(A.invoker()!=mob)&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS))
								return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
							else
							if((mob.getVictim()==target)&&(A.invoker()!=mob)&&(A.abstractQuality()!=Ability.QUALITY_MALICIOUS))
								return super.castingQuality(mob, target,Ability.QUALITY_MALICIOUS);
					}
				}
			}
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB target=mob.location().fetchInhabitant(i);
			if((target!=null)&&(success))
			{
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) nullified.":"^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>.^?");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					Ability revokeThis=null;
					boolean foundSomethingAtLeast=false;
					for(int a=0;a<target.numEffects();a++) // personal affects
					{
						Ability A=target.fetchEffect(a);
						if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())
						&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
						   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
						{
							foundSomethingAtLeast=true;
							if((A.invoker()!=null)&&((A.invoker().phyStats().level()<=(mob.phyStats().level()+(2*super.getXLEVELLevel(mob))))))
								revokeThis=A;
						}
					}

					if(revokeThis==null)
					{
						if(foundSomethingAtLeast)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"The magic on <T-NAME> appears too powerful to be nullified.");
						else
						if(auto)
							mob.tell(mob,target,null,"Nothing seems to be happening to <T-NAME>.");
					}
					else
						revokeThis.unInvoke();
				}
			}
			else
				beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");
		}

		// return whether it worked
		return success;
	}
}
