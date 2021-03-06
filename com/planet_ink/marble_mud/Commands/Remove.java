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
public class Remove extends StdCommand
{
	public Remove(){}

	private final String[] access={"REMOVE","REM"};
	public String[] getAccessWords(){return access;}



	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Remove what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Item)
		{
			boolean quiet=((commands.size()>1)&&(commands.lastElement() instanceof String)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")));
			Item item=(Item)commands.firstElement();
			CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,quiet?null:"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				return true;
			}
			return false;
		}

		List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_WORNONLY,false);
		if(items.size()==0)
			mob.tell("You don't seem to be wearing that.");
		else
		for(int i=0;i<items.size();i++)
		{
			Item item=(Item)items.get(i);
			CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	
}
