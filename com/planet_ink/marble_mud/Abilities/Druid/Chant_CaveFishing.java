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
public class Chant_CaveFishing extends Chant
{
	public String ID() { return "Chant_CaveFishing"; }
	public String name(){ return "Cave Fishing";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ROOMS;}
	protected int previousResource=-1;
	
	public void unInvoke()
	{
		if((affected instanceof Room)
		&&(this.canBeUninvoked()))
		{
			((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,"The fish start to disappear!");
			((Room)affected).setResource(previousResource);
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;

		Environmental waterSrc=null;
		if((target.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(target.domainType()==Room.DOMAIN_INDOORS_UNDERWATER))
			waterSrc=target;
		else
		if(target.domainType()==Room.DOMAIN_INDOORS_CAVE)
		{
			for(int i=0;i<target.numItems();i++)
			{
				Item I=target.getItem(i);
				if((I instanceof Drink)
				&&(I.container()==null)
				&&(((Drink)I).liquidType()==RawMaterial.RESOURCE_FRESHWATER)
				&&(!CMLib.flags().isGettable(I)))
					waterSrc=I;
			}
			if(waterSrc==null)
			{
				mob.tell("There is no water source here to fish in.");
				return false;
			}
		}
		else
		{
			mob.tell("This chant cannot be used outdoors.");
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
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Fish start swimming around in "+target.name()+"!");
					beneficialAffect(mob, target, asLevel,0);
					Chant_CaveFishing A=(Chant_CaveFishing)target.fetchEffect(ID());
					if(A!=null)
					{
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Fish start swimming around in "+target.name()+"!");
						A.previousResource=target.myResource();
						target.setResource(RawMaterial.CODES.FISHES()[CMLib.dice().roll(1,RawMaterial.CODES.FISHES().length,-1)]);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAME>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
