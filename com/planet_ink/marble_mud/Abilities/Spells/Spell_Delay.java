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
public class Spell_Delay extends Spell
{
	public String ID() { return "Spell_Delay"; }
	public String name(){return "Delay";}
	public String displayText(){return "(Delay spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	private Ability shooter=null;
	protected Vector parameters=null;
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	protected int overridemana(){return Ability.COST_ALL;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooter==null)||(parameters==null))
			return;
		if(canBeUninvoked())
		{
			shooter = (Ability)shooter.copyOf();
			MOB newCaster=CMClass.getMOB("StdMOB");
			newCaster.setName("the thin air");
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			newCaster.basePhyStats().setLevel(invoker.phyStats().level()+(2*getXLEVELLevel(invoker)));
			newCaster.recoverPhyStats();
			newCaster.recoverCharStats();
			newCaster.setLocation((Room)affected);
			newCaster.addAbility(shooter);
			try
			{
				shooter.setProficiency(100);
				shooter.invoke(newCaster,parameters,null,false,invoker.phyStats().level()+(2*getXLEVELLevel(invoker)));
			}
			catch(Exception e){Log.errOut("DELAY/"+CMParms.combine(parameters,0),e);}
			newCaster.delAbility(shooter);
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooter=null;
			parameters=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify what arcane spell to delay, and any necessary parameters.");
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
				mob.tell("You cannot delay that spell here -- there are other players present!");
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
		if((target.fetchEffect(this.ID())!=null)||(givenTarget!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"A delay has already been cast here!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), auto?"":"^S<S-NAME> point(s) and shout(s) 'NOW!'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell("You hear a clock start ticking down in your head...20...19...");
				beneficialAffect(mob,mob.location(),asLevel,5);
				shooter=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> point(s) and shout(s) 'NOW', but then look(s) frustrated.");

		// return whether it worked
		return success;
	}
}
