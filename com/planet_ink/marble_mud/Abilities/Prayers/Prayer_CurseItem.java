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
public class Prayer_CurseItem extends Prayer
{
	public String ID() { return "Prayer_CurseItem"; }
	public String name(){ return "Curse Item";}
	public String displayText(){ return "(Cursed)";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_EVIL);
		int xlvl=super.getXLEVELLevel(invoker());
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()+(10+(2*xlvl)));
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()-1);
	}

	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The curse on "+((Item)affected).name()+" is lifted.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("The curse is lifted.");
		super.unInvoke();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;

		Item item=(Item)affected;

		MOB mob=msg.source();
		if((msg.tool()==item)&&(msg.sourceMinor()==CMMsg.TYP_THROW))
		{
			mob.tell("You can't seem to get rid of "+item.name()+".");
			return false;
		}
		else
		if(!msg.amITarget(item))
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_REMOVE:
			if(!item.amWearingAt(Wearable.IN_INVENTORY))
			{
				if(item.amWearingAt(Wearable.WORN_WIELD)||item.amWearingAt(Wearable.WORN_HELD))
				{
					mob.tell("You can't seem to let go of "+item.name()+".");
					return false;
				}
				mob.tell("You can't seem to remove "+item.name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
			mob.tell("You can't seem to get rid of "+item.name()+".");
			return false;
		}
		return true;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((target instanceof MOB)&&(mob!=target))
			{
				Item I=Prayer_Curse.getSomething((MOB)target,true);
				if(I==null)
					I=Prayer_Curse.getSomething((MOB)target,false);
				if(I==null) return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
			target=Prayer_Curse.getSomething(mobTarget,true);
		if((target==null)&&(mobTarget!=null))
			target=Prayer_Curse.getSomething(mobTarget,false);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> <T-IS-ARE> cursed!":"^S<S-NAME> curse(s) <T-NAMESELF>.^?");
			CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,verbalCastCode(mob,mobTarget,auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					Prayer_Curse.endLowerBlessings(target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
					success=maliciousAffect(mob,target,asLevel,0,-1);
					target.recoverPhyStats();
					mob.recoverPhyStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
