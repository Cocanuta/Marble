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
public class ResourceMgr extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("RESOURCE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("RESOURCE");
			return "";
		}
		else
		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			for(Iterator<String> k=Resources.findResourceKeys("");k.hasNext();)
			{
				String key=k.next();
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!key.equals(lastID))))
				{
					httpReq.addRequestParameters("RESOURCE",key);
					return "";
				}
				lastID=key;
			}
			httpReq.addRequestParameters("RESOURCE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		else
		if(parms.containsKey("DELETE"))
		{
			String key=httpReq.getRequestParameter("RESOURCE");
			if((key!=null)&&(Resources.getResource(key)!=null))
			{
				Resources.removeResource(key);
				return "Resource '"+key+"' deleted.";
			}
			return "<!--EMPTY-->";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}
	
}
