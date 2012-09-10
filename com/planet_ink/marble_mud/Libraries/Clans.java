package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.marble_mud.core.threads.ServiceEngine;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.DefaultClan;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.Clan.AutoPromoteFlag;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Function;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.marble_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;


import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2012 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
public class Clans extends StdLibrary implements ClanManager
{
	public SHashtable<String,Clan> all				  =new SHashtable<String,Clan>();
	public long	 		  		   lastGovernmentLoad = 0;
	
	public String ID(){return "Clans";}
	public boolean shutdown()
	{
		for(Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		}
		all.clear();
		return true;
	}

	public boolean isCommonClanRelations(String clanName1, String clanName2, int relation)
	{
		if((clanName1.length()==0)||(clanName2.length()==0)) return relation==Clan.REL_NEUTRAL;
		Clan C1=getClan(clanName1);
		Clan C2=getClan(clanName2);
		if((C1==null)||(C2==null)) return relation==Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(clanName2);
		int i2=C2.getClanRelations(clanName1);
		if((i1==i2)
		&&((i1==Clan.REL_WAR)
		   ||(i1==Clan.REL_ALLY)))
		   return i1==relation;
		for(Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((C!=C1)&&(C!=C2))
			{
				if((i1!=Clan.REL_WAR)
				&&(C1.getClanRelations(C.clanID())==Clan.REL_ALLY)
				&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR))
					i1=Clan.REL_WAR;
				if((i2!=Clan.REL_WAR)
				&&(C2.getClanRelations(C.clanID())==Clan.REL_ALLY)
				&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR))
					i2=Clan.REL_WAR;
			}
		}
		if(i1==i2) return relation==i1;
		
		if(Clan.REL_NEUTRALITYGAUGE[i1]<Clan.REL_NEUTRALITYGAUGE[i2]) return relation==i1;
		return relation==i2;
	}
	
	public int getClanRelations(String clanName1, String clanName2)
	{
		if((clanName1.length()==0)||(clanName2.length()==0)) return Clan.REL_NEUTRAL;
		Clan C1=getClan(clanName1);
		Clan C2=getClan(clanName2);
		if((C1==null)||(C2==null)) return Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(clanName2);
		int i2=C2.getClanRelations(clanName1);
		int rel=Clan.RELATIONSHIP_VECTOR[i1][i2];
		if(rel==Clan.REL_WAR) return Clan.REL_WAR;
		if(rel==Clan.REL_ALLY) return Clan.REL_ALLY;
		for(Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((C!=C1)
			&&(C!=C2)
			&&(((C1.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR)))
					return Clan.REL_WAR;
		}
		return rel;
	}

	public Clan getClan(String id)
	{
		if(id.length()==0) return null;
		Clan C=(Clan)all.get(id.toUpperCase());
		if(C!=null) return C;
		for(Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			C=(Clan)e.nextElement();
			if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
				return C;
		}
		return null;
	}
	public Clan findClan(String id)
	{
		Clan C=getClan(id);
		if(C!=null) return C;
		for(Enumeration<Clan> e=all.elements();e.hasMoreElements();)
		{
			C=(Clan)e.nextElement();
			if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
				return C;
		}
		return null;
	}

	public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members) {
		if(M == null)
			return false;
		if(members.contains(M.Name()))
			return true;
		if((M.getLiegeID().length()>0)
		&&(M.isMarriedToLiege())
		&&(members.contains(M.getLiegeID())))
			return true;
		for(Enumeration<MOB.Tattoo> e=M.tattoos();e.hasMoreElements();)
		{
			MOB.Tattoo T=e.nextElement();
			if(T.tattooName.startsWith("PARENT:"))
			{
				String name=T.tattooName.substring("PARENT:".length());
				MOB M2=CMLib.players().getLoadPlayer(name.toLowerCase());
				if((M2 != null)&&isFamilyOfMembership(M2,members))
					return true;
			}
		}
		return false;
	}
	
	public Enumeration<Clan> clans()
	{
		return all.elements();
	}
	public int numClans()
	{
		return all.size();
	}
	public void addClan(Clan C)
	{
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
			CMLib.threads().startTickDown(C,Tickable.TICKID_CLAN,(int)CMProps.getTicksPerDay());
		all.put(C.clanID().toUpperCase(),C);
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "+"+C.name()));
	}
	public void removeClan(Clan C)
	{
		CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		all.remove(C.clanID().toUpperCase());
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "-"+C.name()));
	}

	public void tickAllClans()
	{
		for(Enumeration<Clan> e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			C.tick(C,Tickable.TICKID_CLAN);
		}
	}
	
	public void clanAnnounceAll(String msg)
	{
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel((String)channels.get(i),"ALL",msg,true);
	}

	public Enumeration<String> clansNames(){return all.keys();}
	public String translatePrize(int trophy)
	{
		String prizeStr="";
		switch(trophy)
		{
			case Clan.TROPHY_AREA: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
			case Clan.TROPHY_CONTROL: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
			case Clan.TROPHY_EXP: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
			case Clan.TROPHY_PK: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPPK); break;
		}
		if(prizeStr.length()==0) return "None";
		if(prizeStr.length()>0)
		{
			Vector<String> V=CMParms.parse(prizeStr);
			if(V.size()>=2)
			{
				String type=((String)V.lastElement()).toUpperCase();
				String amt=(String)V.firstElement();
				if("EXPERIENCE".startsWith(type))
					return amt+" experience point bonus.";
			}
		}
		return prizeStr;
	}
	public boolean trophySystemActive()
	{
		return (CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
			|| (CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
			|| (CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
			|| (CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0);
		
	}
	
	public boolean goForward(MOB mob, Clan C, Vector<? extends Object> commands, Clan.Function function, boolean voteIfNecessary)
	{
		if((mob==null)||(C==null)) return false;
		Clan.Authority allowed=C.getAuthority(mob.getClanRole(),function);
		if(allowed==Clan.Authority.CAN_DO) return true;
		if(allowed==Clan.Authority.CAN_NOT_DO) return false;
		if(function==Clan.Function.ASSIGN)
		{
			if(C.getAuthority(mob.getClanRole(),Clan.Function.VOTE_ASSIGN)!=Clan.Authority.CAN_DO)
			   return false;
		}
		else
		if(C.getAuthority(mob.getClanRole(),Clan.Function.VOTE_OTHER)!=Clan.Authority.CAN_DO)
		   return false;
		if(!voteIfNecessary) return true;
		String matter=CMParms.combine(commands,0);
		for(Enumeration<Clan.ClanVote> e=C.votes();e.hasMoreElements();)
		{
			Clan.ClanVote CV=(Clan.ClanVote)e.nextElement();
			if((CV.voteStarter.equalsIgnoreCase(mob.Name()))
			&&(CV.voteStatus==Clan.VSTAT_STARTED))
			{
				mob.tell("This matter must be voted upon, but you already have a vote underway.");
				return false;
			}
			if(CV.matter.equalsIgnoreCase(matter))
			{
				mob.tell("This matter must be voted upon, and is already BEING voted upon.  Use CLANVOTE to see.");
				return false;
			}
		}
		if(mob.session()==null) return false;
		try{
			int numVotes=C.getNumVoters(function);
			if(numVotes==1) return true;

			if(mob.session().confirm("This matter must be voted upon.  Would you like to start the vote now (y/N)?","N"))
			{
				Clan.ClanVote CV=new Clan.ClanVote();
				CV.matter=matter;
				CV.voteStarter=mob.Name();
				CV.function=function.ordinal();
				CV.voteStarted=System.currentTimeMillis();
				CV.votes=new DVector(2);
				CV.voteStatus=Clan.VSTAT_STARTED;
				C.addVote(CV);
				C.updateVotes();
				final Clan.Function voteFunctionType = (function == Clan.Function.ASSIGN) ? Clan.Function.VOTE_ASSIGN : Clan.Function.VOTE_OTHER;
				final List<Integer> votingRoles = new Vector<Integer>();
				for(int i=0;i<C.getRolesList().length;i++)
					if(C.getAuthority(i, voteFunctionType)==Clan.Authority.CAN_DO)
						votingRoles.add(Integer.valueOf(i));
				if(votingRoles.size()>0)
				{
					final String firstRoleName = C.getRoleName(votingRoles.iterator().next().intValue(), true, true);
					final String rest = " "+firstRoleName+" should use CLANVOTE to participate.";
					if(votingRoles.size() >= (C.getRolesList().length-2))
					{
						if(function == Clan.Function.ASSIGN)
							clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new election to vote upon. "+rest);
						else
							clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
					}
					else
					if(votingRoles.size()==1)
						clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
					else
					{
						final String[] roleNames = new String[votingRoles.size()];
						for(int i=0;i<votingRoles.size();i++)
						{
							Integer roleID=votingRoles.get(i);
							roleNames[i]=C.getRoleName(roleID.intValue(), true, true);
						}
						String list = CMLib.english().toEnglishStringList(roleNames);
						clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "
								+list+" should use CLANVOTE to participate.");
					}
				}
				mob.tell("Your vote has started.  Use CLANVOTE to cast your vote.");
				return false;
			}
		}
		catch(java.io.IOException e){}
		mob.tell("Without a vote, this command can not be executed.");
		return false;
	}

	protected String indt(int x)
	{
		return CMStrings.SPACES.substring(0,x*4);
	}

	public long getLastGovernmentLoad()
	{
		return lastGovernmentLoad;
	}

	public String getGovernmentHelp(MOB mob, String named, boolean exact)
	{
		ClanGovernment helpG=null;
		for(ClanGovernment G : getStockGovernments())
			if(G.getName().equalsIgnoreCase(named))
				helpG=G;
		if((helpG==null)&&(exact)) return null;
		if(helpG==null)
			for(ClanGovernment G : getStockGovernments())
				if(G.getName().toUpperCase().startsWith(named.toUpperCase()))
					helpG=G;
		if(helpG==null)
		{
			List<ClanGovernment> gtypes=new Vector<ClanGovernment>();
			String name=null;
			for(ClanGovernment G : getStockGovernments())
				for(ClanPosition P : G.getPositions())
					if(P.getName().equalsIgnoreCase(named)||P.getPluralName().equalsIgnoreCase(named))
					{
						gtypes.add(G);
						name=P.getName();
					}
			if(gtypes.size()==0)
			{
				if(exact) return null;
				for(ClanGovernment G : getStockGovernments())
					for(ClanPosition P : G.getPositions())
						if(P.getName().toUpperCase().startsWith(named.toUpperCase())
							||P.getPluralName().toUpperCase().startsWith(named.toUpperCase()))
						{
							gtypes.add(G);
							name=P.getName();
						}
			}
			if(gtypes.size()==0)
				return null;
			String[] typeNames=new String[gtypes.size()];
			for(int g=0;g<gtypes.size();g++)
				typeNames[g]=gtypes.get(g).getName();
			return "The "+name+" is a rank or position within the following clan types: "
				   +CMLib.english().toEnglishStringList(typeNames)
				   +".  Please see help on CLAN or on one of the listed clan types for more information. ";
		}
		return helpG.getHelpStr();
	}
	
	public ClanGovernment createSampleGovernment()
	{
		Authority[] pows1=new Authority[Function.values().length];
		for(int i=0;i<pows1.length;i++) pows1[i]=Authority.CAN_NOT_DO;
		Authority[] pows2=new Authority[Function.values().length];
		for(int i=0;i<pows2.length;i++) pows2[i]=Authority.CAN_DO;
		ClanPosition P1=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P1.setID("APPLICANT");
		P1.setRoleID(0);
		P1.setRank(0);
		P1.setName("Applicant");
		P1.setPluralName("Applicants");
		P1.setMax(Integer.MAX_VALUE);
		P1.setInnerMaskStr("");
		P1.setFunctionChart(pows1);
		P1.setPublic(false);
		ClanPosition P2=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
		P2.setID("MEMBER");
		P2.setRoleID(1);
		P2.setRank(1);
		P2.setName("Member");
		P2.setPluralName("Members");
		P2.setMax(Integer.MAX_VALUE);
		P2.setInnerMaskStr("");
		P2.setFunctionChart(pows2);
		P2.setPublic(false);
		Set<Integer> usedTypeIDs=new HashSet<Integer>();
		ClanGovernment[] gvts=(ClanGovernment[])Resources.getResource("parsed_clangovernments");
		int id=0;
		if(gvts!=null)
		{
			for(ClanGovernment G2 : gvts)
				usedTypeIDs.add(Integer.valueOf(G2.getID()));
			for(int i=0;i<gvts.length;i++)
				if(!usedTypeIDs.contains(Integer.valueOf(i)))
				{
					id=i; break;
				}
		}
		
		ClanGovernment G=(ClanGovernment)CMClass.getCommon("DefaultClanGovernment");
		G.setID(id);
		G.setName("Sample Govt");
		G.setPositions(new ClanPosition[]{P1,P2});
		G.setAutoRole(0);
		G.setAcceptPos(1);
		G.setRequiredMaskStr("");
		G.setAutoPromoteBy(AutoPromoteFlag.NONE);
		G.setPublic(true);
		G.setFamilyOnly(false);
		G.setOverrideMinMembers(Integer.valueOf(1));
		G.setConquestEnabled(true);
		G.setConquestItemLoyalty(true);
		G.setConquestByWorship(false);
		G.setShortDesc("Change Me!");
		G.setLongDesc("");
		G.setMaxVoteDays(10);
		G.setVoteQuorumPct(66);
		G.setDefault(true);
		return G;
	}
	
	
	public void reSaveGovernmentsXML()
	{
		ClanGovernment[] govt = getStockGovernments();
		String xml = makeGovernmentXML(govt);
		if(!Resources.updateFileResource("clangovernments.xml", xml))
		{
			Log.errOut("Clans","Can't save clangovernments.xml");
		}
		Resources.removeResource("parsed_clangovernments");
		getStockGovernments();
	}
	
	public ClanGovernment createGovernment(String name)
	{
		ClanGovernment[] gvts=getStockGovernments();
		List<ClanGovernment> govts = new SVector<ClanGovernment>(gvts);
		for(ClanGovernment G : gvts)
			if(G.getName().equalsIgnoreCase(name))
				return null;
		ClanGovernment newG=createSampleGovernment();
		Set<Integer> takenIDs=new HashSet<Integer>();
		for(ClanGovernment g : gvts)
			takenIDs.add(Integer.valueOf(g.getID()));
		int newID=CMLib.dice().roll(1, Integer.MAX_VALUE, 0);
		for(int i=0;i<gvts.length+1;i++)
			if(!takenIDs.contains(Integer.valueOf(i)))
				newID=i;
		newG.setID(newID);
		newG.setName(name);
		govts.add(newG);
		Resources.submitResource("parsed_clangovernments", govts.toArray(new ClanGovernment[0]));
		return newG;
	}
	
	public boolean removeGovernment(ClanGovernment government)
	{
		ClanGovernment[] gvts=getStockGovernments();
		if(gvts.length==1) return false;
		List<ClanGovernment> govts = new SVector<ClanGovernment>(gvts);
		govts.remove(government);
		if(govts.size()==gvts.length) return false;
		Resources.submitResource("parsed_clangovernments", govts.toArray(new ClanGovernment[0]));
		return true;
	}
	
	public ClanGovernment[] getStockGovernments()
	{
		ClanGovernment[] gvts=(ClanGovernment[])Resources.getResource("parsed_clangovernments");
		if(gvts==null)
		{
			synchronized(this)
			{
				if(gvts==null)
				{
					StringBuffer str=Resources.getFileResource("clangovernments.xml", true);
					if(str==null)
						gvts=new ClanGovernment[0];
					else
					{
						gvts=parseGovernmentXML(str);
					}
					if((gvts==null)||(gvts.length==0))
					{
						ClanGovernment gvt=createSampleGovernment();
						gvt.setDefault(true);
						gvts=new ClanGovernment[]{gvt};
					}
					lastGovernmentLoad=System.currentTimeMillis();
					Resources.submitResource("parsed_clangovernments",gvts);
				}
			}
		}
		return gvts;
	}
	
	public ClanGovernment getDefaultGovernment()
	{
		final ClanGovernment[] gvts=getStockGovernments();
		for(ClanGovernment gvt : gvts)
		{
			if(gvt.isDefault())
				return gvt;
		}
		return gvts[0];
	}
	
	public ClanGovernment getStockGovernment(int typeid)
	{
		final ClanGovernment[] gvts=getStockGovernments();
		if(gvts.length <= typeid)
		{
			Log.errOut("Clans","Someone mistakenly requested stock government typeid "+typeid);
			return gvts[0];
		}
		return gvts[typeid];
	}
	
	public String makeGovernmentXML(ClanGovernment gvt)
	{
		final StringBuilder str=new StringBuilder("");
		str.append("<CLANTYPE ").append("TYPEID="+gvt.getID()+" ").append("NAME=\""+gvt.getName()+"\">\n");
		if(gvt.isDefault())
			str.append(indt(1)).append("<ISDEFAULT>true</ISDEFAULT>\n");
		str.append(indt(1)).append("<SHORTDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.getShortDesc())).append("</SHORTDESC>\n");
		str.append(indt(1)).append("<LONGDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.getLongDesc())).append("</LONGDESC>\n");
		str.append(indt(1)).append("<POSITIONS>\n");
		Set<Clan.Function> voteSet = new HashSet<Clan.Function>(); 
		for(int p=gvt.getPositions().length-1;p>=0;p--)
		{
			ClanPosition pos = gvt.getPositions()[p];
			str.append(indt(2)).append("<POSITION ").append("ID=\""+pos.getID()+"\" ").append("ROLEID="+pos.getRoleID()+" ")
								.append("RANK="+pos.getRank()+" ").append("NAME=\""+pos.getName()+"\" ").append("PLURAL=\""+pos.getPluralName()+"\" ")
								.append("MAX="+pos.getMax()+" ").append("INNERMASK=\""+CMLib.xml().parseOutAngleBrackets(pos.getInnerMaskStr())+"\" ")
								.append("PUBLIC=\""+pos.isPublic()+"\">\n");
			for(Clan.Function func : Clan.Function.values()) 
				if(pos.getFunctionChart()[func.ordinal()]==Clan.Authority.CAN_DO)
					str.append(indt(3)).append("<POWER>").append(func.toString()).append("</POWER>\n");
				else
				if(pos.getFunctionChart()[func.ordinal()]==Clan.Authority.MUST_VOTE_ON)
					voteSet.add(func);
			str.append(indt(2)).append("</POSITION>\n");
		}
		str.append(indt(1)).append("</POSITIONS>\n");
		if(voteSet.size()==0)
			str.append(indt(1)).append("<VOTING />\n");
		else
		{
			str.append(indt(1)).append("<VOTING ").append("MAXDAYS="+gvt.getMaxVoteDays()+" QUORUMPCT="+gvt.getVoteQuorumPct()+">\n");
			for(Clan.Function func : voteSet)
				str.append(indt(2)).append("<POWER>").append(func.toString()).append("</POWER>\n");
			str.append(indt(1)).append("</VOTING>\n");
		}
		str.append(indt(1)).append("<AUTOPOSITION>").append(gvt.getPositions()[gvt.getAutoRole()].getID()).append("</AUTOPOSITION>\n");
		str.append(indt(1)).append("<ACCEPTPOSITION>").append(gvt.getPositions()[gvt.getAcceptPos()].getID()).append("</ACCEPTPOSITION>\n");
		str.append(indt(1)).append("<REQUIREDMASK>").append(CMLib.xml().parseOutAngleBrackets(gvt.getRequiredMaskStr())).append("</REQUIREDMASK>\n");
		str.append(indt(1)).append("<AUTOPROMOTEBY>").append(gvt.getAutoPromoteBy().toString()).append("</AUTOPROMOTEBY>\n");
		str.append(indt(1)).append("<PUBLIC>").append(gvt.isPublic()).append("</PUBLIC>\n");
		str.append(indt(1)).append("<FAMILYONLY>").append(gvt.isFamilyOnly()).append("</FAMILYONLY>\n");
		str.append(indt(1)).append("<XPPERLEVELFORMULA>").append(gvt.getXpCalculationFormulaStr()).append("</XPPERLEVELFORMULA>\n");
		if(gvt.getOverrideMinMembers() == null)
			str.append(indt(1)).append("<OVERRIDEMINMEMBERS />\n");
		else
			str.append(indt(1)).append("<OVERRIDEMINMEMBERS>").append(gvt.getOverrideMinMembers().toString()).append("</OVERRIDEMINMEMBERS>\n");
		str.append(indt(1)).append("<CONQUEST>\n");
		{
			str.append(indt(2)).append("<ENABLED>").append(gvt.isConquestEnabled()).append("</ENABLED>\n");
			str.append(indt(2)).append("<ITEMLOYALTY>").append(gvt.isConquestItemLoyalty()).append("</ITEMLOYALTY>\n");
			str.append(indt(2)).append("<DEITYBASIS>").append(gvt.isConquestByWorship()).append("</DEITYBASIS>\n");
		}
		str.append(indt(1)).append("</CONQUEST>\n");
		gvt.getClanLevelAbilities(Integer.valueOf(Integer.MAX_VALUE));
		final Enumeration<AbilityMapping> m= CMLib.ableMapper().getClassAbles(gvt.ID(), false);
		if(!m.hasMoreElements())
			str.append(indt(1)).append("<ABILITIES />\n");
		else
		{
			str.append(indt(1)).append("<ABILITIES>\n");
			for(;m.hasMoreElements();)
			{
				AbilityMapping map=m.nextElement();
				str.append(indt(2)).append("<ABILITY ID=\""+map.abilityID+"\" PROFF="+map.defaultProficiency+" LEVEL="+map.qualLevel+" QUALIFYONLY="+(!map.autoGain)+" />\n");
			}
			str.append(indt(1)).append("</ABILITIES>\n");
		}
		final List<Ability> effectList = gvt.getClanLevelEffects(null, Integer.valueOf(Integer.MAX_VALUE));
		if(effectList.size()==0)
			str.append(indt(1)).append("<EFFECTS />\n");
		else
		{
			str.append(indt(1)).append("<EFFECTS>\n");
			for(int a=0;a<effectList.size();a++)
			{
				Ability A=effectList.get(a);
				int lvl = CMath.s_int(gvt.getStat("GETREFFLVL"+a));
				str.append(indt(2)).append("<EFFECT ID=\""+A.ID()+"\" LEVEL="+lvl+" PARMS=\""+CMLib.xml().parseOutAngleBrackets(A.text())+"\" />\n");
			}
			str.append(indt(1)).append("</EFFECTS>\n");
		}
		
		str.append("</CLANTYPE>\n");
		return str.toString();
	}
	
	public String makeGovernmentXML(ClanGovernment gvts[])
	{
		final StringBuilder str=new StringBuilder("");
		str.append("<CLANTYPES>\n");
		for(ClanGovernment gvt : gvts)
			str.append(makeGovernmentXML(gvt));
		str.append("</CLANTYPES>\n");
		return str.toString();
	}
	
	public ClanGovernment[] parseGovernmentXML(StringBuffer xml)
	{
		List<XMLLibrary.XMLpiece> xmlV = CMLib.xml().parseAllXML(xml);
		XMLLibrary.XMLpiece clanTypesTag = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPES");
		List<XMLLibrary.XMLpiece> clanTypes = null;
		if(clanTypesTag != null)
			clanTypes = clanTypesTag.contents;
		else
		{
			XMLLibrary.XMLpiece clanType = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPE");
			if(clanType != null)
			{
				clanTypes = new SVector<XMLLibrary.XMLpiece>();
				clanTypes.add(clanType);
			}
			else
			{
				Log.errOut("Clans","No CLANTYPES found in xml"); 
				return null;
			}
		}
		 
		List<ClanGovernment> governments=new SVector<ClanGovernment>();
		for(XMLLibrary.XMLpiece clanTypePieceTag : clanTypes)
		{
			final String typeName=clanTypePieceTag.parms.get("NAME");
			final int typeID=CMath.s_int(clanTypePieceTag.parms.get("TYPEID"));
			final boolean isDefault=CMath.s_bool(clanTypePieceTag.parms.get("ISDEFAULT"));
			
			Authority[]	baseFunctionChart = new Authority[Function.values().length];
			for(int i=0;i<Function.values().length;i++)
				baseFunctionChart[i]=Authority.CAN_NOT_DO;
			XMLLibrary.XMLpiece votingTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "VOTING");
			int maxVotingDays = CMath.s_int(votingTag.parms.get("MAXDAYS"));
			int minVotingPct = CMath.s_int(votingTag.parms.get("QUORUMPCT"));
			for(XMLLibrary.XMLpiece piece : votingTag.contents)
			{
				if(piece.tag.equalsIgnoreCase("POWER"))
				{
					Function power = (Function)CMath.s_valueOf(Clan.Function.values(),piece.value);
					if(power == null)
						Log.errOut("Clans","Illegal power found in xml: "+piece.value);
					else
						baseFunctionChart[power.ordinal()] = Authority.MUST_VOTE_ON;
				}
			}
			
			final List<ClanPosition> positions=new SVector<ClanPosition>();
			XMLLibrary.XMLpiece positionsTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "POSITIONS");
			for(XMLLibrary.XMLpiece posPiece : positionsTag.contents)
			{
				if(posPiece.tag.equalsIgnoreCase("POSITION"))
				{
					Authority[]	functionChart = baseFunctionChart.clone();
					final String ID=posPiece.parms.get("ID");
					final int roleID=CMath.s_int(posPiece.parms.get("ROLEID"));
					final int rank=CMath.s_int(posPiece.parms.get("RANK"));
					final String name=posPiece.parms.get("NAME");
					final String pluralName=posPiece.parms.get("PLURAL");
					final int max=CMath.s_int(posPiece.parms.get("MAX"));
					final boolean isPublic=CMath.s_bool(posPiece.parms.get("PUBLIC"));
					final String innerMaskStr=CMLib.xml().restoreAngleBrackets(posPiece.parms.get("INNERMASK"));
					for(XMLLibrary.XMLpiece powerPiece : posPiece.contents)
					{
						if(powerPiece.tag.equalsIgnoreCase("POWER"))
						{
							Function power = (Function)CMath.s_valueOf(Clan.Function.values(),powerPiece.value);
							if(power == null)
								Log.errOut("Clans","Illegal power found in xml: "+powerPiece.value);
							else
								functionChart[power.ordinal()] = Authority.CAN_DO;
						}
					}
					ClanPosition P=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
					P.setID(ID);
					P.setRoleID(roleID);
					P.setRank(rank);
					P.setName(name);
					P.setPluralName(pluralName);
					P.setMax(max);
					P.setInnerMaskStr(innerMaskStr);
					P.setFunctionChart(functionChart);
					P.setPublic(isPublic);
					positions.add(P);
				}
			}
			ClanPosition[] posArray = new ClanPosition[positions.size()];
			for(ClanPosition pos : positions)
				if((pos.getRoleID()>=0)&&(pos.getRoleID()<positions.size()))
					if(posArray[pos.getRoleID()]!=null)
					{
						Log.errOut("Clans","Bad ROLEID "+pos.getRoleID()+" in positions list in "+typeName);
						posArray=new ClanPosition[0];
						break;
					}
					else
					{
						posArray[pos.getRoleID()]=pos;
					}
			if(posArray.length==0)
			{
				Log.errOut("Clans","Missing positions in "+typeName);
				continue;
			}
			String	autoRoleStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "AUTOPOSITION");
			ClanPosition autoRole=null;
			for(ClanPosition pos : positions)
				if(pos.getID().equalsIgnoreCase(autoRoleStr) )
					autoRole=pos;
			if(autoRole==null)
			{
				Log.errOut("Clans","Illegal role found in xml: "+autoRoleStr);
				continue;
			}
			String	acceptRoleStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "ACCEPTPOSITION");
			ClanPosition acceptRole=null;
			for(ClanPosition pos : positions)
				if(pos.getID().equalsIgnoreCase(acceptRoleStr) )
					acceptRole=pos;
			if(acceptRole==null)
			{
				Log.errOut("Clans","Illegal acceptRole found in xml: "+acceptRoleStr);
				continue;
			}
			String requiredMaskStr=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "REQUIREDMASK"));
			String shortDesc=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "SHORTDESC"));
			String longDesc=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "LONGDESC"));
			String autoPromoteStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "AUTOPROMOTEBY");
			Clan.AutoPromoteFlag autoPromote = AutoPromoteFlag.valueOf(autoPromoteStr);
			if(autoPromote==null)
			{
				Log.errOut("Clans","Illegal AUTOPROMOTEBY found in xml: "+autoPromoteStr);
				continue;
			}
			boolean isPublic=CMath.s_bool(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "PUBLIC"));
			boolean isFamilyOnly=CMath.s_bool(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "FAMILYONLY"));
			String	overrideMinMembersStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "OVERRIDEMINMEMBERS");
			Integer overrideMinMembers = null;
			if((overrideMinMembersStr!=null)&&CMath.isInteger(overrideMinMembersStr))
				overrideMinMembers=Integer.valueOf(CMath.s_int(overrideMinMembersStr));
			String xpPerLevelFormulaStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "XPPERLEVELFORMULA");
			XMLLibrary.XMLpiece conquestTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "CONQUEST");
			boolean conquestEnabled=true;
			boolean conquestItemLoyalty=true;
			boolean conquestDeityBasis=false;
			if(conquestTag!=null)
			{
				conquestEnabled=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "ENABLED"));
				conquestItemLoyalty=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "ITEMLOYALTY"));
				conquestDeityBasis=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "DEITYBASIS"));
			}
			ClanGovernment G=(ClanGovernment)CMClass.getCommon("DefaultClanGovernment");
			G.setID(typeID);
			G.setName(typeName);
			G.setPositions(posArray);
			G.setAutoRole(autoRole.getRoleID());
			G.setAcceptPos(acceptRole.getRoleID());
			G.setRequiredMaskStr(requiredMaskStr);
			G.setAutoPromoteBy(autoPromote);
			G.setPublic(isPublic);
			G.setFamilyOnly(isFamilyOnly);
			G.setOverrideMinMembers(overrideMinMembers);
			G.setConquestEnabled(conquestEnabled);
			G.setConquestItemLoyalty(conquestItemLoyalty);
			G.setConquestByWorship(conquestDeityBasis);
			G.setShortDesc(shortDesc);
			G.setLongDesc(longDesc);
			G.setXpCalculationFormulaStr(xpPerLevelFormulaStr);
			G.setMaxVoteDays(maxVotingDays);
			G.setVoteQuorumPct(minVotingPct);
			G.setDefault(isDefault);

			XMLLibrary.XMLpiece abilitiesTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "ABILITIES");
			if((abilitiesTag!=null)&&(abilitiesTag.contents!=null)&&(abilitiesTag.contents.size()>0))
			{
				G.setStat("NUMRABLE", Integer.toString(abilitiesTag.contents.size()));
				for(int x=0;x<abilitiesTag.contents.size();x++)
				{
					XMLLibrary.XMLpiece able = abilitiesTag.contents.get(x);
					G.setStat("GETRABLE"+x, able.parms.get("ID"));
					G.setStat("GETRABLEPROF"+x, able.parms.get("PROFF"));
					G.setStat("GETRABLEQUAL"+x, able.parms.get("QUALIFYONLY"));
					G.setStat("GETRABLELVL"+x, able.parms.get("LEVEL"));
				}
			}
			XMLLibrary.XMLpiece effectsTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "EFFECTS");
			if((effectsTag!=null)&&(effectsTag.contents!=null)&&(effectsTag.contents.size()>0))
			{
				G.setStat("NUMREFF", Integer.toString(effectsTag.contents.size()));
				for(int x=0;x<effectsTag.contents.size();x++)
				{
					XMLLibrary.XMLpiece able = effectsTag.contents.get(x);
					G.setStat("GETREFF"+x, able.parms.get("ID"));
					G.setStat("GETREFFPARM"+x, CMLib.xml().restoreAngleBrackets(able.parms.get("PARMS")));
					G.setStat("GETREFFLVL"+x, able.parms.get("LEVEL"));
				}
			}
			governments.add(G);
		}
		ClanGovernment[] govts=new ClanGovernment[governments.size()];
		for(ClanGovernment govt : governments)
			if((govt.getID() < 0)||(govt.getID() >=governments.size()) || (govts[govt.getID()]!=null))
			{
				Log.errOut("Clans","Bad TYPEID "+govt.getID());
				return new ClanGovernment[0];
			}
			else
				govts[govt.getID()]=govt;
		
		if(governments.size()>0)
		{
			for(int i=0;i<govts.length;i++)
				if(govts[i]==null)
					govts[i]=governments.get(0);
			return govts;
		}
		else
		{
			return null;
		}
	}

	public void clanAnnounce(MOB mob, String msg)
	{
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(mob,(String)channels.get(i),msg,true);
	}
	
	public boolean authCheck(String clanID, int roleID, Function function) 
	{
		if((clanID==null)||(clanID.length()==0)) return false;
		Clan C=(Clan)all.get(clanID.toUpperCase());
		if(C==null) return false;
		return C.getAuthority(roleID, function)!=Clan.Authority.CAN_NOT_DO;
	}
}
