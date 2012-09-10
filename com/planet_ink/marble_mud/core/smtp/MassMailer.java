package com.planet_ink.marble_mud.core.smtp;
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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.net.*;
import java.util.*;

import com.planet_ink.marble_mud.core.exceptions.*;
import java.io.*;

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
public class MassMailer implements Runnable
{
	private final LinkedList<MassMailerEntry> entries=new LinkedList<MassMailerEntry>();
	private final CMProps 		  page;
	private final String		  domain;
	private final HashSet<String> oldEmailComplaints;
	
	private static class MassMailerEntry
	{
		public final JournalsLibrary.JournalEntry mail;
		public final String  journalName;
		public final String  overrideReplyTo;
		public final boolean usePrivateRules;
		public MassMailerEntry(JournalsLibrary.JournalEntry mail, String journalName, String overrideReplyTo, boolean usePrivateRules)
		{
			this.mail=mail;
			this.journalName=journalName;
			this.overrideReplyTo=overrideReplyTo;
			this.usePrivateRules=usePrivateRules;
		}
	}
	
	public MassMailer(CMProps page, String domain, HashSet<String> oldEmailComplaints)
	{
		this.page=page;
		this.domain=domain;
		this.oldEmailComplaints=oldEmailComplaints;
	}
	
	public String domainName() { return domain;}
	
	public int getFailureDays()
	{
		String s=page.getStr("FAILUREDAYS");
		if(s==null) return (365*20);
		int x=CMath.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	
	public int getEmailDays()
	{
		String s=page.getStr("EMAILDAYS");
		if(s==null) return (365*20);
		int x=CMath.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	
	public boolean deleteEmailIfOld(String journalName, String key, long date, int days)
	{
		Calendar IQE=Calendar.getInstance();
		IQE.setTimeInMillis(date);
		IQE.add(Calendar.DATE,days);
		if(IQE.getTimeInMillis()<System.currentTimeMillis())
		{
			// email is a goner
			CMLib.database().DBDeleteJournal(journalName, key);
			return true;
		}
		return false;
	}
	
	public void addMail(JournalsLibrary.JournalEntry mail, String journalName, String overrideReplyTo, boolean usePrivateRules)
	{
		entries.add(new MassMailerEntry(mail,journalName,overrideReplyTo,usePrivateRules));
	}
	
	protected boolean rightTimeToSendEmail(long email)
	{
		long curr=System.currentTimeMillis();
		Calendar IQE=Calendar.getInstance();
		IQE.setTimeInMillis(email);
		Calendar IQC=Calendar.getInstance();
		IQC.setTimeInMillis(curr);
		if(CMath.absDiff(email,curr)<(30*60*1000)) return true;
		while(IQE.before(IQC))
		{
			if(CMath.absDiff(IQE.getTimeInMillis(),IQC.getTimeInMillis())<(30*60*1000))
				return true;
			IQE.add(Calendar.DATE,1);
		}
		return false;
	}


	@Override
	public void run() 
	{
		for(MassMailerEntry entry : entries)
		{
			final JournalsLibrary.JournalEntry mail=entry.mail;
			final String journalName=entry.journalName;
			final String overrideReplyTo=entry.overrideReplyTo;
			final boolean usePrivateRules=entry.usePrivateRules;
			
			String key=mail.key;
			String from=mail.from;
			String to=mail.to;
			long date=mail.update;
			String subj=mail.subj;
			String msg=mail.msg.trim();
	
			if(to.equalsIgnoreCase("ALL")||(to.toUpperCase().trim().startsWith("MASK="))) continue;
	
			if(!rightTimeToSendEmail(date)) continue;
	
			// check for valid recipient
			MOB toM=CMLib.players().getLoadPlayer(to);
			if(toM==null)
			{
				Log.errOut("SMTPServer","Invalid to address '"+to+"' in email: "+msg);
				CMLib.database().DBDeleteJournal(journalName,key);
				continue;
			}
	
			// check to see if the sender is ignored
			if((toM.playerStats()!=null)
			&&(toM.playerStats().getIgnored().contains(from)))
			{
				// email is ignored
				CMLib.database().DBDeleteJournal(journalName,key);
				continue;
			}
	
			// check email age
			if((usePrivateRules)
			&&(!CMath.bset(mail.attributes, JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED))
			&&(deleteEmailIfOld(journalName, key, date, getEmailDays())))
				continue;
			
			if(CMath.bset(toM.getBitmap(),MOB.ATT_AUTOFORWARD)) // forwarding OFF
				continue;
	
			if((toM.playerStats()==null)
			||(toM.playerStats().getEmail().length()==0)) // no email addy to forward TO
				continue;
	
			SMTPLibrary.SMTPClient SC=null;
			try
			{
				if(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME).length()>0)
					SC=CMLib.smtp().getClient(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),SMTPLibrary.DEFAULT_PORT);
				else
					SC=CMLib.smtp().getClient(toM.playerStats().getEmail());
			}
			catch(BadEmailAddressException be)
			{
				if((!usePrivateRules)
				&&(!CMath.bset(mail.attributes, JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED)))
				{
					// email is a goner if its a list
					CMLib.database().DBDeleteJournal(journalName,key);
					continue;
				}
				// otherwise it has its n days
				continue;
			}
			catch(java.io.IOException ioe)
			{
				if(!oldEmailComplaints.contains(toM.Name()))
				{
					oldEmailComplaints.add(toM.Name());
					Log.errOut("SMTPServer","Unable to send '"+toM.playerStats().getEmail()+"' for '"+toM.name()+"': "+ioe.getMessage());
				}
				if(!CMath.bset(mail.attributes, JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED))
					deleteEmailIfOld(journalName, key, date,getFailureDays());
				continue;
			}
	
			String replyTo=(overrideReplyTo!=null)?(overrideReplyTo):from;
			try
			{
				SC.sendMessage(from+"@"+domainName(),
							   replyTo+"@"+domainName(),
							   toM.playerStats().getEmail(),
							   usePrivateRules?toM.playerStats().getEmail():replyTo+"@"+domainName(),
							   subj,
							   CMLib.coffeeFilter().simpleOutFilter(msg));
				//this email is HISTORY!
				CMLib.database().DBDeleteJournal(journalName, key);
			}
			catch(java.io.IOException ioe)
			{
				// it has FAILUREDAYS days to get better.
				if(deleteEmailIfOld(journalName, key, date,getFailureDays()))
					Log.errOut("SMTPServer","Permanently unable to send to '"+toM.playerStats().getEmail()+"' for user '"+toM.name()+"': "+ioe.getMessage()+".");
				else
					Log.errOut("SMTPServer","Failure to send to '"+toM.playerStats().getEmail()+"' for user '"+toM.name()+"'.");
			}
		}
	}
	
}
