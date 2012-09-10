package com.planet_ink.marble_mud.core.intermud.cm1.commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.intermud.cm1.RequestHandler;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

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
public class MUDInfo extends CM1Command
{
	public String getCommandWord(){ return "MUDINFO";}
	public MUDInfo(RequestHandler req, String parameters) {
		super(req, parameters);
	}

	public void run()
	{
		try
		{
			if((parameters.length()==0)||(parameters.equalsIgnoreCase("STATUS")))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.SYSTEM_MUDSTATUS)+"]");
			else
			if(parameters.equalsIgnoreCase("PORTS"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+"]");
			else
			if(parameters.equalsIgnoreCase("VERSION"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.SYSTEM_MUDVER)+"]");
			else
			if(parameters.equalsIgnoreCase("DOMAIN"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+"]");
			else
			if(parameters.equalsIgnoreCase("NAME"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+"]");
			else
				req.sendMsg("[FAIL "+getHelp(req.getUser(), null, null)+"]");
		}
		catch(java.io.IOException ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target){return true;}
	public String getHelp(MOB user, PhysicalAgent target, String rest)
	{
		return "USAGE: MUDINFO STATUS, PORTS, VERSION, DOMAIN, NAME";
	}
}
