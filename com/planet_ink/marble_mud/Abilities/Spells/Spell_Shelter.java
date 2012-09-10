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
public class Spell_Shelter extends Spell
{
	public String ID() { return "Spell_Shelter"; }
	public String name(){return "Shelter";}
	public String displayText(){return "(In a shelter)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	public Room previousLocation=null;
	public Room shelter=null;
	
	public Room getPreviousLocation(MOB mob)
	{
		if(previousLocation==null)
		{
			if(text().length()>0)
				previousLocation=CMLib.map().getRoom(text());
			while((previousLocation==null)||(!CMLib.flags().canAccess(mob, previousLocation)))
				previousLocation=CMLib.map().getRandomRoom();
		}
		return previousLocation;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
		{
			int i=0;
			if(shelter==null)
				shelter=mob.location();
			while(i<shelter.numInhabitants())
			{
				mob=shelter.fetchInhabitant(0);
				if(mob==null) break;
				mob.tell("You return to your previous location.");

				CMMsg enterMsg=CMClass.getMsg(mob,previousLocation,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of nowhere!");
				getPreviousLocation(mob).bringMobHere(mob,false);
				getPreviousLocation(mob).send(mob,enterMsg);
				CMLib.commands().postLook(mob,true);
			}
			shelter=null;
			previousLocation=null;
		}
		super.unInvoke();
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(((msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target()==shelter))
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(shelter!=null)
		&&(shelter.isInhabitant(msg.source())))
		{
			getPreviousLocation(msg.source()).bringMobHere(msg.source(),false);
			unInvoke();
		}
		return super.okMessage(host,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.fetchEffect(ID())!=null)
		{
			mob.fetchEffect(ID()).unInvoke();
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms, speak(s), and suddenly vanish(es)!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Set<MOB> h=properTargets(mob,givenTarget,false);
				if(h==null) return false;
	
				Room thisRoom=mob.location();
				previousLocation=thisRoom;
				shelter=CMClass.getLocale("MagicShelter");
				Room newRoom=shelter;
				shelter.setArea(mob.location().getArea());
				miscText=CMLib.map().getExtendedRoomID(mob.location());
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					CMMsg enterMsg=CMClass.getMsg(follower,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of nowhere.");
					CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,verbalCastCode(mob,newRoom,auto),"<S-NAME> disappear(s) into oblivion.");
					if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						thisRoom.delInhabitant(follower);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CMLib.commands().postLook(follower,true);
						if(follower==mob)
							beneficialAffect(mob,mob,asLevel,999999);
					}
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and and speak(s), but nothing happens.");

		return success;
	}
}
