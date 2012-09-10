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
public class AutoTitles extends StdLibrary implements AutoTitlesLibrary
{
	public String ID(){return "AutoTitles";}
	private TriadSVector<String,String,MaskingLibrary.CompiledZapperMask> autoTitles=null;

	public String evaluateAutoTitle(String row, boolean addIfPossible)
	{
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		while((x>=1)&&(row.charAt(x-1)=='\\')) x=row.indexOf('=',x+1);
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		String title=row.substring(0,x).trim();
		String mask=row.substring(x+1).trim();

		if(title.length()==0)return "Error: Blank title: "+title+"="+mask+"!";
		if(mask.length()==0)return "Error: Blank mask: "+title+"="+mask+"!";
		if(addIfPossible)
		{
			if(autoTitles==null) reloadAutoTitles();
			for(Triad<String,String,MaskingLibrary.CompiledZapperMask> triad : autoTitles)
				if(triad.first.equalsIgnoreCase(title))
					return "Error: Duplicate title: "+title+"="+mask+"!";
			autoTitles.add(new Triad<String,String,MaskingLibrary.CompiledZapperMask>(title,mask,CMLib.masking().maskCompile(mask)));
		}
		return null;
	}
	public boolean isExistingAutoTitle(String title)
	{
		if(autoTitles==null) reloadAutoTitles();
		title=title.trim();
		for(Triad<String,String,MaskingLibrary.CompiledZapperMask> triad : autoTitles)
			if(triad.first.equalsIgnoreCase(title))
				return true;
		return false;
	}

	public Enumeration<String> autoTitles()
	{
		if(autoTitles==null) reloadAutoTitles();
		return autoTitles.firstElements();
	}

	public String getAutoTitleMask(String title)
	{
		if(autoTitles==null) reloadAutoTitles();
		for(Triad<String,String,MaskingLibrary.CompiledZapperMask> triad : autoTitles)
			if(triad.first.equalsIgnoreCase(title))
				return triad.second;
		return "";
	}

	public boolean evaluateAutoTitles(MOB mob)
	{
		if(mob==null) return false;
		PlayerStats P=mob.playerStats();
		if(P==null) return false;
		if(autoTitles==null) reloadAutoTitles();
		String title=null;
		MaskingLibrary.CompiledZapperMask mask=null;
		int pdex=0;
		List<String> ptV=P.getTitles();
		boolean somethingDone=false;
		synchronized(ptV)
		{
			for(Triad<String,String,MaskingLibrary.CompiledZapperMask> triad : autoTitles)
			{
				mask=triad.third;
				title=triad.first;
				pdex=ptV.indexOf(title);
				if(pdex<0)
				{
					String fixedTitle = CMStrings.removeColors(title).replace('\'', '`');
					for(int p=ptV.size()-1;p>=0;p--)
					{
						try {
							String tit=CMStrings.removeColors((String)ptV.get(p)).replace('\'', '`');
							if(tit.equalsIgnoreCase(fixedTitle))
							{ pdex=p; break;}
						}catch(java.lang.IndexOutOfBoundsException ioe){}
					}
				}
				
				if(CMLib.masking().maskCheck(mask,mob,true))
				{
					if(pdex<0)
					{
						if(ptV.size()>0)
							ptV.add(0,title);
						else
							ptV.add(title);
						somethingDone=true;
					}
				}
				else
				if(pdex>=0)
				{
					somethingDone=true;
					ptV.remove(pdex);
				}
			}
		}
		return somethingDone;
	}

	public void dispossesTitle(String title)
	{
		List<String> list=CMLib.database().getUserList();
		String fixedTitle = CMStrings.removeColors(title).replace('\'', '`');
		for(String playerName : list)
		{
			MOB M=CMLib.players().getLoadPlayer(playerName);
			if(M.playerStats()!=null)
			{
				List<String> ptV=M.playerStats().getTitles();
				synchronized(ptV)
				{
					int pdex=ptV.indexOf(title);
					if(pdex<0)
					{
						for(int p=ptV.size()-1;p>=0;p--)
						{
							try {
								String tit=CMStrings.removeColors((String)ptV.get(p)).replace('\'', '`');
								if(tit.equalsIgnoreCase(fixedTitle))
								{ pdex=p; break;}
							}catch(java.lang.IndexOutOfBoundsException ioe){}
						}
					}
					if(pdex>=0)
					{
						ptV.remove(pdex);
						if(!CMLib.flags().isInTheGame(M,true))
							CMLib.database().DBUpdatePlayerPlayerStats(M);
					}
				}
			}
		}
	}

	public void reloadAutoTitles()
	{
		autoTitles=new TriadSVector<String,String,MaskingLibrary.CompiledZapperMask>();
		List<String> V=Resources.getFileLineVector(Resources.getFileResource("titles.txt",true));
		String WKID=null;
		for(int v=0;v<V.size();v++)
		{
			String row=(String)V.get(v);
			WKID=evaluateAutoTitle(row,true);
			if(WKID==null) continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("CharCreation",WKID);
		}
		for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if(M.playerStats()!=null)
			{
				if((evaluateAutoTitles(M))&&(!CMLib.flags().isInTheGame(M,true)))
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}
	
}
