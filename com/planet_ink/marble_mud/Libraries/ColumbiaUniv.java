package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
public class ColumbiaUniv extends StdLibrary implements ExpertiseLibrary
{
	public String ID(){return "ColumbiaUniv";}

	protected SHashtable<String,ExpertiseLibrary.ExpertiseDefinition> completeEduMap=new SHashtable<String,ExpertiseLibrary.ExpertiseDefinition>();
	protected SHashtable<String,List<String>> baseEduSetLists=new SHashtable<String,List<String>>();
	@SuppressWarnings("unchecked")
    protected Hashtable<String,String>[] completeUsageMap=new Hashtable[ExpertiseLibrary.NUM_XFLAGS];
	protected Properties helpMap=new Properties();
	protected DVector rawDefinitions=new DVector(7);

	public ExpertiseLibrary.ExpertiseDefinition addDefinition(String ID, String name, String baseName, String listMask, String finalMask, String[] costs, String[] data)
	{
		ExpertiseLibrary.ExpertiseDefinition def=getDefinition(ID);
		if(def!=null) return  def;
		if(CMSecurity.isExpertiseDisabled(ID.toUpperCase())) return null;
		if(CMSecurity.isExpertiseDisabled("*")) return null;
		for(int i=1;i<ID.length();i++)
			if(CMSecurity.isExpertiseDisabled(ID.substring(0,i).toUpperCase()+"*")) 
				return null;
		def=new  ExpertiseLibrary.ExpertiseDefinition();
		def.ID=ID.toUpperCase();
		def.name=name;
		def.baseName=baseName;
		def.addListMask(listMask);
		def.addFinalMask(finalMask);
		def.data=(data==null)?new String[0]:data;
		int practices=CMath.s_int(costs[0]);
		int trains=CMath.s_int(costs[1]);
		int qpCost=CMath.s_int(costs[2]);
		int expCost=CMath.s_int(costs[3]);
		//int timeCost=CMath.s_int(costs[0]);
		if(practices>0) def.addCost(CostType.PRACTICE, Double.valueOf(practices));
		if(trains>0) def.addCost(CostType.TRAIN, Double.valueOf(trains));
		if(qpCost>0) def.addCost(CostType.QP, Double.valueOf(qpCost));
		if(expCost>0) def.addCost(CostType.XP, Double.valueOf(expCost));
		//if(timeCost>0) def.addCost(CostType.PRACTICE, Double.valueOf(practices));
		completeEduMap.put(def.ID,def);
		baseEduSetLists.clear();
		return def;
	}
	public String getExpertiseHelp(String ID, boolean exact)
	{
		if(ID==null) return null;
		ID=ID.toUpperCase();
		if(exact) return helpMap.getProperty(ID);
		for(Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();
			if(key.startsWith(ID)) return helpMap.getProperty(key);
		}
		for(Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();
			if(CMLib.english().containsString(key, ID)) return helpMap.getProperty(key);
		}
		return null;
	}
	
