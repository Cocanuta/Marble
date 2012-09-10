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
public class Spell_Augury extends Spell
{
	public String ID() { return "Spell_Augury"; }
	public String name(){return "Augury";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Divine the fate of which direction?");
			return false;
		}
		String targetName=CMParms.combine(commands,0);

		Exit exit=null;
		Exit opExit=null;
		Room room=null;
		int dirCode=Directions.getGoodDirectionCode(targetName);
		if(dirCode>=0)
		{
			exit=mob.location().getExitInDir(dirCode);
			room=mob.location().getRoomInDir(dirCode);
			if(room!=null)
				opExit=mob.location().getReverseExit(dirCode);
		}
		else
		{
			mob.tell("Divine the fate of which direction?");
			return false;
		}
		if((exit==null)||(room==null))
		{
			mob.tell("You couldn't go that way if you wanted to!");
			return false;
		}

		if(!super.invoke(mob,commands,null,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger "+Directions.getDirectionName(dirCode)+", incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				boolean aggressiveMonster=false;
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mon=room.fetchInhabitant(m);
					if(mon!=null)
						for(Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
						{
							Behavior B=e.nextElement();
							if((B!=null)&&(B.grantsAggressivenessTo(mob)))
							{
								aggressiveMonster=true;
								break;
							}
						}
				}
				mob.location().send(mob,msg);
				if((aggressiveMonster)
				||CMLib.flags().isDeadlyOrMaliciousEffect(room)
				||CMLib.flags().isDeadlyOrMaliciousEffect(exit)
				||((opExit!=null)&&(CMLib.flags().isDeadlyOrMaliciousEffect(opExit))))
					mob.tell("You feel going that way would be bad.");
				else
					mob.tell("You feel going that way would be ok.");
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> point(s) <S-HIS-HER> finger "+Directions.getDirectionName(dirCode)+", incanting, but then loses concentration.");


		// return whether it worked
		return success;
	}
}
