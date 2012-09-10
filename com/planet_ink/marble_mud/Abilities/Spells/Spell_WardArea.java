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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_WardArea extends Spell implements Trap
{
	public String ID() { return "Spell_WardArea"; }
	public String name(){return "Ward Area";}
	public String displayText(){return "(Ward Area spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	private Ability shooter=null;
	protected Vector parameters=null;
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	protected boolean sprung=false;

	public MOB theInvoker()
	{
		if(invoker()!=null) return invoker();
		if(text().length()>0)
			invoker=CMLib.players().getPlayer(text());
		return invoker();
	}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean disabled(){return sprung;}
	public void disable(){unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Physical P){return false;}
	public List<Item> getTrapComponents() { return new Vector(); }
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{beneficialAffect(mob,P,qualifyingClassLevel+trapBonus,0);return (Trap)P.fetchEffect(ID());}

	public boolean sprung(){return sprung;}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(sprung) return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				if(msg.targetMinor()==CMMsg.TYP_LEAVE)
					return true;
				spring(msg.source());
				if(sprung)
					return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public void spring(MOB mob)
	{
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if((invoker()!=null)&&(mob!=null)&&(!invoker().mayIFight(mob)))
			return;
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a magical ward trap.");
		else
		{
			MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName("the thin air");
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			if(invoker()!=null)
				newCaster.basePhyStats().setLevel(invoker.phyStats().level()+super.getXLEVELLevel(invoker()));
			else
				newCaster.basePhyStats().setLevel(10);
			newCaster.recoverPhyStats();
			newCaster.recoverCharStats();
			if(invoker()!=null)
				newCaster.setLiegeID(invoker().Name());
			newCaster.setLocation((Room)affected);
			try
			{
				shooter.invoke(newCaster,parameters,mob,true,0);
			}
			catch(Exception e){Log.errOut("WARD/"+CMParms.combine(parameters,0),e);}
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		unInvoke();
		sprung=true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(sprung)
			return;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if(msg.targetMinor()==CMMsg.TYP_LEAVE)
				spring(msg.source());
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooter=null;
			parameters=null;
		}
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify what arcane spell to set, and any necessary parameters.");
			return false;
		}
		commands.insertElementAt("CAST",0);
		shooter=CMLib.english().getToEvoke(mob,commands);
		parameters=commands;
		if((shooter==null)||((shooter.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL))
		{
			parameters=null;
			shooter=null;
			mob.tell("You don't know any arcane spell by that name.");
			return false;
		}

		if(shooter.enchantQuality()==Ability.QUALITY_MALICIOUS)
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M!=mob)&&(!M.mayIFight(mob)))
			{
				mob.tell("You cannot set that spell here -- there are other players present!");
				return false;
			}
		}
		Physical target = mob.location();
		if((target.fetchEffect(this.ID())!=null)||(givenTarget!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"A ward trap has already been set here!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), auto?"":"^S<S-NAME> set(s) a magical trap.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if(CMLib.law().doesOwnThisProperty(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMLib.database().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),asLevel,9999);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to set a magic trap, but fail(s).");

		// return whether it worked
		return success;
	}
}
