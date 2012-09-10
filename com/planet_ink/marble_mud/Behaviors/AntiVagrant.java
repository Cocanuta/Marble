package com.planet_ink.marble_mud.Behaviors;
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
public class AntiVagrant extends ActiveTicker
{
	public String ID(){return "AntiVagrant";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	protected int speakDown=3;
	protected MOB target=null;
	protected boolean kickout=false;
	protected boolean anywhere=false;

	public AntiVagrant()
	{
		super();
		minTicks=2; maxTicks=3; chance=99;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "vagrant disliking";
	}

	public void setParms(String parms)
	{
		kickout=parms.toUpperCase().indexOf("KICK")>=0;
		anywhere=parms.toUpperCase().indexOf("ANYWHERE")>=0;
		super.setParms(parms);
	}

	public void wakeVagrants(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		if(anywhere||(observer.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))
		{
			if(target!=null)
			if(CMLib.flags().isSleeping(target)&&(target!=observer)&&(CMLib.flags().canBeSeenBy(target,observer)))
			{
				CMLib.commands().postSay(observer,target,"Damn lazy good for nothing!",false,false);
				CMMsg msg=CMClass.getMsg(observer,target,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> shake(s) <T-NAME> awake.");
				if(observer.location().okMessage(observer,msg))
				{
					observer.location().send(observer,msg);
					target.tell(observer.name()+" shakes you awake.");
					CMLib.commands().postStand(target,true);
					if((kickout)&&(CMLib.flags().isStanding(target)))
						CMLib.tracking().beMobile(target,true,false,false,false,null,null);
				}
			}
			else
			if((CMLib.flags().isSitting(target)&&(target!=observer))&&(CMLib.flags().canBeSeenBy(target,observer)))
			{
				CMLib.commands().postSay(observer,target,"Get up and move along!",false,false);
				CMMsg msg=CMClass.getMsg(observer,target,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stand(s) <T-NAME> up.");
				if(observer.location().okMessage(observer,msg))
				{
					observer.location().send(observer,msg);
					CMLib.commands().postStand(target,true);
					if((kickout)&&(CMLib.flags().isStanding(target)))
						CMLib.tracking().beMobile(target,true,false,false,false,null,null);
				}
			}
			target=null;
			for(int i=0;i<observer.location().numInhabitants();i++)
			{
				MOB mob=observer.location().fetchInhabitant(i);
				if((mob!=null)
				&&(mob!=observer)
				&&((CMLib.flags().isSitting(mob))||(CMLib.flags().isSleeping(mob)))
				&&(CMLib.flags().canBeSeenBy(mob,observer)))
				{
				   target=mob;
				   break;
				}
			}
		}
	}


	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		// believe it or not, this is for arrest behavior.
		super.executeMsg(affecting,msg);
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().toUpperCase().indexOf("SIT")>=0))
			speakDown=3;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB) return true;

		// believe it or not, this is for arrest behavior.
		if(speakDown>0)	{	speakDown--;return true;	}

		if((canFreelyBehaveNormal(ticking))&&(canAct(ticking,tickID)))
		{
			MOB mob=(MOB)ticking;
			wakeVagrants(mob);
			return true;
		}
		return true;
	}
}
