package com.planet_ink.marble_mud.core.intermud.cm1;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.threads.CMThreadFactory;
import com.planet_ink.marble_mud.core.threads.CMThreadPoolExecutor;
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
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

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
public class CM1Server extends Thread
{
	private String		name = "";
	private int 		port = 27755;
	private boolean 	shutdownRequested = false;
	private boolean 	isShutdown = false;
	private Selector	servSelector = null;
	private ServerSocketChannel	
						servChan = null;
	private SHashtable<SocketChannel,RequestHandler> 
						handlers = new SHashtable<SocketChannel,RequestHandler>();
	private String		iniFile;
	private CMProps 	page;
	
	
	
	public CM1Server(String serverName, String iniFile)
	{
		super(serverName);
		if(!loadPropPage(iniFile))
			throw new IllegalArgumentException();
		int serverPort = page.getInt("PORT");
		this.iniFile=iniFile;
		name=serverName+"@"+serverPort;
		setName(name);
		port=serverPort;
		shutdownRequested = false;
	}
	
	public String getINIFilename() { return iniFile;}
	
	protected boolean loadPropPage(String iniFile)
	{
		if (page==null || !page.isLoaded())
		{
			page=new CMProps (iniFile);
			if(!page.isLoaded())
			{
				Log.errOut(getName(),"failed to load " + iniFile);
				return false;
			}
		}
		return true;
	}
	
	public void run()
	{
		while(!shutdownRequested)
		{
			try
			{
				servChan = ServerSocketChannel.open();
				ServerSocket serverSocket = servChan.socket();
				servSelector = Selector.open();
				if((page.getStr("BIND")!=null)&&(page.getStr("BIND").trim().length()>0))
					serverSocket.bind (new InetSocketAddress(InetAddress.getByName(page.getStr("BIND")),port));
				else
					serverSocket.bind (new InetSocketAddress (port));
				Log.sysOut("CM1Server","Started "+name+" on port "+port);
				servChan.configureBlocking (false);
				servChan.register (servSelector, SelectionKey.OP_ACCEPT);
				shutdownRequested = false;
				while (!shutdownRequested)
				{
					try
					{
					   int n = servSelector.select();
					   if (n == 0) continue;
					   
					   Iterator<SelectionKey> it = servSelector.selectedKeys().iterator();
					   while (it.hasNext()) 
					   {
						  SelectionKey key = it.next();
						  if (key.isAcceptable()) 
						  {
							 ServerSocketChannel server = (ServerSocketChannel) key.channel();
							 SocketChannel channel = server.accept();
							 if (channel != null) 
							 {
								RequestHandler handler=new RequestHandler(channel,page.getInt("IDLETIMEOUTMINS"));
								channel.configureBlocking (false);
								channel.register (servSelector, SelectionKey.OP_READ, handler);
								handlers.put(channel,handler);
								handler.sendMsg("CONNECTED TO "+name.toUpperCase());
							 } 
							 //sayHello (channel);
						  }
						  try
						  {
							  if (key.isReadable()) 
							  {
								  RequestHandler handler = (RequestHandler)key.attachment();
								  if((!handler.isRunning())&&(!handler.needsClosing()))
							  		  CMLib.threads().executeRunnable(handler);
							  }
						  }
						  finally
						  {
							  it.remove();
						  }
						}
						for(SocketChannel schan : handlers.keySet())
							try
							{
								RequestHandler handler=handlers.get(schan);
								if((handler!=null)&&(handler.needsClosing()))
									handler.shutdown();
							}
							catch(Exception e){}
					}
					catch(CancelledKeyException t)
					{
						// ignore
					}
				}
			}
			catch(Exception t)
			{
				Log.errOut("CM1Server",t);
			}
			finally
			{
				if(servSelector != null)
					try {servSelector.close();}catch(Exception e){}
				if(servChan != null)
					try {servChan.close();}catch(Exception e){}
				for(SocketChannel schan : handlers.keySet())
					try
					{
						RequestHandler handler=handlers.get(schan);
						if(handler!=null)handler.shutdown();
					}
					catch(Exception e){}
				handlers.clear();
				Log.sysOut("CM1Server","Shutdown complete");
			}
		}
		isShutdown = true;
	}
	
	public void shutdown()
	{
		shutdownRequested = true;
		long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (!isShutdown))
		{
			try {Thread.sleep(1000);}catch(Exception e){}
			if(servSelector != null)
				try {servSelector.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
			if((servChan != null)&&(!isShutdown))
				try {servChan.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
			this.interrupt();
		}
	}
}
