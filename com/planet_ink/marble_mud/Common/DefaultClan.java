package com.planet_ink.marble_mud.Common;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.threads.ServiceEngine;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Function;
import com.planet_ink.marble_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.marble_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.marble_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
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
public class DefaultClan implements Clan
{
	public String ID(){return "DefaultClan";}
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	protected String clanName="";
	protected String clanPremise="";
	protected String clanRecall="";
	protected String clanMorgue="";
	protected String clanClass="";
	protected int	 clanLevel=0;
	protected String clanDonationRoom="";
	protected int 	 clanTrophies=0;
	protected int 	 autoPosition=-1;
	protected String acceptanceSettings="";
	protected int 	 clanStatus=0;
	protected String lastClanKillRecord=null;
	protected double taxRate=0.0;
	protected volatile long exp=0;
	protected Object expSync = new Object();
	protected Vector<ClanVote> voteList=null;
	protected Vector<Long> clanKills=new Vector<Long>();

	//*****************
	public Hashtable<String,long[]> relations=new Hashtable<String,long[]>();
	public int government=0;
	public long lastGovernmentLoadTime=-1;
	public ClanGovernment govt = null;
	//*****************
	
	protected final static List<Ability> empty=new LinkedList<Ability>(); 

	/** return a new instance of the object*/
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClan();}}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			return (Clan)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultClan();
		}
	}

	public ClanGovernment getGovernment() { return govt();}
	
	protected ClanGovernment govt() 
	{
		if((govt != null) && ((government < 0) || (lastGovernmentLoadTime == CMLib.clans().getLastGovernmentLoad())))
			return govt;
		else
		{
			
			ClanGovernment govt = CMLib.clans().getStockGovernment(government);
			if(govt == null)
			{
				govt = CMLib.clans().getDefaultGovernment();
				government = govt.getID();
			}
			lastGovernmentLoadTime = CMLib.clans().getLastGovernmentLoad();
			return govt;
		}
	}
	
	private synchronized void clanKills()
	{
		if(lastClanKillRecord==null)
		{
			List<PlayerData> V=CMLib.database().DBReadData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS");
			clanKills.clear();
			if(V.size()==0)
				lastClanKillRecord="";
			else
			{
				lastClanKillRecord=((DatabaseEngine.PlayerData)V.get(0)).xml;
				Vector<String> V2=CMParms.parseSemicolons(lastClanKillRecord,true);
				for(int v=0;v<V2.size();v++)
					clanKills.addElement(Long.valueOf(CMath.s_long((String)V2.elementAt(v))));
			}
		}
	}

	private void updateClanKills()
	{
		Long date=null;
		StringBuffer str=new StringBuffer("");
		for(int i=clanKills.size()-1;i>=0;i--)
		{
			date=(Long)clanKills.elementAt(i);
			if(date.longValue()<(System.currentTimeMillis()))
				clanKills.removeElementAt(i);
			else
				str.append(date.longValue()+";");
		}
		if((lastClanKillRecord==null)||(!lastClanKillRecord.equals(str.toString())))
		{
			lastClanKillRecord=str.toString();
			CMLib.database().DBReCreateData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS",str.toString());
		}
	}

	public void updateVotes()
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration<ClanVote> e=votes();e.hasMoreElements();)
		{
			ClanVote CV=(ClanVote)e.nextElement();
			str.append(CMLib.xml().convertXMLtoTag("BY",CV.voteStarter));
			str.append(CMLib.xml().convertXMLtoTag("FUNC",CV.function));
			str.append(CMLib.xml().convertXMLtoTag("ON",""+CV.voteStarted));
			str.append(CMLib.xml().convertXMLtoTag("STATUS",""+CV.voteStatus));
			str.append(CMLib.xml().convertXMLtoTag("CMD",CV.matter));
			if((CV.votes!=null)&&(CV.votes.size()>0))
			{
				str.append("<VOTES>");
				for(int v=0;v<CV.votes.size();v++)
				{
					str.append("<VOTE>");
					str.append(CMLib.xml().convertXMLtoTag("BY",(String)CV.votes.elementAt(v,1)));
					str.append(CMLib.xml().convertXMLtoTag("YN",""+((Boolean)CV.votes.elementAt(v,2)).booleanValue()));
					str.append("</VOTE>");
				}
				str.append("</VOTES>");
			}
		}
		if(str.length()>0)
			CMLib.database().DBReCreateData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
		else
			CMLib.database().DBDeleteData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
	}
	public void addVote(ClanVote CV)
	{
		if(!(CV instanceof ClanVote))
			return;
		votes();
		voteList.add((ClanVote)CV);
	}
	public void delVote(ClanVote CV)
	{
		votes();
		voteList.remove(CV);
	}

	public void recordClanKill()
	{
		clanKills();
		clanKills.addElement(Long.valueOf(System.currentTimeMillis()));
		updateClanKills();
	}
	public int getCurrentClanKills()
	{
		clanKills();
		return clanKills.size();
	}

	public boolean isOnlyFamilyApplicants()
	{
		return govt().isFamilyOnly();
	}

	public boolean isLoyaltyThroughItems()
	{
		return govt().isConquestItemLoyalty();
	}

	public boolean isWorshipConquest()
	{
		return govt().isConquestByWorship();
	}

	public long calculateMapPoints()
	{
		return calculateMapPoints(getControlledAreas());
	}
	public long calculateMapPoints(List<Area> controlledAreas)
	{
		long points=0;
		if(controlledAreas!=null)
			for(Area A : controlledAreas)
			{
				LegalBehavior B=CMLib.law().getLegalBehavior(A);
				if(B!=null)
					points+=B.controlPoints();
			}
		return points;
	}

	public List<Area> getControlledAreas()
	{
		Vector<Area> done=new Vector<Area>();
		for(Enumeration<Area> e=CMLib.map().sortedAreas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			LegalBehavior B=CMLib.law().getLegalBehavior(A);
			if(B!=null)
			{
				String controller=B.rulingOrganization();
				Area A2=CMLib.law().getLegalObject(A);
				if(controller.equals(clanID())&&(!done.contains(A2)))
					done.addElement(A2);
			}
		}
		return done;
	}

	public Enumeration<ClanVote> votes()
	{
		if(voteList==null)
		{
			List<PlayerData> V=CMLib.database().DBReadData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
			voteList=new Vector<ClanVote>();
			for(int v=0;v<V.size();v++)
			{
				ClanVote CV=new ClanVote();
				String rawxml=((DatabaseEngine.PlayerData)V.get(v)).xml;
				if(rawxml.trim().length()==0) return voteList.elements();
				List<XMLLibrary.XMLpiece> xml=CMLib.xml().parseAllXML(rawxml);
				if(xml==null)
				{
					Log.errOut("Clans","Unable to parse: "+rawxml);
					return voteList.elements();
				}
				List<XMLLibrary.XMLpiece> voteData=CMLib.xml().getContentsFromPieces(xml,"BALLOTS");
				if(voteData==null){ Log.errOut("Clans","Unable to get BALLOTS data."); return voteList.elements();}
				CV.voteStarter=CMLib.xml().getValFromPieces(voteData,"BY");
				CV.voteStarted=CMLib.xml().getLongFromPieces(voteData,"ON");
				CV.function=CMLib.xml().getIntFromPieces(voteData,"FUNC");
				CV.voteStatus=CMLib.xml().getIntFromPieces(voteData,"STATUS");
				CV.matter=CMLib.xml().getValFromPieces(voteData,"CMD");
				CV.votes=new DVector(2);
				List<XMLLibrary.XMLpiece> xV=CMLib.xml().getContentsFromPieces(voteData,"VOTES");
				if((xV!=null)&&(xV.size()>0))
				{
					for(int x=0;x<xV.size();x++)
					{
						XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.get(x);
						if((!iblk.tag.equalsIgnoreCase("VOTE"))||(iblk.contents==null))
							continue;
						String userID=CMLib.xml().getValFromPieces(iblk.contents,"BY");
						boolean yn=CMLib.xml().getBoolFromPieces(iblk.contents,"YN");
						CV.votes.addElement(userID,Boolean.valueOf(yn));
					}
				}
				voteList.addElement(CV);
			}
		}
		return voteList.elements();
	}

	public int getAutoPosition()
	{
		return autoPosition<0?govt().getAutoRole():autoPosition;
	}
	
	public void setAutoPosition(int pos)
	{
		if(pos == govt().getAutoRole())
			autoPosition=-1;
		else
			autoPosition=pos;
	}

	public void setExp(long newexp)
	{
		synchronized(expSync)
		{
			final long oldxp=exp;
			exp=newexp;
			if(exp<0) exp=0;
			final LinkedList<CMath.CompiledOperation> form = govt().getXPCalculationFormula();
			if(oldxp < exp) // we gained
			{
				double nextLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()}, 0.0);
				if((double)exp > nextLevelXP)
				{
					setClanLevel(getClanLevel()+1);
					clanAnnounce(""+getGovernmentName()+" "+name()+" has attained clan level "+getClanLevel()+"!");
					update();
				}
			}
			else
			if((oldxp > exp) && (getClanLevel()>1))
			{
				double prevLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()-1}, 0.0);
				if((double)exp < prevLevelXP)
				{
					setClanLevel(getClanLevel()-1);
					clanAnnounce(""+getGovernmentName()+" "+name()+" has reverted to clan level "+getClanLevel()+"!");
					update();
				}
			}
		}
	}
	public void adjExp(int howMuch) { if(howMuch!=0) setExp(getExp()+howMuch); }
	public long getExp(){return exp;}

	public int getTrophies(){return clanTrophies;}
	public void setTrophies(int trophyFlag){clanTrophies=trophyFlag;}

	public void setTaxes(double rate)
	{
		taxRate=rate;
	}
	public double getTaxes(){return taxRate;}

	public int getClanRelations(String id)
	{
		long i[]=(long[])relations.get(id.toUpperCase());
		if(i!=null) return (int)i[0];
		return  REL_NEUTRAL;
	}

	public long getLastRelationChange(String id)
	{
		long i[]=(long[])relations.get(id.toUpperCase());
		if(i!=null) return i[1];
		return 0;
	}
	public void setClanRelations(String id, int rel, long time)
	{
		relations.remove(id.toUpperCase());
		long[] i=new long[2];
		i[0]=rel;
		i[1]=time;
		relations.put(id.toUpperCase(),i);
	}

	public int getGovernmentID(){return government;}
	public void setGovernmentID(int type){government=type; lastGovernmentLoadTime=-1;}

	public void create()
	{
		CMLib.database().DBCreateClan(this);
		CMLib.clans().addClan(this);
	}

	public void update()
	{
		CMLib.database().DBUpdateClan(this);
	}

	public void addMember(MOB M, int role)
	{
		M.setClanID(clanID());
		M.setClanRole(role);
		CMLib.database().DBUpdateClanMembership(M.Name(), M.getClanID(), role);
		updateClanPrivileges(M);
	}
	public void delMember(MOB M)
	{
		CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
		M.setClanID("");
		M.setClanRole(0);
		updateClanPrivileges(M);
	}

	public boolean updateClanPrivileges(MOB M)
	{
		boolean did=false;
		if(M==null) return false;
		if(M.getClanID().equals(clanID())
		&&(getAuthority(M.getClanRole(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
		{
			if(getClanClassC()!=null)
			{
				CharClass CC=getClanClassC();
				if(M.baseCharStats().getCurrentClass()!=CC)
				{
					M.baseCharStats().setCurrentClass(CC);
					did=true;
					M.recoverCharStats();
				}
			}
		}
		M.delAbility(M.fetchAbility("Spell_ClanHome"));
		M.delAbility(M.fetchAbility("Spell_ClanDonate"));
		M.delAbility(M.fetchAbility("Spell_Flagportation"));

		if(M.playerStats()!=null)
		for(ClanPosition pos : govt().getPositions())
		{
			String title="*, "+pos.getName()+" of "+name();
			if((M.getClanRole()==pos.getRoleID())
			&&(M.getClanID().equals(clanID()))
			&&(getAuthority(M.getClanRole(),Function.CLAN_TITLES)!=Clan.Authority.CAN_NOT_DO))
			{
				if(!M.playerStats().getTitles().contains(title))
					M.playerStats().getTitles().add(title);
			}
			else
			if(M.playerStats().getTitles().contains(title))
				M.playerStats().getTitles().remove(title);
		}
		if(M.getClanID().length()==0)
		{
			Item I=null;
			Vector<Item> itemsToMove=new Vector<Item>();
			for(int i=0;i<M.numItems();i++)
			{
				I=M.getItem(i);
				if(I instanceof ClanItem)
					itemsToMove.addElement(I);
			}
			for(int i=0;i<itemsToMove.size();i++)
			{
				I=(Item)itemsToMove.elementAt(i);
				Room R=null;
				if((getDonation()!=null)
				&&(getDonation().length()>0))
					R=CMLib.map().getRoom(getDonation());
				if((R==null)
				&&(getRecall()!=null)
				&&(getRecall().length()>0))
					R=CMLib.map().getRoom(getRecall());
				if(I instanceof Container)
				{
					List<Item> V=((Container)I).getContents();
					for(int v=0;v<V.size();v++)
						((Item)V.get(v)).setContainer(null);
				}
				I.setContainer(null);
				I.wearAt(Wearable.IN_INVENTORY);
				if(R!=null)
					R.moveItemTo(I);
				else
				if(M.isMine(I))
					I.destroy();
				did=true;
			}
		}
		if((did)&&(!CMSecurity.isSaveFlag("NOPLAYERS")))
			CMLib.database().DBUpdatePlayer(M);
		return did;
	}

	public void destroyClan()
	{
		List<MemberRecord> members=getMemberList();
		for(MemberRecord member : members)
		{
			MOB M=CMLib.players().getLoadPlayer(member.name);
			if(M!=null)
			{
				M.setClanID("");
				M.setClanRole(0);
				updateClanPrivileges(M);
				CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
			}
		}
		CMLib.database().DBDeleteJournal("a Journal of "+getGovernmentName()+" "+getName(), null);
		CMLib.database().DBDeleteClan(this);
		CMLib.clans().removeClan(this);
	}

	protected CharClass getClanClassC()
	{
		if(clanClass.length()==0) return null;
		CharClass C=CMClass.getCharClass(clanClass);
		if(C==null)C=CMClass.findCharClass(clanClass);
		return C;
	}

	public String getDetail(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		boolean member=(mob!=null)&&mob.getClanID().equalsIgnoreCase(clanID())&&(getAuthority(mob.getClanRole(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO);
		boolean sysmsgs=(mob!=null)&&CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS);
		final LinkedList<CMath.CompiledOperation> form = govt().getXPCalculationFormula();
		double nextLevelXP = CMath.parseMathExpression(form, new double[]{getClanLevel()}, 0.0);
		msg.append("^x"+getGovernmentName()+" Profile   :^.^N "+clanID()+"\n\r"
				  +"-----------------------------------------------------------------\n\r"
				  +getPremise()+"\n\r"
				  +"-----------------------------------------------------------------\n\r"
				  +"^xLevel           :^.^N "+getClanLevel()+((member||sysmsgs)?("                      (Next at ^w"+nextLevelXP+"^Nxp)\n\r"):"\n\r")
				  +"^xType            :^.^N "+govt().getName()+"\n\r");
		if(getAcceptanceSettings().length()>0)
		{
			msg.append("^xQualifications  :^.^N "+CMLib.masking().maskDesc(getAcceptanceSettings())+"\n\r");
			if(getBasicRequirementMask().length()>0)
				msg.append("^x           Plus :^.^N "+CMLib.masking().maskDesc(getBasicRequirementMask())+"\n\r");
		}
		else
		if(getBasicRequirementMask().length()>0)
			msg.append("^xQualifications  :^.^N "+CMLib.masking().maskDesc(getBasicRequirementMask())+"\n\r");
		else
			msg.append("^xQualifications  :^.^N Anyone may apply\n\r");
		CharClass clanC=getClanClassC();
		if(clanC!=null) msg.append("^xClass           :^.^N "+clanC.name()+"\n\r");
		msg.append("^xExp. Tax Rate   :^.^N "+((int)Math.round(getTaxes()*100))+"%\n\r");
		if(member||sysmsgs)
		{
			msg.append("^xExperience Pts. :^.^N "+getExp()+"\n\r");
			if(getMorgue().length()>0)
			{
				Room R=CMLib.map().getRoom(getMorgue());
				if(R!=null)
					msg.append("^xMorgue          :^.^N "+R.displayText()+"\n\r");
			}
			if(getDonation().length()>0)
			{
				Room R=CMLib.map().getRoom(getDonation());
				if(R!=null)
					msg.append("^xDonations       :^.^N "+R.displayText()+"\n\r");
			}
			if(getRecall().length()>0)
			{
				Room R=CMLib.map().getRoom(getRecall());
				if(R!=null)
					msg.append("^xRecall          :^.^N "+R.displayText()+"\n\r");
			}
		}
		final Set<ClanPosition> sortedPositions=new HashSet<ClanPosition>();
		for(int i=0;i<govt().getPositions().length;i++)
		{
			ClanPosition topRankedPos=null;
			for(ClanPosition pos : govt().getPositions())
				if((pos.isPublic())
				&&(!sortedPositions.contains(pos))
				&&((topRankedPos==null)||(pos.getRank() < topRankedPos.getRank())))
					topRankedPos = pos;
			if(topRankedPos != null)
			{
				msg.append("^x"+CMStrings.padRight(topRankedPos.getPluralName(),16)+":^.^N "+crewList(topRankedPos.getRoleID())+"\n\r");
				sortedPositions.add(topRankedPos);
			}
		}
		msg.append("^xTotal Members   :^.^N "+getSize()+"\n\r");
		if(CMLib.clans().numClans()>1)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^x"+CMStrings.padRight("Clan Relations",16)+":^.^N \n\r");
			for(Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			{
				Clan C=(Clan)e.nextElement();
				if(C!=this)
				{
					msg.append("^x"+CMStrings.padRight(C.name(),16)+":^.^N ");
					msg.append(CMStrings.capitalizeAndLower(REL_DESCS[getClanRelations(C.clanID())]));
					int orel=C.getClanRelations(clanID());
					if(orel!=REL_NEUTRAL)
						msg.append(" (<-"+CMStrings.capitalizeAndLower(REL_DESCS[orel])+")");
					msg.append("\n\r");
				}
			}
		}
		if(member||sysmsgs)
		{
			updateClanPrivileges(mob);
			for(ClanPosition pos : govt().getPositions())
				if((!pos.isPublic())&&(member)
				&&((pos.getRoleID()!=govt().getAutoRole())||(pos.getRoleID()==govt().getAcceptPos())))
				{
					msg.append("-----------------------------------------------------------------\n\r"
							  +"^x"+CMStrings.padRight(pos.getPluralName(),16)
							  +":^.^N "+crewList(pos.getRoleID())+"\n\r");
				}
			if((mob!=null)
			&&(govt().getAutoRole()!=govt().getAcceptPos())
			&&((getAuthority(mob.getClanRole(),Function.ACCEPT)!=Clan.Authority.CAN_NOT_DO)||sysmsgs))
			{
				ClanPosition pos=govt().getPositions()[getAutoPosition()];
				msg.append("-----------------------------------------------------------------\n\r"
						+"^x"+CMStrings.padRight(pos.getPluralName(),16)+":^.^N "+crewList(pos.getRoleID())+"\n\r");
			}
		}
		Vector<String> control=new Vector<String>();
		List<Area> controlledAreas=getControlledAreas();
		long controlPoints=calculateMapPoints(controlledAreas);
		for(Area A : controlledAreas)
			control.addElement(A.name());
		if(control.size()>0)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^xClan Controlled Areas (% revolt):^.^N\n\r");
			Collections.sort(control);
			int col=0;
			final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,mob);
			for(int i=0;i<control.size();i++)
			{
				if((++col)>3)
				{
					msg.append("\n\r");
					col=1;
				}
				Area A=CMLib.map().getArea((String)control.elementAt(i));
				if(A!=null)
				{
					LegalBehavior B=CMLib.law().getLegalBehavior(A);
					Area legalA=CMLib.law().getLegalObject(A);
					int pctRevolt=0;
					if((B!=null)&&(legalA!=null)) pctRevolt=B.revoltChance();
					msg.append("^c"+CMStrings.padRight(A.name()+"^N ("+pctRevolt+"%)",COL_LEN)+"^N");
				}
			}
			msg.append("\n\r");
		}
		if((CMLib.clans().trophySystemActive())&&(getTrophies()!=0))
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^xTrophies awarded:^.^N\n\r");
			for(int i=0;i<TROPHY_DESCS.length;i++)
				if((TROPHY_DESCS[i].length()>0)&&(CMath.bset(getTrophies(),i)))
				{
					msg.append(TROPHY_DESCS[i]+" ");
					switch(i){
						case TROPHY_AREA: msg.append("("+control.size()+") "); break;
						case TROPHY_CONTROL: msg.append("("+controlPoints+") "); break;
						case TROPHY_EXP: msg.append("("+getExp()+") "); break;
					}
					msg.append(" Prize: "+CMLib.clans().translatePrize(i)+"\n\r");
				}
		}
		if(((mob!=null)&&(getAuthority(mob.getClanRole(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))||sysmsgs)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^xClan Level Benefits:^.^N\n\r");
			List<AbilityMapper.AbilityMapping> abilities=CMLib.ableMapper().getUpToLevelListings(govt().ID(),getClanLevel(),true,false);
			if(abilities.size()>0)
			{
				final List<String> names = new Vector<String>();
				for(AbilityMapper.AbilityMapping aMap : abilities) 
				{
					final Ability A=CMClass.getAbility(aMap.abilityID);
					if(A!=null)
						names.add(A.name()+(aMap.autoGain?"":"(q)"));
				}
				msg.append(CMLib.lister().makeColumns(mob,names,null,3));
				msg.append("\n\r");
			}
			final List<Ability> effects = clanEffects(null);
			for(Ability A : effects)
				msg.append(A.accountForYourself()).append("\n\r");
		}
		return msg.toString();
	}

	private String crewList(int posType)
	{
		StringBuffer list=new StringBuffer("");
		List<MemberRecord> members = getMemberList(posType);
		if(members.size()>1)
		{
			for(int j=0;j<(members.size() - 1);j++)
				list.append(members.get(j).name+", ");
			list.append("and "+members.get(members.size()-1).name);
		}
		else
		if(members.size()>0)
			list.append(members.get(0).name);
		return list.toString();
	}

	public String getGovernmentName() { return govt().getName();}

	public boolean canBeAssigned(MOB mob, int role)
	{
		if(mob==null) return false;
		if((role<0)||(role>govt().getPositions().length)) return false;
		ClanPosition pos = govt().getPositions()[role];
		return CMLib.masking().maskCheck(fixRequirementMask(pos.getInnerMaskStr()), mob, true);
	}

	public Authority getAuthority(int roleID, Function function)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return Authority.CAN_NOT_DO;
		return govt().getPositions()[roleID].getFunctionChart()[function.ordinal()];
	}

	public String fixRequirementMask(final String oldMask)
	{
		if((oldMask==null)||(oldMask.trim().length()==0)) return "";
		StringBuffer mask=new StringBuffer(oldMask.trim());
		if(mask.length()==0) return "";
		MOB M=getResponsibleMember();
		int x=mask.indexOf("%[");
		while(x>=0)
		{
			int y=mask.indexOf("]%",x+1);
			if(y>x)
			{
				String tag=mask.substring(x+2,y);
				String value="Unknown";
				if(M!=null)
				{
					if(tag.equalsIgnoreCase("WORSHIPCHARID"))
					{
						value=M.getWorshipCharID();
						if(value.length()==0)
							value="ANY";
					}
					else
					if(CMLib.coffeeMaker().isAnyGenStat(M, tag))
						value=CMLib.coffeeMaker().getAnyGenStat(M, tag);
				}
				else
				if(tag.equalsIgnoreCase("WORSHIPCHARID"))
					value="ANY";
				mask.replace(x, y+2, value);
			}
			if(x>=mask.length()-1)
				break;
			x=mask.indexOf("%[",x+1);
		}
		return mask.toString();
	}
	
	public String getBasicRequirementMask()
	{
		return fixRequirementMask(govt().getRequiredMaskStr());
	}
	public List<MemberRecord> getRealMemberList(int PosFilter)
	{
		List<MemberRecord> members=getMemberList(PosFilter);
		if(members==null) return null;
		List<MemberRecord> realMembers=new Vector<MemberRecord>();
		for(MemberRecord member : members)
			if(CMLib.players().playerExists(member.name))
				realMembers.add(member);
		return members;
	}

	public int getSize() { return getMemberList(-1).size();}

	public String name() {return clanName;}
	public String getName() {return clanName;}
	public String clanID() {return clanName;}
	public void setName(String newName) {clanName = newName; }

	public String getPremise() {return clanPremise;}
	public void setPremise(String newPremise){ clanPremise = newPremise;}

	public int getClanLevel() {return clanLevel;}
	public void setClanLevel(int newClanLevel)
	{ 
		if(newClanLevel<=0) 
			clanLevel=1;
		else
			clanLevel = newClanLevel;
	}

	public String getAcceptanceSettings() { return acceptanceSettings; }
	public void setAcceptanceSettings(String newSettings) { acceptanceSettings=newSettings; }

	public String getClanClass(){return clanClass;}
	public void setClanClass(String newClass){clanClass=newClass;}

	public String getPolitics() 
	{
		StringBuffer str=new StringBuffer("");
		str.append("<POLITICS>");
		str.append(CMLib.xml().convertXMLtoTag("GOVERNMENT",""+getGovernmentID()));
		str.append(CMLib.xml().convertXMLtoTag("TAXRATE",""+getTaxes()));
		str.append(CMLib.xml().convertXMLtoTag("EXP",""+getExp()));
		str.append(CMLib.xml().convertXMLtoTag("LEVEL",""+getClanLevel()));
		str.append(CMLib.xml().convertXMLtoTag("CCLASS",""+getClanClass()));
		str.append(CMLib.xml().convertXMLtoTag("AUTOPOS",""+getAutoPosition()));
		if(relations.size()==0)
			str.append("<RELATIONS/>");
		else
		{
			str.append("<RELATIONS>");
			for(Enumeration<String> e=relations.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				str.append("<RELATION>");
				str.append(CMLib.xml().convertXMLtoTag("CLAN",key));
				long[] i=(long[])relations.get(key);
				str.append(CMLib.xml().convertXMLtoTag("STATUS",""+i[0]));
				str.append("</RELATION>");
			}
			str.append("</RELATIONS>");
		}
		str.append("</POLITICS>");
		return str.toString();
	}
	
	public void setPolitics(String politics)
	{
		relations.clear();
		government=0;
		if(politics.trim().length()==0) return;
		List<XMLLibrary.XMLpiece> xml=CMLib.xml().parseAllXML(politics);
		if(xml==null)
		{
			Log.errOut("Clans","Unable to parse: "+politics);
			return;
		}
		List<XMLLibrary.XMLpiece> poliData=CMLib.xml().getContentsFromPieces(xml,"POLITICS");
		if(poliData==null){ Log.errOut("Clans","Unable to get POLITICS data."); return;}
		government=CMLib.xml().getIntFromPieces(poliData,"GOVERNMENT");
		exp=CMLib.xml().getLongFromPieces(poliData,"EXP");
		setClanLevel(CMLib.xml().getIntFromPieces(poliData,"LEVEL"));
		setExp(exp); // may change the level
		taxRate=CMLib.xml().getDoubleFromPieces(poliData,"TAXRATE");
		clanClass=CMLib.xml().getValFromPieces(poliData,"CCLASS");
		autoPosition=CMLib.xml().getIntFromPieces(poliData,"AUTOPOS");

		// now RESOURCES!
		List<XMLLibrary.XMLpiece> xV=CMLib.xml().getContentsFromPieces(poliData,"RELATIONS");
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.get(x);
				if((!iblk.tag.equalsIgnoreCase("RELATION"))||(iblk.contents==null))
					continue;
				String relClanID=CMLib.xml().getValFromPieces(iblk.contents,"CLAN");
				int rel=CMLib.xml().getIntFromPieces(iblk.contents,"STATUS");
				setClanRelations(relClanID,rel,0);
			}
		}
	}

	public int getStatus() { return clanStatus; }
	public void setStatus(int newStatus) { clanStatus=newStatus; }

	public String getRecall() { return clanRecall; }
	public void setRecall(String newRecall) { clanRecall=newRecall; }

	public String getMorgue() { return clanMorgue; }
	public void setMorgue(String newMorgue) { clanMorgue=newMorgue; }

	public String getDonation() { return clanDonationRoom; }
	public void setDonation(String newDonation) { clanDonationRoom=newDonation; }

	public List<MemberRecord> getMemberList()
	{
		return getMemberList(-1);
	}

	public List<MemberRecord> getMemberList(int PosFilter)
	{
		List<MemberRecord> members;
		members = CMLib.database().DBClanMembers(clanID());
		Vector<MemberRecord> filteredMembers=new Vector<MemberRecord>();
		for(MemberRecord member : members)
			if((member.role==PosFilter)||(PosFilter<0))
				filteredMembers.add(member);
		return filteredMembers;
	}

	public int getNumVoters(Function function)
	{
		int voters=0;
		List<MemberRecord> members=getMemberList();
		final Function voteFunc = (function == Function.ASSIGN) ? Function.VOTE_ASSIGN : Function.VOTE_OTHER;
		for(MemberRecord member : members)
			if(getAuthority(member.role, voteFunc)==Authority.CAN_DO)
				voters++;
		return voters;
	}

	public List<Integer> getTopRankedRoles(Function func)
	{
		final List<ClanPosition> allRoles=new LinkedList<ClanPosition>();
		for(ClanPosition pos : govt().getPositions())
			if((func==null)||(pos.getFunctionChart()[func.ordinal()]!=Authority.CAN_NOT_DO))
				allRoles.add(pos);
		final List<Integer> roleIDs=new LinkedList<Integer>();
		int topRank=Integer.MAX_VALUE;
		for(ClanPosition pos : allRoles)
			if(pos.getRank() < topRank)
				topRank=pos.getRank();
		for(ClanPosition pos : allRoles)
			if(pos.getRank() == topRank)
				roleIDs.add(Integer.valueOf(pos.getRoleID()));
		return roleIDs;
	}
	
	public int getNumberRoles(){ return govt().getPositions().length;}

	public int getTopQualifiedRoleID(Function func, MOB mob) 
	{
		if(mob==null) return govt().getAutoRole();
		ClanPosition topPos = null;
		for(ClanPosition pos : govt().getPositions())
			if(canBeAssigned(mob,pos.getRoleID()) 
			&&((topPos==null)||(pos.getRank() < topPos.getRank()))
			&&((func==null)||(getAuthority(pos.getRoleID(),func)!=Authority.CAN_NOT_DO)))
				topPos = pos;
		if(topPos == null) return govt().getAutoRole();
		return topPos.getRoleID();
	}
	
	public int getRoleFromName(String position)
	{
		position=position.toUpperCase().trim();
		for(ClanPosition pos : govt().getPositions())
			if(pos.getID().equalsIgnoreCase(position)
			||pos.getName().equalsIgnoreCase(position)
			||pos.getPluralName().equalsIgnoreCase(position))
				return pos.getRoleID();
		for(ClanPosition pos : govt().getPositions())
			if(pos.getID().toUpperCase().startsWith(position)
			||pos.getName().toUpperCase().equalsIgnoreCase(position))
				return pos.getRoleID();
		for(ClanPosition pos : govt().getPositions())
			if((pos.getID().toUpperCase().indexOf(position)>0)
			||(pos.getName().toUpperCase().indexOf(position)>0))
				return pos.getRoleID();
		return -1;
	}
	
	public boolean isPubliclyListedFor(MOB mob)
	{
		if((!govt().isPublic())
		&&(!mob.getClanID().equals(clanID())))
			return false;
		return true;
	}
	
	public String[] getRolesList()
	{
		final List<String> roleNames=new LinkedList<String>();
		for(final ClanPosition pos : govt().getPositions())
			roleNames.add(pos.getName());
		return roleNames.toArray(new String[0]);
	}

	public int getMostInRole(int roleID)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return 0;
		return govt().getPositions()[roleID].getMax();
	}

	
	public String getRoleName(int roleID, boolean titleCase, boolean plural)
	{
		if((roleID<0)||(roleID>=govt().getPositions().length))
			return "";
		ClanPosition pos=govt().getPositions()[roleID];
		if(plural)
		{
			if(!titleCase)
				return pos.getPluralName().toLowerCase();
			else
				return pos.getPluralName();
		}
		if(!titleCase)
			return pos.getName().toLowerCase();
		else
			return pos.getName();
	}

	protected boolean isSafeFromPurge()
	{
		final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
			{
				String B=(String)protectedOnes.get(b);
				if(B.equalsIgnoreCase(clanID()))
					return true;
			}
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Tickable.TICKID_CLAN)
			return true;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLANTICKS))
			return true;
		try{
			List<MemberRecord> members=getMemberList();
			int activeMembers=0;
			long deathMilis=CMProps.getIntVar(CMProps.SYSTEMI_DAYSCLANDEATH)*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*CMProps.getTickMillis();
			for(MemberRecord member : members)
			{
				long lastLogin=member.timestamp;
				if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
					activeMembers++;
			}
			
			int minimumMembers = CMProps.getIntVar(CMProps.SYSTEMI_MINCLANMEMBERS);
			if(govt().getOverrideMinMembers()!=null)
				minimumMembers = govt().getOverrideMinMembers().intValue();
			if(activeMembers<minimumMembers)
			{
				if(!isSafeFromPurge())
				{
					if(getStatus()==CLANSTATUS_FADING)
					{
						Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
						destroyClan();
						StringBuffer buf=new StringBuffer("");
						for(MemberRecord member : members)
							buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
						Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
						return true;
					}
					setStatus(CLANSTATUS_FADING);
					final List<Integer> topRoles=getTopRankedRoles(Function.ASSIGN);
					for(MemberRecord member : members)
					{
						String name = member.name;
						int role=member.role;
						//long lastLogin=((Long)members.elementAt(j,3)).longValue();
						if(topRoles.contains(Integer.valueOf(role)))
						{
							MOB player=CMLib.players().getLoadPlayer(name);
							if(player!=null)
								CMLib.smtp().emailIfPossible("AutoPurge",player,"AutoPurge: "+name(), 
										""+getGovernmentName()+" "+name()+" is in danger of being deleted if at least "+(minimumMembers-activeMembers)
										+" members do not log on within 24 hours.");
						}
					}
					
					Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
					clanAnnounce(""+getGovernmentName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
					update();
				}
			}
			else
			switch(getStatus())
			{
			case CLANSTATUS_FADING:
				setStatus(CLANSTATUS_ACTIVE);
				clanAnnounce(""+getGovernmentName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
				break;
			case CLANSTATUS_PENDING:
				setStatus(CLANSTATUS_ACTIVE);
				Log.sysOut("Clans",""+getGovernmentName()+" '"+getName()+" now active with "+activeMembers+".");
				clanAnnounce(""+getGovernmentName()+" "+name()+" now has sufficient members.  The "+getGovernmentName()+" is now fully approved.");
				break;
			default:
				break;
			}

			// handle any necessary promotions
			if(govt().getAutoPromoteBy() != AutoPromoteFlag.NONE)
			{
				List<Integer> highPositionList = getTopRankedRoles(null);
				List<MemberRecord> highMembers=new LinkedList<MemberRecord>();
				for(MemberRecord member : members)
					if((((System.currentTimeMillis()-member.timestamp)<deathMilis)||(deathMilis==0))
					&&(highPositionList.contains(Integer.valueOf(member.role))))
						highMembers.add(member);
				
				AutoPromoteFlag basePromoteBy = govt().getAutoPromoteBy();
				boolean overWrite = false;
				if(basePromoteBy==AutoPromoteFlag.LEVEL_OVERWRITE)
				{
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.LEVEL;
				}
				else
				if(basePromoteBy==AutoPromoteFlag.RANK_OVERWRITE)
				{
					overWrite=true;
					basePromoteBy=AutoPromoteFlag.RANK;
				}
				
				if(overWrite || (highMembers.size()==0))
				{
					List<MemberRecord> highestQualifiedMembers = new LinkedList<MemberRecord>();
					for(MemberRecord member : members)
					{
						if((member.role<0)||(member.role>=govt().getPositions().length))
							continue;
						MOB M=CMLib.players().getLoadPlayer(member.name);
						if(M==null) continue;
						for(Integer posI : highPositionList)
							if((((System.currentTimeMillis()-member.timestamp)<deathMilis)||(deathMilis==0))
							&&(canBeAssigned(M, posI.intValue())))
								highestQualifiedMembers.add(member);
					}
					if(basePromoteBy==AutoPromoteFlag.RANK)
					{
						ClanPosition bestPos=null;
						for(MemberRecord member : highestQualifiedMembers)
						{
							ClanPosition currentPos = govt().getPositions()[member.role];
							if((bestPos==null)||(currentPos.getRank() < bestPos.getRank()))
								bestPos=currentPos;
						}
						if(bestPos!=null)
							for(Iterator<MemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
								if(i.next().role != bestPos.getRoleID())
									i.remove();
					}
					
					int highestLevel=-1;
					for(MemberRecord member : highestQualifiedMembers)
					{
						MOB M=CMLib.players().getLoadPlayer(member.name);
						if(M==null) continue;
						if(M.basePhyStats().level() > highestLevel)
							highestLevel=M.basePhyStats().level();
					}
					for(Iterator<MemberRecord> i=highestQualifiedMembers.iterator();i.hasNext();)
					{
						MOB M=CMLib.players().getLoadPlayer(i.next().name);
						if(M==null) continue;
						if(M.basePhyStats().level()!=highestLevel)
							i.remove();
					}
					if(highestQualifiedMembers.size()>0)
					{
						if(overWrite)
						{
							ClanPosition newRole=govt().getPositions()[govt().getAcceptPos()];
							for(MemberRecord member : highMembers)
								if(!highestQualifiedMembers.contains(member))
								{
									MOB M=CMLib.players().getLoadPlayer(member.name);
									if(M==null) continue;
									clanAnnounce(member.name+" is now a "+newRole.getName()+" of the "+getGovernmentName()+" "+name()+".");
									Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was autodemoted to "+newRole.getName()+".");
									M.setClanRole(newRole.getRoleID());
									CMLib.database().DBUpdateClanMembership(M.Name(), name(), newRole.getRoleID());
								}
						}
						for(MemberRecord member : highestQualifiedMembers)
							if(!highMembers.contains(member))
							{
								MOB M=CMLib.players().getLoadPlayer(member.name);
								if(M==null) continue;
								ClanPosition newRole=null;
								for(Integer posI : highPositionList)
									if(canBeAssigned(M, posI.intValue()))
									{
										newRole=govt().getPositions()[posI.intValue()];
										break;
									}
								if(newRole!=null)
								{
									clanAnnounce(member.name+" is now a "+newRole.getName()+" of the "+getGovernmentName()+" "+name()+".");
									Log.sysOut("Clans",member.name+" of "+getGovernmentName()+" "+name()+" was autopromoted to "+newRole.getName()+".");
									M.setClanRole(newRole.getRoleID());
									CMLib.database().DBUpdateClanMembership(M.Name(), name(), newRole.getRoleID());
									break;
								}
							}
					}
				}
				highMembers.clear();
				members=getMemberList();
				for(MemberRecord member : members)
					if((((System.currentTimeMillis()-member.timestamp)<deathMilis)||(deathMilis==0))
					&&(highPositionList.contains(Integer.valueOf(member.role))))
						highMembers.add(member);
				
				if(highMembers.size()==0)
				{
					if(!isSafeFromPurge())
					{
						Log.sysOut("Clans","Clan '"+getName()+" deleted for lack of leadership.");
						destroyClan();
						StringBuffer buf=new StringBuffer("");
						for(MemberRecord member : members)
							buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
						Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
						return true;
					}
				}
			}

			boolean anyVoters = false;
			for(ClanPosition pos : govt().getPositions())
				if((pos.getFunctionChart()[Function.VOTE_ASSIGN.ordinal()]==Clan.Authority.CAN_DO)
				||(pos.getFunctionChart()[Function.VOTE_OTHER.ordinal()]==Clan.Authority.CAN_DO))
					anyVoters=true;
			// now do votes
			if(anyVoters&&(votes()!=null))
			{
				boolean updateVotes=false;
				Vector<ClanVote> votesToRemove=new Vector<ClanVote>();
				long duration=govt().getMaxVoteDays();
				if(duration<=0) duration=54;
				duration=duration*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*CMProps.getTickMillis();
				for(Enumeration<ClanVote> e=votes();e.hasMoreElements();)
				{
					ClanVote CV=(ClanVote)e.nextElement();
					int numVotes=getNumVoters(Function.values()[CV.function]);
					int quorum=govt().getVoteQuorumPct();
					quorum=(int)Math.round(CMath.mul(CMath.div(quorum,100.0),numVotes));
					if(quorum<2) quorum=2;
					if(numVotes==1) quorum=1;
					long endsOn=CV.voteStarted+duration;
					if(CV.voteStatus==VSTAT_STARTED)
					{
						if(CV.votes==null) CV.votes=new DVector(2);
						boolean voteIsOver=false;
						if(System.currentTimeMillis()>endsOn)
							voteIsOver=true;
						else
						if(CV.votes.size()==numVotes)
							voteIsOver=true;
						if(voteIsOver)
						{
							CV.voteStarted=System.currentTimeMillis();
							updateVotes=true;
							if(CV.votes.size()<quorum)
								CV.voteStatus=VSTAT_FAILED;
							else
							{
								int yeas=0;
								int nays=0;
								for(int i=0;i<CV.votes.size();i++)
									if(((Boolean)CV.votes.elementAt(i,2)).booleanValue())
										yeas++;
									else
										nays++;
								if(yeas<=nays)
									CV.voteStatus=VSTAT_FAILED;
								else
								{
									CV.voteStatus=VSTAT_PASSED;
									MOB mob=CMClass.getFactoryMOB();
									mob.setName(clanID());
									mob.setClanID(clanID());
									mob.setClanRole(getTopRankedRoles(Function.values()[CV.function]).get(0).intValue());
									mob.basePhyStats().setLevel(1000);
									if(mob.location()==null)
									{
										mob.setLocation(mob.getStartRoom());
										if(mob.location()==null)
											mob.setLocation(CMLib.map().getRandomRoom());
									}
									Vector<String> V=CMParms.parse(CV.matter);
									mob.doCommand(V,Command.METAFLAG_FORCED);
									mob.destroy();
								}
							}
						}
					}
					else
					if(System.currentTimeMillis()>endsOn)
					{
						updateVotes=true;
						votesToRemove.addElement(CV);
					}
				}
				for(int v=0;v<votesToRemove.size();v++)
					delVote(votesToRemove.elementAt(v));
				if(updateVotes)
					updateVotes();
			}

			if(CMLib.clans().trophySystemActive())
			{
				// calculate winner of the exp contest
				if(CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
				{
					Clan winner=null;
					for(Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
					{
						Clan C=(Clan)e.nextElement();
						if((winner==null)||(C.getExp()>winner.getExp()))
							winner=C;
					}
					if(winner==this)
					{
						if((!CMath.bset(getTrophies(),TROPHY_EXP))&&(getExp()>0))
						{
							setTrophies(getTrophies()|TROPHY_EXP);
							CMLib.clans().clanAnnounceAll("The "+getGovernmentName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
						}
					}
					else
					if(CMath.bset(getTrophies(),TROPHY_EXP))
					{
						setTrophies(getTrophies()-TROPHY_EXP);
						clanAnnounce("The "+getGovernmentName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
					}
				}

				// calculate winner of the pk contest
				if(CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0)
				{
					Clan winner=null;
					for(Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
					{
						Clan C=(Clan)e.nextElement();
						if((winner==null)||(C.getCurrentClanKills()>winner.getCurrentClanKills()))
							winner=C;
					}
					if(winner==this)
					{
						if((!CMath.bset(getTrophies(),TROPHY_PK))
						&&(getCurrentClanKills()>0))
						{
							setTrophies(getTrophies()|TROPHY_PK);
							CMLib.clans().clanAnnounceAll("The "+getGovernmentName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
						}
					}
					else
					if(CMath.bset(getTrophies(),TROPHY_PK))
					{
						setTrophies(getTrophies()-TROPHY_PK);
						clanAnnounce("The "+getGovernmentName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
					}
				}

				// calculate winner of the conquest contests
				if((CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
				||(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0))
				{
					long mostClansControlled=-1;
					Clan winnerMostClansControlled=null;
					long mostControlPoints=-1;
					Clan winnerMostControlPoints=null;
					List<Area> tempControl=null;
					long tempNumber=0;
					for(Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
					{
						Clan C=(Clan)e.nextElement();
						tempControl=C.getControlledAreas();
						tempNumber=C.calculateMapPoints(tempControl);
						if((winnerMostClansControlled==null)||(tempControl.size()>mostClansControlled))
						{
							winnerMostClansControlled=C;
							mostClansControlled=tempControl.size();
						}
						if((winnerMostControlPoints==null)||(tempNumber>mostControlPoints))
						{
							winnerMostControlPoints=C;
							mostControlPoints=tempNumber;
						}
					}
					if((winnerMostClansControlled==this)
					&&(CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
					&&(mostClansControlled>0))
					{
						if(!CMath.bset(getTrophies(),TROPHY_AREA))
						{
							setTrophies(getTrophies()|TROPHY_AREA);
							CMLib.clans().clanAnnounceAll("The "+getGovernmentName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
						}
					}
					else
					if(CMath.bset(getTrophies(),TROPHY_AREA))
					{
						setTrophies(getTrophies()-TROPHY_AREA);
						clanAnnounce("The "+getGovernmentName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
					}
					if((winnerMostControlPoints==this)
					&&(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
					&&(mostControlPoints>0))
					{
						if(!CMath.bset(getTrophies(),TROPHY_CONTROL))
						{
							setTrophies(getTrophies()|TROPHY_CONTROL);
							CMLib.clans().clanAnnounceAll("The "+getGovernmentName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
						}
					}
					else
					if(CMath.bset(getTrophies(),TROPHY_CONTROL))
					{
						setTrophies(getTrophies()-TROPHY_CONTROL);
						clanAnnounce("The "+getGovernmentName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
					}
				}
			}
			update(); // also saves exp, and trophies
		}
		catch(Exception x2)
		{
			Log.errOut("Clans",x2);
		}
		return true;
	}

	public boolean doesOutRank(int highRoleID, int lowRoleID)
	{
		ClanGovernment govt=govt();
		if((highRoleID == lowRoleID)
		||(highRoleID < 0)
		||(highRoleID >= govt.getPositions().length))
			return false;
		if((lowRoleID<0)
		||(lowRoleID >= govt.getPositions().length))
			return true;
		return govt.getPositions()[highRoleID].getRank() < govt.getPositions()[lowRoleID].getRank();
	}
	
	public void clanAnnounce(String msg)
	{
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel((String)channels.get(i),clanID(),msg,true);
	}

	private static final List<Ability> emptyAbles =new Vector<Ability>(1);
	
	public List<Ability> clanAbilities(MOB mob)
	{
		if((mob==null)||(getAuthority(mob.getClanRole(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
			return govt().getClanLevelAbilities(Integer.valueOf(getClanLevel()));
		return emptyAbles;
	}
	
	public int numClanEffects(MOB mob)
	{
		return govt().getClanLevelEffectsSize(mob, Integer.valueOf(getClanLevel()));
	}
	
	public ChameleonList<Ability> clanEffects(MOB mob)
	{
		return govt().getClanLevelEffects(mob,Integer.valueOf(getClanLevel()));
	}
	
	public int applyExpMods(int exp)
	{
		boolean changed=false;
		if((getTaxes()>0.0)&&(exp>1))
		{
			int clanshare=(int)Math.round(CMath.mul(exp,getTaxes()));
			if(clanshare>0)
			{
				exp-=clanshare;
				adjExp(clanshare);
				changed=true;
			}
		}
		for(int i=0;i<TROPHY_DESCS_SHORT.length;i++)
			if((TROPHY_DESCS_SHORT[i].length()>0)
			&&(CMath.bset(getTrophies(),i)))
			{
				String awardStr=null;
				switch(i)
				{
				case TROPHY_AREA: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
				case TROPHY_CONTROL: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
				case TROPHY_EXP: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
				case TROPHY_PK: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPPK); break;
				}
				if(awardStr!=null)
				{
					int amount=0;
					double pct=0.0;
					Vector<String> V=CMParms.parse(awardStr);
					if(V.size()>=2)
					{
						String type=((String)V.lastElement()).toUpperCase();
						String amt=(String)V.firstElement();
						if(amt.endsWith("%"))
							pct=CMath.div(CMath.s_int(amt.substring(0,amt.length()-1)),100.0);
						else
							amount=CMath.s_int(amt);
						if("EXPERIENCE".startsWith(type))
							exp+=((int)Math.round(CMath.mul(exp,pct)))+amount;
					}
				}
			}
		if(changed) update();
		return exp;
	}
	
	public MOB getResponsibleMember()
	{
		final List<MemberRecord> members=getMemberList();
		final List<Integer> topRoles=getTopRankedRoles(null);
		MOB respMember = null;
		int level = -1;
		for(MemberRecord member : members)
		{
			if(topRoles.contains(Integer.valueOf(member.role)))
			{
				MOB M=CMLib.players().getLoadPlayer(member.name);
				if((M!=null)&&(M.basePhyStats().level() > level))
					respMember = M;
			}
		}
		if(respMember != null)
			return respMember;
		String memberName = null;
		ClanPosition newPos=null;
		for(MemberRecord member : members)
			if((member.role<govt().getPositions().length)
			&&(member.role>=0)
			&&((newPos==null)||(govt().getPositions()[member.role].getRank()<newPos.getRank())))
			{
				newPos = govt().getPositions()[member.role];
				memberName = member.name;
			}
		if(memberName != null)
			return CMLib.players().getLoadPlayer(memberName);
		return null;
	}

	public String[] getStatCodes(){ return CLAN_STATS;}
	public int getSaveStatIndex(){ return CLAN_STATS.length;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	public String getStat(String code) {
		int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
		if(dex<0) return "";
		switch(dex)
		{
		case 0: return getAcceptanceSettings();
		case 1: return getDetail(null);
		case 2: return getDonation();
		case 3: return ""+getExp();
		case 4: return ""+getGovernmentName();
		case 5: return getMorgue();
		case 6: return getPolitics();
		case 7: return getPremise();
		case 8: return getRecall();
		case 9: return ""+getSize();
		case 10: return Clan.CLANSTATUS_DESC[getStatus()];
		case 11: return ""+getTaxes();
		case 12: return ""+getTrophies();
		case 13: return "0";
		case 14: {
			 List<Area> areas=getControlledAreas();
			 StringBuffer list=new StringBuffer("");
			 for(int i=0;i<areas.size();i++)
				 list.append("\""+areas.get(i).name()+"\" ");
			 return list.toString().trim();
		}
		case 15: 
		{
			List<MemberRecord> members=getMemberList();
				 StringBuffer list=new StringBuffer("");
				 for(MemberRecord member : members)
					 list.append("\""+member.name+"\" ");
				 return list.toString().trim();
		}
		case 16: {
			MOB M=getResponsibleMember();
			if(M!=null) return M.Name();
			return "";
		}
		case 17: return Integer.toString(getClanLevel());
		}
		return "";
	}
	
	public void setStat(String code, String val) {
		int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
		if(dex<0) return;
		switch(dex) {
		case 0: setAcceptanceSettings(val); break;
		case 1: break; // detail
		case 2: setDonation(val); break;
		case 3: setExp(CMath.s_long(val.trim())); break;
		case 4: setGovernmentID(CMath.s_int(val.trim())); break;
		case 5: setMorgue(val); break;
		case 6: setPolitics(val); break;
		case 7: setPremise(val); break;
		case 8: setRecall(val); break;
		case 9: break; // size
		case 10: setStatus(CMath.s_int(val.trim())); break;
		case 11: setTaxes(CMath.s_double(val.trim())); break;
		case 12: setTrophies(CMath.s_int(val.trim())); break;
		case 13: break; // type
		case 14: break; // areas
		case 15: break; // memberlist
		case 16: break; // topmember
		case 17: setClanLevel(CMath.s_int(val.trim())); break; // clanlevel
		}
	}
	
}
