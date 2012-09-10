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
public class Spell_Gate extends Spell
{
	public String ID() { return "Spell_Gate"; }
	public String name(){return "Gate";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	protected int overrideMana(){return Ability.COST_ALL-50;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean isBadRoom(final Room room, final MOB mob, final Room newRoom)
	{
		return (room==null)
		||(room==newRoom)
		||(room==mob.location())
		||(!CMLib.flags().canAccess(mob,room))
		||(CMLib.law().getLandTitle(room)!=null);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			MOB M=null;
			int tries=0;
			while(((++tries)<100)&&(M==null))
			{
				Room R=CMLib.map().getRandomRoom();
				if(R.numInhabitants()>0)
					M=R.fetchRandomInhabitant();
				if((M!=null)&&(M.name().equals(mob.name())))
					M=null;
			}
			if(M!=null)
				commands.addElement(M.Name());
		}
		if(commands.size()<1)
		{
			mob.tell("Gate to whom?");
			return false;
		}
		String areaName=CMParms.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		List<MOB> candidates=new Vector();
		MOB target=null;
		try{
			candidates=CMLib.map().findInhabitants(CMLib.map().rooms(), mob, areaName, 10);
		}catch(NoSuchElementException nse){}
		Room newRoom=null;
		if(candidates.size()>0)
		{
			target=(MOB)candidates.get(CMLib.dice().roll(1,candidates.size(),-1));
			newRoom=target.location();
		}

		if((newRoom==null) || (target == null))
		{
			mob.tell("You can't seem to fixate on '"+CMParms.combine(commands,0)+"', perhaps they don't exist?");
			return false;
		}

		
		int adjustment=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(target.isMonster()) adjustment=adjustment*3;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,-adjustment,auto);
		String addOn=".";
		if(!success)
		{
			Room room=null;
			int x=0;
			while(isBadRoom(room,mob,newRoom) && ((++x)<1000))
				room=CMLib.map().getRandomRoom();
			if(isBadRoom(room,mob,newRoom))
			{
				beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");
				room=null;
			}
			addOn=", but the spell goes AWRY!!";
			newRoom=room;
		}

		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),"^S<S-NAME> invoke(s) a teleportation spell"+addOn+"^?");
		if((mob.location().okMessage(mob,msg))&&(newRoom!=null)&&(newRoom.okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			Set<MOB> h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CMProps.msp("appear.wav",10));
				CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a burst of light.");
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
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
