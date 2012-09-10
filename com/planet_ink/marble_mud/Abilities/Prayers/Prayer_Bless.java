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
public class Prayer_Bless extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_Bless"; }
	public String name(){ return "Bless";}
	public String displayText(){ return "(Blessed)";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;}
	public long flags(){return Ability.FLAG_HOLY;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOOD);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
		if(affected instanceof MOB)
			affectableStats.setArmor(affectableStats.armor()-5-(2*getXLEVELLevel(invoker())));
		else
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}



	public void unInvoke()
	{


		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(canBeUninvoked())
			if((affected instanceof Item)&&(((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
				((MOB)((Item)affected).owner()).tell("The blessing on "+((Item)affected).name()+" fades.");
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your aura of blessing fades.");
		super.unInvoke();
	}

	public static Item getSomething(MOB mob, boolean cursedOnly)
	{
		Vector good=new Vector();
		Vector great=new Vector();
		Item target=null;
		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem(i);
			if((I.container()==null)&&((!cursedOnly)||(isCursed(I))))
				if(I.amWearingAt(Wearable.IN_INVENTORY))
					good.addElement(I);
				else
					great.addElement(I);
		}
		if(great.size()>0)
			target=(Item)great.elementAt(CMLib.dice().roll(1,great.size(),-1));
		else
		if(good.size()>0)
			target=(Item)good.elementAt(CMLib.dice().roll(1,good.size(),-1));
		return target;
	}

	public static void endAllOtherBlessings(MOB from, Physical target, int level)
	{
		List<Ability> V=CMLib.flags().domainAffects(target,Ability.DOMAIN_BLESSING);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.get(v);
			if((CMLib.ableMapper().lowestQualifyingLevel(A.ID())<level)
			||(from==A.invoker())
			||(target==from)
			||(target==A.invoker()))
				A.unInvoke();
		}
	}
	public static void endLowerBlessings(Physical target, int level)
	{
		List<Ability> V=CMLib.flags().domainAffects(target,Ability.DOMAIN_BLESSING);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.get(v);
			if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<level)
				A.unInvoke();
		}
	}
	public static void endLowerCurses(Physical target, int level)
	{
		List<Ability> V=CMLib.flags().domainAffects(target,Ability.DOMAIN_CURSING);
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.get(v);
			if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<=level)
				A.unInvoke();
		}
	}
	
	public boolean supportsMending(Physical item)
	{ 
		return (item instanceof MOB)
				&&((Prayer_Bless.getSomething((MOB)item,true)!=null)
					||(CMLib.flags().domainAffects(item,Ability.DOMAIN_CURSING).size()>0));
	}

	public static boolean isCursed(Item item)
	{
		if(CMLib.flags().isSeen(item))
		{
			if(!CMLib.flags().isRemovable(item))
				return true;
			if(!CMLib.flags().isDroppable(item))
				return true;
		}
		return CMLib.flags().domainAffects(item,Ability.DOMAIN_CURSING).size()>0;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"<T-NAME> appear(s) blessed!":"^S<S-NAME> bless(es) <T-NAMESELF>"+inTheNameOf(mob)+".^?")+CMProps.msp("bless.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=getSomething(target,true);
				HashSet<Item> alreadyDone=new HashSet<Item>();
				while((I!=null)&&(!alreadyDone.contains(I)))
				{
					alreadyDone.add(I);
					CMMsg msg2=CMClass.getMsg(target,I,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,"<S-NAME> release(s) <T-NAME>.");
					target.location().send(target,msg2);
					endLowerCurses(I,CMLib.ableMapper().lowestQualifyingLevel(ID()));
					I.recoverPhyStats();
					I=getSomething(target,true);
				}
				Prayer_Bless.endAllOtherBlessings(mob,target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
				endLowerCurses(target,CMLib.ableMapper().lowestQualifyingLevel(ID()));
				beneficialAffect(mob,target,asLevel,0);
				target.recoverPhyStats();
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for blessings, but nothing happens.");
		// return whether it worked
		return success;
	}
}
