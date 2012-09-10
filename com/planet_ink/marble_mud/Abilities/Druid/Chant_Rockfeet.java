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
public class Chant_Rockfeet extends Chant
{
	public String ID() { return "Chant_Rockfeet"; }
	public String name(){return "Rockfeet";}
	public String displayText(){return "(Rockfeet)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;}
	public int maxRange(){return adjustedMaxInvokerRange(10);}
	public int minRange(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public boolean bubbleAffect(){return true;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}

	public void unInvoke()
	{
		MOB M=null;
		if(affected instanceof MOB)
			M=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(M!=null)&&(!M.amDead()))
			M.tell("Your hands and feet don't seem so heavy any more.");
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS)
		   ||CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
		{
			if(CMLib.dice().rollPercentage()>(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)*3))
			{
				msg.source().curState().adjMovement(-1,msg.source().maxState());
				msg.source().curState().adjFatigue(CMProps.getTickMillis(),msg.source().maxState());
			}
		}
		return;
	}

   public int castingQuality(MOB mob, Physical target)
   {
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).getWearPositions(Wearable.WORN_HANDS)==0)
				&&(((MOB)target).getWearPositions(Wearable.WORN_FEET)==0))
					return Ability.QUALITY_INDIFFERENT;
			}
			Room R=mob.location();
			if(R!=null)
			{
			}
		}
		return super.castingQuality(mob,target);
	}    
   
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.getWearPositions(Wearable.WORN_HANDS)==0)
		&&(target.getWearPositions(Wearable.WORN_FEET)==0))
		{
			if(!auto)
				mob.tell(target.name()+" doesn't have hands or feet to affect...");
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

			CMMsg msg = CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) at <T-NAME> heavily!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					maliciousAffect(mob,target,asLevel,0,-1);
					target.tell("Your hands and feet feel extremely heavy!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
