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
public class Spell_FakeFood extends Spell
{
	public String ID() { return "Spell_FakeFood"; }
	public String name(){return "Fake Food";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> invoke(s) a spell dramatically.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Food F=(Food)CMClass.getItem("GenFood");
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1: F.setName("a shiny apple");
						F.setDisplayText("A shiny red apple sits here.");
						F.setDescription("It looks tasty and crisp!");
						break;
				case 2: F.setName("a nice peach");
						F.setDisplayText("A nice peach sits here.");
						F.setDescription("It looks tasty!");
						break;
				case 3: F.setName("a big pot pie");
						F.setDisplayText("A big pot pie has been left here.");
						F.setDescription("It sure looks good!");
						break;
				case 4: F.setName("a juicy steak");
						F.setDisplayText("A juicy steak has been left here.");
						F.setDescription("It sure looks good!");
						break;
				case 5: F.setName("a bit of food");
						F.setDisplayText("A bit of food has been left here.");
						F.setDescription("It sure looks good!");
						break;
				}
				F.setNourishment(0);
				F.setBaseValue(0);
				for(int f=0;f<5;f++)
				{
					Food F2=(Food)F.copyOf();
					F2.recoverPhyStats();
					mob.location().addItem(F2,ItemPossessor.Expire.Resource);
					mob.location().show(mob,null,F2,CMMsg.MSG_OK_VISUAL,"<O-NAME> appears!");
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
