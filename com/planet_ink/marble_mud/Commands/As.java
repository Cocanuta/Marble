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
public class As extends StdCommand
{
	public As(){}

	private final String[] access={"AS"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("As whom do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AS))||(mob.isMonster()))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		final Session mySession=mob.session();
		MOB M=CMLib.players().getLoadPlayer(cmd);
		if(M==null)
			M=mob.location().fetchInhabitant(cmd);
		if(M==null)
		{
			try
			{
				List<MOB> targets=CMLib.map().findInhabitants(CMLib.map().rooms(), mob, cmd, 50);
				if(targets.size()>0) 
					M=(MOB)targets.get(CMLib.dice().roll(1,targets.size(),-1));
			}
			catch(NoSuchElementException e){}
		}
		if(M==null)
		{
			mob.tell("You don't know of anyone by that name.");
			return false;
		}
		if(M.soulMate()!=null)
		{
			mob.tell(M.Name()+" is being possessed at the moment.");
			return false;
		}
		if((CMSecurity.isASysOp(M))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!M.isMonster())
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ORDER))
			{
				mob.tell("You can't do things as players if you can't order them.");
				return false;
			}
		}
		if(M==mob)
		{
			if(((String)commands.firstElement()).equalsIgnoreCase("here")
			   ||((String)commands.firstElement()).equalsIgnoreCase("."))
			{
				commands.removeElementAt(0);
			}
			M.doCommand(commands,metaFlags|Command.METAFLAG_AS);
			return false;
		}
		Room oldRoom=M.location();
		boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
		boolean dead=M.amDead();
		final Session hisSession=M.session();
		synchronized(mySession)
		{
			//int myBitmap=mob.getBitmap();
			//int oldBitmap=M.getBitmap();
			M.setSession(mySession);
			mySession.setMob(M);
			M.setSoulMate(mob);
			//mySession.initTelnetMode(oldBitmap);
			if(((String)commands.firstElement()).equalsIgnoreCase("here")
			   ||((String)commands.firstElement()).equalsIgnoreCase("."))
			{
				if((M.location()!=mob.location())&&(!mob.location().isInhabitant(M)))
					mob.location().bringMobHere(M,false);
				commands.removeElementAt(0);
			}
			if(dead) M.bringToLife();
			if((M.location()==null)&&(oldRoom==null)&&(mob.location()!=null))
			{
				inside=false;
				mob.location().bringMobHere(M,false);
			}
		}
		CMLib.s_sleep(100);
		M.doCommand(commands,metaFlags|Command.METAFLAG_AS);
		synchronized(mySession)
		{
			if(M.playerStats()!=null) M.playerStats().setLastUpdated(0);
			if((oldRoom!=null)&&(inside)&&(!oldRoom.isInhabitant(M)))
				oldRoom.bringMobHere(M,false);
			else
			if((oldRoom==null)||(!inside))
			{
				if(M.location()!=null)
					M.location().delInhabitant(M);
				M.setLocation(oldRoom);
			}
			M.setSoulMate(null);
			M.setSession(hisSession);
			mySession.setMob(mob);
		}
		CMLib.s_sleep(100);
		//mySession.initTelnetMode(myBitmap);
		if(dead) M.removeFromGame(true,true);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.AS);}

	
}
