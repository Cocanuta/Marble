package com.planet_ink.marble_mud.WebMacros;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class ClassRaceNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String cclass=httpReq.getRequestParameter("CLASS");
		if(cclass.trim().length()==0) return " @break@";
		CharClass C=CMClass.getCharClass(cclass.trim());
		if(C==null) return " @break";
		String last=httpReq.getRequestParameter("RACE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("RACE");
			return "";
		}
		String lastID="";
		MOB mob=CMClass.getFactoryMOB();
		for(int i: CharStats.CODES.BASE())
			mob.baseCharStats().setStat(i,25);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		for(Enumeration r=CMClass.races();r.hasMoreElements();)
		{
			Race R=(Race)r.nextElement();
			mob.baseCharStats().setMyRace(R);
			mob.recoverCharStats();
			if(((CMProps.isTheme(R.availabilityCode())&&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
				||(parms.containsKey("ALL")))
			&&(C.qualifiesForThisClass(mob,true)))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!R.ID().equals(lastID))))
				{
					httpReq.addRequestParameters("RACE",R.ID());
					mob.destroy();
					return "";
				}
				lastID=R.ID();
			}
		}
		mob.destroy();
		httpReq.addRequestParameters("RACE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
