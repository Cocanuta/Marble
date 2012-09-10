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
public class Chant_ChargeMetal extends Chant
{
	public String ID() { return "Chant_ChargeMetal"; }
	public String name(){return "Charge Metal";}
	public String displayText(){return "(Charged)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}

	protected Vector affectedItems=new Vector();

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		affectedItems=new Vector();
	}
	
	public Item wieldingMetal(MOB mob)
	{
		for(int i=0;i<mob.numItems();i++)
		{
			Item item=mob.getItem(i);
			if((item!=null)
			&&(!item.amWearingAt(Wearable.IN_INVENTORY))
			&&(CMLib.flags().isMetal(item))
			&&(item.container()==null)
			&&(!mob.amDead()))
				return item;
		}
		return null;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null) return true;
		if(!(affected instanceof Item)) return true;

		Item I=(Item)affected;
		if((I.owner()==null)
		||(!(I.owner() instanceof MOB))
		||(I.amWearingAt(Wearable.IN_INVENTORY)))
			return true;

		MOB mob=(MOB)I.owner();
		if((!msg.amITarget(mob))
		&&((msg.targetMinor()==CMMsg.TYP_ELECTRIC)
			||((msg.sourceMinor()==CMMsg.TYP_ELECTRIC)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))))
		{
			msg.source().location().show(mob,null,I,CMMsg.MSG_OK_VISUAL,"<O-NAME> attracts a charge to <S-NAME>!");
			msg.modify(msg.source(),
					   mob,
					   msg.tool(),
					   msg.sourceCode(),
					   msg.sourceMessage(),
					   msg.targetCode(),
					   msg.targetMessage(),
					   msg.othersCode(),
					   msg.othersMessage());
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
		{
			super.unInvoke();
			return;
		}

		if(canBeUninvoked())
		if(affected instanceof MOB)
		{
			for(int i=0;i<affectedItems.size();i++)
			{
				Item I=(Item)affectedItems.elementAt(i);
				Ability A=I.fetchEffect(this.ID());
				while(A!=null)
				{
					I.delEffect(A);
					A=I.fetchEffect(this.ID());
				}

			}
		}
		super.unInvoke();
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				Item I=wieldingMetal((MOB)target);
				if(I==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;
		Item I=null;
		if(target instanceof MOB) I=wieldingMetal((MOB)target);

		if((target instanceof Item)
		&&(CMLib.flags().isMetal(target)))
			I=(Item)target;
		else
		if(target instanceof Item)
		{
			mob.tell(target.name()+" is not made of metal!");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(I!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,I,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
