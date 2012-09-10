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
public class Chant_SpeedAging extends Chant
{
	public String ID() { return "Chant_SpeedAging"; }
	public String name(){ return "Speed Aging";}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int overridemana(){return Ability.COST_ALL;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY,true);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int type=verbalCastCode(mob,target,auto);
		if((target instanceof MOB)
		&&(CMath.bset(type,CMMsg.MASK_MALICIOUS))
		&&(((MOB)target).charStats().getStat(CharStats.STAT_AGE)>0))
		{
			MOB mobt=(MOB)target;
			if(mobt.charStats().ageCategory()<=Race.AGE_CHILD)
				type=CMath.unsetb(type,CMMsg.MASK_MALICIOUS);
			else
			if((mobt.getLiegeID().equals(mob.Name()))||(mobt.amFollowing()==mob))
				type=CMath.unsetb(type,CMMsg.MASK_MALICIOUS);
			else
			if((mobt.charStats().ageCategory()<=Race.AGE_MATURE)
			&&(mobt.getLiegeID().length()>0))
				type=CMath.unsetb(type,CMMsg.MASK_MALICIOUS);
		}
				
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,type,auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect("Age");
				if((!(target instanceof MOB))
				&&(!(target instanceof CagedAnimal))
				&&(A==null))
				{
					if(target instanceof Food)
					{
						mob.tell(target.name()+" rots away!");
						((Item)target).destroy();
					}
					else
					if(target instanceof Item)
					{
						switch(((Item)target).material()&RawMaterial.MATERIAL_MASK)
						{
							case RawMaterial.MATERIAL_CLOTH:
							case RawMaterial.MATERIAL_FLESH:
							case RawMaterial.MATERIAL_LEATHER:
							case RawMaterial.MATERIAL_PAPER:
							case RawMaterial.MATERIAL_VEGETATION:
							case RawMaterial.MATERIAL_WOODEN:
							{
								mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" rots away!");
								((Item)target).destroy();
								break;
							}
						default:
							mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages, but nothing happens to it.");
							break;
						}
					}
					else
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages, but nothing happens to it.");
					success=false;
				}
				else
				if((target instanceof MOB)
				&&((A==null)||(A.displayText().length()==0)))
				{
					MOB M=(MOB)target;
					mob.location().show(M,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> age(s) a bit.");
					if(M.baseCharStats().getStat(CharStats.STAT_AGE)<=0)
						M.setAgeMinutes(M.getAgeMinutes()+(M.getAgeMinutes()/10));
					else
					if((M.playerStats()!=null) && (M.playerStats().getBirthday()!=null))
					{
						double aging=CMath.mul(M.baseCharStats().getStat(CharStats.STAT_AGE),.10);
						int years=(int)Math.round(Math.floor(aging));
						int monthsInYear=CMLib.time().globalClock().getMonthsInYear();
						int months=(int)Math.round(CMath.mul(aging-Math.floor(aging),monthsInYear));
						M.playerStats().getBirthday()[2]-=years;
						M.playerStats().getBirthday()[1]-=months;
						if(M.playerStats().getBirthday()[1]<1)
						{
							M.playerStats().getBirthday()[2]--;
							years++;
							M.playerStats().getBirthday()[1]=monthsInYear+M.playerStats().getBirthday()[1];
						}
						M.baseCharStats().setStat(CharStats.STAT_AGE,M.baseCharStats().getStat(CharStats.STAT_AGE)+years);
					}
					M.recoverPhyStats();
					M.recoverCharStats();
				}
				else
				if(A!=null)
				{
					long start=CMath.s_long(A.text());
					long age=System.currentTimeMillis()-start;
					if(age<(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*CMProps.getTickMillis()))
						age=(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*CMProps.getTickMillis());
					A.setMiscText(""+(start-(age/10)));
					if(target instanceof MOB)
						mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> age(s) a bit.");
					else
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages a bit.");
					target.recoverPhyStats();
				}
				else
					return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
			}
		}
		else
		if(CMath.bset(type,CMMsg.MASK_MALICIOUS))
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
