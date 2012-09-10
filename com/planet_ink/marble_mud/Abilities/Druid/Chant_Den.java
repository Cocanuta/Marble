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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_Den extends Chant
{
	public String ID() { return "Chant_Den"; }
	public String name(){ return "Den";}
	public String displayText(){return "(Den)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked())
		{
			Room R=room.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.roomID().equalsIgnoreCase("")))
			{
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The den fades away...");
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					if(M!=null)	room.bringMobHere(M,false);
				}
				while(R.numItems()>0)
				{
					Item I=R.getItem(0);
					if(I!=null) room.moveItemTo(I);
				}
				R.destroy();
				room.rawDoors()[Directions.UP]=null;
				room.setRawExit(Directions.UP,null);
			}
			room.clearSky();
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target = mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("There is already a den here!");
			return false;
		}
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("This magic will only work in a cave.");
			return false;
		}
		if(mob.location().roomID().length()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		Vector dirChoices=new Vector();
		for(int d=0;d<Directions.DIRECTIONS_BASE().length;d++)
		{
			if(mob.location().getRoomInDir(Directions.DIRECTIONS_BASE()[d])==null)
				dirChoices.addElement(Integer.valueOf(Directions.DIRECTIONS_BASE()[d]));
		}
		if(dirChoices.size()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int d=((Integer)dirChoices.elementAt(CMLib.dice().roll(1,dirChoices.size(),-1))).intValue();

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

			CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob,null,auto), auto?"":"^S<S-NAME> chant(s) for a den!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Your den, carefully covered, appears to the "+Directions.getDirectionName(d)+"!");
				Room newRoom=CMClass.getLocale("CaveRoom");
				newRoom.setDisplayText("A musty den");
				newRoom.setDescription("You are in a dark rocky den!");
				newRoom.setArea(mob.location().getArea());
				mob.location().rawDoors()[d]=newRoom;
				mob.location().setRawExit(d,CMClass.getExit("HiddenWalkway"));
				newRoom.rawDoors()[Directions.getOpDirectionCode(d)]=mob.location();
				Ability A=CMClass.getAbility("Prop_RoomView");
				A.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
				Exit E=CMClass.getExit("Open");
				E.addNonUninvokableEffect(A);
				A=CMClass.getAbility("Prop_PeaceMaker");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoRecall");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoSummon");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleport");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleportOut");
				if(A!=null) newRoom.addEffect(A);

				newRoom.setRawExit(Directions.getOpDirectionCode(d),E);
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,mob.location(),asLevel,CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for a den, but the magic fades.");

		// return whether it worked
		return success;
	}
}
