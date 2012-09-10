package com.planet_ink.marble_mud.Abilities.Prayers;
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
import com.planet_ink.marble_mud.Libraries.interfaces.TrackingLibrary;
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
public class Prayer_SeekersPrayer extends Prayer
{
	public String ID() { return "Prayer_SeekersPrayer"; }
	public String name(){ return "Seekers Prayer";}
	public String displayText(){ return "";}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell("Not while you're fighting!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target=mob;
		if((auto)&&(givenTarget!=null)) target=givenTarget;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),auto?"":"^S<T-NAME> "+prayWord(mob)+" for knowledge of seekers.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int numSeekers=super.getXLEVELLevel(mob) + 2;
				if(CMLib.ableMapper().qualifyingLevel(mob, this)>1)
					numSeekers += (super.adjustedLevel(mob, 0) / CMLib.ableMapper().qualifyingLevel(mob, this));
				List<Quest> seeks=new Vector<Quest>();
				for(Enumeration<Quest> q = CMLib.quests().enumQuests(); q.hasMoreElements();)
				{
					final Quest Q = q.nextElement();
					final MOB M=Q.getQuestMob(1);
					if( Q.name().equalsIgnoreCase("holidays") 
					|| !Q.running() 
					|| (M==null) 
					|| (!CMLib.flags().isInTheGame(M,true))
					|| (!CMLib.flags().canAccess(mob, M.location())) )
					{
						continue;
					}
					seeks.add(Q);
				}
				if(seeks.size()==0)
					mob.tell("You receive no visions of seekers.");
				else
				{
					while(seeks.size() > numSeekers)
						seeks.remove(CMLib.dice().roll(1, seeks.size(), -1));
					String starting;
					switch(CMLib.dice().roll(1, 10, 0))
					{
					case 1: starting="The visions show an image of "; break;
					case 2: starting="You see "; break;
					case 3: starting="You receive divine feelings of "; break;
					case 4: starting="A voice tells you of"; break;
					case 5: starting="Someone whispers about"; break;
					case 6: starting="It is revealed to you that"; break;
					case 7: starting="In your visions, you see "; break;
					case 8: starting="In your mind you hear about"; break;
					case 9: starting="Your spirit tells you about"; break;
					default: starting="You know of"; break;
					}
					final StringBuilder message=new StringBuilder(starting);
					for(int p=0;p<seeks.size();p++)
					{
						final Quest Q=seeks.get(p);
						final MOB M=Q.getQuestMob(1);
						if((p==seeks.size()-1)&&(p>0))
							message.append(", and");
						else
						if(p>0)
							message.append(",");
						message.append(" ").append(M.name()).append(" in \"").append(M.location().getArea().name()).append("\"");
					}
					message.append(".");
					mob.tell(message.toString());
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<T-NAME> "+prayWord(mob)+", but nothing is revealed.");

		return success;
	}
}
