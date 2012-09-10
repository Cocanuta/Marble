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
public class Prop_RideResister extends Prop_HaveResister
{
	public String ID() { return "Prop_RideResister"; }
	public String name(){ return "Resistance due to riding";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS;}

	public String accountForYourself()
	{ return "Those mounted gain resistances: "+describeResistance(text());}

	public int triggerMask() { return TriggeredAffect.TRIGGER_MOUNT; }

	public boolean canResist(Environmental E)
	{
		if((affected instanceof Rideable)
		&&(E instanceof MOB)
		&&(((Rideable)affected).amRiding((MOB)E))
		&&(((MOB)E).location()!=null))
			return true;
		return false;
	}
}
