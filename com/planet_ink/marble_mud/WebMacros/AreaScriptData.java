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

import java.net.URLEncoder;
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
public class AreaScriptData extends AreaScriptNext 
{
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		
		String area=httpReq.getRequestParameter("AREA");
		if((area==null)||(area.length()==0)) return "@break@";
		String script=httpReq.getRequestParameter("AREASCRIPT");
		if((script==null)||(script.length()==0)) return "@break@";
		TreeMap<String,ArrayList<AreaScriptInstance>> list = getAreaScripts(httpReq,area);
		ArrayList<AreaScriptInstance> subList = list.get(script);
		if(subList == null) return " @break@";
		AreaScriptInstance entry = null;
		String last=httpReq.getRequestParameter("AREASCRIPTHOST");
		if((last!=null)&&(last.length()>0))
		{
			for(AreaScriptInstance inst : subList)
			{
				String hostName = CMParms.combineWith(inst.path, '.',0, inst.path.size()) + "." + inst.fileName;
				if(hostName.equalsIgnoreCase(last))
				{
					entry=inst;
					break;
				}
			}
		}
		else
			entry=(subList.size()>0)?subList.get(0):null;
			
		if(parms.containsKey("NEXT")||parms.containsKey("RESET"))
		{
			if(parms.containsKey("RESET"))
			{
				if(last!=null) httpReq.removeRequestParameter("AREASCRIPTHOST");
				return "";
			}
			String lastID="";
			for(AreaScriptInstance inst : subList)
			{
				String hostName = CMParms.combineWith(inst.path, '.',0, inst.path.size()) + "." + inst.fileName;
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!hostName.equals(lastID))))
				{
					httpReq.addRequestParameters("AREASCRIPTHOST",hostName);
					last=hostName;
					return "";
				}
				lastID=hostName;
			}
			httpReq.addRequestParameters("AREASCRIPTHOST","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		
		StringBuilder str = new StringBuilder("");
		
		if(parms.containsKey("NUMHOSTS"))
			str.append(subList.size()+", ");
		
		if(parms.containsKey("FILE") && (entry != null))
			str.append(Resources.makeFileResourceName(entry.fileName)+", ");
		
		if(parms.containsKey("RESOURCEKEY") && (entry != null))
			str.append(entry.instanceKey+", ");
		
		if(parms.containsKey("RESOURCEKEYENCODED") && (entry != null))
			try{
				str.append(URLEncoder.encode(entry.instanceKey,"UTF-8")+", ");
			}catch(Exception e){}
		
		if(parms.containsKey("PATH") && (entry != null))
		{
			String path=Resources.makeFileResourceName(entry.fileName);
			int x=path.lastIndexOf('/');
			str.append(((x<0)?"":path.substring(0,x))+", ");
		}

		if(parms.containsKey("ENTRYPATH") && (entry != null))
			str.append(CMParms.combineWith(entry.path, '.',0, entry.path.size())+", ");
		
		if(parms.containsKey("CUSTOMSCRIPT") && (entry != null))
			try{
				String s = entry.customScript;
				if(parms.containsKey("PARSELED") && (s.trim().length()>0))
				{
					StringBuffer st=new StringBuffer("");
					List<List<String>> V = CMParms.parseDoubleDelimited(s,'~',';');
					for(List<String> LV : V)
					{
						for(String L : LV)
							st.append(L+"\n\r");
						st.append("\n\r");
					}
					s=st.toString();
				}
				str.append(webify(new StringBuffer(s))+", ");
			}catch(Exception e){}
		
		if(parms.containsKey("FILENAME") && (entry != null))
		{
			String path=Resources.makeFileResourceName(entry.fileName);
			int x=path.lastIndexOf('/');
			str.append(((x<0)?path:path.substring(x+1))+", ");
		}
		
		if(parms.containsKey("ENCODEDPATH") && (entry != null))
		{
			String path=Resources.makeFileResourceName(entry.fileName);
			int x=path.lastIndexOf('/');
			try{
				str.append(URLEncoder.encode(((x<0)?"":path.substring(0,x)),"UTF-8")+", ");
			}catch(Exception e){}
		}
		
		if(parms.containsKey("ENCODEDFILENAME") && (entry != null))
		{
			String path=Resources.makeFileResourceName(entry.fileName);
			int x=path.lastIndexOf('/');
			try{
				str.append(URLEncoder.encode(((x<0)?path:path.substring(x+1)),"UTF-8")+", ");
			}catch(Exception e){}
		}
		
		if(parms.containsKey("ROOM") && (entry != null) && (entry.path.size()>1))
			str.append(entry.path.get(1)+", ");
		
		if(parms.containsKey("AREA") && (entry != null))
			str.append(entry.path.get(0)+", ");
		
		if(parms.containsKey("SCRIPTKEY") && (entry != null))
			str.append(entry.instanceKey+", ");
		
		if(parms.containsKey("CLEARRESOURCE") && (entry != null))
			Resources.removeResource(entry.instanceKey);
		
		if(parms.containsKey("ISCUSTOM") && (entry != null))
			str.append(entry.key.equalsIgnoreCase("Custom")+", ");
		
		if(parms.containsKey("ISFILE") && (entry != null))
			str.append(!entry.key.equalsIgnoreCase("Custom")+", ");
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
