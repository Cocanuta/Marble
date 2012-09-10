package com.planet_ink.marble_mud.core.smtp;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.threads.CMThreadFactory;
import com.planet_ink.marble_mud.core.threads.CMThreadPoolExecutor;
import com.planet_ink.marble_mud.core.threads.ServiceEngine;
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
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import com.planet_ink.marble_mud.core.exceptions.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.JournalsLibrary.JournalEntry;

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
public class SMTPserver extends Thread implements Tickable
{
	public static final float  HOST_VERSION_MAJOR=(float)1.1;
	public static final float  HOST_VERSION_MINOR=(float)1.0;
	public static final String ServerVersionString = "marblemud SMTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;
	
	public String ID(){return "SMTPserver";}
	public String name(){return "SMTPserver";}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new SMTPserver(mud);}}
	public void initializeClass(){}
	public CMObject copyOf(){try{return (SMTPserver)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
	public long 		tickStatus=STATUS_NOT;
	public long 		lastAllProcessing=System.currentTimeMillis();
	public boolean 		isOK = false;
	private MudHost 	mud;
	public CMProps 		page=null;
	public ServerSocket servsock=null;
	public CMProps 		iniPage=null;
	private boolean 	displayedBlurb=false;
	private String 		domain="marblemud";
	private int			maxThreads = 3;
	private int			threadTimeoutMins = 10;
	private HashSet<String> 		 oldEmailComplaints=new HashSet<String>();
	private CMThreadPoolExecutor  	 threadPool;

	public SMTPserver()
	{
		super("SMTP"); 
		mud=null;
		isOK=false;
		threadPool = new CMThreadPoolExecutor("SMTP", 0, 3, 30, TimeUnit.SECONDS, 5, 256);
		threadPool.setThreadFactory(new CMThreadFactory("SMTP"));
		setDaemon(true);
	}
	
	public SMTPserver(MudHost a_mud)
	{
		super("SMTP");
		mud = a_mud;

		if (!initServer())
			isOK = false;
		else
			isOK = true;
		threadPool = new CMThreadPoolExecutor("SMTP", 0, maxThreads, 30, TimeUnit.SECONDS, threadTimeoutMins,256);
		setDaemon(true);
	}

	public long getTickStatus(){return tickStatus;}
	public MudHost getMUD()	{return mud;}
	public String domainName(){return domain;}
	public String mailboxName(){return CMProps.getVar(CMProps.SYSTEM_MAILBOX);}

	public Properties getCommonPropPage()
	{
		if (iniPage==null || !iniPage.isLoaded())
		{
			iniPage=new CMProps ("web/common.ini");
			if(!iniPage.isLoaded())
				Log.errOut("SMTPserver","Unable to load common.ini!");
		}
		return iniPage;
	}

	protected boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"SMTPserver unable to read ini file.");
			return false;
		}

		if (CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase().length()==0)
		{
			Log.errOut(getName(),"Set your marblemud.ini parameter: DOMAIN");
			return false;
		}
		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"Set your marblemud.ini parameter: PORT");
			return false;
		}
		if(CMath.isNumber(page.getStr("REQUESTTIMEOUTMINS")))
			threadTimeoutMins=CMath.s_int(page.getStr("REQUESTTIMEOUTMINS"));
		
		if(CMath.isNumber(page.getStr("MAXTHREADS")))
			maxThreads=CMath.s_int(page.getStr("MAXTHREADS"));

		domain=CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase();
		String mailbox=page.getStr("MAILBOX");
		if(mailbox==null) mailbox="";
		CMProps.setVar(CMProps.SYSTEM_MAILBOX,mailbox.trim());
		CMProps.setIntVar(CMProps.SYSTEMI_MAXMAILBOX,getMaxMsgs());

		CMProps.setBoolVar(CMProps.SYSTEMB_EMAILFORWARDING,CMath.s_bool(page.getStr("FORWARD")));

		if (!displayedBlurb)
		{
			displayedBlurb = true;
			//Log.sysOut(getName(),"SMTPserver (C)2005-2012 Bo Zimmerman");
		}
		if(mailbox.length()==0)
			Log.sysOut(getName(),"Player mail box system is disabled.");

		return true;
	}
	
	public TreeMap<String, JournalsLibrary.SMTPJournal> parseJournalList(String journalStr)
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=new TreeMap<String, JournalsLibrary.SMTPJournal>(); 
		if((journalStr==null)||(journalStr.length()>0))
		{
			Vector<String> V=CMParms.parseCommas(journalStr,true);
			if(V.size()>0)
			{
				for(int v=0;v<V.size();v++)
				{
					String s=((String)V.elementAt(v)).trim();
					String parm="";
					int x=s.indexOf('(');
					if((x>0)&&(s.endsWith(")")))
					{
						parm=s.substring(x+1,s.length()-1).trim();
						s=s.substring(0,x).trim();
					}
					Vector<String> PV=CMParms.parseSpaces(parm,true);
					StringBuffer crit=new StringBuffer("");
					boolean forward=false;
					boolean subscribeOnly=false;
					boolean keepAll=false;
					for(int pv=0;pv<PV.size();pv++)
					{
						String ps=(String)PV.elementAt(pv);
						if(ps.equalsIgnoreCase("forward"))
							forward=true;
						else
						if(ps.equalsIgnoreCase("subscribeonly"))
							subscribeOnly=true;
						else
						if(ps.equalsIgnoreCase("keepall"))
							keepAll=true;
						else
							crit.append(s+" ");
					}
					set.put(s.toUpperCase().trim(), 
							new JournalsLibrary.SMTPJournal(s, forward, subscribeOnly, keepAll, crit.toString().trim()));
				}
			}
		}
		return set;
	}

	public String getAnEmailJournal(String journal)
	{
		journal=CMStrings.replaceAll(journal,"_"," ");
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.name : null;
	}
	@SuppressWarnings("unchecked")
	public TreeMap<String, JournalsLibrary.SMTPJournal> getJournalSets()
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=(TreeMap<String, JournalsLibrary.SMTPJournal>)
															Resources.getResource("SYSTEM_SMTP_JOURNALS");
		if(set==null)
		{
			set=parseJournalList(page.getStr("JOURNALS"));
			Resources.submitResource("SYSTEM_SMTP_JOURNALS", set);
		}
		return set;
	}
	
	public JournalsLibrary.SMTPJournal getAJournal(String journal)
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=getJournalSets();
		if(set==null) return null;
		return set.get(journal.toUpperCase().trim());
	}
	public boolean isAForwardingJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.forward : false;
	}
	public boolean isASubscribeOnlyJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.subscribeOnly : false;
	}
	public boolean isAKeepAllJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.keepAll : false;
	}
	public MaskingLibrary.CompiledZapperMask getJournalCriteria(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.criteria : null;
	}

	protected boolean loadPropPage()
	{
		if (page==null || !page.isLoaded())
		{
			String fn = "web/email.ini";
			page=new CMProps (getCommonPropPage(), fn);
			if(!page.isLoaded())
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}
		return true;
	}

	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.isLoaded()))
		{
			Log.errOut(getName(),"ERROR: SMTPserver will not run with no properties. Shutting down.");
			isOK = false;
			return;
		}


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


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

		try
		{
			setName(getName()+"@"+page.getInt("PORT"));
			servsock=new ServerSocket(page.getInt("PORT"), q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+page.getInt("PORT"));
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());


			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				while(CMLib.threads().isAllSuspended())
					Thread.sleep(1000);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SMTPSERVER))
					Log.debugOut("SMTPserver","Connection received: "+sock.getInetAddress().getHostAddress());
				if(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
					threadPool.execute(new ProcessSMTPrequest(sock,this));
				else
				{
					sock.getOutputStream().write(("421 Mud down.. try later.\r\n").getBytes());
					sock.getOutputStream().flush();
					sock.close();
				}
				sock=null;
			}
		}
		catch(Exception e)
		{
			// if we've been interrupted, servsock will be null and serverOK will be true
			Log.errOut(getName(),e.getMessage());
			// this prevents initHost() from running if run() has failed (eg socket in use)
			if (!serverOK)
				isOK = false;
		}

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt
	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		try{servsock.close(); Thread.sleep(100);}catch(Exception e){}
		threadPool.shutdown();
		if(getTickStatus()==Tickable.STATUS_NOT)
			tick(this,Tickable.TICKID_READYTOSTOP);
		else
		{
			int att=0;
			while((att<100)&&(getTickStatus()!=Tickable.STATUS_NOT))
			{try{att++;Thread.sleep(100);}catch(Exception e){}}
		}
		threadPool.shutdownNow();
		CMLib.killThread(this,1000,30);
	}

	public void shutdown()	{shutdown(null);}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickStatus!=STATUS_NOT) return true;

		boolean updatedMailingLists=false;
		tickStatus=STATUS_START;
		if((tickID==Tickable.TICKID_READYTOSTOP)||(tickID==Tickable.TICKID_EMAIL))
		{
			MassMailer massMailer = new MassMailer(page,domain,oldEmailComplaints);
			
			TreeMap<String, JournalsLibrary.SMTPJournal> set=getJournalSets();
			
			// this is where it should attempt any mail forwarding
			// remember, a 5 day old private mail message is a goner
			// remember that new to all messages need to be parsed
			// for subscribe/unsubscribe and deleted, or then
			// forwarded to all members private boxes.  Lots of work to do!
			for(JournalsLibrary.SMTPJournal smtpJournal : set.values())
			{
				final String journalName=smtpJournal.name;
				if(smtpJournal.forward)
				{
					// Vector mailingList=?
					final List<JournalsLibrary.JournalEntry> msgs=CMLib.database().DBReadJournalMsgs(journalName);
					for(final JournalsLibrary.JournalEntry msg : msgs)
					{
						if(msg.to.equalsIgnoreCase("ALL"))
						{
							final String subj=msg.subj;
							final String msgStr=msg.msg.trim();
							if((subj.equalsIgnoreCase("subscribe"))
							||(msgStr.equalsIgnoreCase("subscribe")))
							{
								// add to mailing list
								updatedMailingLists= subscribeToJournal(journalName, msg) || updatedMailingLists;
							}
							else
							if((subj.equalsIgnoreCase("unsubscribe"))
							||(msgStr.equalsIgnoreCase("unsubscribe")))
							{
								// remove from mailing list
								updatedMailingLists= unSubscribeFromJournal(journalName, msg) || updatedMailingLists;
							}
							else
							{
								processJournalPosting(journalName, msg, massMailer);
							}
						}
					}
				}
			}

			// here is where the mail is actually sent
			if((tickID==Tickable.TICKID_EMAIL)
			&&(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING)))
			{
				if((mailboxName()!=null)&&(mailboxName().length()>0))
				{
					List<JournalsLibrary.JournalEntry> emails=CMLib.database().DBReadJournalMsgs(mailboxName());
					if(emails!=null)
					for(JournalsLibrary.JournalEntry mail : emails)
					{
						if((mail.data.length()>0)&&(isAForwardingJournal(mail.data)))
							massMailer.addMail(mail, mailboxName(), mail.data, true);
						else
							massMailer.addMail(mail, mailboxName(), null, true);
					}
				}
			}
			lastAllProcessing=System.currentTimeMillis();
			if(updatedMailingLists)
			{
				Resources.updateCachedMultiLists("mailinglists.txt");
				updatedMailingLists=false;
			}
			new Thread(massMailer).start();
		}
		System.gc();
		try{Thread.sleep(1000);}catch(Exception ex){}
		tickStatus=STATUS_NOT;
		return true;
	}

	public void processJournalPosting(final String journalName, final JournalsLibrary.JournalEntry msg, final MassMailer massMailer)
	{
		if((msg.update>lastAllProcessing)&&(msg.to!=null)&&(!msg.to.equalsIgnoreCase("JOURNALINTRO")))
		{
			String jrnlSubj;
			if(msg.subj.indexOf("["+journalName+"]")<0)
			{
				if(msg.subj.startsWith("RE: "))
					jrnlSubj="RE: ["+journalName+"] "+msg.subj.substring(4);
				else
					jrnlSubj="["+journalName+"] "+msg.subj;
			}
			else
				jrnlSubj=msg.subj;
			String jrnlMessage=msg.msg;
			if(jrnlMessage.startsWith("<HTML><BODY>"))
			{
				
			}
			else
			{
				if(jrnlMessage.indexOf("<HTML>")>0)
				{
					jrnlMessage=CMStrings.replaceAll(jrnlMessage,"<HTML>","");
					jrnlMessage=CMStrings.replaceAll(jrnlMessage,"</HTML>","");
					jrnlMessage=CMStrings.replaceAll(jrnlMessage,"<BODY>","");
					jrnlMessage=CMStrings.replaceAll(jrnlMessage,"</BODY>","");
				}
				else
				jrnlMessage="<HTML><BODY>"+jrnlMessage+"</BODY></HTML>";
			}
			Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
			List<String> mylist=lists.get(journalName);
			if((mylist!=null)&&(mylist.contains(msg.from)))
			{
				for(int i=0;i<mylist.size();i++)
				{
					String to2=(String)mylist.get(i);
					if(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
					{
						CMLib.database().DBWriteJournalEmail(mailboxName(),journalName,msg.from,to2,jrnlSubj,jrnlMessage);
					}
				}
			}
		}
		if(!isAKeepAllJournal(journalName))
			CMLib.database().DBDeleteJournal(journalName,msg.key);
		else
		{
			Calendar IQE=Calendar.getInstance();
			IQE.setTimeInMillis(msg.update);
			IQE.add(Calendar.DATE,getJournalDays());
			if(IQE.getTimeInMillis()<System.currentTimeMillis())
				CMLib.database().DBDeleteJournal(journalName,msg.key);
		}
	}
	
	public boolean subscribeToJournal(final String journalName, final JournalsLibrary.JournalEntry msg)
	{
		CMLib.database().DBDeleteJournal(journalName,msg.key);
		
		boolean updatedMailingLists = false;
		if(CMLib.players().playerExists(msg.from))
		{
			Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
			List<String> mylist=lists.get(journalName);
			if(mylist==null)
			{
				mylist=new Vector<String>();
				lists.put(journalName,mylist);
			}
			boolean found=false;
			for(int l=0;l<mylist.size();l++)
				if(((String)mylist.get(l)).equalsIgnoreCase(msg.from))
					found=true;
			if(!found)
			{
				mylist.add(msg.from);
				updatedMailingLists=true;
				if(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
				{
					String subscribeTitle=page.getStr("SUBSCRIBEDTITLE");
					if((subscribeTitle==null)||(subscribeTitle.length()==0))
						subscribeTitle="Subscribed";
					String subscribedMsg=page.getStr("SUBSCRIBEDMSG");
					if((subscribedMsg==null)||(subscribedMsg.length()==0))
						subscribedMsg="You are now subscribed to "+journalName+". To unsubscribe, send an email with a subject of unsubscribe.";
					subscribeTitle=CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(subscribeTitle,"<NAME>",journalName),false);
					subscribedMsg=CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(subscribedMsg,"<NAME>",journalName),false);
					CMLib.database().DBWriteJournalEmail(mailboxName(),journalName,journalName,msg.from,subscribeTitle,subscribedMsg);
				}
			}
		}
		return updatedMailingLists;
	}

	public boolean unSubscribeFromJournal(final String journalName, final JournalsLibrary.JournalEntry msg)
	{
		CMLib.database().DBDeleteJournal(journalName,msg.key);
		
		boolean updatedMailingLists = false;
		Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
		List<String> mylist=lists.get(journalName);
		if(mylist==null) return false;
		for(int l=mylist.size()-1;l>=0;l--)
			if(((String)mylist.get(l)).equalsIgnoreCase(msg.from))
			{
				mylist.remove(l);
				updatedMailingLists=true;
				if(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
				{
					String unsubscribeTitle=page.getStr("UNSUBSCRIBEDTITLE");
					if((unsubscribeTitle==null)||(unsubscribeTitle.length()==0))
						unsubscribeTitle="Subscribed";
					String unsubscribedMsg=page.getStr("UNSUBSCRIBEDMSG");
					if((unsubscribedMsg==null)||(unsubscribedMsg.length()==0))
						unsubscribedMsg="You are no longer subscribed to "+journalName+". To subscribe again, send an email with a subject of subscribe.";
					unsubscribeTitle=CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(unsubscribeTitle,"<NAME>",journalName),false);
					unsubscribedMsg=CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(unsubscribedMsg,"<NAME>",journalName),false);
					CMLib.database().DBWriteJournalEmail(mailboxName(),journalName,journalName,msg.from,unsubscribeTitle,unsubscribedMsg);
				}
			}
		return updatedMailingLists;
	}

	
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

	public int getMaxMsgs()
	{
		String s=page.getStr("MAXMSGS");
		if(s==null) return Integer.MAX_VALUE;
		int x=CMath.s_int(s);
		if(x==0) return Integer.MAX_VALUE;
		return x;
	}
	public int getJournalDays()
	{
		String s=page.getStr("JOURNALDAYS");
		if(s==null) return (365*20);
		int x=CMath.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	public long getMaxMsgSize()
	{
		String s=page.getStr("MAXMSGSIZE");
		if(s==null) return Long.MAX_VALUE;
		long x=CMath.s_long(s);
		if(x==0) return Long.MAX_VALUE;
		return x;
	}
}
