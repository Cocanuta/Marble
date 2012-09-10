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
public class Chant_SpeedBirth extends Chant
{
	public String ID() { return "Chant_SpeedBirth"; }
	public String name(){ return "Speed Birth";}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	protected int overridemana(){return Ability.COST_ALL;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Ability A=target.fetchEffect("Pregnancy");
		long start=0;
		long end=0;
		long days=0;
		long remain=0;
		String rest=null;
		if(A!=null)
		{
			int x=A.text().indexOf('/');
			if(x>0)
			{
				int y=A.text().indexOf('/',x+1);
				if(y>x)
				{
					start=CMath.s_long(A.text().substring(0,x));
					end=CMath.s_long(A.text().substring(x+1,y));
					remain=end-System.currentTimeMillis();
					long divisor=CMProps.getTickMillis()*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
					days=remain/divisor; // down to days;
					rest=A.text().substring(y);
				}
				else
					A=null;
			}
			else
				A=null;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(A!=null)&&(remain>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(remain<=20000)
				{
					mob.tell("Birth is imminent!");
					return true;
				}
				else
				if(days<1)
					remain=20000;
				else
					remain=remain/2;
				A.setMiscText((start-remain)+"/"+(end-remain)+rest);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> appear(s) even MORE pregnant!");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
