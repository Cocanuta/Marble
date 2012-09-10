package com.planet_ink.marble_mud.core.http;
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
import com.planet_ink.marble_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;
import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
   Portions Copyright 2002 Jeff Kamenek
   Portions Copyright 2002-2012 Bo Zimmerman

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
public class HTTPserver extends Thread implements MudHost
{
	private static final float HOST_VERSION_MAJOR=(float)1.0;
	private static final float HOST_VERSION_MINOR=(float)0.3;
	private static final String[] STATUS_STRINGS={"waiting","processing","done"};
	private static CMProps webCommon=null;

	// this gets sent in HTTP response
	//  also used by @WEBSERVERVERSION@
	public final static String getServerVersionString()
	{
		return "marblemud HTTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR; 
	}

	private final long 	startupTime = System.currentTimeMillis();
	private CMProps 	 page=null;
	private boolean 	 isOK = false;
	private boolean 	 isAdminServer = false;
	private ServerSocket servsock=null;
	private MudHost 	 mud;
	private String 		 partialName;
	private int 		 state=0;
	private int 		 myPort=27744;
	private int 		 myServerNumber=0;
	private boolean 	 acceptConnections=true;
	private int 		 maxThreads = 10;
	private int 		 maxTimeoutMins = 45;
	private String 		 serverDir = null;
	private String 		 serverTemplateDir = null;
	private FileGrabber	 pageGrabber=new FileGrabber(this);
	private FileGrabber	 templateGrabber=new FileGrabber(this);
	private CMThreadPoolExecutor threadPool;

	public HTTPserver(MudHost a_mud, String a_name, int num)
	{
		super("HTTP"+a_name+((num>0)?""+(num+1):""));
		partialName = a_name;		//name without prefix
		mud = a_mud;
		myServerNumber=num;
		setDaemon(true);
		if (!initServer(num))
			isOK = false;
		else
			isOK = true;
		threadPool = new CMThreadPoolExecutor("HTTP"+a_name+((num>0)?""+(num+1):""),0, maxThreads, 30, TimeUnit.SECONDS, maxTimeoutMins, 256);
	}

	public String getPartialName()	{return partialName;}
	public MudHost getMUD()	{return mud;}
	public String getServerDir() {return serverDir;}
	public String getServerTemplateDir() {return serverTemplateDir;}
	public long getUptimeSecs() { return (System.currentTimeMillis()-startupTime)/1000;}
	public FileGrabber getPageGrabber() { return pageGrabber;}
	public FileGrabber getTemplateGrabber() { return templateGrabber;}

	public Properties getCommonPropPage()
	{
		if (webCommon==null || !webCommon.isLoaded())
		{
			webCommon=new CMProps ("web/common.ini");
			if(!webCommon.isLoaded())
				Log.errOut("HTTPserver","Unable to load common.ini!");
		}
		return webCommon;
	}

