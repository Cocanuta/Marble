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
public class Spell_Grow extends Spell
{
	public String ID() { return "Spell_Grow"; }
	public String name(){return "Grow";}
	public String displayText(){return "(Grow)";}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	
	protected int getOldWeight()
	{
		if(!CMath.isInteger(super.text()))
		{
			if(affected!=null)
				super.setMiscText(Integer.toString(affected.basePhyStats().weight()));
			return 0;
		}
		return CMath.s_int(text());
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			double aff=1.0 + CMath.mul(0.1,(invoker().phyStats().level()+(2*getXLEVELLevel(invoker()))));
			affectableStats.setHeight((int)Math.round(CMath.mul(affectableStats.height(),aff)));
		}
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)/2);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+((invoker().phyStats().level()+(2*getXLEVELLevel(invoker())))/5));
	}

	public void unInvoke()
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(getOldWeight()<1)
				mob.baseCharStats().getMyRace().setHeightWeight(mob.basePhyStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));
			else
				mob.basePhyStats().setWeight(getOldWeight());
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shrink(s) back down to size.");
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				double aff=1.0 + CMath.mul(0.1,(target.phyStats().level()));
				aff=aff*aff;
				beneficialAffect(mob,target,asLevel,0);
				Ability A=target.fetchEffect(ID());
				if(A!=null)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> grow(s) to an enormous size!");
					setMiscText(Integer.toString(target.basePhyStats().weight()));
					A.setMiscText(Integer.toString(target.basePhyStats().weight()));
					target.basePhyStats().setWeight((int)Math.round(CMath.mul(target.basePhyStats().weight(),aff)));
					CMLib.utensils().confirmWearability(target);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens.");


		// return whether it worked
		return success;
	}
}
