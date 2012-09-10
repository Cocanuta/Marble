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
public class Spell_AnimateWeapon extends Spell
{
	public String ID() { return "Spell_AnimateWeapon"; }
	public String name(){return "Animate Weapon";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public int overrideMana(){return 100;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location().isContent((Item)affected)))
		{
			if(invoker().isInCombat())
			{
				boolean isHit=(CMLib.combat().rollToHit(CMLib.combat().adjustedAttackBonus(invoker(),invoker().getVictim())+((Item)affected).phyStats().attackAdjustment(),CMLib.combat().adjustedArmor(invoker().getVictim()), 0));
				if((!isHit)||(!(affected instanceof Weapon)))
					invoker().location().show(invoker(),invoker().getVictim(),affected,CMMsg.MSG_OK_ACTION,"<O-NAME> attacks <T-NAME> and misses!");
				else
					CMLib.combat().postDamage(invoker(),invoker().getVictim(),affected,
											CMLib.dice().roll(1,affected.phyStats().damage(),5),
											CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,
											((Weapon)affected).weaponType(),affected.name()+" attacks and <DAMAGE> <T-NAME>!");
			}
			else
			if(CMLib.dice().rollPercentage()>75)
			switch(CMLib.dice().roll(1,5,0))
			{
			case 1:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" twiches a bit.");
				break;
			case 2:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" is looking for trouble.");
				break;
			case 3:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" practices its moves.");
				break;
			case 4:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" makes a few fake attacks.");
				break;
			case 5:
				invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,affected.name()+" dances around.");
				break;
			}
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!super.okMessage(myHost,msg))
		||(affected==null)
		||(!(affected instanceof Item)))
		{
			unInvoke();
			return false;
		}
		if(msg.amITarget(affected))
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				unInvoke();
				break;
			}
		return true;
	}

	public void unInvoke()
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room))
			((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_ACTION,affected.name()+" stops moving.");
		super.unInvoke();
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;
		if(!(target instanceof Weapon))
		{
			mob.tell("That's not a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.unWear();
				if(mob.isMine(target))
					mob.location().show(mob,target,CMMsg.MSG_DROP,"<T-NAME> flies out of <S-YOUPOSS> hands!");
				else
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> starts flying around!");
				if(mob.location().isContent(target))
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> twitch(es) oddly, but does nothing more.");


		// return whether it worked
		return success;
	}
}
