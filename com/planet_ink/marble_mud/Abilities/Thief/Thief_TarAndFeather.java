package com.planet_ink.marble_mud.Abilities.Thief;
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
public class Thief_TarAndFeather extends ThiefSkill
{
	public String ID() { return "Thief_TarAndFeather"; }
	public String name(){ return "Tar And Feather";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"TARANDFEATHER","TAR"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 100;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_LEGAL;}

	public void affectPhyStats(Physical host, PhyStats stats)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return;
		if((((Item)affected).amWearingAt(Wearable.IN_INVENTORY))||(((Item)affected).amDestroyed()))
		{
			Item I=(Item)affected;
			affected.delEffect(this);
			setAffectedOne(null);
			I.destroy();
		}
	}
	
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if((!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;
		if((!auto)&&(!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
		{
			mob.tell(target.name()+" must be prone or bound first.");
			return false;
		}
		for(int i=0;i<target.numItems();i++)
		{
			Item I=target.getItem(i);
			if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))&&(!I.amWearingAt(Wearable.WORN_FLOATING_NEARBY)))
			{
				mob.tell(target.name()+" must be undressed first.");
				return false;
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,"<S-NAME> tar(s) and feather(s) <T-NAMESELF>!");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Item I=CMClass.getArmor("GenArmor");
			if(I!=null)
			{
				target.addItem(I);
				long wearCode=0;
				Wearable.CODES codes = Wearable.CODES.instance();
				for(int i=0;i<codes.all_ordered().length;i++)
				{
					long code = codes.all_ordered()[i];
					if((!CMath.bset(target.charStats().getWearableRestrictionsBitmap(),code))
					&&(code!=Wearable.WORN_FLOATING_NEARBY)
					&&(code!=Wearable.WORN_EYES)
					&&(code!=Wearable.WORN_MOUTH))
						wearCode|=code;
				}
				for(int i=0;i<Race.BODY_WEARGRID.length;i++)
				{
					if((target.charStats().getBodyPart(i)<=0)
					&&(Race.BODY_WEARGRID[i][1]>0))
						wearCode=CMath.unsetb(wearCode,Race.BODY_WEARGRID[i][0]);
				}
				I.setRawProperLocationBitmap(wearCode);
				I.setRawWornCode(wearCode);
				I.setName("a coating of tar and feathers");
				I.setDisplayText("a pile of tar and feathers sits here.");
				I.basePhyStats().setSensesMask(PhyStats.SENSE_ITEMNOREMOVE);
				I.phyStats().setSensesMask(PhyStats.SENSE_ITEMNOREMOVE);
				I.setRawLogicalAnd(true);
				I.addNonUninvokableEffect((Ability)this.copyOf());
				Behavior B=CMClass.getBehavior("Decay");
				long thetime=(long)CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*3;
				B.setParms("notrigger=1 answer=dissolves! min="+thetime+" max="+thetime+" chance=100");
				I.addBehavior(B);
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to tar and feather <T-NAMESELF>, but fail(s).");
		return success;
	}
}
