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
public class ThinPlayerData extends StdWebMacro {
	
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(last==null) return " @break@";
		StringBuffer str=new StringBuffer("");
		if(last.length()>0)
		{
			String sort=httpReq.getRequestParameter("SORTBY");
			if(sort==null) sort="";
			PlayerLibrary.ThinPlayer player = null;
			Enumeration pe=CMLib.players().thinPlayers(sort, httpReq.getRequestObjects());
			for(;pe.hasMoreElements();)
			{
				PlayerLibrary.ThinPlayer TP=(PlayerLibrary.ThinPlayer)pe.nextElement();
				if(TP.name.equalsIgnoreCase(last))
				{
					player = TP; 
					break;
				}
			}
			if(player == null) return " @break@";
			for(String key : parms.keySet())
			{
				int x=CMLib.players().getCharThinSortCode(key.toUpperCase().trim(),false);
				if(x>=0)
				{
					String value = CMLib.players().getThinSortValue(player, x);
					if(PlayerLibrary.CHAR_THIN_SORT_CODES[x].equals("LAST"))
						value=CMLib.time().date2String(CMath.s_long(value));
					str.append(value+", ");
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}

}
