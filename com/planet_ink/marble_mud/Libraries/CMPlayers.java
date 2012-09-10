package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.threads.CMSupportThread;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
	public String ID(){return "CMPlayers";}
	
	protected SVector<MOB> 				playersList			= new SVector<MOB>();
	protected SVector<PlayerAccount>	accountsList		= new SVector<PlayerAccount>();
	protected CrossRefTreeMap<MOB,Room> playerLocations		= new CrossRefTreeMap<MOB,Room>(Integer.MAX_VALUE,1);
	protected long[] 					autoPurgeDaysLevels	= new long[1];
	protected long[] 					prePurgeLevels		= new long[1];
	protected int						autoPurgeHash		= 0;
	
	private CMSupportThread thread=null;
	
	public CMSupportThread getSupportThread() { return thread;}

	public int numPlayers() { return playersList.size(); }
	public synchronized void addPlayer(MOB newOne)
	{
		if(getPlayer(newOne.Name())!=null) return;
		if(playersList.contains(newOne)) return;
		PlayerAccount acct = null;
		if(newOne.playerStats()!=null)
			acct=newOne.playerStats().getAccount();
		playersList.add(newOne);
		addAccount(acct);
	}
	
	public synchronized void delPlayer(MOB oneToDel) 
	{ 
		if(oneToDel != null)
		{
    		playersList.remove(oneToDel);
    		playerLocations.removeFirst(oneToDel);
		}
	}
	
	public Set<MOB> getPlayersHere(Room room)
	{
		return playerLocations.getSecond(room);
	}
	
	public void changePlayersLocation(MOB mob, Room room)
	{
		if(mob != null)
		{
    		if(room == null)
    			playerLocations.removeFirst(mob);
    		else
    			playerLocations.change(mob, room);
		}
	}

	
	public PlayerAccount getLoadAccount(String calledThis)
	{
		PlayerAccount A = getAccount(calledThis);
		if(A!=null) return A;
		return CMLib.database().DBReadAccount(calledThis);
	}
	public synchronized void addAccount(PlayerAccount acct)
	{
		if(acct==null) return;
		if(accountsList.contains(acct)) return;
		for(PlayerAccount A : accountsList) // dont consolodate this.
			if(A.accountName().equals(acct.accountName()))
				return;
		accountsList.add(acct);
	}
	public PlayerAccount getAccount(String calledThis)
	{
		calledThis=CMStrings.capitalizeAndLower(calledThis);
		for(PlayerAccount A : accountsList)
			if(A.accountName().equals(calledThis))
				return A;
		
		for (MOB M : playersList)
			if((M.playerStats()!=null)
			&&(M.playerStats().getAccount()!=null)
			&&(M.playerStats().getAccount().accountName().equals(calledThis)))
			{
				addAccount(M.playerStats().getAccount());
				return M.playerStats().getAccount();
			}
		return null;
	}
	public MOB getPlayer(String calledThis)
	{
		calledThis=CMStrings.capitalizeAndLower(calledThis);
		for (MOB M : playersList)
			if (M.Name().equals(calledThis))
				return M;
		return null;
	}

	public MOB getLoadPlayer(String last)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return null;
		MOB M=getPlayer(last);
		if(M!=null) return M;
		if(playerExists(last))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(CMStrings.capitalizeAndLower(last));
			CMLib.database().DBReadPlayer(M);
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
			M.recoverPhyStats();
			M.recoverCharStats();
			Ability A=null;
			for(int a=0;a<M.numAbilities();a++)
			{
				A=M.fetchAbility(a);
				if(A!=null) A.autoInvocation(M);
			}
		}
		return M;
	}

	public boolean accountExists(String name)
	{
		if(name==null) return false;
		name=CMStrings.capitalizeAndLower(name);
		return getLoadAccount(name)!=null;
	}
	
	public boolean playerExists(String name)
	{
		if(name==null) return false;
		name=CMStrings.capitalizeAndLower(name);
		for(MOB M: playersList)
			if(M.Name().equals(name))
				return true;
		return CMLib.database().DBUserSearch(name)!=null;
	}
	public Enumeration<MOB> players() { return playersList.elements(); }
	public Enumeration<PlayerAccount> accounts() { return accountsList.elements(); }

	public void obliteratePlayer(MOB deadMOB, boolean quiet)
	{
		if(deadMOB==null) return;
		if(getPlayer(deadMOB.Name())!=null)
		{
		   deadMOB=getPlayer(deadMOB.Name());
		   delPlayer(deadMOB);
		}
		for(Session S : CMLib.sessions().allIterable())
			if((!S.isStopped())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
			   deadMOB=S.mob();
		CMMsg msg=CMClass.getMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:"A horrible death cry is heard throughout the land.");
		Room deadLoc=deadMOB.location();
		if(deadLoc!=null)
			deadLoc.send(deadMOB,msg);
		try
		{
			for(Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(R!=deadLoc))
				{
					if(R.okMessage(deadMOB,msg))
						R.sendOthers(deadMOB,msg);
					else
					{
						addPlayer(deadMOB);
						return;
					}
				}
			}
		}catch(NoSuchElementException e){}
		StringBuffer newNoPurge=new StringBuffer("");
		List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		boolean somethingDone=false;
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				String B=(String)protectedOnes.get(b);
				if(!B.equalsIgnoreCase(deadMOB.name()))
					newNoPurge.append(B+"\n");
				else
					somethingDone=true;
			}
			if(somethingDone)
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
		}

		CMLib.database().DBDeleteMOB(deadMOB);
		if(deadMOB.session()!=null)
			deadMOB.session().stopSession(false,false,false);
		Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
		deadMOB.destroy();
	}
	
	public synchronized void obliterateAccountOnly(PlayerAccount deadAccount)
	{
		deadAccount = getLoadAccount(deadAccount.accountName());
		if(deadAccount==null) return;
		accountsList.remove(deadAccount);
		
		StringBuffer newNoPurge=new StringBuffer("");
		List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		boolean somethingDone=false;
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				String B=(String)protectedOnes.get(b);
				if(!B.equalsIgnoreCase(deadAccount.accountName()))
					newNoPurge.append(B+"\n");
				else
					somethingDone=true;
			}
			if(somethingDone)
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
		}

		CMLib.database().DBDeleteAccount(deadAccount);
		Log.sysOut("Scoring",deadAccount.accountName()+" has been deleted.");
	}
	
	public int savePlayers()
	{
		int processed=0;
		for(MOB mob : playersList)
		{
			if(!mob.isMonster())
			{
				CMLib.factions().updatePlayerFactions(mob,mob.location());
				thread.setStatus("just saving "+mob.Name());
				CMLib.database().DBUpdatePlayerMOBOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				thread.setStatus("saving "+mob.Name()+", "+mob.numItems()+" items");
				CMLib.database().DBUpdatePlayerItems(mob);
				thread.setStatus("saving "+mob.Name()+", "+mob.numAbilities()+" abilities");
				CMLib.database().DBUpdatePlayerAbilities(mob);
				thread.setStatus("saving "+mob.numFollowers()+" followers of "+mob.Name());
				CMLib.database().DBUpdateFollowers(mob);
				PlayerAccount account = mob.playerStats().getAccount();
				mob.playerStats().setLastUpdated(System.currentTimeMillis());
				if(account!=null)
				{
					thread.setStatus("saving account "+account.accountName()+" for "+mob.Name());
					CMLib.database().DBUpdateAccount(account);
					account.setLastUpdated(System.currentTimeMillis());
				}
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				thread.setStatus("just saving "+mob.Name());
				CMLib.database().DBUpdatePlayerMOBOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				thread.setStatus("just saving "+mob.Name()+", "+mob.numItems()+" items");
				CMLib.database().DBUpdatePlayerItems(mob);
				thread.setStatus("just saving "+mob.Name()+", "+mob.numAbilities()+" abilities");
				CMLib.database().DBUpdatePlayerAbilities(mob);
				mob.playerStats().setLastUpdated(System.currentTimeMillis());
				processed++;
			}
		}
		return processed;
	}
	
	public String getThinSortValue(ThinPlayer player, int code) 
	{
		switch(code) {
		case 0: return player.name;
		case 1: return player.charClass;
		case 2: return player.race;
		case 3: return Integer.toString(player.level);
		case 4: return Integer.toString(player.age);
		case 5: return Long.toString(player.last);
		case 6: return player.email;
		case 7: return player.ip;
		}
		return player.name;
	}
	
	public String getThinSortValue(PlayerAccount account, int code) 
	{
		switch(code) {
		case 0: return account.accountName();
		case 1: return Long.toString(account.lastDateTime());
		case 2: return account.getEmail();
		case 3: return account.lastIP();
		case 4: return Integer.toString(account.numPlayers());
		}
		return account.accountName();
	}
	
	public int getCharThinSortCode(String codeName, boolean loose) 
	{
		int x=CMParms.indexOf(CHAR_THIN_SORT_CODES,codeName);
		if(x<0)x=CMParms.indexOf(CHAR_THIN_SORT_CODES2,codeName);
		if(!loose) return x;
		if(x<0)
			for(int s=0;s<CHAR_THIN_SORT_CODES.length;s++)
				if(CHAR_THIN_SORT_CODES[s].startsWith(codeName))
					x=s;
		if(x<0)
			for(int s=0;s<CHAR_THIN_SORT_CODES2.length;s++)
				if(CHAR_THIN_SORT_CODES2[s].startsWith(codeName))
					x=s;
		return x;
	}
	
	public int getAccountThinSortCode(String codeName, boolean loose) 
	{
		int x=CMParms.indexOf(ACCOUNT_THIN_SORT_CODES,codeName);
		if(!loose) return x;
		if(x<0)
			for(int s=0;s<ACCOUNT_THIN_SORT_CODES.length;s++)
				if(ACCOUNT_THIN_SORT_CODES[s].startsWith(codeName))
					x=s;
		return x;
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<ThinPlayer> thinPlayers(String sort, Map<String, Object> cache)
	{
		Vector<PlayerLibrary.ThinPlayer> V=(cache==null)?null:(Vector<PlayerLibrary.ThinPlayer>)cache.get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=new Vector<PlayerLibrary.ThinPlayer>();
			V.addAll(CMLib.database().getExtendedUserList());
			int code=getCharThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				List<PlayerLibrary.ThinPlayer> unV=V;
				V=new Vector<PlayerLibrary.ThinPlayer>();
				while(unV.size()>0)
				{
					ThinPlayer M=unV.get(0);
					String loweStr=getThinSortValue(M,code);
					ThinPlayer lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=unV.get(i);
						String val=getThinSortValue(M,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestM=M;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.remove(lowestM);
					V.add(lowestM);
				}
			}
			if(cache!=null)
				cache.put("PLAYERLISTVECTOR"+sort,V);
		}
		return V.elements();
	}

	@SuppressWarnings("unchecked")
	public Enumeration<PlayerAccount> accounts(String sort, Map<String, Object> cache)
	{
		Vector<PlayerAccount> V=(cache==null)?null:(Vector<PlayerAccount>)cache.get("ACCOUNTLISTVECTOR"+sort);
		if(V==null)
		{
			V=new Vector<PlayerAccount>();
			V.addAll(CMLib.database().DBListAccounts(null));
			int code=getAccountThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				Vector<PlayerAccount> unV=V;
				V=new Vector<PlayerAccount>();
				while(unV.size()>0)
				{
					PlayerAccount A=unV.get(0);
					String loweStr=getThinSortValue(A,code);
					PlayerAccount lowestA=A;
					for(int i=1;i<unV.size();i++)
					{
						A=unV.get(i);
						String val=getThinSortValue(A,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestA=A;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestA=A;
						}
					}
					unV.remove(lowestA);
					V.add(lowestA);
				}
			}
			if(cache!=null)
				cache.put("ACCOUNTLISTVECTOR"+sort,V);
		}
		return V.elements();
	}

	private boolean isProtected(List<String> protectedOnes, String name)
	{
		boolean protectedOne=false;
		for(int p=0;p<protectedOnes.size();p++)
		{
			String P=(String)protectedOnes.get(p);
			if(P.equalsIgnoreCase(name))
			{
				protectedOne=true;
				break;
			}
		}
		if(protectedOne)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(thread.getName(),name+" is protected from purging.");
			return true;
		}
		return false;
	}
	
	private boolean autoPurge()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.AUTOPURGE))
			return true;
		String mask=CMProps.getVar(CMProps.SYSTEM_AUTOPURGE);
		if(mask.hashCode() != this.autoPurgeHash)
		{
			int lastLevel=CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)+100;
			long[][] presorted=CMLib.utensils().compileConditionalRange(CMParms.parseCommas(mask.trim(),true), 2, 0, lastLevel);
			autoPurgeDaysLevels=new long[lastLevel+1];
			prePurgeLevels=new long[lastLevel+1];
			for(int i=0;i<autoPurgeDaysLevels.length;i++) autoPurgeDaysLevels[i]=0;
			for(int i=0;i<prePurgeLevels.length;i++) prePurgeLevels[i]=0;
			for(int i=0;i<presorted.length;i++)
			{
				final long[] set=presorted[i];
				long val=set[0];
				if(set[0]<=0) continue;
				long prepurge=set[1];
				long realVal=(val*TimeManager.MILI_DAY);
				long purgePoint=realVal-(prepurge*TimeManager.MILI_DAY);
				if(val <= 0)
				{
					realVal = 0;
					purgePoint = 0;
				}
				if(autoPurgeDaysLevels[i]==0) autoPurgeDaysLevels[i]=realVal;
				if(prePurgeLevels[i]==0) prePurgeLevels[i]=purgePoint;
			}
			this.autoPurgeHash=mask.hashCode();
		}
		thread.setStatus("autopurge process");
		List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if(protectedOnes==null) protectedOnes=new Vector<String>();

		for(ThinPlayer user : allUsers)
		{
			String name=user.name;
			int level=user.level;
			long userLastLoginDateTime=user.last;
			long purgeDateTime;
			long warnDateTime;
			if(level>autoPurgeDaysLevels.length)
			{
				if(autoPurgeDaysLevels[autoPurgeDaysLevels.length-1]==0)
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
						Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Nothing will be done about it.");
					continue;
				}
				purgeDateTime=userLastLoginDateTime + autoPurgeDaysLevels[autoPurgeDaysLevels.length-1];
				warnDateTime=userLastLoginDateTime + prePurgeLevels[prePurgeLevels.length-1];
			}
			else
			if(level>=0)
			{
				if(autoPurgeDaysLevels[level]==0)
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
						Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Nothing will be done about it.");
					continue;
				}
				purgeDateTime=userLastLoginDateTime + autoPurgeDaysLevels[level];
				warnDateTime=userLastLoginDateTime + prePurgeLevels[level];
			}
			else
				continue;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(thread.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+" will be warned on "+CMLib.time().date2String(warnDateTime)+" and purged on "+CMLib.time().date2String(purgeDateTime));
			if((System.currentTimeMillis()>purgeDateTime)||(System.currentTimeMillis()>warnDateTime))
			{
				if(isProtected(protectedOnes, name))
					continue;
				
				List<String> warnedOnes=Resources.getFileLineVector(Resources.getFileResource("warnedplayers.ini",false));
				long foundWarningDateTime=-1;
				StringBuffer warnStr=new StringBuffer("");
				if((warnedOnes!=null)&&(warnedOnes.size()>0))
					for(int b=0;b<warnedOnes.size();b++)
					{
						String B=((String)warnedOnes.get(b)).trim();
						long warningDateTime=-1;
						if(B.trim().length()>0)
						{
							int lastSpace=B.lastIndexOf(' ');
							warningDateTime=CMath.s_long(B.substring(lastSpace+1).trim());
							if((warningDateTime > 0) && (System.currentTimeMillis() < warningDateTime + (10 * TimeManager.MILI_DAY)))
								warnStr.append(B+"\n");
							if(B.toUpperCase().startsWith(name.toUpperCase()+" "))
								foundWarningDateTime=warningDateTime;
						}
					}
				if((foundWarningDateTime<0)
				&&(System.currentTimeMillis()>warnDateTime))
				{
					MOB M=getLoadPlayer(name);
					if((M!=null)&&(M.playerStats()!=null))
					{
						warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
						Resources.updateFileResource("::warnedplayers.ini",warnStr);
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
							Log.debugOut(thread.getName(),name+" is now warned.");
						warnPrePurge(M,purgeDateTime-System.currentTimeMillis());
					}
				}
				else
				if((System.currentTimeMillis()>purgeDateTime)
				&&(foundWarningDateTime > 0)
				&&((System.currentTimeMillis()-foundWarningDateTime)>TimeManager.MILI_DAY))
				{
					MOB M=getLoadPlayer(name);
					if((M!=null)&&(!CMSecurity.isASysOp(M))&&(!CMSecurity.isAllowedAnywhere(M, CMSecurity.SecFlag.NOPURGE)))
					{
						obliteratePlayer(M,true);
						M.destroy();
						Log.sysOut(thread.getName(),"AutoPurged user "+name+". Last logged in "+(CMLib.time().date2String(userLastLoginDateTime))+".");
					}
				}
			}
		}
		
		// accounts!
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.PURGEACCOUNTS))&&(CMProps.getIntVar(CMProps.SYSTEMI_ACCOUNTPURGEDAYS)>0))
			for(final Enumeration<PlayerAccount> pe=CMLib.players().accounts("",null); pe.hasMoreElements();)
			{
				PlayerAccount PA=pe.nextElement();
				if((PA.numPlayers() > 0)
				||(isProtected(protectedOnes, PA.accountName())))
					continue;
				final long lastDateTimePurge = PA.lastDateTime() + (TimeManager.MILI_DAY * (long)CMProps.getIntVar(CMProps.SYSTEMI_ACCOUNTPURGEDAYS));
				final long lastUpdatedPurge = PA.lastUpdated() + (TimeManager.MILI_DAY * (long)CMProps.getIntVar(CMProps.SYSTEMI_ACCOUNTPURGEDAYS));
				final long accountExpPurge = PA.getAccountExpiration() + (TimeManager.MILI_DAY * (long)CMProps.getIntVar(CMProps.SYSTEMI_ACCOUNTPURGEDAYS));
				long lastTime = lastDateTimePurge;
				if(lastUpdatedPurge > lastTime) lastTime=lastUpdatedPurge;
				if(accountExpPurge > lastTime) lastTime=accountExpPurge;
				if(System.currentTimeMillis()>lastTime)
				{
					Log.sysOut(thread.getName(),"AutoPurged account "+PA.accountName()+".");
					CMLib.players().obliterateAccountOnly(PA);
				}
			}
		return true;
	}

	private void warnPrePurge(MOB mob, long timeLeft)
	{
		// check for valid recipient
		if(mob==null) return;

		if((mob.playerStats()==null)
		||(mob.playerStats().getEmail().length()==0)) // no email addy to forward TO
			return;

		//  timeLeft is in millis
		String from="AutoPurgeWarning";
		String to=mob.Name();
		String subj=CMProps.SYSTEM_MUDNAME+" Autopurge Warning: "+to;
		String textTimeLeft="";
		if(timeLeft<0)
			timeLeft = 1000*60*60*24;
		if(timeLeft>(1000*60*60*24*2))
		{
			int days=(int)CMath.div((double)timeLeft,1000*60*60*24);
			textTimeLeft = days + " days";
		}
		else
		{
			int hours=(int)CMath.div((double)timeLeft,1000*60*60);
			textTimeLeft = hours + " hours";
		}
		
		String msg="Your character, "+to+", is going to be autopurged by the system in "+textTimeLeft+".  If you would like to keep this character active, please re-login.  This is an automated message, please do not reply.";

		SMTPLibrary.SMTPClient SC=null;
		try
		{
			if(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME).length()>0)
				SC=CMLib.smtp().getClient(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),SMTPLibrary.DEFAULT_PORT);
			else
				SC=CMLib.smtp().getClient(mob.playerStats().getEmail());
		}
		catch(BadEmailAddressException be)
		{
			Log.errOut(thread.getName(),"Unable to notify "+to+" of impending autopurge.  Invalid email address.");
			return;
		}
		catch(java.io.IOException ioe)
		{
			return;
		}

		String replyTo="AutoPurge";
		String domain=CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase();
		try
		{
			SC.sendMessage(from+"@"+domain,
						   replyTo+"@"+domain,
						   mob.playerStats().getEmail(),
						   mob.playerStats().getEmail(),
						   subj,
						   CMLib.coffeeFilter().simpleOutFilter(msg));
		}
		catch(java.io.IOException ioe)
		{
			Log.errOut(thread.getName(),"Unable to notify "+to+" of impending autopurge.");
		}
	}

	public boolean activate() {
		if(thread==null)
			thread=new CMSupportThread("THPlayers"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging(CMSecurity.DbgFlag.PLAYERTHREAD), CMSecurity.DisFlag.PLAYERTHREAD);
		if(!thread.isStarted())
			thread.start();
		return true;
	}
	
	public boolean shutdown() {
		playersList.clear();
		playerLocations.clear();
		thread.shutdown();
		return true;
	}
	
	public void forceTick()
	{
		if(thread.getStatus().equalsIgnoreCase("sleeping"))
		{
			thread.interrupt();
			return;
		}
	}

	public void run()
	{
		thread.setStatus("pinging connections");
		CMLib.database().pingAllConnections();
		thread.setStatus("not saving players");
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.PLAYERTHREAD)))
		{
			thread.setStatus("checking player titles.");
			for(MOB M : playersList)
				if(M.playerStats()!=null)
				{
					if((CMLib.titles().evaluateAutoTitles(M))&&(!CMLib.flags().isInTheGame(M,true)))
						CMLib.database().DBUpdatePlayerMOBOnly(M);
				}
			autoPurge();
			if(!CMSecurity.isSaveFlag("NOPLAYERS"))
				savePlayers();
			thread.setStatus("not saving players");
		}
	}
}
