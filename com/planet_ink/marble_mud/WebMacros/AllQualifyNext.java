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
public class AllQualifyNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()   {return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ALLQUALID");
		if(parms.containsKey("RESET"))
		{   
			if(last!=null) httpReq.removeRequestParameter("ALLQUALID");
			return "";
		}
		String which=httpReq.getRequestParameter("ALLQUALWHICH");
		if(parms.containsKey("WHICH"))
			which=parms.get("WHICH");	
		if((which==null)||(which.length()==0)) which="ALL";
		Map<String,Map<String,AbilityMapper.AbilityMapping>> allQualMap=CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
		Map<String,AbilityMapper.AbilityMapping> map=allQualMap.get(which.toUpperCase().trim());
		if(map==null) return " @break@";
		
		String lastID="";
		String abilityID;
		for(Iterator<String> i=map.keySet().iterator();i.hasNext();)
		{
			abilityID=(String)i.next();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!abilityID.equalsIgnoreCase(lastID))))
			{
				httpReq.addRequestParameters("ALLQUALID",abilityID);
				return "";
			}
			lastID=abilityID;
		}
		httpReq.addRequestParameters("ALLQUALID","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