	protected boolean initServer(int which)
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"ERROR: HTTPserver unable to read ini file.");
			return false;
		}

		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: PORT");
			return false;
		}
		if(which>0)
		{
			Vector V=CMParms.parseCommas(page.getStr("PORT"),true);
			if(which>=V.size())
			{
				Log.errOut(getName(),"ERROR: not enough PORT entries to support #"+(which+1));
				return false;
			}
		}
		if (page.getStr("DEFAULTFILE").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: DEFAULTFILE");
			return false;
		}
		if (page.getStr("VIRTUALPAGEEXTENSION").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: VIRTUALPAGEEXTENSION");
			return false;
		}

		if (page.getStr("BASEDIRECTORY").length()==0)
		{
			serverDir = "web/" + partialName;
		}
		else
		{
			serverDir = page.getStr("BASEDIRECTORY");
		}

		// don't want any trailing / chars
		serverDir = FileGrabber.fixDirName(serverDir);

		if (!pageGrabber.setBaseDirectory(serverDir))
		{
			Log.errOut(getName(),"Could not set server base directory: "+serverDir);
			return false;
		}

		if (page.getStr("TEMPLATEDIRECTORY").length()==0)
			serverTemplateDir = serverDir + ".templates";
		else
			serverTemplateDir = page.getStr("TEMPLATEDIRECTORY");

		if (CMath.isNumber(page.getStr("MAXTHREADS")))
			maxThreads=CMath.s_int(page.getStr("MAXTHREADS"));
		
		if (CMath.isNumber(page.getStr("REQUESTTIMEOUTMINS")))
			maxTimeoutMins=CMath.s_int(page.getStr("REQUESTTIMEOUTMINS"));
		
		// don't want any trailing / chars
		serverTemplateDir = FileGrabber.fixDirName(serverTemplateDir);

		if (!templateGrabber.setBaseDirectory(serverTemplateDir))
		{
			Log.errOut(getName(),"Could not set server template directory: "+serverTemplateDir);
			return false;
		}

		addVirtualDirectories();


		return true;
	}

	public Map<String, String> getVirtualDirectories(){return pageGrabber.getVirtualDirectories();}

	private void addVirtualDirectories()
	{
		for (Enumeration e = page.keys() ; e.hasMoreElements() ;)
		{
			String s = (String) e.nextElement();

			// nb: hard-coded!
			if (s.startsWith("MOUNT/"))
			{
				// nb: hard-coded! - leaves in '/'
				String v = s.substring(5);

				pageGrabber.addVirtualDirectory(v,page.getStr(s));
			}
		}
	}

	protected boolean loadPropPage()
	{
		if (page==null || !page.isLoaded())
		{
			String fn = "web/" + getPartialName() + ".ini";
			page=new CMProps(getCommonPropPage(), fn);
			if(!page.isLoaded())
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}

		return true;
	}

	public void acceptConnection(Socket sock) 
		throws SocketException, IOException
	{
		if(acceptConnections)
		{
			while(CMLib.threads().isAllSuspended()) {
				try { Thread.sleep(1000); } catch(Exception e) { throw new IOException(e.getMessage());}
			}
			state=1;
			ProcessHTTPrequest W=new ProcessHTTPrequest(sock,this,page,isAdminServer);
			threadPool.execute(W);
		}
	}
	
	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.isLoaded()))
		{
			Log.errOut(getName(),"ERROR: HTTPserver will not run with no properties. WebServer shutting down.");
			isOK = false;
			return;
		}


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


		if (page.getStr("ADMIN") != null && page.getStr("ADMIN").equalsIgnoreCase("true"))
			isAdminServer = true;
		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(getName(),"ERROR: Could not bind to address " + page.getStr("BIND"));
			}
		}

		serverOK = true;
		try
		{
			Vector allports=CMParms.parseCommas(page.getStr("PORT"),true);
			myPort=CMath.s_int((String)allports.elementAt(myServerNumber));
			servsock=new ServerSocket(myPort, q_len, bindAddr);
			setName(getName()+"@"+myPort);
			Log.sysOut(getName(),"Started on port: "+myPort);
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());
		}
		catch(Exception t)
		{
			Log.errOut(getName(),t);
			serverOK=false;
			isOK=false;
		}
		
		while(isOK && serverOK)
		{
			try
			{
				state=0;
				sock=servsock.accept();
				acceptConnection(sock);
			} 
			catch(Exception t) 
			{
				if((t!=null)&&(t.getMessage()!=null)&&(!t.getMessage().equals("null")))
					Log.errOut(getName(),t.getMessage());
				try 
				{
					if(servsock==null)
					{
						sock = null;
						break;
					}
					if(servsock.isClosed())
					{
						Log.sysOut(getName(),"Reconnecting.");
						servsock=new ServerSocket(myPort, q_len, bindAddr);
					}
				} 
				catch(Exception e) { Log.errOut(getName(),e); }
			}
			sock = null;
		}
		state=2;
		try
		{
			if(servsock!=null)
				servsock.close();
		}
		catch(IOException e)
		{
		}

		//Log.sysOut(getName(),"Thread stopped!");
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt

	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		isOK=false;
		if (S != null)
			S.println( getName() + " shutting down.");
		if(threadPool.getActiveCount()>0)
			threadPool.shutdownNow();
		else
			threadPool.shutdown();
		try{ this.servsock.close(); Thread.sleep(100); }catch(Exception e){}
		CMLib.killThread(this,500,1);
	}

	public void shutdown()	{shutdown(null);}



	// interrupt does NOT interrupt the ServerSocket.accept() call...
	//  override it so it does
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				//jef: we MUST set it to null
				// (so run() can tell it was interrupted & didn't have an error)
				servsock = null;
			}
			catch(IOException e)
			{
			}
		}
		super.interrupt();
	}

	public int totalPorts()
	{
		return CMParms.parseCommas(page.getStr("PORT"),true).size();
	}
	public int getPort()
	{
		return myPort;
	}

	public String getPortStr()
	{
		return page.getStr("PORT");
	}
	public String getHost(){return getName();}
	public void shutdown(Session S, boolean keepItDown, String externalCommand){
		shutdown(S);
	}
	public String getStatus()
	{
		return STATUS_STRINGS[state];
	}
	public void setAcceptConnections(boolean truefalse){ acceptConnections=truefalse;}
	public boolean isAcceptingConnections(){ return acceptConnections;}

	public String getLanguage() 
	{
		String lang = CMProps.instance().getStr("LANGUAGE").toUpperCase().trim();
		if(lang.length()==0) return "English";
		for(int i=0;i<LanguageLibrary.ISO_LANG_CODES.length;i++)
			if(lang.equals(LanguageLibrary.ISO_LANG_CODES[i][0]))
				return LanguageLibrary.ISO_LANG_CODES[i][1];
		return "English";
	}

	public List<Runnable> getOverdueThreads()
	{
		Vector<Runnable> V=new Vector();
		V.addAll(threadPool.getTimeoutOutRuns(Integer.MAX_VALUE));
		return V;
	}

	public String executeCommand(String cmd)
		throws Exception
	{
		throw new Exception("Not implemented");
	}
}
