package com.planet_ink.marble_mud.Libraries;
import java.util.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.MOB;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.threads.CMSupportThread;

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
public class Sessions extends StdLibrary implements SessionsList
{
	public String ID(){return "Sessions";}
	private CMSupportThread thread=null;
	private volatile long lastSweepTime = System.currentTimeMillis(); 
	
	public SLinkedList<Session> all=new SLinkedList<Session>();
	private final static Filterer<Session> localOnlineFilter=new Filterer<Session>(){
		public boolean passesFilter(Session obj) { 
			if((obj!=null) && (!obj.isStopped()) && (obj.getStatus()==Session.STATUS_OK))
			{
				MOB M=obj.mob();
				return ((M!=null)&&M.amActive()&&(CMLib.flags().isInTheGame(M,true)));
			}
			return false;
		}
	};
	
	public CMSupportThread getSupportThread() { return thread;}
	
	public Iterator<Session> all(){return all.iterator();}
	public Iterable<Session> allIterable(){return all;}
	public Iterator<Session> localOnline(){
		return new FilteredIterator<Session>(all.iterator(),localOnlineFilter);
	}
	public Iterable<Session> localOnlineIterable(){
		return new FilteredIterable<Session>(all,localOnlineFilter);
	}
	
	public int getCountAll()
	{
		return getCount(all());
	}
	
	public int getCountLocalOnline()
	{
		return getCount(localOnline());
	}
	
	protected int getCount(Iterator<Session> i)
	{
		int xt=0;
		for(;i.hasNext();)
		{
			i.next();
			xt++;
		}
		return xt;
	}
	
	public Session getAllSessionAt(int index)
	{
		return getAllSessionAt(all(),index);
	}
	
	protected Session getAllSessionAt(Iterator<Session> i, int index)
	{
		int xt=0;
		Session S;
		for(;i.hasNext();)
		{
			S=i.next();
			if(xt==index)
				return S;
			xt++;
		}
		return null;
	}
	
	public synchronized void add(Session s)
	{
		if(!all.contains(s))
			all.add(s);
	}
	
	public synchronized void remove(Session s)
	{
		all.remove(s);
	}
	
	public void stopSessionAtAllCosts(Session S)
	{
		if(S==null) return;
		S.stopSession(true,true,false);
		if(all.contains(S))
		{
			S.run();
			if(all.contains(S))
			{
				S.stopSession(true,true,true);
				remove(S);
			}
		}
	}
	
