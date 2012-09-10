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
public class Spell_IllusoryWall extends Spell
{
	public String ID() { return "Spell_IllusoryWall"; }
	public String name(){return "Illusory Wall";}
	protected int canAffectCode(){return CAN_EXITS;}
	protected int canTargetCode(){return CAN_EXITS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{

		String whatToOpen=CMParms.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode<0)
		{
			mob.tell("Cast which direction?!");
			return false;
		}

		Exit exit=mob.location().getExitInDir(dirCode);
		Room room=mob.location().getRoomInDir(dirCode);

		if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob)))
		{
			mob.tell("That way is already closed.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> whisper(s) "+Directions.getDirectionName(dirCode)+", but nothing happens.");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,exit,this,verbalCastCode(mob,exit,auto),auto?"":"^S<S-NAME> whisper(s) "+Directions.getDirectionName(dirCode)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,exit,asLevel,0);
			}
		}

		return success;
	}
}
