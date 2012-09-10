package com.planet_ink.marble_mud.core.intermud.cm1.commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.intermud.cm1.RequestHandler;
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
import com.planet_ink.marble_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;
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
public class GetStat extends CM1Command
{
	public String getCommandWord(){ return "GETSTAT";}
	public GetStat(RequestHandler req, String parameters) {
		super(req, parameters);
	}
	
	protected static final String[] STATTYPES={"SESSION","MOB","CHAR","STATE","PHYSICAL","BASECHAR","MAXSTATE","BASESTATE","BASEPHYSICAL","PLAYERSTATS", "ITEM", "EXIT", "ROOM", "AREA"};
	protected static final String[] TYPESTYPE={"P",		 "MP", "MP",  "MP",   "MPIREA",  "MP",  	"MP",      "MP",	   "MPIREA",	  "P",  		 "I",    "E",    "R",    "A"};
	
	protected static final String[] PHYSSTATS={"EFFECT"};
	protected static final String[] PHYASTATS={"BEHAVIOR"};
	protected static final String[] MOBASTATS={"ABILITY","FACTION","EXPERTISE","FOLLOWER"};
	protected static final String[] ITEMSTATS={"ITEM"};
	protected static final String[] ROOMSTATS={"MOB"};
	
	public char getTypeCode(Physical P)
	{
		if(P instanceof MOB) return ((MOB)P).isMonster()?'M':'P';
		if(P instanceof Item) return 'I';
		if(P instanceof Room) return 'R';
		if(P instanceof Exit) return 'E';
		if(P instanceof Area) return 'A';
		return ' ';
	}
	
	public boolean isApplicableTypeCode(String type, Physical P)
	{
		char c=getTypeCode(P);
		for(int i=0;i<STATTYPES.length;i++)
			if(STATTYPES[i].equalsIgnoreCase(type))
				return TYPESTYPE[i].indexOf(c)>=0;
		return false;
	}
	
	public String[] getApplicableStatCodes(Physical P)
	{
		char c=getTypeCode(P);
		List<String> majorCodes = new LinkedList<String>();
		for(int i=0;i<STATTYPES.length;i++)
			if(TYPESTYPE[i].indexOf(c)>=0)
				majorCodes.add(STATTYPES[i]);
		return majorCodes.toArray(new String[0]);
	}
	
	public Modifiable getModifiable(String type, Physical P)
	{
		int x=CMParms.indexOf(STATTYPES,type.toUpperCase().trim());
		if(x<0) return null;
		if(!isApplicableTypeCode(type,P))
			return null;
		
		switch(x)
		{
		case 0: return ((MOB)P).session();
		case 1: return (Modifiable)P;
		case 2: return ((MOB)P).charStats();
		case 3: return ((MOB)P).curState();
		case 4: return ((Physical)P).phyStats();
		case 5: return ((MOB)P).baseCharStats();
		case 6: return ((MOB)P).maxState();
		case 7: return ((MOB)P).baseState();
		case 8: return ((Physical)P).basePhyStats();
		case 9: return ((MOB)P).playerStats();
		case 10:return (Modifiable)P;
		case 11:return (Modifiable)P;
		case 12:return (Modifiable)P;
		case 13:return (Modifiable)P;
		}
		return null;
	}
	
	public boolean UseGenBuilder(Physical P, Modifiable m)
	{
		return (P!=null)&&(!P.isGeneric())
				&&((m instanceof MOB)||(m instanceof Item));
	}

	public String[] getStatCodes(Physical P, Modifiable m)
	{
		SLinkedList<String> codes;
		if(!UseGenBuilder(P,m))
			codes=new SLinkedList<String>(m.getStatCodes());
		else
		if(m instanceof MOB)
			codes=new SLinkedList<String>(GenericBuilder.GENMOBCODES);
		else
		if(m instanceof Item)
			codes=new SLinkedList<String>(GenericBuilder.GENITEMCODES);
		else
			return new String[0];
		if(m instanceof Physical)
			codes.addAll(PHYSSTATS);
		if(m instanceof PhysicalAgent)
			codes.addAll(PHYASTATS);
		if(m instanceof MOB)
			codes.addAll(MOBASTATS);
		if(m instanceof ItemPossessor)
			codes.addAll(ITEMSTATS);
		if(m instanceof Room)
			codes.addAll(ROOMSTATS);
		return codes.toArray(new String[0]);
	}

