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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Spell_HearThoughts extends Spell
{
	public String ID() { return "Spell_HearThoughts"; }
	public String name(){return "Hear Thoughts";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":"^S<S-NAME> concentrate(s) and listen(s) carefully!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final List<Room> rooms=CMLib.tracking().getRadiantRooms(mob.location(), new TrackingLibrary.TrackingFlags(), 50);
				final List<MOB> mobs=new LinkedList<MOB>();
				int numMobs= 8 + super.getXLEVELLevel(mob);
				for(Room R : rooms)
				{
					for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						MOB M=m.nextElement();
						if((numMobs>0)&&(M!=mob))
						{
							mobs.add(M);
							numMobs--;
						}
					}
					if(numMobs<=0)
						break;
				}
				rooms.clear();
				for(MOB target : mobs)
				{
					Room room=target.location();
					if(room==null) continue;
					String adjective="";
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=18)
						adjective+="massively intelligent, ";
					else
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=13)
						adjective+="very intelligent, ";
					else
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=10)
						adjective+="intelligent, ";
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=18)
						adjective+="incredibly wise, ";
					else
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=13)
						adjective+="very wise, ";
					else
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=10)
						adjective+="wise, ";
					mob.tell("Regarding "+target.Name()+", a "+adjective+target.charStats().getMyRace().name()+" "+target.charStats().getCurrentClass().name()+" at "+room.displayText()+":");
					StringBuilder thoughts=new StringBuilder("");
					LegalBehavior LB=CMLib.law().getLegalBehavior(target.location());
					Area AO=CMLib.law().getLegalObject(target.location());
					if((LB!=null)&&(AO!=null))
					{
						if(LB.isJudge(AO, target))
							thoughts.append("You detect the legalese thoughts of a judge.  ");
						else
						if(LB.isAnyOfficer(AO, target))
							thoughts.append("You detect the stern thoughts of a law officer.  ");
					}
					for(Enumeration<Behavior> b=target.behaviors();b.hasMoreElements();)
					{
						final Behavior B=b.nextElement();
						final String accounting=B.accountForYourself();
						if(accounting.length()==0) continue;
						String prefix;
						switch(CMLib.dice().roll(1, 4, 0))
						{
						case 1: prefix="You sense thoughts of "; break;
						case 2: prefix="You hear thoughts of "; break;
						case 3: prefix="You detect thoughts of "; break;
						default: prefix="You can see thoughts of "; break;
						}
						thoughts.append(prefix).append(accounting).append("  ");
					}
					if(thoughts.length()==0)
						mob.tell("You don't detect any other thoughts.\n\r");
					else
						mob.tell(thoughts.append("\n\r").toString());
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> concentrate(s), but look(s) frustrated.");

		// return whether it worked
		return success;
	}
}
