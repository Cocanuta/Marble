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
public class Spell_SlowProjectiles extends Spell
{
	public String ID() { return "Spell_SlowProjectiles"; }
	public String name(){return "Slow Projectiles";}
	public String displayText(){return "(Slow Projectiles)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.source().getVictim()==msg.target())
		&&(msg.source().rangeToTarget()>0)
		&&(msg.tool() instanceof Weapon)
		&&(((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)&&(((Weapon)msg.tool()).requiresAmmunition())
			||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN)))
		&&(msg.source().location()!=null)
		&&(msg.source().location()==affected)
		&&(!msg.source().amDead()))
		{
			if(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN)
				msg.source().location().show(msg.source(),null,msg.tool(),CMMsg.MSG_OK_VISUAL,"<O-NAME> flies slowly by.");
			else
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,"The shot from "+msg.tool().name()+" flies slowly by.");
			int damage=(msg.value())/2;
			msg.setValue(damage);
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,"Projectiles are already slow here!");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) slowly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a field of slowness, but fail(s).");

		return success;
	}
}
