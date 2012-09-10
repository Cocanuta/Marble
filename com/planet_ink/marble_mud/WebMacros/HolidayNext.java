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
@SuppressWarnings({"unchecked","rawtypes"})
public class HolidayNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()   {return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("HOLIDAY");
		if(parms.containsKey("RESET"))
		{   
			if(last!=null) httpReq.removeRequestParameter("HOLIDAY");
			return "";
		}
		Object resp=CMLib.quests().getHolidayFile();
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
		if(resp instanceof String)
			return (String)resp;
		else
			return "[Unknown error.]";
		Vector holidays=new Vector();
		List<String> line=null;
		String var=null;
		List<String> V=null;
		for(int s=1;s<steps.size();s++)
		{
			String step=(String)steps.get(s);
			V=Resources.getFileLineVector(new StringBuffer(step));
			List<List<String>> cmds=CMLib.quests().parseQuestCommandLines(V,"SET",0);
			//Vector areaLine=null;
			List<String> nameLine=null;
			for(int v=0;v<cmds.size();v++)
			{
				line=cmds.get(v);
				if(line.size()>1)
				{
					var=((String)line.get(1)).toUpperCase();
					//if(var.equals("AREAGROUP"))
					//{ areaLine=line;}
					if(var.equals("NAME"))
					{ nameLine=line;}
				}
			}
			if(nameLine!=null)
			{
				/*String areaName=null;
				if(areaLine==null)
					areaName="*special*";
				else
					areaName=CMParms.combineWithQuotes(areaLine,2);*/
				String name=CMParms.combine(nameLine,2);
				holidays.addElement(name);
			}
		}
		String lastID="";
		for(Enumeration q=holidays.elements();q.hasMoreElements();)
		{
			String holidayID=(String)q.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!holidayID.equalsIgnoreCase(lastID))))
			{
				httpReq.addRequestParameters("HOLIDAY",holidayID);
				return "";
			}
			lastID=holidayID;
		}
		httpReq.addRequestParameters("HOLIDAY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
