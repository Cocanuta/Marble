package com.planet_ink.marble_mud.Exits;
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
public class GapExit extends StdExit
{
	public String ID(){	return "GapExit";}
	public String Name(){ return "a crevasse";}
	public String description(){return "Looks like you'll have to jump it.";}

	public int mobWeight(MOB mob)
	{
		int weight=mob.basePhyStats().weight();
		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem(i);
			if((I!=null)&&(!I.amWearingAt(Wearable.WORN_FLOATING_NEARBY)))
				weight+=I.phyStats().weight();
		}
		return weight;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		MOB mob=msg.source();
		if(((msg.amITarget(this))||(msg.tool()==this))
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!CMLib.flags().isInFlight(mob))
		&&(!CMLib.flags().isFalling(mob)))
		{
			int chance=(int)Math.round(CMath.div(mobWeight(mob),mob.maxCarry())*(100.0-(3.0*mob.charStats().getStat(CharStats.STAT_STRENGTH))));
			if(CMLib.dice().rollPercentage()<chance)
			{
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to jump the crevasse, but miss(es) the far ledge!");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s)!!!!");
				CMLib.combat().postDeath(null,mob,null);
				return false;
			}
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		MOB mob=msg.source();
		if(((msg.amITarget(this))||(msg.tool()==this))
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!CMLib.flags().isInFlight(mob))
		&&(!CMLib.flags().isFalling(mob)))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> jump(s) the crevasse!");
	}
}
