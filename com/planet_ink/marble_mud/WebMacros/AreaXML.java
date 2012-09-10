package com.planet_ink.marble_mud.WebMacros;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
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
import com.planet_ink.marble_mud.core.exceptions.HTTPServerException;


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
public class AreaXML extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public boolean isAWebPath(){return true;}
	public boolean preferBinary(){return true;}
	public String getSpecialContentHeader(String filename){
		return "Content-Disposition: attachment; filename="+filename + "\r\n"
			  +"Content-Type: application/cmare" + "\r\n";
	}
	
	public String getFilename(ExternalHTTPRequests httpReq, String filename)
	{
		MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob==null) return "area.xml";
		Area pickedA=getLoggedArea(httpReq,mob);
		if(pickedA==null) return "area.xml";
		String fileName="";
		if(pickedA.getArchivePath().length()>0)
			fileName=pickedA.getArchivePath();
		else
			fileName=pickedA.Name();
		if(fileName.indexOf('.')<0)
			fileName=fileName+".cmare";
		return fileName;
	}
	
	protected Area getLoggedArea(ExternalHTTPRequests httpReq, MOB mob)
	{
		String AREA=httpReq.getRequestParameter("AREA");
		if(AREA==null) return null;
		if(AREA.length()==0) return null;
		Area A=CMLib.map().getArea(AREA);
		if(A==null) return null;
		if(CMSecurity.isASysOp(mob)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}
	
	public byte[] runBinaryMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
	{
		MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob==null) return null;
		Area pickedA=getLoggedArea(httpReq,mob);
		if(pickedA==null) return null;
		Command C=CMClass.getCommand("Export");
		if(C==null) return null;
		Object resultO=null;
		try
		{
			resultO=C.executeInternal(mob,0,"AREA","DATA","MEMORY",Integer.valueOf(4),null,pickedA,mob.location());
		}
		catch(Exception e)
		{
			return null;
		}
		if(!(resultO instanceof String)) 
			return null;
		return ((String)resultO).getBytes();
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
	{
		return "[Unimplemented string method!]";
	}
}
