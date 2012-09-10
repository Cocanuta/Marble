package com.planet_ink.marble_mud.core.intermud.cm1.commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.intermud.cm1.RequestHandler;
import com.planet_ink.marble_mud.core.intermud.cm1.commands.Listen.ListenCriterium;
import com.planet_ink.marble_mud.core.intermud.cm1.commands.Listen.Listener;
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
import com.planet_ink.marble_mud.Libraries.interfaces.ChannelsLibrary;
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
public class Monitor extends Listen
{
	public String getCommandWord(){ return "MONITOR";}
	
	public Monitor(RequestHandler req, String parameters) 
	{
		super(req, parameters);
	}
	
	protected void sendMsg(Listener listener, String msg) throws IOException
	{
		synchronized(listener)
		{
			listener.msgs.add(listener.channelName+": "+msg);
		}
	}
	
	public void run()
	{
		try
		{
			String name;
			String rest="";
			int x=parameters.indexOf(' ');
			if(x>0)
			{
				name=parameters.substring(0,x).trim();
				if(name.trim().length()==0)
					name=null;
				else
					rest=parameters.substring(x+1).trim();
			}
			else
				name=null;
			if(name==null)
			{
				req.sendMsg("[FAIL No "+getCommandWord()+"ER name given]");
				return;
			}
			List<ListenCriterium> crit=getCriterium(rest);
			if(crit==null)
				return;
			else
			if(crit.size()==0)
			{
				List<String> msgs=new LinkedList<String>();
				for(Listener l : listeners)
					if(l.channelName.equalsIgnoreCase(name))
					{
						synchronized(l)
						{
							for(Iterator<String> i = l.msgs.iterator();i.hasNext();)
							{
								String s=i.next();
								msgs.add(s);
								i.remove();
							}
						}
					}
				if(msgs.size()==0)
					req.sendMsg("[FAIL NONE]");
				else
				{
					req.sendMsg("[OK /MESSAGES:"+msgs.size()+"]");
					for(String s : msgs)
						req.sendMsg("[MESSAGE "+s+"]");
				}
			}
			else
			{
				Listener newListener = new Listener(name,crit.toArray(new ListenCriterium[0]));
				CMLib.commands().addGlobalMonitor(newListener);
				req.addDependent(newListener.channelName, newListener);
				listeners.add(newListener);
				req.sendMsg("[OK]");
			}
		}
		catch(Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
}
