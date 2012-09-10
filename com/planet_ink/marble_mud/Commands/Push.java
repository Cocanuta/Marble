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
public class Push extends Go
{
	public Push(){}

	private final String[] access={"PUSH"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Environmental pushThis=null;
		String dir="";
		int dirCode=-1;
		Environmental E=null;
		if(commands.size()>1)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
			{
				if((mob.location().getRoomInDir(dirCode)==null)
				||(mob.location().getExitInDir(dirCode)==null)
				||(!mob.location().getExitInDir(dirCode).isOpen()))
				{
					mob.tell("You can't push anything that way.");
					return false;
				}
				E=mob.location().getRoomInDir(dirCode);
				dir=" "+Directions.getDirectionName(dirCode);
				commands.removeElementAt(commands.size()-1);
			}
		}
		if(dir.length()==0)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
				pushThis=mob.location().getExitInDir(dirCode);
		}
		String itemName=CMParms.combine(commands,1);
		if(pushThis==null)
			pushThis=mob.location().fetchFromRoomFavorItems(null,itemName);
		if(pushThis==null)
			pushThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Wearable.FILTER_ANY);

		if((pushThis==null)||(!CMLib.flags().canBeSeenBy(pushThis,mob)))
		{
			mob.tell("You don't see '"+itemName+"' here.");
			return false;
		}
		int malmask=(pushThis instanceof MOB)?CMMsg.MASK_MALICIOUS:0;
		String msgStr = "<S-NAME> push(es) <T-NAME>"+dir+".";
		CMMsg msg=CMClass.getMsg(mob,pushThis,E,CMMsg.MSG_PUSH|malmask,msgStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((dir.length()>0)&&(msg.tool() instanceof Room))
			{
				Room R=(Room)msg.tool();
				if(R.okMessage(mob,msg))
				{
					dirCode=CMLib.tracking().findRoomDir(mob,R);
					if(dirCode>=0)
					{
						if(msg.othersMessage().equals(msgStr))
							msg.setOthersMessage("<S-NAME> push(es) <T-NAME> into here.");
						R.sendOthers(mob,msg);
						if(pushThis instanceof Item)
							R.moveItemTo((Item)pushThis,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
						else
						if(pushThis instanceof MOB)
							CMLib.tracking().walk((MOB)pushThis,dirCode,((MOB)pushThis).isInCombat(),false,true,true);
					}
				}
			}
					
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	
}
