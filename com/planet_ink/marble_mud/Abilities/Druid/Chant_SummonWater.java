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
public class Chant_SummonWater extends Chant
{
	public String ID() { return "Chant_SummonWater"; }
	public String name(){ return "Summon Water";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected Room SpringLocation=null;
	protected Item littleSpring=null;

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,"The little spring dries up.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroy();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) for water.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=CMClass.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,asLevel,0);
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for water, but nothing happens.");

		// return whether it worked
		return success;
	}
}
