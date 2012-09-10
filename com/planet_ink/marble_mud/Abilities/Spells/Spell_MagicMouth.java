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
public class Spell_MagicMouth extends Spell
{
	public String ID() { return "Spell_MagicMouth"; }
	public String name(){return "Magic Mouth";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	Room myRoomContainer=null;
	int myTrigger=CMMsg.TYP_ENTER;
	String message="NO MESSAGE ENTERED";

	boolean waitingForLook=false;

	public void doMyThing()
	{
		myRoomContainer.showHappens(CMMsg.MSG_NOISE,"\n\r\n\r"+affected.name()+" says '"+message+"'.\n\r\n\r");
		unInvoke();
		return;
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);


		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if((msg.amITarget(myRoomContainer))
		&&(!CMLib.flags().isSneaking(msg.source())))
		{
			if((waitingForLook)
			&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			{
				doMyThing();
				return;
			}
			else
			if(msg.targetMinor()==myTrigger)
				waitingForLook=true;
		}
		else
		if(msg.amITarget(affected))
		{
			if((msg.targetMinor()==myTrigger)
			&&(!CMLib.flags().isSneaking(msg.source())))
			{
				doMyThing();
				return;
			}
			switch(myTrigger)
			{
			case CMMsg.TYP_GET:
				if(
				   (msg.targetMinor()==CMMsg.TYP_OPEN)
				 ||(msg.targetMinor()==CMMsg.TYP_GIVE)
				 ||(msg.targetMinor()==CMMsg.TYP_DELICATE_HANDS_ACT)
				 ||(msg.targetMinor()==CMMsg.TYP_JUSTICE)
				 ||(msg.targetMinor()==CMMsg.TYP_GENERAL)
				 ||(msg.targetMinor()==CMMsg.TYP_LOCK)
				 ||(msg.targetMinor()==CMMsg.TYP_PULL)
				 ||(msg.targetMinor()==CMMsg.TYP_PUSH)
				 ||(msg.targetMinor()==CMMsg.TYP_UNLOCK))
				{
					doMyThing();
					return;
				}
			}
		}

	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<3)
		{
			mob.tell("You must specify:\n\r 1. What object you want the spell cast on.\n\r 2. Whether it is triggered by TOUCH, HOLD, WIELD, WEAR, or someone ENTERing the same room. \n\r 3. The message you wish the object to impart. ");
			return false;
		}
		Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,((String)commands.elementAt(0)),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target instanceof MOB)
		{
			mob.tell("You can't can't cast this on "+target.name()+".");
			return false;
		}

		String triggerStr=((String)commands.elementAt(1)).trim().toUpperCase();

		if(triggerStr.startsWith("HOLD"))
			myTrigger=CMMsg.TYP_HOLD;
		else
		if(triggerStr.startsWith("WIELD"))
			myTrigger=CMMsg.TYP_WIELD;
		else
		if(triggerStr.startsWith("WEAR"))
			myTrigger=CMMsg.TYP_WEAR;
		else
		if(triggerStr.startsWith("TOUCH"))
			myTrigger=CMMsg.TYP_GET;
		else
		if(triggerStr.startsWith("ENTER"))
			myTrigger=CMMsg.TYP_ENTER;
		else
		{
			mob.tell("You must specify the trigger event that will cause the mouth to speak.\n\r'"+triggerStr+"' is not correct, but you can try TOUCH, WEAR, WIELD, HOLD, or ENTER.\n\r");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> invoke(s) a spell upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				message=CMParms.combine(commands,2);
				beneficialAffect(mob,target,asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell upon <T-NAMESELF>, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
