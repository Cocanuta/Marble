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
@SuppressWarnings("rawtypes")
public class Activate extends StdCommand
{
	public Activate(){}

	private final String[] access={"ACTIVATE","ACT","A",">"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Activate what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		PhysicalAgent P=mob.location().fetchFromMOBRoomFavorsItems(mob,null,what,Wearable.FILTER_ANY);
		Item item=null;
		if(mob.riding() instanceof Electronics)
		{
			if((P==null)||(cmd.equalsIgnoreCase(">")))
				item=(Item)mob.riding();
		}
		else
			commands.removeElementAt(commands.size()-1);
		if((item==null)&&(P instanceof Electronics))
			item=(Item)P;
		if((P==null)||(!CMLib.flags().canBeSeenBy(P,mob)))
			mob.tell("You don't see anything called "+what+" here that you can activate.");
		else
		if(item==null)
			mob.tell("You can't activate "+P.name()+"'.");

		String rest=CMParms.combine(commands,0);
		CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_ACTIVATE,null,CMMsg.MSG_ACTIVATE,rest,CMMsg.MSG_ACTIVATE,null);
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isASysOp(mob);}

	
}