	public void delDefinition(String ID)
	{
		completeEduMap.remove(ID);
		baseEduSetLists.clear();
	}
	public Enumeration<ExpertiseDefinition> definitions(){ return completeEduMap.elements();}
	public ExpertiseDefinition getDefinition(String ID){ return (ID==null)?null:(ExpertiseDefinition)completeEduMap.get(ID.trim().toUpperCase());}
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly)
	{
		ExpertiseDefinition D=getDefinition(ID);
		if(D!=null) return D;
		for(Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(D.name.equalsIgnoreCase(ID)) return D;
		}
		if(exactOnly) return null;
		for(Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(D.ID.startsWith(ID)) return D;
		}
		for(Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(CMLib.english().containsString(D.name,ID)) return D;
		}
		return null;
	}
	
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob)
	{
		ExpertiseDefinition D=null;
		List<ExpertiseDefinition> V=new Vector<ExpertiseDefinition>();
		for(Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=(ExpertiseDefinition)e.nextElement();
			if(((D.compiledFinalMask()==null)||(CMLib.masking().maskCheck(D.compiledFinalMask(),mob,true)))
			&&((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true))))
				V.add(D);
		}
		return V;
	}
	public List<ExpertiseDefinition> myListableExpertises(MOB mob)
	{
		ExpertiseDefinition D=null;
		List<ExpertiseDefinition> V=new Vector<ExpertiseDefinition>();
		for(Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=(ExpertiseDefinition)e.nextElement();
			if((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true)))
				V.add(D);
		}
		return V;
	}
	public int numExpertises(){return completeEduMap.size();}
	
	private String expertMath(String s,int l)
	{
		int x=s.indexOf('{');
		while(x>=0)
		{
			int y=s.indexOf('}',x);
			if(y<0) break;
			s=s.substring(0,x)+CMath.parseIntExpression(s.substring(x+1,y))+s.substring(y+1);
			x=s.indexOf('{');
		}
		return s;
	}
	
	public int getExpertiseLevel(MOB mob, String expertise)
	{
		if((mob==null)||(expertise==null)) return 0;
		int level=0;
		expertise=expertise.toUpperCase();
		String X=null;
		for(int i=0;i<mob.numExpertises();i++)
		{
			X=mob.fetchExpertise(i);
			if((X!=null)&&(X.startsWith(expertise)))
			{
				int x=CMath.s_int(X.substring(expertise.length()));
				if(x>level) level=x;
			}
		}
		return level;
	}

	public List<String> getStageCodes(String baseExpertiseCode)
	{
		String key=null;
		if(baseExpertiseCode==null) return new ReadOnlyVector<String>(1);
		baseExpertiseCode=baseExpertiseCode.toUpperCase();
		if(!baseEduSetLists.containsKey(baseExpertiseCode))
		{
			synchronized(("ListedEduBuild:"+baseExpertiseCode).intern())
			{
				if(!baseEduSetLists.containsKey(baseExpertiseCode))
				{
					List<String> codes=new LinkedList<String>();
					for(Enumeration<String> e=completeEduMap.keys();e.hasMoreElements();)
					{
						key=(String)e.nextElement();
						if(key.startsWith(baseExpertiseCode)
						&&(CMath.isInteger(key.substring(baseExpertiseCode.length()))||CMath.isRomanNumeral(key.substring(baseExpertiseCode.length()))))
							codes.add(key);
					}
					baseEduSetLists.put(baseExpertiseCode, new ReadOnlyVector<String>(codes));
				}
			}
		}
		return baseEduSetLists.get(baseExpertiseCode);
	}

	public int getStages(String baseExpertiseCode){return getStageCodes(baseExpertiseCode).size();}

	public String getGuessedBaseExpertiseName(final String expertiseCode)
	{
		int lastBadChar=expertiseCode.length()-1;
		while( (lastBadChar>=0)
		&&(CMath.isInteger(expertiseCode.substring(lastBadChar))||CMath.isRomanNumeral(expertiseCode.substring(lastBadChar))))
			lastBadChar--;
		if(lastBadChar<expertiseCode.length()-1)
			return expertiseCode.substring(0,lastBadChar+1);
		return expertiseCode;
	}
	public List<String> getPeerStageCodes(final String expertiseCode)
	{
		return getStageCodes(getGuessedBaseExpertiseName(expertiseCode));
	}
	public String getApplicableExpertise(String ID, int code)
	{
		return (String)completeUsageMap[code].get(ID);
	}
	public int getApplicableExpertiseLevel(String ID, int code, MOB mob)
	{
		return getExpertiseLevel(mob,(String)completeUsageMap[code].get(ID));
	}
	
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible)
	{
		int levels=0;
		HashSet<String> flags=new HashSet<String>();
		String s=null;
		String skillMask=null;
		String[] costs=new String[5];
		String[] data=new String[0];
		String WKID=null;
		String name,WKname=null;
		String listMask,WKlistMask=null;
		String finalMask,WKfinalMask=null;
		List<String> skillsToRegister=null;
		ExpertiseLibrary.ExpertiseDefinition def=null;
		boolean didOne=false;
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0)) return null;
		int x=row.indexOf('=');
		if(x<0) return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!"; 
		if(row.trim().toUpperCase().startsWith("DATA_"))
		{
			String lastID=ID;
			ID=row.substring(0,x).toUpperCase();
			row=row.substring(x+1);
			ID=ID.substring(5).toUpperCase();
			if(ID.length()==0) ID=lastID;
			if((lastID==null)||(lastID.length()==0))
				return "Error: No last expertise found for data: "+lastID+"="+row;
			else
			if(this.getDefinition(ID)!=null)
			{
				def=getDefinition(ID);
				WKID=def.name.toUpperCase().replace(' ','_');
				if(addIfPossible)
				{
					def.data=CMParms.parseCommas(row,true).toArray(new String[0]);
				}
			}
			else
			{
				List<String> stages=getStageCodes(ID);
				if(addIfPossible)
    				for(int s1=0;s1<stages.size();s1++)
    				{
    					def=getDefinition(stages.get(s1));
    					if(def==null) continue;
    					def.data=CMParms.parseCommas(row,true).toArray(new String[0]);
    				}
			}
			return null;
		}
		if(row.trim().toUpperCase().startsWith("HELP_"))
		{
			String lastID=ID;
			ID=row.substring(0,x).toUpperCase();
			row=row.substring(x+1);
			ID=ID.substring(5).toUpperCase();
			if(ID.length()==0) ID=lastID;
			if((lastID==null)||(lastID.length()==0))
				return "Error: No last expertise found for help: "+lastID+"="+row;
			else
			if(getDefinition(ID)!=null)
			{
				def=getDefinition(ID);
				WKID=def.name.toUpperCase().replace(' ','_');
				if(addIfPossible)
				{
					helpMap.remove(WKID);
					helpMap.put(WKID,row);
				}
			}
			else
			{
				List<String> stages=getStageCodes(ID);
				if((stages==null)||(stages.size()==0))
					return "Error: Expertise not yet defined: "+ID+"="+row;
				def=getDefinition(stages.get(0));
				if(def!=null)
				{
					WKID=def.name.toUpperCase().replace(' ','_');
					x=WKID.lastIndexOf('_');
					if((x>=0)&&(CMath.isInteger(WKID.substring(x+1))||CMath.isRomanNumeral(WKID.substring(x+1))))
					{
						WKID=WKID.substring(0,x);
						if(addIfPossible)
						if(!helpMap.containsKey(WKID))
							helpMap.put(WKID,row+"\n\r(See help on "+def.name+").");
					}
				}
				if(addIfPossible)
				for(int s1=0;s1<stages.size();s1++)
				{
					def=getDefinition(stages.get(s1));
					if(def==null) continue;
					WKID=def.name.toUpperCase().replace(' ','_');
					if(!helpMap.containsKey(WKID)) helpMap.put(WKID,row);
				}
			}
			return null;
		}
		ID=row.substring(0,x).toUpperCase();
		row=row.substring(x+1);
		Vector<String> parts=CMParms.parseCommas(row,false);
		if(parts.size()!=11)
			return "Error: Expertise row malformed (Requires 11 entries/10 commas): "+ID+"="+row;
		name=(String)parts.elementAt(0);
		if(name.length()==0)
			return "Error: Expertise name ("+name+") malformed: "+ID+"="+row;
		if(!CMath.isInteger((String)parts.elementAt(1)))
			return "Error: Expertise num ("+((String)parts.elementAt(1))+") malformed: "+ID+"="+row;
		levels=CMath.s_int((String)parts.elementAt(1));
		flags.clear();
		flags.addAll(CMParms.parseAny(((String)parts.elementAt(2)).toUpperCase(),'|',true));
		
		skillMask=(String)parts.elementAt(3);
		if(skillMask.length()==0)
			return "Error: Expertise skill mask ("+skillMask+") malformed: "+ID+"="+row;
		skillsToRegister=CMLib.masking().getAbilityEduReqs(skillMask);
		if(skillsToRegister.size()==0)
			return "Error: Expertise no skills ("+skillMask+") found: "+ID+"="+row;
		listMask=skillMask+" "+((String)parts.elementAt(4));
		finalMask=(((String)parts.elementAt(5)));
		for(int i=6;i<11;i++)
			costs[i-6]=(String)parts.elementAt(i);
		didOne=false;
		for(int u=0;u<completeUsageMap.length;u++)
			didOne=didOne||flags.contains(ExpertiseLibrary.XFLAG_CODES[u]);
		if(!didOne)
			return "Error: No flags ("+((String)parts.elementAt(2)).toUpperCase()+") were set: "+ID+"="+row;
		if(addIfPossible)
		{
			String baseName=CMStrings.replaceAll(CMStrings.replaceAll(ID,"@X2",""),"@X1","").toUpperCase();
			for(int l=1;l<=levels;l++)
			{
				WKID=CMStrings.replaceAll(ID,"@X1",""+l);
				WKID=CMStrings.replaceAll(WKID,"@X2",""+CMath.convertToRoman(l));
				WKname=CMStrings.replaceAll(name,"@x1",""+l);
				WKname=CMStrings.replaceAll(WKname,"@x2",""+CMath.convertToRoman(l));
				WKlistMask=CMStrings.replaceAll(listMask,"@x1",""+l);
				WKlistMask=CMStrings.replaceAll(WKlistMask,"@x2",""+CMath.convertToRoman(l));
				WKfinalMask=CMStrings.replaceAll(finalMask,"@x1",""+l);
				WKfinalMask=CMStrings.replaceAll(WKfinalMask,"@x2",""+CMath.convertToRoman(l));
				if((l>1)&&(listMask.toUpperCase().indexOf("-EXPERT")<0))
				{
					s=CMStrings.replaceAll(ID,"@X1",""+(l-1));
					s=CMStrings.replaceAll(s,"@X2",""+CMath.convertToRoman(l-1));
					WKlistMask="-EXPERTISE \"+"+s+"\" "+WKlistMask;
				}
				WKlistMask=expertMath(WKlistMask,l);
				WKfinalMask=expertMath(WKfinalMask,l);
				def=addDefinition(WKID,WKname,baseName,WKlistMask,WKfinalMask,costs,data);
				if(def!=null){
					def.compiledFinalMask();
					def.compiledListMask();
				}
			}
		}
		ID=CMStrings.replaceAll(ID,"@X1","");
		ID=CMStrings.replaceAll(ID,"@X2","");
		for(int u=0;u<completeUsageMap.length;u++)
			if(flags.contains(ExpertiseLibrary.XFLAG_CODES[u]))
				for(int k=0;k<skillsToRegister.size();k++)
					completeUsageMap[u].put((String)skillsToRegister.get(k),ID);
		return addIfPossible?ID:null;
	}
	
	public void recompileExpertises()
	{
		for(int u=0;u<completeUsageMap.length;u++)
			completeUsageMap[u]=new Hashtable<String,String>();
		helpMap.clear();
		List<String> V=Resources.getFileLineVector(Resources.getFileResource("skills/expertises.txt",true));
		String ID=null,WKID=null;
		for(int v=0;v<V.size();v++)
		{
			String row=(String)V.get(v);
			WKID=this.confirmExpertiseLine(row,ID,true);
			if(WKID==null) continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("ColumbiaUniv",WKID);
			else
				ID=WKID;
		}
	}
}
