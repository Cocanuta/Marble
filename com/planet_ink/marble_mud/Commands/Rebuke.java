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
public class Rebuke extends StdCommand
{
	public Rebuke(){}

	private final String[] access={"REBUKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Rebuke whom?");
			return false;
		}
		String str=CMParms.combine(commands,1);
		MOB target=mob.location().fetchInhabitant(str);
		if((target==null)&&(mob.getWorshipCharID().length()>0)
		&&(CMLib.english().containsString(mob.getWorshipCharID(),str)))
			target=CMLib.map().getDeity(str);
		if((target==null)&&(mob.getLiegeID().length()>0)
		&&(CMLib.english().containsString(mob.getLiegeID(),str)))
			target=CMLib.players().getLoadPlayer(mob.getLiegeID());
		if((target==null)&&(mob.numFollowers()>0))
			target=mob.fetchFollower(str);
		
		if(target==null)
		{
			mob.tell("You don't see anybody called '"+CMParms.combine(commands,1)+"' or you aren't serving '"+CMParms.combine(commands,1)+"'.");
			return false;
		}

		CMMsg msg=null;
		msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_REBUKE,"<S-NAME> rebuke(s) "+target.Name()+".");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		if((target.amFollowing()==mob)&&(target.location()!=null))
		{
			Room R=target.location();
			msg=CMClass.getMsg(target,target.amFollowing(),null,CMMsg.MSG_NOFOLLOW,"<S-NAME> stop(s) following <T-NAMESELF>.");
			// no room OKaffects, since the damn leader may not be here.
			if(target.okMessage(mob,msg))
				R.send(mob,msg);
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return false;}

	
}