	public boolean isAStat(Physical P, Modifiable m, String stat)
	{
		if(!UseGenBuilder(P,m))
			return m.isStat(stat);
		String[] codes = getStatCodes(P,m);
		if(codes != null)
			for(int i=0;i<codes.length;i++)
				if(codes[i].equalsIgnoreCase(stat))
					return true;
		return false;
	}

	public boolean isAuthorized(MOB user, PhysicalAgent target)
	{
		if(target instanceof MOB)
		{
			if(CMLib.players().playerExists(target.Name()))
				return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDPLAYERS);
			return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDMOBS);
		}
		else
		if(target instanceof Item)
			return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDITEMS);
		else
		if(target instanceof Room)
			return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDROOMS);
		else
		if(target instanceof Exit)
			return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDEXITS);
		else
		if(target instanceof Area)
			return CMSecurity.isAllowed(user,user.location(),CMSecurity.SecFlag.CMDAREAS);
		else
			return false;
	}
	
	public void run()
	{
		try
		{
			PhysicalAgent P=req.getTarget();
			if(P==null)
			{
				req.sendMsg("[FAIL NO TARGET]");
				return;
			}
			if(!isAuthorized(req.getUser(),P))
			{
				req.sendMsg("[FAIL UNAUTHORIZED]");
				return;
			}
			String stat = "";
			String type = parameters.toUpperCase().trim();
			String rest = "";
			//char adjuster=' ';
			int x=parameters.indexOf(' ');
			if(x>0)
			{
				type=parameters.substring(0,x).toUpperCase().trim();
				stat=parameters.substring(x+1).toUpperCase().trim();
				x=stat.lastIndexOf(' ');
				if(x>0)
				{
					rest=stat.substring(x+1).trim();
					stat=stat.substring(0,x);
				}
				if((stat.length()>0)&&(!Character.isLetter(stat.charAt(0))))
				{
				//	adjuster=stat.charAt(0);
					stat=stat.substring(1);
				}
			}
			Modifiable mod=getModifiable(type,P);
			if(mod==null)
			{
				req.sendMsg("[FAIL "+getHelp(req.getUser(), P, "")+"]");
				return;
			}
			if((stat.length()==0)||(!isAStat(P,mod,stat)))
			{
				req.sendMsg("[FAIL USAGE: GETSTAT "+type+" "+CMParms.toStringList(getStatCodes(P,mod))+"]");
				return;
			}
			if(mod instanceof Physical)
				switch(CMParms.indexOf(PHYSSTATS, stat))
				{
					case -1: break;
					case 0: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((Physical)mod).numEffects()+"]");
						else
						{
							Ability A=((Physical)mod).fetchEffect(CMath.s_int(rest));
							if(A==null)
								req.sendMsg("[FAIL NO EFFECT "+rest+"]"); 
							else
								req.sendMsg("[OK "+A.ID()+" "+A.text()+"]"); 
						}
						return;
					}
				}
			if(mod instanceof PhysicalAgent)
				switch(CMParms.indexOf(PHYASTATS, stat))
				{
					case -1: break;
					case 0: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((PhysicalAgent)mod).numBehaviors()+"]");
						else
						{
							Behavior A=((PhysicalAgent)mod).fetchBehavior(CMath.s_int(rest));
							if(A==null)
								req.sendMsg("[FAIL NO BEHAVIOR "+rest+"]"); 
							else
								req.sendMsg("[OK "+A.ID()+" "+A.getParms()+"]");
						}
						return;
					}
				}
			if(mod instanceof MOB)
				switch(CMParms.indexOf(MOBASTATS, stat))
				{
					case -1: break;
					case 0: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((MOB)mod).numAllAbilities()+"]");
						else
						{
							Ability A=((MOB)mod).fetchAbility(CMath.s_int(rest));
							if(A==null)
								req.sendMsg("[FAIL NO ABILITY "+rest+"]"); 
							else
								req.sendMsg("[OK "+A.ID()+" "+A.proficiency()+" "+A.text()+"]");
						}
						return;
					}
					case 1: 
					{
						if(rest.trim().length()==0)
						{
							StringBuilder factions=new StringBuilder("");
							for(Enumeration<String> f=((MOB)mod).fetchFactions();f.hasMoreElements();)
								factions.append(' ').append(f);
							req.sendMsg("[OK"+factions.toString()+"]");
						}
						else
						{
							Faction F=CMLib.factions().getFaction(rest);
							if(F==null)
								req.sendMsg("[FAIL "+rest+" NOT EXIST]");
							else
							{
								int f=((MOB)mod).fetchFaction(F.factionID());
								if(f==Integer.MAX_VALUE)
									req.sendMsg("[FAIL NO FACTION "+F.factionID()+"]"); 
								else
									req.sendMsg("[OK "+f+"]");
							}
						}
						return;
					}
					case 2: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((MOB)mod).numExpertises()+"]");
						else
						{
							String s=((MOB)mod).fetchExpertise(CMath.s_int(rest));
							if(s==null)
								req.sendMsg("[FAIL NO EXPERTISE "+rest+"]"); 
							else
								req.sendMsg("[OK "+s+"]");
						}
						return;
					}
					case 3: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((MOB)mod).numFollowers()+"]");
						else
						{
							MOB M=((MOB)mod).fetchFollower(CMath.s_int(rest));
							if(M==null)
								req.sendMsg("[FAIL NO FOLLOWER "+rest+"]"); 
							else
								req.sendMsg("[OK "+M.Name()+"]");
						}
						return;
					}
				}
			if(mod instanceof ItemPossessor)
				switch(CMParms.indexOf(ITEMSTATS, stat))
				{
					case -1: break;
					case 0: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((ItemPossessor)mod).numItems()+"]");
						else
						{
							Item I=((ItemPossessor)mod).getItem(CMath.s_int(rest));
							if(I==null)
								req.sendMsg("[FAIL NO ITEM "+rest+"]"); 
							else
								req.sendMsg("[OK "+I.Name()+"]");
						}
						return;
					}
				}
			if(mod instanceof Room)
				switch(CMParms.indexOf(ROOMSTATS, stat))
				{
					case -1: break;
					case 0: 
					{
						if(rest.trim().length()==0)
							req.sendMsg("[OK "+((Room)mod).numInhabitants()+"]");
						else
						{
							MOB M=((Room)mod).fetchInhabitant(CMath.s_int(rest));
							if(M==null)
								req.sendMsg("[FAIL NO MOB "+rest+"]"); 
							else
								req.sendMsg("[OK "+M.Name()+"]");
						}
						return;
					}
				}
			
			if(!UseGenBuilder(P,mod))
				req.sendMsg("[OK "+mod.getStat(stat)+"]");
			else
			{
				String[] codes = this.getStatCodes(P, mod);
				for(int i=0;i<codes.length;i++)
					if(codes[i].equalsIgnoreCase(stat))
						if(P instanceof MOB)
							req.sendMsg("[OK "+CMLib.coffeeMaker().getGenMobStat((MOB)P, stat)+"]");
						else
						if(P instanceof Item)
							req.sendMsg("[OK "+CMLib.coffeeMaker().getGenItemStat((Item)P, stat)+"]");
						
			}
		}
		catch(Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target)
	{
		return (user != null);
	}
	public String getHelp(MOB user, PhysicalAgent target, String rest)
	{
		Modifiable mod=null;
		if((rest!=null)&&(rest.trim().length()>0))
		{
			int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(0,x).toUpperCase().trim();
			if(isApplicableTypeCode(rest, target))
				mod=getModifiable(rest.toUpperCase().trim(), target);
		}
		if(mod==null)
			return "USAGE: "+getCommandWord()+" "+CMParms.toStringList(getApplicableStatCodes(target));
		else
		if(rest != null)
			return "USAGE: "+getCommandWord()+" "+rest.toUpperCase().trim()+" "+CMParms.toStringList(getStatCodes(target,mod));
		else
			return "USAGE: "+getCommandWord()+" "+CMParms.toStringList(getStatCodes(target,mod));
		
	}
}
