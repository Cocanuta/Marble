package com.planet_ink.marble_mud.Abilities.Properties;
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
public class Prop_NoPKill extends Property
{
	public String ID() { return "Prop_NoPKill"; }
	public String name(){ return "No Player Killing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MALICIOUS)))
			&&(msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(!((MOB)msg.target()).isMonster())
			&&(!msg.source().isMonster()))
		{
			if(CMath.s_int(text())==0)
			{
				msg.source().tell("Player killing is forbidden here.");
				msg.source().setVictim(null);
				return false;
			}
			int levelDiff=msg.source().phyStats().level()-((MOB)msg.target()).phyStats().level();
			if(levelDiff<0) levelDiff=levelDiff*-1;
			if(levelDiff>CMath.s_int(text()))
			{
				msg.source().tell("Player killing is forbidden for characters whose level difference is greater than "+CMath.s_int(text())+".");
				msg.source().setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
