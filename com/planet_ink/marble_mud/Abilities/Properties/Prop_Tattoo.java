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
@SuppressWarnings("rawtypes")
public class Prop_Tattoo extends Property
{
	public String ID() { return "Prop_Tattoo"; }
	public String name(){ return "A Tattoo";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}

	public static Vector getTattoos(MOB mob)
	{
		Vector tattos=new Vector();
		Ability A=mob.fetchAbility("Prop_Tattoo");
		if(A!=null)
			tattos=CMParms.parseSemicolons(A.text().toUpperCase(),true);
		else
		{
			A=mob.fetchEffect("Prop_Tattoo");
			if(A!=null)
				tattos=CMParms.parseSemicolons(A.text().toUpperCase(),true);
		}
		return tattos;
	}

	public void setMiscText(String text)
	{
		if(affected instanceof MOB)
		{
			MOB M=(MOB)affected;
			Vector V=CMParms.parseSemicolons(text,true);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				int x=s.indexOf(' ');
				if((x>0)&&(CMath.isNumber(s.substring(0,x))))
					M.addTattoo(new MOB.Tattoo(s.substring(x+1).trim(),CMath.s_int(s.substring(0,x))));
				else
					M.addTattoo(new MOB.Tattoo(s));
			}
		}
		savable=false;
	}
}
