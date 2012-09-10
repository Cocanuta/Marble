package com.planet_ink.marble_mud.Abilities.Songs;
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
public class Dance_Morris extends Dance
{
	public String ID() { return "Dance_Morris"; }
	public String name(){ return "Morris";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected String danceOf(){return name()+" Dance";}
	private boolean missedLastOne=false;
	
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==invoker) return;
		affectableStats.setArmor(affectableStats.armor()+(2*adjustedLevel(invoker(),0)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(2*adjustedLevel(invoker(),0)));
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;
		if(affected==invoker) return true;

		MOB mob=(MOB)affected;
		// preventing distracting player from doin anything else
		if(msg.amISource(mob)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(!missedLastOne)
		&&(CMLib.dice().rollPercentage()>mob.charStats().getSave(CharStats.STAT_SAVE_MIND)))
		{
			missedLastOne=true;
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> become(s) distracted.");
			return false;
		}
		missedLastOne=false;
		return super.okMessage(myHost,msg);
	}

}
