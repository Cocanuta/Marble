package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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

public class SlaveryParser extends StdLibrary implements SlaveryLibrary
{
	public String ID(){return "SlaveryParser";}
	public boolean tick(Tickable ticking, int tickID) { return true; }
	public Object[] fpmap=null;
	
	public List<Map<String,String>> findMatch(MOB mob, List<String> prereq)
	{
		List<Map<String,String>> possibilities=new Vector<Map<String,String>>();
		Map<String,String> map=new Hashtable<String,String>();
		if(fpmap==null)
		{
			fpmap=new Object[pmap.length];
			for(int p=0;p<pmap.length;p++)
				fpmap[p]=CMParms.toStringArray(CMParms.parse(pmap[p][0]));
		}
		String[] chk=null;
		String[] req=CMParms.toStringArray(prereq);
		boolean reject=false;
		int ci=0,ri=0;
		CMObject[] commands=new CMObject[req.length];
		Social[] socials=new Social[req.length];
		for(int i=0;i<req.length;i++)
		{
			socials[i]=CMLib.socials().fetchSocial(req[i],true);
			commands[i]=CMLib.english().findCommand(mob,new XVector<String>(req[i].toUpperCase()));
		}
		for(int p=0;p<fpmap.length;p++)
		{
			chk=(String[])fpmap[p];
			ci=0;ri=0;
			reject=false;
			while((!reject)&&(ci<chk.length)&&(ri<req.length))
			{
				if(chk[ci].equals(req[ri]))
				{ 
					ci++; ri++; 
					reject=false;
				}
				else
				if(chk[ci].charAt(0)=='%')
				{
					switch(chk[ci].charAt(1))
					{
					case 's':
						if(socials[ri]==null)
							reject=true;
						else
						{
							map.put("%s",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					case 'd':
						if(Directions.getGoodDirectionCode(req[ri])<0)
							reject=true;
						else
						{
							map.put("%d",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					case 'm':
					case 'g':
					case '*':
					case 'r':
					case 'i':
						String code=chk[ci];
						int remain=chk.length-ci;
						String str=req[ri];
						ri++;
						ci++;
						reject=false;
						while(ri<=(req.length-remain))
						{
							String nxt="";
							if(ci<chk.length)
							{
								nxt=chk[ci];
								if(nxt.startsWith("%"))
									nxt="";
							}
							if((nxt.length()>0)
							&&(ri<req.length)
							&&(req[ri].equals(nxt)))
							   break;
							if(ri<req.length)
								str=str+" "+req[ri];
							ri++;
						}
						map.put(code,str);
						break;
					case 'k':
						if(commands[ri]==null)
						   reject=true;
						else
						{
							map.put("%k",req[ri]);
							reject=false;
							ci++;
							ri++;
						}
						break;
					default:
						break;
					}
				}
				else
					reject=true;
			}
			if((reject)||(ci!=chk.length)||(ri!=req.length))
			{
				map.clear();
				continue;
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
				Log.debugOut("GEAS","POSS-"+pmap[p][1]);
			map.put("INSTR",pmap[p][1]);
			possibilities.add(map);
			map=new Hashtable<String,String>();
		}
		return possibilities;
	}

	public String cleanWord(String s)
	{
		String chars=".,;!?'";
		for(int x=0;x<chars.length();x++)
			for(int i=0;i<chars.length();i++)
			{
				while(s.startsWith(""+chars.charAt(i)))
					s=s.substring(1).trim();
				while(s.endsWith(""+chars.charAt(i)))
					s=s.substring(0,s.length()-1).trim();
			}
		return s;
	}

	public geasSteps processRequest(MOB you, MOB me, String req)
	{
		Vector<String> REQ=CMParms.parse(req.toLowerCase().trim());
		for(int v=0;v<REQ.size();v++)
			REQ.setElementAt(cleanWord((String)REQ.elementAt(v)),v);
		List<Map<String,String>> poss=findMatch(me,REQ);
		if(poss.size()==0)
		{
			req=CMParms.combine(REQ,0);
			boolean doneSomething=true;
			boolean didAnything=false;
			while(doneSomething)
			{
				doneSomething=false;
				for(int i=0;i<universalStarters.length;i++)
					if(req.startsWith(universalStarters[i]))
					{
						doneSomething=true;
						didAnything=true;
						req=req.substring(universalStarters.length).trim();
					}
			}
			if(didAnything)
			{
				REQ=CMParms.parse(req);
				poss=findMatch(me,REQ);
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
			Log.debugOut("GEAS","POSSTOTAL-"+poss.size());
		geasSteps geasSteps=new geasSteps(you,me);
		if(poss.size()==0)
		{
			geasStep g=new geasStep(geasSteps);
			g.que.add(CMParms.parse("wanderquery "+req));
			geasSteps.addElement(g);
		}
		else
		{
			for(int i=0;i<poss.size();i++)
			{
				geasStep g=new geasStep(geasSteps);
				Map<String,String> map=(Map<String,String>)poss.get(i);
				Vector<String> all=CMParms.parseSemicolons((String)map.get("INSTR"),true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.GEAS))
					Log.debugOut("GEAS",CMParms.toStringList(all));
				g.que=new Vector<List<String>>();
				for(int a=0;a<all.size();a++)
					g.que.add(CMParms.parse((String)all.elementAt(a)));
				if(you!=null)   map.put("%c",you.name());
				map.put("%n",me.name());
				for(int q=0;q<g.que.size();q++)
				{
					List<String> V=g.que.get(q);
					for(int v=0;v<V.size();v++)
					{
						String s=(String)V.get(v);
						if(s.startsWith("%"))
							V.set(v,CMLib.english().cleanArticles((String)map.get(s.trim())));
					}
				}
				geasSteps.addElement(g);
			}
		}
		return geasSteps;
	}
}
