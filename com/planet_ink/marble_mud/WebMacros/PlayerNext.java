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
public class PlayerNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("PLAYER");
			return "";
		}
		String lastID="";
		String sort=httpReq.getRequestParameter("SORTBY");
		if(sort==null) sort="";
		Enumeration pe=CMLib.players().thinPlayers(sort,httpReq.getRequestObjects());
		for(;pe.hasMoreElements();)
		{
			PlayerLibrary.ThinPlayer user=(PlayerLibrary.ThinPlayer)pe.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!user.name.equals(lastID))))
			{
				httpReq.addRequestParameters("PLAYER",user.name);
				return "";
			}
			lastID=user.name;
		}
		httpReq.addRequestParameters("PLAYER","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
