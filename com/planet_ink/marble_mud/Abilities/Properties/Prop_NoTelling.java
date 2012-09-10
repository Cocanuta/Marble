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
public class Prop_NoTelling extends Property
{
	public String ID() { return "Prop_NoTelling"; }
	public String name(){ return "Tel Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public String accountForYourself()
	{ return "No Telling Field";	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;


		if((msg.sourceMinor()==CMMsg.TYP_TELL)
		&&((!(affected instanceof MOB))||(msg.source()==affected)))
		{
			if(affected instanceof MOB)
				msg.source().tell("Your message drifts into oblivion.");
			else
				msg.source().tell("This is a no-tell area.");
			return false;
		}
		return true;
	}
}
