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
public class Spell_KnowOrigin extends Spell
{
	public String ID() { return "Spell_KnowOrigin"; }
	public String name(){return "Know Origin";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public Room origin(MOB mob, Environmental meThang)
	{
		if(meThang instanceof LandTitle)
			return (Room)((LandTitle)meThang).getAllTitledRooms().get(0);
		else
		if(meThang instanceof MOB)
			return ((MOB)meThang).getStartRoom();
		else
		if(meThang instanceof Item)
		{
			Item me=(Item)meThang;
			try
			{
				// check mobs worn items first!
				String srchStr="$"+me.Name()+"$";
				Environmental E=CMLib.map().findFirstShopStocker(CMLib.map().rooms(), mob, srchStr, 10);
				if(E!=null) return CMLib.map().getStartRoom(E);
				E=CMLib.map().findFirstInventory(CMLib.map().rooms(), mob, srchStr, 10);
				if(E!=null) return CMLib.map().getStartRoom(E);
				return CMLib.map().findWorldRoomLiberally(mob,srchStr, "I",10,600000);
			}catch(NoSuchElementException nse){}
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Room R=origin(mob,target);
		boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(R!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s), divining the origin of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(target.name()+" seems to come from '"+R.roomTitle(mob)+"'.");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to divine something, but fail(s).");

		return success;
	}
}
