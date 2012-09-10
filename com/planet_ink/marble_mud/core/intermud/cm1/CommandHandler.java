package com.planet_ink.marble_mud.core.intermud.cm1;
import com.planet_ink.marble_mud.core.interfaces.*;
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
import com.planet_ink.marble_mud.core.intermud.cm1.commands.*;
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
@SuppressWarnings("unchecked")
public class CommandHandler implements Runnable
{
	private String cmd;
	private String rest;
	private RequestHandler req; 
	private static final Map<String,Class<? extends CM1Command>> commandList=new Hashtable<String,Class<? extends CM1Command>>();
	private static final void AddCommand(Class<? extends CM1Command> c) throws InstantiationException, IllegalAccessException 
	{ 
		CM1Command c1 = CM1Command.newInstance(c,null,"");
		commandList.put(c1.getCommandWord(),c);
	}
	
	static
	{
		String className=CommandHandler.class.getName();
		String packageName=className;
		int x=packageName.lastIndexOf('.');
		if(x>0)
			packageName=packageName.substring(0,x)+".commands.";
		if (!className.startsWith("/"))
			className = "/" + className;
		
		className = className.replace('.', '/');
		className = className + ".class";

		URL classUrl = CommandHandler.class.getClass().getResource(className);
		if (classUrl != null) 
		{
		   String temp = classUrl.getFile();
		   if (temp.startsWith("file:")) {
			  temp=temp.substring(5);
		   }
		   x=temp.lastIndexOf('/');
		   if(x>0)
		   {
			   File dir=new File(temp.substring(0,x)+"/commands");
			   if((dir.exists())&&(dir.isDirectory()))
				   for(File F : dir.listFiles())
					   if(F.getName().endsWith(".class")
					   &&(!F.getName().equals("CM1Command.class"))
					   &&(F.getName().indexOf('$')<0))
					   {
						   String name=packageName + F.getName().substring(0,F.getName().length()-6);
						   try
						   {
							   AddCommand((Class<? extends CM1Command>)CMClass.instance().loadClass(name,true));
						   }
						   catch(Exception e)
						   {
							   e.printStackTrace();
						   }
					   }
		   }
		}
	}
	
	public CommandHandler(RequestHandler req, String command)
	{
		this.req=req;
		int x=command.indexOf(' ');
		if(x<0)
		{
			cmd=command;
			rest="";
		}
		else
		{
			cmd=command.substring(0,x).trim();
			rest=command.substring(x+1).trim();
		}
		
	}

	public void run() 
	{
		if(cmd.length()>0)
		{
			try
			{
				if(cmd.equalsIgnoreCase("HELP"))
				{
					if(rest.length()>0)
					{
						Class<? extends CM1Command> commandClass = commandList.get(rest.toUpperCase().trim());
						CM1Command command = CM1Command.newInstance(commandClass, req, rest);
						if((command == null) || (!command.passesSecurityCheck(req.getUser(), req.getTarget())))
							req.sendMsg("[FAIL UNKNOWN: "+rest.toUpperCase().trim()+"]");
						else
							req.sendMsg("[OK "+command.getCommandWord()+" "+command.getHelp(req.getUser(), req.getTarget(), rest)+"]");
					}
					else
					{
						StringBuilder str=new StringBuilder("[OK HELP");
						for(String cmdWord : commandList.keySet())
						{
							Class<? extends CM1Command> commandClass = commandList.get(cmdWord);
							CM1Command command = CM1Command.newInstance(commandClass, req, "");
							if(command.passesSecurityCheck(req.getUser(), req.getTarget()))
								str.append(" "+cmdWord);
						}
						str.append("]");
						req.sendMsg(str.toString());
					}
					return;
				}
				Class<? extends CM1Command> commandClass = commandList.get(cmd.toUpperCase().trim());
				CM1Command command = CM1Command.newInstance(commandClass, req, rest);
				if((command == null) || (!command.passesSecurityCheck(req.getUser(), req.getTarget())))
					req.sendMsg("[UNKNOWN "+cmd.toUpperCase().trim()+"]");
				else
					command.run();
			}
			catch(java.io.IOException ioe)
			{
				req.close();
			}
		}
	}
	
	
}
