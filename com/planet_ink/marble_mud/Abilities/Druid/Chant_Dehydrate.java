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

import java.util.List;
import java.util.Vector;


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
public class Chant_Dehydrate extends Chant
{
	public String ID() { return "Chant_Dehydrate"; }
	public String name(){return "Dehydrate";}
	public String displayText(){return "(Dehydrate)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

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
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof MOB)
					{
						((MOB)target).curState().adjThirst(-150 - ((mob.phyStats().level()+(2*super.getXLEVELLevel(mob))) * 100),((MOB)target).maxState().maxThirst(((MOB)target).baseWeight()));
						mob.location().show(((MOB)target),null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) incredibly thirsty!");
					}
					else
					if(target instanceof Item)
					{
						if(target instanceof Container)
						{
							List<Item> V=((Container)target).getContents();
							for(int i=0;i<V.size();i++)
							{
								Item I=(Item)V.get(i);
								if(I instanceof Drink)
								{
									if(((Drink)I).liquidRemaining()<10000)
										((Drink)I).setLiquidRemaining(0);
									if(I instanceof RawMaterial)
										I.destroy();
								}
							}
							if(target instanceof Drink)
							{
								if(((Drink)target).liquidRemaining()<10000)
									((Drink)target).setLiquidRemaining(0);
							}
							if(target instanceof RawMaterial)
								((Item)target).destroy();
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