	public boolean activate() 
	{
		if(thread==null)
			thread=new CMSupportThread("THSessions"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					100, this, CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD), CMSecurity.DisFlag.SESSIONTHREAD);
		if(!thread.isStarted())
		{
			thread.setStatus("sleeping");
			thread.disableDBCheck();
			thread.start();
		}
		return true;
	}
	
	public boolean shutdown() 
	{
		thread.shutdown();
		return true;
	}
	
	public MOB findPlayerOnline(String srchStr, boolean exactOnly)
	{
		Session S=findPlayerSessionOnline(srchStr, exactOnly);
		if(S==null) return null;
		return S.mob();
	}
	
	public Session findPlayerSessionOnline(String srchStr, boolean exactOnly)
	{
		// then look for players
		for(Session S : localOnlineIterable())
			if(S.mob().Name().equalsIgnoreCase(srchStr))
				return S;
		for(Session S : localOnlineIterable())
			if(S.mob().name().equalsIgnoreCase(srchStr))
				return S;
		// keep looking for players
		if(!exactOnly)
		{
			for(Session S : localOnlineIterable())
				if(CMLib.english().containsString(S.mob().Name(),srchStr))
					return S;
			for(Session S : localOnlineIterable())
				if(CMLib.english().containsString(S.mob().name(),srchStr))
					return S;
		}
		return null;
	}
	
	public void run()
	{
		final double numThreads=(double)all.size();
		if(numThreads>0.0)
		{
			final double milliSleep = 10.0 / numThreads;
			final double floorMilliSleep = Math.floor(milliSleep);
			final long millis=Math.round(floorMilliSleep);
			final int nanos=(int)Math.round((milliSleep - floorMilliSleep) * 100000.0);
			try
			{
				for(Session S : all)
					if(!S.isRunning()) 
					{
						CMLib.threads().executeRunnable(S);
						Thread.sleep(millis, nanos);
					}
			}
			catch(InterruptedException ioe)
			{
				
			}
		}
		
		if(((System.currentTimeMillis() - lastSweepTime) < MudHost.TIME_UTILTHREAD_SLEEP)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.UTILITHREAD))
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.SESSIONTHREAD)))
			return;
		lastSweepTime = System.currentTimeMillis();
		thread.setStatus("checking player sessions.");
		for(Session S : all)
		{
			long time=System.currentTimeMillis()-S.lastLoopTime();
			if(time>0)
			{
				if((S.mob()!=null)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
				{
					long check=60000;

					if((S.previousCMD()!=null)
					&&(S.previousCMD().size()>0)
					&&(((String)S.previousCMD().get(0)).equalsIgnoreCase("IMPORT")
					   ||((String)S.previousCMD().get(0)).equalsIgnoreCase("EXPORT")
					   ||((String)S.previousCMD().get(0)).equalsIgnoreCase("CHARGEN")
					   ||((String)S.previousCMD().get(0)).equalsIgnoreCase("MERGE")))
						check=check*600;
					else
					if((S.mob()!=null)&&(CMSecurity.isAllowed(S.mob(),S.mob().location(),CMSecurity.SecFlag.CMDROOMS)))
						check=check*15;
					else
					if(S.getStatus()==Session.STATUS_LOGIN)
						check=check*5;

					if(time>(check*10))
					{
						String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
						if((S.previousCMD()==null)||(S.previousCMD().size()==0)||(S.getStatus()==Session.STATUS_LOGIN)||(S.getStatus()==Session.STATUS_ACCOUNTMENU))
							Log.sysOut(thread.getName(),"Kicking out: "+((S.mob()==null)?"Unknown":S.mob().Name())+" who has spent "+time+" millis out-game.");
						else
						{
							Log.errOut(thread.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
							Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
							if(S instanceof Thread)
								thread.debugDumpStack("Sessions",(Thread)S);
						}
						thread.setStatus("killing session ");
						stopSessionAtAllCosts(S);
						thread.setStatus("checking player sessions.");
					}
					else
					if(time>check)
					{
						if((S.mob()==null)||(S.mob().Name()==null)||(S.mob().Name().length()==0))
							stopSessionAtAllCosts(S);
						else
						if((S.previousCMD()!=null)&&(S.previousCMD().size()>0))
						{
							String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
							if((S.isLockedUpWriting())
							&&(CMLib.flags().isInTheGame(S.mob(),true)))
							{
								Log.errOut(thread.getName(),"LOGGED OFF Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time+": "+S.isLockedUpWriting());
								stopSessionAtAllCosts(S);
							}
							else
								Log.errOut(thread.getName(),"Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
							if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
								Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
							else
								Log.errOut(thread.getName(),"STATUS  is :"+S.getStatus()+", no last command available.");
						}
					}
				}
				else
				if(time>(60000))
				{
					String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
					if(S.getStatus()==Session.STATUS_LOGIN)
						Log.sysOut(thread.getName(),"Kicking out login session after "+time+" millis.");
					else
					{
						Log.errOut(thread.getName(),"KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
						if(S instanceof Thread)
							thread.debugDumpStack("Sessions",(Thread)S);
					}
					if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
						Log.errOut(thread.getName(),"STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					thread.setStatus("killing session ");
					stopSessionAtAllCosts(S);
					thread.setStatus("checking player sessions");
				}
			}
		}
		
	}
}
