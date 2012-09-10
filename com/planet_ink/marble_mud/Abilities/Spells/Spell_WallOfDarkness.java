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
public class Spell_WallOfDarkness extends Spell
{
	public String ID() { return "Spell_WallOfDarkness"; }
	public String name(){return "Wall of Darkness";}
	public String displayText(){return "(Wall of Darkness)";}
	public int maxRange(){return adjustedMaxInvokerRange(10);}
	public int minRange(){return 1;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	protected Item theWall=null;

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		MOB mob=msg.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&(mob.getVictim()==invoker)
		&&(mob.rangeToTarget()>=1)
		&&(msg.amITarget(invoker))
		&&((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0))
		{
			if((msg.tool()!=null)&&(msg.tool() instanceof Ability))
			{
				mob.tell("You cannot see through the wall of darkness to target "+mob.getVictim().name()+".");
				return false;
			}
			if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&(!((Weapon)msg.tool()).amWearingAt(Wearable.IN_INVENTORY))
			&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED))
			{
				mob.tell("You cannot see through the wall of darkness to target "+mob.getVictim().name()+".");
				if(mob.isMonster())
					CMLib.commands().postRemove(mob,(Item)msg.tool(),true);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((theWall!=null)
			&&(invoker!=null)
			&&(theWall.owner()!=null)
			&&(theWall.owner() instanceof Room)
			&&(((Room)theWall.owner()).isContent(theWall)))
			{
				((Room)theWall.owner()).showHappens(CMMsg.MSG_OK_VISUAL,"The wall of darkness fades.");
				Item wall=theWall;
				theWall=null;
				wall.destroy();
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null)
			   &&(!invoker.location().isContent(theWall)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell("You really should be in ranged combat to cast this.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().getItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell("There is already a wall of darkness here.");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target = mob.location();


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),auto?"An eerie wall of darkness appears!":"^S<S-NAME> conjur(s) up a eerie wall of darkness!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of darkness");
				I.setDisplayText("an eerie wall of darkness lingers here");
				I.setDescription("It`s black.");
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				CMLib.flags().setGettable(I,false);
				I.recoverPhyStats();
				mob.location().addItem(I);
				theWall=I;
				beneficialAffect(mob,I,asLevel,10);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
