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
public class RoomNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String area=httpReq.getRequestParameter("AREA");
		if((area==null)||(CMLib.map().getArea(area)==null))
			return " @break@";
		Area A=CMLib.map().getArea(area);
		String last=httpReq.getRequestParameter("ROOM");
		if(parms.containsKey("RESET"))
		{   
			if(last!=null) httpReq.removeRequestParameter("ROOM");
			return "";
		}
		String lastID="";
		
		for(Enumeration d=A.getProperRoomnumbers().getRoomIDs();d.hasMoreElements();)
		{
			String roomid=(String)d.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!roomid.equals(lastID))))
			{
				httpReq.addRequestParameters("ROOM",roomid);
				return "";
			}
			lastID=roomid;
		}
		httpReq.addRequestParameters("ROOM","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
