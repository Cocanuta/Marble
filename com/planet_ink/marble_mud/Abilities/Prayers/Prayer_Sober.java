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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prayer_Sober extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_Sober"; }
	public String name(){ return "Sober";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean supportsMending(Physical item)
	{ 
		if(!(item instanceof MOB)) return false;
		MOB caster=CMClass.getFactoryMOB();
		caster.basePhyStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		caster.phyStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		boolean canMend=returnOffensiveAffects(caster,item).size()>0;
		caster.destroy();
		return canMend;
	}
	
	public List<Ability> returnOffensiveAffects(MOB caster, Physical fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				if((CMath.bset(A.flags(),Ability.FLAG_INTOXICATING))
				&&((A.invoker()==null)
				   ||((A.invoker()!=null)
					  &&(A.invoker().phyStats().level()<=(caster.phyStats().level()+20+(2*super.getXLEVELLevel(caster)))))))
						offenders.addElement(A);
			}
		}
		return offenders;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(supportsMending((MOB)target))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		List<Ability> offensiveAffects=returnOffensiveAffects(mob,target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to sober up.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.get(a)).unInvoke();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
