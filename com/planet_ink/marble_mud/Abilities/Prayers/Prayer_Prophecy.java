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
public class Prayer_Prophecy extends Prayer
{
	public String ID() { return "Prayer_Prophecy"; }
	public String name(){ return "Prophecy";}
	public String displayText(){ return "(In a Prophetic Trance)";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}

	public void unInvoke()
	{
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		if((mob.amDead())||(this.tickDown>0)||(mob.isInCombat()))
		{
			if(mob.location()!=null)
				mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL,"<S-NAME> end(s) <S-HIS-HER> trance.");
			super.unInvoke();
			return;
		}
		
		int numProphesies=super.getXLEVELLevel(mob) + 2;
		if(CMLib.ableMapper().qualifyingLevel(mob, this)>1)
			numProphesies += (super.adjustedLevel(mob, 0) / CMLib.ableMapper().qualifyingLevel(mob, this));
		List<Pair<Integer,Quest>> prophesies=new Vector<Pair<Integer,Quest>>();
		for(Enumeration<Quest> q = CMLib.quests().enumQuests(); q.hasMoreElements();)
		{
			Quest Q = q.nextElement();
			if( Q.isCopy() || (Q.duration()==0) ||(Q.name().equalsIgnoreCase("holidays")))
			{
				continue;
			}
			int ticksRemaining=Integer.MAX_VALUE;
			if(Q.waiting())
			{
				ticksRemaining = Q.waitRemaining();
				if(ticksRemaining<=0)
					ticksRemaining=Integer.MAX_VALUE;
			}
			else
			if(Q.running())
			{
				ticksRemaining = Q.ticksRemaining();
				if(ticksRemaining<=0)
					ticksRemaining=Integer.MAX_VALUE;
			}
			if(ticksRemaining != Integer.MAX_VALUE)
			{
				if(prophesies.size()<numProphesies)
					prophesies.add(new Pair<Integer,Quest>(Integer.valueOf(ticksRemaining),Q));
				else
				{
					Pair<Integer,Quest> highP=null;
					for(Pair<Integer,Quest> P : prophesies)
						if((highP==null)||(P.first.intValue()>highP.first.intValue()))
							highP=P;
					if((highP==null)||(highP.first.intValue() > ticksRemaining))
					{
						prophesies.remove(highP);
						prophesies.add(new Pair<Integer,Quest>(Integer.valueOf(ticksRemaining),Q));
					}
				}
			}
		}
		if(prophesies.size()==0)
			mob.tell("You receive no prophetic visions.");
		else
		{
			TimeClock clock =CMLib.time().localClock(mob);
			String starting;
			switch(CMLib.dice().roll(1, 10, 0))
			{
			case 1: starting="The visions say that "; break;
			case 2: starting="You see that"; break;
			case 3: starting="You feel that"; break;
			case 4: starting="A voice tells you that"; break;
			case 5: starting="Someone whispers that"; break;
			case 6: starting="It is revealed to you that"; break;
			case 7: starting="In your visions, you see that"; break;
			case 8: starting="In your mind you hear that"; break;
			case 9: starting="Your spirit tells you that"; break;
			default: starting="You prophesy that"; break;
			}
			final StringBuilder message=new StringBuilder(starting);
			for(int p=0;p<prophesies.size();p++)
			{
				Pair<Integer,Quest> P=prophesies.get(p);
				Quest Q=P.second;
				String name=Q.name();
				final long timeTil= P.first.longValue() * CMProps.getTickMillis();
				final String timeTilDesc = clock.deriveEllapsedTimeString(timeTil);
				final String possibleBetterName=Q.getStat("DISPLAY");
				if(possibleBetterName.length()>0)
					name=possibleBetterName;
				name=name.replace('_',' ');
				Vector<String> V=CMParms.parseSpaces(name,true);
				for(int v=V.size()-1;v>=0;v--)
				{
					if(CMath.isNumber(V.get(v)))
						V.remove(v);
				}
				name=CMParms.combineWithQuotes(V, 0);
				final String ending;
				if(Q.running())
					ending=" will end in ";
				else
					ending=" will begin in ";
				if((p==prophesies.size()-1)&&(p>0))
					message.append(", and");
				else
				if(p>0)
					message.append(",");
				message.append(" \"").append(name).append("\"").append(ending).append(timeTilDesc);
			}
			message.append(".");
			mob.tell(message.toString());
		}
		super.unInvoke();
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				unInvoke();
				mob.recoverPhyStats();
			}
			else
			if((abilityCode()==0)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
			&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
			&&(msg.othersMajor()>0))
			{
				if(msg.othersMajor(CMMsg.MASK_SOUND))
				{
					unInvoke();
					mob.recoverPhyStats();
				}
				else
				switch(msg.othersMinor())
				{
				case CMMsg.TYP_SPEAK:
				case CMMsg.TYP_CAST_SPELL:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					{
						unInvoke();
						mob.recoverPhyStats();
					}
					break;
				}
			}
		}
	}
	
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),auto?"":"^S<T-NAME> "+prayWord(mob)+", entering a divine trance.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,3);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<T-NAME> "+prayWord(mob)+", but nothing happens.");

		return success;
	}
}
