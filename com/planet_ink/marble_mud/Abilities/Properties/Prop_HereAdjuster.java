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
public class Prop_HereAdjuster extends Prop_HaveAdjuster
{
	public String ID() { return "Prop_HereAdjuster"; }
	public String name(){ return "Adjustments to stats when here";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	
	public String accountForYourself()
	{
		return super.fixAccoutingsWithMask("Affects on those here: "+parameters[0],parameters[1]);
	}

	public int triggerMask() { return TriggeredAffect.TRIGGER_ENTER; }
	
	public boolean canApply(MOB mob)
	{
		if(affected==null) return true;
		if((mob.location()!=affected)
		||((mask!=null)&&(!CMLib.masking().maskCheck(mask,mob,false))))
			return false;
		return true;
	}
}
