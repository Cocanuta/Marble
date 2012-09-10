package com.planet_ink.marble_mud.Common;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.CMSecurity.SecGroup;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.core.exceptions.CMException;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.StdMOB;
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
public class DefaultPlayerStats implements PlayerStats
{
	public String ID(){return "DefaultPlayerStats";}
	protected final static int TELL_STACK_MAX_SIZE=50;
	protected final static int GTELL_STACK_MAX_SIZE=50;
	
	protected long  	hygiene=0; 
	protected String[]  xtraValues=null;
	protected String	lastIP="";
	protected long  	lLastDateTime=System.currentTimeMillis();
	protected long  	lastUpdated=0;
	protected int   	channelMask;
	protected String	email="";
	protected String	password="";
	protected String	colorStr="";
	protected String	prompt="";
	protected String	poofin="";
	protected String	poofout="";
	protected String	tranpoofin="";
	protected String	tranpoofout="";
	protected String	announceMsg="";
	protected String	savedPose="";
	protected String	notes="";
	protected int   	wrap=78;
	protected int   	pageBreak=CMProps.getIntVar(CMProps.SYSTEMI_PAGEBREAK);
	protected int[] 	birthday=null;
	protected MOB   	replyTo=null;
	protected int   	replyType=0;
	protected long  	replyTime=0;
	protected PlayerAccount 	  account = null;
	protected SHashSet<String>    friends=new SHashSet<String>();
	protected SHashSet<String>    ignored=new SHashSet<String>();
	protected SVector<String>     tellStack=new SVector<String>();
	protected SVector<String>     gtellStack=new SVector<String>();
	protected SVector<String>     titles=new SVector<String>();
	protected STreeMap<String,String> alias=new STreeMap<String,String>();
	
	
	protected SecGroup   		securityFlags=new SecGroup(new CMSecurity.SecFlag[]{});
	protected long  			accountExpiration=0;
	protected RoomnumberSet 	visitedRoomSet=null;
	protected DVector   		levelInfo=new DVector(3);
	protected SHashSet<String>  introductions=new SHashSet<String>();
	protected ItemCollection	extItems;

	public DefaultPlayerStats() 
	{
		super();
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		extItems=(ItemCollection)CMClass.getCommon("WeakItemCollection");
	}
	
