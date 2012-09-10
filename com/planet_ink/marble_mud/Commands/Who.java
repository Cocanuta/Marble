package com.planet_ink.marble_mud.Commands;
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
public class Who extends StdCommand
{
	public Who(){}

	private final String[] access={"WHO","WH"};
	public String[] getAccessWords(){return access;}
	
	protected static final String shortHead=
		 "^x["
		+CMStrings.padRight("Race",12)+" "
		+CMStrings.padRight("Class",12)+" "
		+CMStrings.padRight("Level",7)
		+"] Character name^.^N\n\r";
		 
	
	public StringBuffer showWhoShort(MOB who)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			if(who.charStats().getCurrentClass().raceless())
				msg.append(CMStrings.padRight(" ",12)+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().raceName(),12)+" ");
		}
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(' ');
		if(x>=0) levelStr=levelStr.substring(x).trim();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		{
			if(who.charStats().getMyRace().classless())
				msg.append(CMStrings.padRight(" ",12)+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().displayClassName(),12)+" ");
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		{
			if(who.charStats().getMyRace().leveless()
			||who.charStats().getCurrentClass().leveless())
				msg.append(CMStrings.padRight(" ",7));
			else
				msg.append(CMStrings.padRight(levelStr,7));
		}
		String name=null;
		if(CMath.bset(who.phyStats().disposition(),PhyStats.IS_CLOAKED))
			name="("+(who.Name().equals(who.name())?who.titledName():who.name())+")";
		else
			name=(who.Name().equals(who.name())?who.titledName():who.name());
		if((who.session()!=null)&&(who.session().afkFlag()))
		{
			long t=(who.session().getIdleMillis()/1000);
			String s=t+"s";
			if(t>600)
			{
				t=t/60;
				s=t+"m";
				if(t>120)
				{
					t=t/60;
					s=t+"h";
					if(t>48)
					{
						t=t/24;
						s=t+"d";
					}
				}
			}
			name=name+(" (idle: "+s+")");
		}
		msg.append("] "+CMStrings.padRight(name,40));
		msg.append("\n\r");
		return msg;
	}
	
	public String getWho(MOB mob, Set<String> friends, String mobName)
	{
		StringBuffer msg=new StringBuffer("");
		for(Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();

			if((mob2!=null)
			&&((((mob2.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
				||((CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.WIZINV))&&(mob.phyStats().level()>=mob2.phyStats().level()))))
			&&((friends==null)||(friends.contains(mob2.Name())||(friends.contains("All"))))
			&&(mob2.phyStats().level()>0))
				msg.append(showWhoShort(mob2));
		}
		if((mobName!=null)&&(msg.length()==0))
			return "";
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
				head.append(CMStrings.padRight("Race",12)+" ");
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
				head.append(CMStrings.padRight("Class",12)+" ");
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
				head.append(CMStrings.padRight("Level",7));
			head.append("] Character name^.^N\n\r");
			head.append(msg.toString());
			return head.toString();
		}
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String mobName=CMParms.combine(commands,1);
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.startsWith("@")))
		{
			if((!(CMLib.intermud().i3online()))
			&&(!CMLib.intermud().imc2online()))
				mob.tell("Intermud is unavailable.");
			else
				CMLib.intermud().i3who(mob,mobName.substring(1));
			return false;
		}
		Set<String> friends=null;
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("friends"))
		&&(mob.playerStats()!=null))
		{
			friends=mob.playerStats().getFriends();
			mobName=null;
		}
		
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("pk")
		||mobName.equalsIgnoreCase("pkill")
		||mobName.equalsIgnoreCase("playerkill")))
		{
			friends=new HashSet();
			for(Session S : CMLib.sessions().allIterable())
			{
				MOB mob2=S.mob();
				if((mob2!=null)&&(CMath.bset(mob2.getBitmap(),MOB.ATT_PLAYERKILL)))
					friends.add(mob2.Name());
			}
		}

		String msg = getWho(mob,friends,mobName);
		if((mobName!=null)&&(msg.length()==0))
			mob.tell("That person doesn't appear to be online.\n\r");
		else
			mob.tell(msg);
		return false;
	}
	
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		return getWho(mob,null,null);
	}
	public boolean canBeOrdered(){return true;}
}
