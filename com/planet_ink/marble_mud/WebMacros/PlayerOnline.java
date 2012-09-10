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
public class PlayerOnline extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public static final int MAX_IMAGE_SIZE=50*1024;
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		String last=httpReq.getRequestParameter("PLAYER");
		java.util.Map<String,String> parms=parseParms(parm);
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			if(parms.size()==0)
			{
				MOB M = CMLib.players().getPlayer(last);
				return String.valueOf((M!=null)&&(M.session()!=null)&&(!M.session().isStopped()));
			} 
			else 
			{
				MOB M=CMLib.players().getLoadPlayer(last);
				if(M==null)
				{
					MOB authM=Authenticate.getAuthenticatedMob(httpReq);
					if((authM!=null)&&(authM.Name().equalsIgnoreCase(last)))
						M=authM;
				}
				if(M!=null)
				{
					String login=Authenticate.getLogin(httpReq);
					if(Authenticate.authenticated(httpReq,login,Authenticate.getPassword(httpReq)))
					{
						boolean canBan=false;
						boolean canModify=false;
						boolean canBoot=false;
						
						MOB authM=CMLib.players().getLoadPlayer(login);
						if((authM!=null)&&(authM.Name().equals(M.Name())))
						{
							canBan=true;
							canModify=true;
							canBoot=true;
						}
						else
						if(authM!=null)
						{
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BAN))
								canBan=true;
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.CMDPLAYERS))
								canModify=true;
							if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BOOT))
								canBoot=true;
						}
						
						if(canBan&&(parms.containsKey("BANBYNAME")))
							CMSecurity.ban(last);
						if(canBan&&(parms.containsKey("BANBYIP")))
							CMSecurity.ban(M.session().getAddress());
						if(canModify&&(parms.containsKey("DELIMG")))
						{
							if(M.rawImage().length()>0)
							{
								M.setImage("");
								CMLib.database().DBUpdatePlayerMOBOnly(M);
							}
						}
						if(canBan&&(parms.containsKey("BANBYEMAIL")))
							CMSecurity.ban(M.playerStats().getEmail());
						if(canModify&&(parms.containsKey("NEWIMAGE")))
						{
							Resources.removeResource("CMPORTRAIT-"+M.Name());
							String file=httpReq.getRequestParameter("FILE");
							if(file==null) file="";
							byte[] buf=(byte[])httpReq.getRequestObjects().get("FILE");
							if(file.length()==0) return "File not uploaded -- no name!";
							if(file.toUpperCase().endsWith(".GIF")
							||file.toUpperCase().endsWith(".JPG")
							||file.toUpperCase().endsWith(".JPEG")
							||file.toUpperCase().endsWith(".BMP"))
							{
								if(buf==null) return "File `"+file+"` not uploaded -- no buffer!";
								if(buf.length>MAX_IMAGE_SIZE) return "File `"+file+"` not uploaded -- size exceeds "+MAX_IMAGE_SIZE+" byte limit!";
								String encoded=B64Encoder.B64encodeBytes(buf);
								M.setImage("PlayerPortrait?PLAYER="+M.Name()+"&FILENAME="+M.Name()+System.currentTimeMillis()+file);
								CMLib.database().DBUpdatePlayerMOBOnly(M);
								CMLib.database().DBReCreateData(M.Name(),"CMPORTRAIT","CMPORTRAIT-"+M.Name(),encoded);
								Resources.submitResource("CMPORTRAIT-"+M.Name(),buf);
								return "Image successfully uploaded.";
							}
							return "File not uploaded -- wrong type!";
						}
						
						if(M.session()!=null)
						{
							if(canBoot&&(parms.containsKey("BOOT")))
							{
								M.session().stopSession(false,false,false);
								return "false";
							}
							return "true";
						}
					}
				}
			}
		}
		return "false";
	}
}
