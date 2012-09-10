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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_Flagportation extends Spell
{
	public String ID() { return "Spell_Flagportation"; }
	public String name(){return "Flagportation";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING|Ability.FLAG_CLANMAGIC;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	private boolean isBadRoom(final Room room, final MOB mob, final Room newRoom)
	{
		return (room==null)
		||(room==newRoom)
		||(room.getArea()==newRoom.getArea())
		||(room==mob.location())
		||(!CMLib.flags().canAccess(mob,room))
		||(CMLib.law().getLandTitle(room)!=null);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{

		Clan C=mob.getClanID().length()>0?mob.getMyClan():null;
		if(C==null)
		{
			mob.tell("You must belong to a clan to use this spell.");
			return false;
		}
		if((!auto)&&(C.getAuthority(mob.getClanRole(), Clan.Function.ENCHANT)==Clan.Authority.CAN_NOT_DO))
		{
			mob.tell("You do not have priviledges to use this spell.");
			return false;
		}
		Vector candidates=new Vector();
		Room R=null;
		Item I=null;
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(int i=0;i<R.numItems();i++)
					{
						I=R.getItem(i);
						if((I!=null)
						&&(I instanceof ClanItem)
						&&(((ClanItem)I).clanID().equals(C.clanID()))
						&&(((ClanItem)I).ciType()==ClanItem.CI_FLAG))
						{
							candidates.addElement(R);
							break;
						}
					}
				}
			}
		}catch(NoSuchElementException nse){}
		if(candidates.size()==0)
		{
			mob.tell("You don't have any flags to flagportate to!");
			return false;
		}

		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=(Room)candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
			CMMsg enterMsg=CMClass.getMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell("Your magic seems unable to take you to that flag.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while((isBadRoom(room,mob,newRoom))&&((++x)<1000))
				room=CMLib.map().getRandomRoom();
			if(isBadRoom(room,mob,newRoom))
				beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke flagportating transportation, but fizzle(s) the spell.");
			newRoom=room;
		}

		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,null,auto),"^S<S-NAME> invoke(s) a flagportating teleportation spell.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Set<MOB> h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CMProps.msp("appear.wav",10));
				CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
				if(thisRoom.okMessage(follower,leaveMsg)&&(newRoom!=null)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace();
					}
					thisRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell("\n\r\n\r");
					CMLib.commands().postLook(follower,true);
				}
			}
		}
		// return whether it worked
		return success;
	}
}
