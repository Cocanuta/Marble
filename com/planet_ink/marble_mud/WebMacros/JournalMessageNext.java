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
public class JournalMessageNext extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String journalName=httpReq.getRequestParameter("JOURNAL");
		if(journalName==null) 
			return " @break@";
		
		if(CMLib.journals().isArchonJournalName(journalName))
		{
			MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if((M==null)||(!CMSecurity.isASysOp(M)))
				return " @break@";
		}
		
		String srch=httpReq.getRequestParameter("JOURNALMESSAGESEARCH");
		if(srch!=null) 
			srch=srch.toLowerCase();
		String last=httpReq.getRequestParameter("JOURNALMESSAGE");
		int cardinal=CMath.s_int(httpReq.getRequestParameter("JOURNALCARDINAL"));
		if(parms.containsKey("RESET"))
		{	
			if(last!=null)
			{
				httpReq.removeRequestParameter("JOURNALMESSAGE");
				httpReq.removeRequestParameter("JOURNALCARDINAL");
			}
			return "";
		}
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		cardinal++;
		JournalsLibrary.JournalEntry entry = null;
		List<JournalsLibrary.JournalEntry> msgs = JournalInfo.getMessages(httpReq, journalName);
		while((entry==null)||(!CMLib.journals().canReadMessage(entry,srch,M,parms.containsKey("NOPRIV"))))
		{
			entry = JournalInfo.getNextEntry(msgs,last);
			if(entry==null)
			{
				httpReq.addRequestParameters("JOURNALMESSAGE","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}
			last=entry.key;
		}
		entry.cardinal=cardinal;
		httpReq.addRequestParameters("JOURNALCARDINAL",""+cardinal);
		httpReq.addRequestParameters("JOURNALMESSAGE",last);
		return "";
	}
}