	protected static String[] CODES={"CLASS","FRIENDS","IGNORE","TITLES",
									 "ALIAS","LASTIP","LASTDATETIME",
									 "CHANNELMASK",
									 "COLORSTR","PROMPT","POOFIN",
									 "POOFOUT","TRANPOOFIN","TRAINPOOFOUT",
									 "ANNOUNCEMSG","NOTES","WRAP","BIRTHDAY",
									 "ACCTEXPIRATION","INTRODUCTIONS","PAGEBREAK",
									 "SAVEDPOSE"};
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return getPrivateList(getFriends());
		case 2: return getPrivateList(getIgnored());
		case 3: return getTitleXML();
		case 4: return getAliasXML();
		case 5: return lastIP;
		case 6: return ""+lLastDateTime;
		case 7: return ""+channelMask;
		case 8: return colorStr;
		case 9: return prompt;
		case 10: return poofin;
		case 11: return poofout;
		case 12: return tranpoofin;
		case 13: return tranpoofout;
		case 14: return announceMsg;
		case 15: return notes;
		case 16: return ""+wrap;
		case 17: return CMParms.toStringList(birthday);
		case 18: return ""+accountExpiration;
		case 19: return getPrivateList(introductions);
		case 20: return ""+pageBreak;
		case 21: return ""+savedPose;
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: break;
		case 1: friends=getHashFrom(val); break;
		case 2: ignored=getHashFrom(val); break;
		case 3: setTitleXML(CMLib.xml().parseAllXML(val)); break;
		case 4: setAliasXML(CMLib.xml().parseAllXML(val)); break;
		case 5: lastIP=val; break;
		case 6: lLastDateTime=CMath.s_long(val); break;
		case 7: channelMask=CMath.s_int(val); break;
		case 8: colorStr=val; break;
		case 9: prompt=val; break;
		case 10: poofin=val; break;
		case 11: poofout=val; break;
		case 12: tranpoofin=val; break;
		case 13: tranpoofout=val; break;
		case 14: announceMsg=val; break;
		case 15: notes=val; break;
		case 16: wrap=CMath.s_int(val); break;
		case 17: setBirthday(val); break;
		case 18: accountExpiration=CMath.s_long(val); break;
		case 19: introductions=getHashFrom(val); break;
		case 20: pageBreak=CMath.s_int(val); break;
		case 21: savedPose=val; break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	private static String[] codes=null;
	public String[] getStatCodes(){
		if(codes==null)
			codes=CMProps.getStatCodesList(CODES,this);
		return codes;
	}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
	
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPlayerStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultPlayerStats O=(DefaultPlayerStats)this.clone();
			O.levelInfo=levelInfo.copyOf();
			if(visitedRoomSet!=null)
				O.visitedRoomSet=(RoomnumberSet)visitedRoomSet.copyOf();
			else
				O.visitedRoomSet=null;
			O.securityFlags=securityFlags.copyOf();
			O.friends=friends.copyOf();
			O.ignored=ignored.copyOf();
			O.tellStack=tellStack.copyOf();
			O.gtellStack=gtellStack.copyOf();
			O.titles=titles.copyOf();
			O.alias=alias.copyOf();
			O.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			O.extItems=(ItemCollection)extItems.copyOf();
			return O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}
	public String lastIP(){return lastIP;}
	public void setLastIP(String ip)
	{
		lastIP=ip;
		if(account != null)
			account.setLastIP(ip);
	}
	public String getEmail()
	{
		if(account != null)
			return account.getEmail();
		if(email==null) 
			return ""; 
		return email;
	}
	public void setEmail(String newAdd)
	{
		email=newAdd;
		if(account != null)
			account.setEmail(newAdd);
	}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time)
	{
		lastUpdated=time;
		if(account != null)
			account.setLastUpdated(time);
	}
	public long lastDateTime(){return lLastDateTime;}
	public void setLastDateTime(long C)
	{ 
		lLastDateTime=C;
		if(account != null)
			account.setLastDateTime(C);
	}
	public String password(){return (account!=null)?account.password():password;}
	public void setPassword(String newPassword)
	{
		password=newPassword;
		if(account != null)
			account.setPassword(newPassword);
	}
	
	public int getWrap(){return wrap;}
	public void setWrap(int newWrap){wrap=newWrap;}
	public int getPageBreak(){return pageBreak;}
	public void setPageBreak(int newBreak){pageBreak=newBreak;}
	public String notes(){return notes;}
	public void setNotes(String newnotes){notes=newnotes;}
	public void setChannelMask(int newMask){ channelMask=newMask;}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){    return replyTo;	}
	public int replyType(){    return replyType;}
	public long replyTime(){	return replyTime;    }
	public void setReplyTo(MOB mob, int replyType)
	{    
		replyTo=mob;
		this.replyType=replyType;
	}
	public void setPrompt(String newPrompt){prompt=newPrompt;}
	public String getColorStr(){return colorStr;}
	public void setColorStr(String newColors){colorStr=newColors;}
	public String announceMessage(){return announceMsg;}
	public void setAnnounceMessage(String msg){announceMsg=msg;}
	public String getSavedPose(){return savedPose;}
	public void setSavedPose(String msg){savedPose=msg;}
	public String getPrompt()
	{
		if((prompt==null)||(prompt.length()==0))
		{
			prompt=CMProps.getVar(CMProps.SYSTEM_DEFAULTPROMPT);
			if((prompt==null)||(prompt.length()==0))
				return "^N%E<^h%hhp ^m%mm ^v%vmv^N>";
		}
		return prompt;
	}

	public boolean isIntroducedTo(String name){return introductions.contains(name.toUpperCase().trim());}
	public void introduceTo(String name){
		if((!isIntroducedTo(name))&&(name.trim().length()>0))
			introductions.add(name.toUpperCase().trim());
	}
	
	public SHashSet<String> getHashFrom(String str)
	{
		SHashSet<String> h=new SHashSet<String>();
		if((str==null)||(str.length()==0)) return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"<INTROS>","");
		str=CMStrings.replaceAll(str,"</INTROS>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
		int x=str.indexOf(';');
		while(x>=0)
		{
			String fi=str.substring(0,x).trim();
			if(fi.length()>0) h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(';');
		}
		if(str.trim().length()>0)
			h.add(str.trim());
		return h;
	}

	public void addTellStack(String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.removeElementAt(0);
		tellStack.addElement(msg);
	}
	
	public List<String> getTellStack()
	{
		return new ReadOnlyList<String>(tellStack);
	}
	private RoomnumberSet roomSet()
	{
		if(visitedRoomSet==null)
			visitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
		return visitedRoomSet;
	}
	public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.removeElementAt(0);
		gtellStack.addElement(msg);
	}
	
	public List<String> getGTellStack()
	{
		return new ReadOnlyList<String>(gtellStack);
	}
	
	public Set<String> getFriends()
	{
		if(account != null)
			return account.getFriends();
		return friends;
	}
	public Set<String> getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		return ignored;
	}
	
	public String[] getAliasNames()
	{
		return alias.keySet().toArray(new String[0]);
	}
	
	public String getAlias(String named)
	{
		if(alias.containsKey(named.toUpperCase().trim()))
			return alias.get(named.toUpperCase().trim());
		return "";
	}
	public void addAliasName(String named)
	{
		named=named.toUpperCase().trim();
		if(getAlias(named).length()==0)
			alias.put(named,"");
	}
	public void delAliasName(String named)
	{
		alias.remove(named.toUpperCase().trim());
	}
	public void setAlias(String named, String value)
	{
		alias.put(named.toUpperCase().trim(),value);
	}
	
	public String getAliasXML()
	{
		if(alias.size()==0) return "";
		StringBuffer str=new StringBuffer("");
		alias.remove("");
		int t=0;
		for(String key : alias.keySet())
		{
			str.append("<ALIAS"+t+">"+key+"</ALIAS"+t+">");
			str.append("<ALIASV"+t+">"+alias.get(key)+"</ALIASV"+t+">");
			t++;
		}
		return str.toString();
	}
	
	public String getActiveTitle()
	{
		if((titles==null)||(titles.size()==0)) return null;
		String s=(String)titles.firstElement();
		if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
			return s;
		return s.substring(1,s.length()-1);
	}
	
	public List<String> getTitles()
	{
		return titles;
	}
	public String getTitleXML()
	{
		if(titles.size()==0) return "";
		StringBuffer str=new StringBuffer("");
		for(int t=titles.size()-1;t>=0;t--)
		{
			String s=(String)titles.elementAt(t);
			if(s.length()==0) titles.removeElementAt(t);
		}
		for(int t=0;t<titles.size();t++)
		{
			String s=(String)titles.elementAt(t);
			str.append("<TITLE"+t+">"+CMLib.coffeeFilter().safetyFilter(s)+"</TITLE"+t+">");
		}
		return str.toString();
	}
	
	public String poofIn(){return poofin;}
	public String poofOut(){return poofout;}
	public String tranPoofIn(){return tranpoofin;}
	public String tranPoofOut(){return tranpoofout;}
	public int[] getBirthday(){return birthday;}
	public int initializeBirthday(int ageHours, Race R)
	{
		birthday=new int[4];
		TimeClock C=CMLib.time().globalClock();
		birthday[0]=C.getDayOfMonth();
		birthday[1]=C.getMonth();
		birthday[2]=C.getYear();
		birthday[3]=C.getYear();
		while(ageHours>15)
		{
			birthday[2]-=1;
			ageHours-=15;
		}
		if(ageHours>0)
		{
			birthday[1]=CMLib.dice().roll(1,C.getMonthsInYear(),0);
			birthday[0]=CMLib.dice().roll(1,C.getDaysInMonth(),0);
		}
		int month=C.getMonth();
		int day=C.getDayOfMonth();
		if((month<birthday[1])||((month==birthday[1])&&(birthday[0]<day)))
			return (R.getAgingChart()[Race.AGE_YOUNGADULT]+C.getYear()-birthday[2])-1;
		return (R.getAgingChart()[Race.AGE_YOUNGADULT]+C.getYear()-birthday[2]);
	}
	
	protected String getPrivateList(Set<String> h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator<String> e=h.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return list.toString();
	}
	public String getXML()
	{
		String f=getPrivateList(getFriends());
		String i=getPrivateList(getIgnored());
		String t=getPrivateList(introductions);
		StringBuffer rest=new StringBuffer("");
		String[] codes=getStatCodes();
		for(int x=getSaveStatIndex();x<codes.length;x++)
		{
			String code=codes[x].toUpperCase();
			rest.append("<"+code+">"+CMLib.xml().parseOutAngleBrackets(getStat(code))+"</"+code+">");
		}
		
		return ((f.length()>0)?"<FRIENDS>"+f+"</FRIENDS>":"")
			+((i.length()>0)?"<IGNORED>"+i+"</IGNORED>":"")
			+((t.length()>0)?"<INTROS>"+t+"</INTROS>":"")
			+"<WRAP>"+wrap+"</WRAP>"
			+"<PAGEBREAK>"+pageBreak+"</PAGEBREAK>"
			+((account!=null)?("<ACCOUNT>"+account.accountName()+"</ACCOUNT>"):"")
			+getTitleXML()
			+getAliasXML()
			+"<ACCTEXP>"+accountExpiration+"</ACCTEXP>"
			+((birthday!=null)?"<BIRTHDAY>"+CMParms.toStringList(birthday)+"</BIRTHDAY>":"")
			+((poofin.length()>0)?"<POOFIN>"+CMLib.xml().parseOutAngleBrackets(poofin)+"</POOFIN>":"")
			+((notes.length()>0)?"<NOTES>"+CMLib.xml().parseOutAngleBrackets(notes)+"</NOTES>":"")
			+((poofout.length()>0)?"<POOFOUT>"+CMLib.xml().parseOutAngleBrackets(poofout)+"</POOFOUT>":"")
			+((announceMsg.length()>0)?"<ANNOUNCE>"+CMLib.xml().parseOutAngleBrackets(announceMsg)+"</ANNOUNCE>":"")
			+((savedPose.length()>0)?"<POSE>"+CMLib.xml().parseOutAngleBrackets(savedPose)+"</POSE>":"")
			+((tranpoofin.length()>0)?"<TRANPOOFIN>"+CMLib.xml().parseOutAngleBrackets(tranpoofin)+"</TRANPOOFIN>":"")
			+((tranpoofout.length()>0)?"<TRANPOOFOUT>"+CMLib.xml().parseOutAngleBrackets(tranpoofout)+"</TRANPOOFOUT>":"")
			+"<DATES>"+this.getLevelDateTimesStr()+"</DATES>"
			+"<SECGRPS>"+getSetSecurityFlags(null)+"</SECGRPS>"
			+roomSet().xml()
			+rest.toString();
	}

	private void setBirthday(String bday)
	{
		if((bday!=null)&&(bday.length()>0))
		{
			Vector<String> V=CMParms.parseCommas(bday,true);
			birthday=new int[4];
			for(int v=0;(v<V.size()) && (v<birthday.length);v++)
				birthday[v]=CMath.s_int((String)V.elementAt(v));
			if(V.size()<4)
			{
				TimeClock C=CMLib.time().globalClock();
				birthday[3]=C.getYear();
			}
		}
	}
	
	private void setAliasXML(List<XMLpiece> xml)
	{
		alias.clear();
		int a=-1;
		while((++a)>=0)
		{
			String name=CMLib.xml().getValFromPieces(xml,"ALIAS"+a);
			String value=CMLib.xml().getValFromPieces(xml,"ALIASV"+a);
			if((name.length()==0)||(value.length()==0))
				break;
			alias.put(name.toUpperCase().trim(),value);
		}
	}
	
	private void setTitleXML(List<XMLpiece> xml)
	{
		titles.clear();
		int t=-1;
		while((++t)>=0)
		{
			String title=CMLib.xml().getValFromPieces(xml,"TITLE"+t);
			if(title.length()==0)
				break;
			titles.addElement(title);
		}
		
	}
	
	public void setXML(String str)
	{
		account = null;
		if(str==null) 
			return;
		List<XMLLibrary.XMLpiece> xml = CMLib.xml().parseAllXML(str);
		friends=getHashFrom(CMLib.xml().getValFromPieces(xml,"FRIENDS"));
		ignored=getHashFrom(CMLib.xml().getValFromPieces(xml,"IGNORED"));
		introductions=getHashFrom(CMLib.xml().getValFromPieces(xml,"INTROS"));
		String expStr = CMLib.xml().getValFromPieces(xml,"ACCTEXP");
		if((expStr!=null)&&(expStr.length()>0))
			setAccountExpiration(CMath.s_long(expStr));
		else
		{
			Calendar C=Calendar.getInstance();
			C.add(Calendar.DATE,CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS));
			setAccountExpiration(C.getTimeInMillis());
		}
		String oldWrap=CMLib.xml().getValFromPieces(xml,"WRAP");
		if(CMath.isInteger(oldWrap)) wrap=CMath.s_int(oldWrap);
		String oldBreak=CMLib.xml().getValFromPieces(xml,"PAGEBREAK");
		if(CMath.isInteger(oldBreak)) 
			pageBreak=CMath.s_int(oldBreak);
		else
			pageBreak=CMProps.getIntVar(CMProps.SYSTEMI_PAGEBREAK);
		getSetSecurityFlags(CMLib.xml().getValFromPieces(xml,"SECGRPS"));
		setAliasXML(xml);
		setTitleXML(xml);
		String bday=CMLib.xml().getValFromPieces(xml,"BIRTHDAY");
		setBirthday(bday);
		
		poofin=CMLib.xml().getValFromPieces(xml,"POOFIN");
		if(poofin==null) poofin="";
		poofin=CMLib.xml().restoreAngleBrackets(poofin);
		
		poofout=CMLib.xml().getValFromPieces(xml,"POOFOUT");
		if(poofout==null) poofout="";
		poofout=CMLib.xml().restoreAngleBrackets(poofout);
		
		tranpoofin=CMLib.xml().getValFromPieces(xml,"TRANPOOFIN");
		if(tranpoofin==null) tranpoofin="";
		tranpoofin=CMLib.xml().restoreAngleBrackets(tranpoofin);
		
		tranpoofout=CMLib.xml().getValFromPieces(xml,"TRANPOOFOUT");
		if(tranpoofout==null) tranpoofout="";
		tranpoofout=CMLib.xml().restoreAngleBrackets(tranpoofout);
		
		announceMsg=CMLib.xml().getValFromPieces(xml,"ANNOUNCE");
		if(announceMsg==null) announceMsg="";
		announceMsg=CMLib.xml().restoreAngleBrackets(announceMsg);
		
		savedPose=CMLib.xml().getValFromPieces(xml,"POSE");
		if(savedPose==null) savedPose="";
		savedPose=CMLib.xml().restoreAngleBrackets(savedPose);
		
		notes=CMLib.xml().getValFromPieces(xml,"NOTES");
		if(notes==null) notes="";
		notes=CMLib.xml().restoreAngleBrackets(notes);
		
		String dates=CMLib.xml().getValFromPieces(xml,"DATES");
		if(dates==null) dates="";
		// now parse all the level date/times
		int lastNum=Integer.MIN_VALUE;
		levelInfo.clear();
		if(dates.length()>0)
		{
			Vector<String> sets=CMParms.parseSemicolons(dates,true);
			for(int ss=0;ss<sets.size();ss++)
			{
				String sStr=(String)sets.elementAt(ss);
				Vector<String> twin=CMParms.parseCommas(sStr,true);
				if((twin.size()!=2)&&(twin.size()!=3))  continue;
				if(CMath.s_int((String)twin.firstElement())>=lastNum)
				{
					levelInfo.addElement(Integer.valueOf(CMath.s_int((String)twin.firstElement())),
											  Long.valueOf(CMath.s_long((String)twin.elementAt(1))),
											  (twin.size()>2)?(String)twin.elementAt(2):"");
					lastNum=CMath.s_int((String)twin.firstElement());
				}
			}
		}
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		String roomSetStr = CMLib.xml().getValFromPieces(xml,"AREAS");
		if(roomSetStr!=null)
			roomSet().parseXML("<AREAS>"+roomSetStr+"</AREAS>");
		else
			roomSet().parseXML("<AREAS />");
		String[] codes=getStatCodes();
		for(int i=getSaveStatIndex();i<codes.length;i++)
		{
			String val=CMLib.xml().getValFromPieces(xml,codes[i].toUpperCase());
			if(val==null) val="";
			setStat(codes[i].toUpperCase(),CMLib.xml().restoreAngleBrackets(val));
		}
		
		String accountName = CMLib.xml().getValFromPieces(xml,"ACCOUNT");
		if((accountName != null)&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
			account = CMLib.players().getLoadAccount(accountName);
	}

	private String getLevelDateTimesStr()
	{
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		StringBuffer buf=new StringBuffer("");
		for(int ss=0;ss<levelInfo.size();ss++)
		{
			buf.append(((Integer)levelInfo.elementAt(ss,1)).intValue()+",");
			buf.append(((Long)levelInfo.elementAt(ss,2)).longValue()+",");
			buf.append((String)levelInfo.elementAt(ss,3)+";");
		}
		return buf.toString();
	}
	public String getSetSecurityFlags(String newFlags)
	{
		if(newFlags != null)
		{
	        securityFlags=CMSecurity.instance().createGroup("", CMParms.parseSemicolons(newFlags,true));
		}
		return securityFlags.toString(';');
		
	}
	public CMSecurity.SecGroup getSecurityFlags()
	{ 
		return securityFlags;
	}
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut)
	{
		poofin=poofIn;
		poofout=poofOut;
		tranpoofin=tranPoofIn;
		tranpoofout=tranPoofOut;
	}
	
	public long getHygiene(){return hygiene;}
	public void setHygiene(long newVal){hygiene=newVal;}
	public boolean adjHygiene(long byThisMuch)
	{
		hygiene+=byThisMuch;
		if(hygiene<1)
		{
			hygiene=0;
			return false;
		}
		return true;
	}
	

	// Acct Expire Code
	public long getAccountExpiration() {
		return  (account != null) ? account.getAccountExpiration() : accountExpiration;
	}
	public void setAccountExpiration(long newVal)
	{
		if(account != null)
			account.setAccountExpiration(newVal);
		accountExpiration=newVal;
	}
	
	public void addRoomVisit(Room R)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
		&&(R!=null)
		&&(!CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
		&&(!(R.getArea() instanceof AutoGenArea)))
			roomSet().add(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Room R)
	{
		return roomSet().contains(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Area A)
	{
		int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0) return true;
		return roomSet().roomCount(A.Name())>0;
	}
	public void unVisit(Room R)
	{
		if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
			roomSet().remove(CMLib.map().getExtendedRoomID(R));
	}
	public void unVisit(Area A)
	{
		Room R;
		for(Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if(roomSet().contains(CMLib.map().getExtendedRoomID(R)))
				roomSet().remove(CMLib.map().getExtendedRoomID(R));
		}
	}
	
	public int percentVisited(MOB mob, Area A)
	{
		if(A==null)
		{
			long totalRooms=0;
			long totalVisits=0;
			for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				A=(Area)e.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				{
					int[] stats=A.getAreaIStats();
					if(stats[Area.Stats.VISITABLE_ROOMS.ordinal()]>0)
					{
						totalRooms+=stats[Area.Stats.VISITABLE_ROOMS.ordinal()];
						totalVisits+=roomSet().roomCount(A.Name());
					}
				}
			}
			if(totalRooms==0) return 100;
			double pct=CMath.div(totalVisits,totalRooms);
			return (int)Math.round(100.0*pct);
		}
		int numRooms=A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()];
		if(numRooms<=0) return 100;
		double pct=CMath.div(roomSet().roomCount(A.Name()),numRooms);
		return (int)Math.round(100.0*pct);
	}
	
	public long leveledDateTime(int level)
	{
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		long lowest=((Long)levelInfo.elementAt(0,2)).longValue();
		for(int l=1;l<levelInfo.size();l++)
		{
			if(level<((Integer)levelInfo.elementAt(l,1)).intValue())
				return lowest;
			lowest=((Long)levelInfo.elementAt(l,2)).longValue();
		}
		return lowest;
	}
	
	public void setLeveledDateTime(int level, Room R)
	{
		if(levelInfo.size()==0)
			levelInfo.addElement(Integer.valueOf(0),Long.valueOf(System.currentTimeMillis()),"");
		long lastTime=0;
		for(int l=0;l<levelInfo.size();l++)
		{
			if(level==((Integer)levelInfo.elementAt(l,1)).intValue())
			{
				levelInfo.setElementAt(l,2,Long.valueOf(System.currentTimeMillis()));
				levelInfo.setElementAt(l,3,CMLib.map().getExtendedRoomID(R));
				return;
			}
			else
			if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
				return;
			else
			if(level<((Integer)levelInfo.elementAt(l,1)).intValue())
			{
				levelInfo.insertElementAt(l,Integer.valueOf(level),Long.valueOf(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
				return;
			}
			lastTime=((Long)levelInfo.elementAt(l,2)).longValue();
		}
		if((System.currentTimeMillis()-lastTime)<TimeManager.MILI_HOUR)
			return;
		levelInfo.addElement(Integer.valueOf(level),Long.valueOf(System.currentTimeMillis()),CMLib.map().getExtendedRoomID(R));
	}
	
	public PlayerAccount getAccount() { return account;}
	public void setAccount(PlayerAccount account) { this.account = account;}
	public ItemCollection getExtItems() { return extItems; }
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
