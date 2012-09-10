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
public class Spell_DeathWarning extends Spell
{
	public String ID() { return "Spell_DeathWarning"; }
	public String name(){return "Death Warning";}
	public String displayText(){return "(Death Warning)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	protected Vector commands=new XVector("FLEE");
	
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell(mob,null,null,"<S-YOUPOSS> death warning magic fades.");
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			MOB mob=(MOB)affected;
			int hitPoints=mob.curState().getHitPoints();
			mob.curState().setHitPoints(1);
			Room room=mob.location();
			mob.tell("^SYou receive a warning of your impending death!!^N");
			mob.doCommand(commands,0);
			if(mob.location()!=room)
			{
				mob.makePeace();
				return false;
			}
			else
				mob.curState().setHitPoints(hitPoints);
			unInvoke();
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> a death's warning.");
			return false;
		}

		if(commands.size()==0)
		{
			if(mob.isMonster())
				commands.add("FLEE");
			else
			{
				mob.tell("You need to specify what you want to do should the warning arrives!");
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> begin(s) listening for a death's warning!":"^S<S-NAME> incant(s) coldly, and begin(s) listening for death's warning!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				Spell_DeathWarning A=(Spell_DeathWarning)target.fetchEffect(ID());
				if(A!=null) A.commands=commands;
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) coldly and listen(s), but the spell fizzles.");

		return success;
	}
}
