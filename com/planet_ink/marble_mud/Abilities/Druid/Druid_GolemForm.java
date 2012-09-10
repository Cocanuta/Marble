package com.planet_ink.marble_mud.Abilities.Druid;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class Druid_GolemForm extends StdAbility
{
	public String ID() { return "Druid_GolemForm"; }
	public String name(){ return "Golem Form";}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"GOLEMFORM"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_SHAPE_SHIFTING;}

	public Race newRace=null;
	public String raceName="";
	public int raceLevel=0;

	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+raceName+" form)";
	}

	private static String[] shapes={
	"Steel Golem",
	"Quartz Golem",
	"Mithril Golem",
	"Diamond Golem",
	"Adamantite Golem"
	};
	private static String[] races={
	"MetalGolem",
	"StoneGolem",
	"MetalGolem",
	"StoneGolem",
	"MetalGolem"
	};

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
			affectableStats.setName(CMLib.english().startWithAorAn(raceName.toLowerCase()));
			int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
			int xlvl=getXLEVELLevel(invoker());
			double bonus=CMath.mul(0.1,xlvl);
			switch(raceLevel)
			{
			case 0:
				affectableStats.setArmor(affectableStats.armor()-10-(xlvl));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10+(xlvl));
				affectableStats.setDamage(affectableStats.damage()+5+(xlvl/2));
				affectableStats.setSpeed(affectableStats.speed()/(1.5-bonus));
				break;
			case 1:
				affectableStats.setArmor(affectableStats.armor()-20-(2*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(2.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20+(2*xlvl));
				affectableStats.setDamage(affectableStats.damage()+10+(xlvl));
				break;
			case 2:
				affectableStats.setArmor(affectableStats.armor()-40-(4*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(2.5-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+40+(4*xlvl));
				affectableStats.setDamage(affectableStats.damage()+20+(xlvl*2));
				break;
			case 3:
				affectableStats.setArmor(affectableStats.armor()-60-(6*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(3.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+80+(8*xlvl));
				affectableStats.setDamage(affectableStats.damage()+40+(xlvl*4));
				break;
			case 4:
				affectableStats.setArmor(affectableStats.armor()-80-(8*xlvl));
				affectableStats.setSpeed(affectableStats.speed()/(4.0-bonus));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+160+(16*xlvl));
				affectableStats.setDamage(affectableStats.damage()+80+(xlvl*8));
				break;
			}
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null) affectableStats.setMyRace(newRace);
	}


	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		switch(raceLevel)
		{
		case 0:
			affectableState.setMovement(affectableState.getMovement()/1);
			break;
		case 1:
			affectableState.setMovement(affectableState.getMovement()/2);
			break;
		case 2:
			affectableState.setMovement(affectableState.getMovement()/4);
			break;
		case 3:
			affectableState.setMovement(affectableState.getMovement()/8);
			break;
		case 4:
			affectableState.setMovement(affectableState.getMovement()/16);
			break;
		}
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> revert(s) to "+mob.charStats().raceName().toLowerCase()+" form.");
	}

	public void setRaceName(MOB mob)
	{
		int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0) classLevel=30;
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);
	}
	public int getRaceLevel(int classLevel)
	{
		if(classLevel<5)
			return 0;
		else
		if(classLevel<10)
			return 1;
		else
		if(classLevel<15)
			return 2;
		else
		if(classLevel<25)
			return 3;
		else
			return 4;
	}
	public Race getRace(int classLevel)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)]);
	}
	public String getRaceName(int classLevel)
	{
		return shapes[getRaceLevel(classLevel)];
	}

	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null) return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_GolemForm))
				return true;
		}
		return false;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).isInCombat())
				&&(!Druid_ShapeShift.isShapeShifted((MOB)target)))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Druid_GolemForm))
			{
				A.unInvoke();
				return true;
			}
		}

		int qualClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		int classLevel=qualClassLevel-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(qualClassLevel<0) classLevel=30;
		String choice=(mob.isMonster()||(commands.size()==0))?getRaceName(classLevel-1):CMParms.combine(commands,0);
		if(choice.trim().length()>0)
		{
			StringBuffer buf=new StringBuffer("Golem Forms:\n\r");
			Vector choices=new Vector();
			for(int i=0;i<classLevel;i++)
			{
				String s=getRaceName(i);
				if(!choices.contains(s))
				{
					choices.addElement(s);
					buf.append(s+"\n\r");
				}
				if(CMLib.english().containsString(s,choice))
				{
					classLevel=i;
					break;
				}
			}
			if(choice.equalsIgnoreCase("list"))
			{
				mob.tell(buf.toString());
				return true;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((CMLib.dice().rollPercentage()<50))
			{
				mob.tell("Extreme emotions disrupt your change.");
				return false;
			}
		}

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> take(s) on "+raceName.toLowerCase()+" form.");
				raceName=getRaceName(classLevel);
				newRace=getRace(classLevel);
				raceLevel=getRaceLevel(classLevel);
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				raceName=CMStrings.capitalizeAndLower(CMLib.english().startWithAorAn(raceName.toLowerCase()));
				CMLib.utensils().confirmWearability(mob);
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
