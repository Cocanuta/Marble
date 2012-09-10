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
public class AddRandomFileFromDir extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		if((parms==null)||(parms.size()==0)) return "";
		StringBuffer buf=new StringBuffer("");
		Vector fileList=new Vector();
		boolean LINKONLY=false;
		for(String val : parms.values())
			if(val.equalsIgnoreCase("LINKONLY"))
				LINKONLY=true;
		for(String filePath : parms.values())
		{
			if(filePath.equalsIgnoreCase("LINKONLY")) continue;
			CMFile directory=httpReq.grabFile(filePath);
			if((!filePath.endsWith("/"))&&(!filePath.endsWith("/")))
				filePath+="/";
			if((directory!=null)&&(directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
					fileList.addElement(filePath+list[l]);
			}
			else
				Log.sysOut("AddRFDir","Directory error: "+filePath);
		}
		if(fileList.size()==0) 
			return buf.toString();
		if(LINKONLY)
			buf.append((String)fileList.elementAt(CMLib.dice().roll(1,fileList.size(),-1)));
		else
			buf.append(httpReq.getPageContent((String)fileList.elementAt(CMLib.dice().roll(1,fileList.size(),-1))));
		return buf.toString();
	}
}
