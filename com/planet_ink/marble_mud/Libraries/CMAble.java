package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.Common.CommonSkill;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Commands.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.AbilityComponent.CompLocation;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class CMAble extends StdLibrary implements AbilityMapper
{
	public String ID(){return "CMAble";}
	
	protected Map<String, Map<String, AbilityMapping>> 
										completeAbleMap 			= new SHashtable<String, Map<String, AbilityMapping>>();
	protected Map<String, Integer>  	lowestQualifyingLevelMap	= new SHashtable<String, Integer>();
	protected Map<String, Integer>  	maxProficiencyMap   		= new SHashtable<String, Integer>();
	protected Map<String, Object>   	allows  					= new SHashtable<String, Object>();
	protected Map<Integer, Set<Integer>>completeDomainMap   		= new SHashtable<Integer,Set<Integer>>();
	protected Map<String, Map<String, AbilityMapping>> 
										reverseAbilityMap   		= new TreeMap<String, Map<String, AbilityMapping>>();
	protected List<AbilityMapping>  	eachClassSet				= null;
	protected Map<String,int[]> 		hardOverrideCache			= new Hashtable<String,int[]>(); 

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  boolean autoGain,
									  List<String> skillPreReqs)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,false,skillPreReqs,""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  boolean autoGain,
									  List<String> skillPreReqs,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,false,skillPreReqs,extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defParm,autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defParm,autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,"",autoGain,false,new Vector(),extraMasks); }

	public int numMappedAbilities()
	{
		return reverseAbilityMap.size();
	}
	
	public void delCharAbilityMapping(String ID, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String, AbilityMapping> ableMap = completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				ableMap.remove(abilityID);
		}
		Map<String,AbilityMapping> revT=reverseAbilityMap.get(abilityID);
		if(revT!=null) revT.remove(ID);
	}
	public void delCharMappings(String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
		for(String abilityID : reverseAbilityMap.keySet())
		{
			Map<String,AbilityMapping> revT=reverseAbilityMap.get(abilityID);
			if(revT!=null) revT.remove(ID);
		}
	}

	public Enumeration<AbilityMapping> getClassAbles(String ID, boolean addAll)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new SHashtable<String, AbilityMapping>());
		final Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		final Map<String, AbilityMapping> allAbleMap=completeAbleMap.get("All");
		if((!addAll)||(allAbleMap==null)) 
			return new IteratorEnumeration<AbilityMapping>(ableMap.values().iterator());
		final Iterator[] iters=new Iterator[]{
				ableMap.values().iterator(),
				new FilteredIterator(allAbleMap.values().iterator(),
					new Filterer<Object>(){
						public boolean passesFilter(Object obj) {
							if((obj instanceof AbilityMapping)
							&&(ableMap.containsKey(((AbilityMapping)obj).abilityID)))
								return false;
							return true;
						}
					}
				)
		};
		return new IteratorEnumeration(new MultiIterator(iters));
	}
	
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,new Vector(),"");}
	
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,new Vector(),extraMasks);}
	
	
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  List<String> preReqSkillsList,
									  String extraMask)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector(),"");}
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector(),extraMasks);}

	public void addDynaAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ 
		delCharAbilityMapping(ID,abilityID);
		if(CMSecurity.isAbilityDisabled(ID.toUpperCase())) return;
		Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
		}
		AbilityMapping able = makeAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret, false,new Vector(),"",null);
		addClassAbility(abilityID,ableMap,able);
	}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  List<String> preReqSkillsList,
									  String extraMask)
	{ addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);}

	
	public void addPreRequisites(String ID, List<String> preReqSkillsList, String extraMask)
	{
		if(preReqSkillsList==null) return;
		for(int v=0;v<preReqSkillsList.size();v++)
		{
			String s=(String)preReqSkillsList.get(v);
			int x=s.indexOf('(');
			if((x>=0)&&(s.endsWith(")")))
				s=s.substring(0,x);
			if((s.indexOf('*')>=0)||(s.indexOf(',')>=0))
			{
				String ID2=ID;
				while(allows.containsValue("*"+ID2))
					ID2="*"+ID2;
				allows.put("*"+ID2,s);
			}
			else
			{
				SVector V=(SVector)allows.get(s);
				if(V==null){ V=new SVector(); allows.put(s,V);}
				if(!V.contains(ID))V.addElement(ID);
			}
		}
		if((extraMask!=null)&&(extraMask.trim().length()>0))
		{
			List<String> preReqsOf=CMLib.masking().getAbilityEduReqs(extraMask);
			for(int v=0;v<preReqsOf.size();v++)
			{
				String s=(String)preReqsOf.get(v);
				if((s.indexOf('*')>=0)||(s.indexOf(',')>=0))
				{
					String ID2=ID;
					while(allows.containsValue("*"+ID2))
						ID2="*"+ID2;
					allows.put("*"+ID2,s);
				}
				else
				{
					SVector V=(SVector)allows.get(s);
					if(V==null)
					{
						V=new SVector();
						allows.put(s,V);
					}
					if(!V.contains(ID))
						V.addElement(ID);
				}
			}
		}
	}

	public boolean isDomainIncludedInAnyAbility(int domain, int acode)
	{
		STreeSet<Integer> V=(STreeSet<Integer>)completeDomainMap.get(Integer.valueOf(domain));
		if(V==null)
		{
			Ability A=null;
			V=new STreeSet<Integer>();
			for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				A=(Ability)e.nextElement();
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
				&&(!V.contains(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)))))
					V.add(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)));
			}
			completeDomainMap.put(Integer.valueOf(domain),V);
		}
		return V.contains(Integer.valueOf(acode));
	}

	public List<QualifyingID> getClassAllowsList(String classID)
	{
		List<AbilityMapping> ABLES=getUpToLevelListings(classID,CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL),false,false);
		SHashtable alreadyDone=new SHashtable();
		List<QualifyingID> DV=new Vector<QualifyingID>(2);
		Integer Ix=null;
		for(AbilityMapping able : ABLES)
		{
			for(Iterator<String> i=getAbilityAllowsList(able.abilityID);i.hasNext();)
			{
				String s = i.next();
				Ix=(Integer)alreadyDone.get(s);
				if(Ix==null)
				{
					alreadyDone.put(s, Integer.valueOf(DV.size()));
					DV.add(new QualifyingID(s,able.qualLevel));
				}
				else
				{
					QualifyingID Q=DV.get(Ix.intValue());
					if((Q!=null)&&(Q.qualifyingLevel>able.qualLevel))
						Q.qualifyingLevel=able.qualLevel;
				}
			}
		}
		return DV;
	}

	public Iterator<String> getAbilityAllowsList(String ableID)
	{
		String abilityID=null;
		Vector remove=null;
		for(String KEYID : allows.keySet())
		{
			if(KEYID.startsWith("*"))
			{
				abilityID=(String)allows.get(KEYID);
				if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
				{
					List<String> orset=getOrSet(ableID,abilityID);
					if(orset.size()!=0)
					{
						String KEYID2=KEYID;
						while(KEYID2.startsWith("*")) KEYID2=KEYID2.substring(1);
						addPreRequisites(KEYID2,orset,"");
						if(remove==null) remove=new Vector();
						remove.addElement(KEYID);
					}
				}
			}
		}
		if(remove!=null)
		{
			for(int r=0;r<remove.size();r++)
				allows.remove(remove.elementAt(r));
		}
		SVector<String> set = (SVector<String>)allows.get(ableID); 
		return (set==null)?new SVector<String>(1).iterator():set.iterator();
	}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String abilityID,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  List<String> preReqSkillsList,
									  String extraMask,
									  Integer[] costOverrides)
	{
		delCharAbilityMapping(ID,abilityID);
		if(CMSecurity.isAbilityDisabled(ID.toUpperCase())) return;
		Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
			handleEachAndClassAbility(ableMap, getAllQualifiesMap(null), ID);
		}
		AbilityMapping able = makeAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,false,preReqSkillsList,extraMask,costOverrides);
		addClassAbility(abilityID,ableMap,able);
	}

	public AbilityMapping makeAbilityMapping(String ID,
											 int qualLevel,
											 String abilityID,
											 int defaultProficiency,
											 int maxProficiency,
											 String defaultParam,
											 boolean autoGain,
											 boolean secret,
											 boolean isAllQualified,
											 List<String> preReqSkillsList,
											 String extraMask,
											 Integer[] costOverrides)
	{
		AbilityMapping able=new AbilityMapping(ID);
		able.abilityID=abilityID;
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.isSecret=secret;
		able.isAllQualified=isAllQualified;
		able.defaultParm=defaultParam==null?"":defaultParam;
		able.defaultProficiency=defaultProficiency;
		able.maxProficiency=maxProficiency;
		able.extraMask=extraMask==null?"":extraMask;
		able.costOverrides=costOverrides;
		able.originalSkillPreReqList=CMParms.toStringList(preReqSkillsList);

		able.skillPreReqs=new DVector(2);
		addPreRequisites(abilityID,preReqSkillsList,extraMask);
		if(preReqSkillsList!=null)
			for(int v=0;v<preReqSkillsList.size();v++)
			{
				String s=(String)preReqSkillsList.get(v);
				int prof=0;
				int x=s.indexOf('(');
				if((x>=0)&&(s.endsWith(")")))
				{
					prof=CMath.s_int(s.substring(x+1,s.length()-1));
					s=s.substring(0,x);
				}
				able.skillPreReqs.addElement(s,Integer.valueOf(prof));
			}
		return able;
	}

	public int getCalculatedMedianLowestQualifyingLevel()
	{
		Integer[] allLevelsArray = lowestQualifyingLevelMap.values().toArray(new Integer[0]);
		Arrays.sort( allLevelsArray );
		if(allLevelsArray.length==0) return 0;
		if(allLevelsArray.length==1) return allLevelsArray[0].intValue();
		return allLevelsArray[(int)Math.round( CMath.div( allLevelsArray.length,2.0 ) )].intValue();
	}
	
	public void addClassAbility(String abilityID, Map<String, AbilityMapping> ableMap, AbilityMapping able)
	{
		if(CMSecurity.isAbilityDisabled(able.abilityID.toUpperCase())) return;
		ableMap.put(abilityID,able);

		int qualLevel = able.qualLevel;
		int maxProficiency = able.maxProficiency;
		
		// set lowest level map
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(abilityID);
		if(qualLevel > 0)
		{
			if((lowLevel==null)
			||(qualLevel<lowLevel.intValue()))
				lowestQualifyingLevelMap.put(abilityID,Integer.valueOf(qualLevel));
		}
		
		// and max proficiency maps
		Integer maxProf=(Integer)maxProficiencyMap.get(abilityID);
		if((maxProf==null)
		||(maxProficiency>maxProf.intValue()))
			maxProficiencyMap.put(abilityID,Integer.valueOf(maxProficiency));
		
		// and the reverse lookup map
		Map<String, AbilityMapping> revT = reverseAbilityMap.get(abilityID);
		if(revT==null)
		{
			revT = new TreeMap<String, AbilityMapping>();
			reverseAbilityMap.put(abilityID,revT);
		}
		if(!revT.containsKey(able.ID)) 
			revT.put(able.ID, able);
		
		// archons get everything
		int arc_level=getQualifyingLevel("Archon",true,abilityID);
		if(((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
		&&(!able.ID.equalsIgnoreCase("Archon"))
		&&(!able.ID.equalsIgnoreCase("All")))
			addCharAbilityMapping("Archon",qualLevel,abilityID,true);
	}
	

	public synchronized void handleEachAndClassAbility(Map<String, AbilityMapping> ableMap, Map<String,Map<String,AbilityMapping>> allQualMap, String ID)
	{
		if(eachClassSet == null)
		{
			eachClassSet = new SLinkedList<AbilityMapping>();
			Map<String,AbilityMapping> eachMap=allQualMap.get("EACH");
			Map<String,AbilityMapping> allAllMap=allQualMap.get("ALL");
			for(AbilityMapping mapped : eachMap.values())
			{
				if(CMSecurity.isAbilityDisabled(mapped.abilityID.toUpperCase())) continue;
				AbilityMapping able = mapped.copyOf();
				eachClassSet.add(able);
			}
			for(AbilityMapping mapped : allAllMap.values())
			{
				if(CMSecurity.isAbilityDisabled(mapped.abilityID.toUpperCase())) continue;
				AbilityMapping able = mapped.copyOf();
				able.ID="All";
				Map<String, AbilityMapping> allMap=completeAbleMap.get("All");
				if(allMap == null)
				{
					allMap=new SHashtable<String,AbilityMapping>();
					completeAbleMap.put("All",allMap);
				}
				able.allQualifyFlag=true;
				addClassAbility(able.abilityID, allMap, able);
			}
		}
		for(final Iterator<AbilityMapping> a=eachClassSet.iterator();a.hasNext();)
		{
			final AbilityMapping able=a.next().copyOf();
			able.ID = ID;
			able.allQualifyFlag=true;
			addClassAbility(able.abilityID, ableMap, able);
		}
	}
	
	
	public boolean qualifiesByAnyCharClass(String abilityID)
	{
		if(completeAbleMap.containsKey("All"))
		{
			Map<String, AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return true;
		}
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			CharClass C=(CharClass)e.nextElement();
			if(completeAbleMap.containsKey(C.ID()))
			{
				Map<String, AbilityMapping> ableMap=completeAbleMap.get(C.ID());
				if(ableMap.containsKey(abilityID))
					return true;
			}
		}
		return false;
	}

	public int lowestQualifyingLevel(String abilityID)
	{
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(abilityID);
		if(lowLevel==null) return 0;
		return lowLevel.intValue();
	}

	public boolean classOnly(String classID, String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(String key : completeAbleMap.keySet())
		{
			if((!key.equalsIgnoreCase(classID))
			&&(completeAbleMap.get(classID).containsKey(abilityID)))
				return false;
		}
		return true;
	}


	public boolean classOnly(MOB mob, String classID, String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			CharClass C=mob.charStats().getMyClass(c);
			if((!C.ID().equals(classID))
			&&(completeAbleMap.containsKey(classID))
			&&(completeAbleMap.get(classID).containsKey(abilityID)))
				return false;
		}
		return true;
	}


	public boolean availableToTheme(String abilityID, int theme, boolean publicly)
	{
		for(String key : completeAbleMap.keySet())
		{
			if(completeAbleMap.get(key).containsKey(abilityID))
			{
				if(key.equalsIgnoreCase("All")) return true;
				CharClass C=CMClass.getCharClass(key);
				if((C!=null)
				&&((C.availabilityCode()&theme)==theme)
				&&((!publicly)||((C.availabilityCode()&Area.THEME_SKILLONLYMASK)==0)))
					return true;
			}
		}
		return false;
	}

	public List<String> getLevelListings(String ID, boolean checkAll, int level)
	{
		List<String> V=new Vector<String>();
		CharClass C=CMClass.getCharClass(ID);
		if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
			return V;
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if(able.qualLevel==level)
					V.add(key);
			}
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel==level)
				&&(!V.contains(key)))
					V.add(key);
			}
		}
		return V;
	}
	public List<AbilityMapping> getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly)
	{
		List<AbilityMapping> DV=new Vector<AbilityMapping>();
		CharClass C=CMClass.getCharClass(ID);
		if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
			level=C.getLevelCap();
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
					DV.add(able);
			}
		}
		if((completeAbleMap.containsKey("All"))&&(!ignoreAll))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
				{
					boolean found=false;
					for(AbilityMapping A : DV)
						if(A.ID.equalsIgnoreCase(key))
						{
							found=true;
							break;
						}
					if(!found)
						DV.add(able);
				}
			}
		}
		return DV;
	}

	public int getQualifyingLevel(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).qualLevel;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
			{
				int qualLevel = ableMap.get(abilityID).qualLevel;
				CharClass C=CMClass.getCharClass(ID);
				if((C!=null)&&(C.getLevelCap()>=0))
					return qualLevel>C.getLevelCap()?-1:qualLevel;
				return qualLevel;
			}
		}
		return -1;
	}

	protected List<String> getOrSet(String errStr, String abilityID)
	{
		Ability preA=null;
		List<String> orset=new Vector();
		List<String> preorset=CMParms.parseCommas(abilityID,true);
		for(int p=0;p<preorset.size();p++)
		{
			abilityID=(String)preorset.get(p);
			if(abilityID.startsWith("*"))
			{
				String a=abilityID.substring(1).toUpperCase();
				for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().endsWith(a))
						orset.add(preA.ID());
				}
			}
			else
			if(abilityID.endsWith("*"))
			{
				String a=abilityID.substring(0,abilityID.length()-1).toUpperCase();
				for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().startsWith(a))
						orset.add(preA.ID());
				}
			}
			else
				orset.add(abilityID);
		}
		for(int o=orset.size()-1;o>=0;o--)
		{
			abilityID=(String)orset.get(o);
			preA=CMClass.getAbility(abilityID);
			if(preA==null)
			{
				preA=CMClass.findAbility(abilityID);
				if(preA!=null)
					orset.set(o,preA.ID());
				else
				{
					Log.errOut("CMAble","Skill "+errStr+" requires nonexistant skill "+abilityID+".");
					orset.clear();
					break;
				}
			}
		}
		return orset;
	}

	public void fillPreRequisites(Ability A, DVector rawPreReqs)
	{
		for(int v=0;v<rawPreReqs.size();v++)
		{
			String abilityID=(String)rawPreReqs.elementAt(v,1);
			if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
			{
				List<String> orset=getOrSet(A.ID(),abilityID);
				if(orset.size()!=0)
					rawPreReqs.setElementAt(v,1,orset);
			}
			else
			{
				Ability otherAbility=CMClass.getAbility(abilityID);
				if(otherAbility==null)
				{
					otherAbility=CMClass.findAbility(abilityID);
					if(otherAbility!=null)
						rawPreReqs.setElementAt(v,1,otherAbility.ID());
					else
					{
						Log.errOut("CMAble","Skill "+A.ID()+" requires nonexistant skill "+abilityID+".");
						break;
					}
				}
			}
		}
	}

	public DVector getCommonPreRequisites(MOB mob, Ability A)
	{
		DVector preReqs=getRawPreRequisites(mob,A);
		if((preReqs==null)||(preReqs.size()==0)) 
			return getCommonPreRequisites(A);
		fillPreRequisites(A,preReqs);
		return preReqs;
	}
	
	public DVector getCommonPreRequisites(Ability A)
	{
		DVector preReqs=getRawPreRequisites("All", false, A.ID());
		if(preReqs==null)
			for(Map<String,AbilityMapping> ableMap : completeAbleMap.values())
				if(ableMap.containsKey(A.ID()))
				{
					preReqs=ableMap.get(A.ID()).skillPreReqs;
					if((preReqs!=null)&&(preReqs.size()>0)) break;
				}
		if((preReqs==null)||(preReqs.size()==0)) return new DVector(2);
		final DVector requisites=preReqs.copyOf(); 
		fillPreRequisites(A,requisites);
		return requisites;
	}

	public String getCommonExtraMask(Ability A)
	{
		String mask=null;
		{
			Map<String,AbilityMapping> ableMap=null;
			if(completeAbleMap.containsKey("All"))
			{
				ableMap=completeAbleMap.get("All");
				if(ableMap.containsKey(A.ID()))
					mask=ableMap.get(A.ID()).extraMask;
			}
		}
		if((mask==null)||(mask.length()==0))
			for(Map<String,AbilityMapping> ableMap : completeAbleMap.values())
				if(ableMap.containsKey(A.ID()))
				{
					mask=ableMap.get(A.ID()).extraMask;
					if((mask!=null)&&(mask.length()>0)) break;
				}
		if((mask==null)||(mask.length()==0)) return "";
		return mask;
	}

	
	public DVector getUnmetPreRequisites(MOB studentM, Ability A)
	{
		DVector V=getRawPreRequisites(studentM,A);
		if((V==null)||(V.size()==0)) return new DVector(2);
		fillPreRequisites(A,V);
		String abilityID=null;
		Integer prof=null;
		Ability A2=null;
		for(int v=V.size()-1;v>=0;v--)
		{
			prof=(Integer)V.elementAt(v,2);
			if(V.elementAt(v,1) instanceof String)
			{
				abilityID=(String)V.elementAt(v,1);
				A2=studentM.fetchAbility(abilityID);
				if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					V.removeElementAt(v);
				else
				if(!qualifiesByLevel(studentM,abilityID))
					V.removeElementAt(v);
			}
			else
			{
				List<String> orset=(List<String>)V.elementAt(v,1);
				for(int o=orset.size()-1;o>=0;o--)
				{
					abilityID=(String)orset.get(o);
					A2=studentM.fetchAbility(abilityID);
					if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					{
						orset.clear();
						break;
					}
					if(!qualifiesByLevel(studentM,abilityID))
						orset.remove(o);
				}
				if(orset.size()==0)
					V.removeElementAt(v);
			}
		}
		return V;
	}

	public DVector getRawPreRequisites(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).skillPreReqs;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).skillPreReqs;
		}
		return null;
	}

	
	public String formatPreRequisites(DVector preReqs)
	{
		StringBuffer names=new StringBuffer("");
		if((preReqs!=null)&&(preReqs.size()>0))
		{
			Integer prof=null;
			for(int p=0;p<preReqs.size();p++)
			{
				prof=(Integer)preReqs.elementAt(p,2);
				if(preReqs.elementAt(p,1) instanceof List)
				{
					List V=(List)preReqs.elementAt(p,1);
					names.append("(One of: ");
					for(int v=0;v<V.size();v++)
					{
						Ability A=CMClass.getAbility((String)V.get(v));
						if(A!=null)
						{
							names.append("'"+A.name()+"'");
							if(V.size()>1)
							{
								if(v==(V.size()-2))
									names.append(", or ");
								else
								if(v<V.size()-2)
									names.append(", ");
							}
						}
					}
					if(prof.intValue()>0)
						names.append(" at "+prof+"%)");
					else
						names.append(")");
				}
				else
				{
					Ability A=CMClass.getAbility((String)preReqs.elementAt(p,1));
					if(A!=null)
					{
						names.append("'"+A.name()+"'");
						if(prof.intValue()>0)
							names.append(" at "+prof+"%");
					}
				}
				if(preReqs.size()>1)
				{
					if(p==(preReqs.size()-2))
						names.append(", and ");
					else
					if(p<preReqs.size()-2)
						names.append(", ");
				}
			}
		}
		return names.toString();
	}

	public final List<String> getCurrentlyQualifyingIDs(final MOB studentM, final String AID)
	{
		final List<String> ids=new LinkedList<String>();
		final CharStats cStats = studentM.charStats();
		for(int c=cStats.numClasses()-1;c>=0;c--)
		{
			CharClass C=cStats.getMyClass(c);
			int qualLevel=getQualifyingLevel(C.ID(),true,AID);
			int classLevel=studentM.charStats().getClassLevel(C);
			if((qualLevel>=0)&&(classLevel>=qualLevel))
				ids.add(C.ID());
		}
		int qualRacelevel=getQualifyingLevel(cStats.getMyRace().ID(),false,AID);
		int charLevel=studentM.basePhyStats().level();
		if((qualRacelevel>=0)&&(charLevel>=qualRacelevel))
			ids.add(cStats.getMyRace().ID());
		final Clan clan=studentM.getMyClan();
		if(clan!=null)
		{
			int qualClanlevel=getQualifyingLevel(clan.getGovernment().ID(),false,AID);
			if((qualClanlevel>=0)&&(clan.getClanLevel()>=qualClanlevel))
				ids.add(clan.getGovernment().ID());
		}
		return ids;
	}
	
	public DVector getRawPreRequisites(MOB studentM, Ability A)
	{
		if((studentM==null)||(A==null)) return new DVector(2);
		final String AID=A.ID();
		final List<String> qualifyingIDs=getCurrentlyQualifyingIDs(studentM,AID);
		DVector reqs=null;
		for(final Iterator<String> i=qualifyingIDs.iterator();i.hasNext();)
		{
			final String ID=i.next();
			reqs=getRawPreRequisites(ID,true,A.ID());
			if(reqs!=null) return reqs.copyOf();
		}
		reqs=getRawPreRequisites(studentM.charStats().getCurrentClass().ID(),true,A.ID());
		return (reqs==null)?new DVector(2):reqs.copyOf();
	}

	public String getExtraMask(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).extraMask;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).extraMask;
		}
		return null;
	}

	public String getApplicableMask(MOB studentM, Ability A)
	{
		if((studentM==null)||(A==null)) return "";
		final String AID=A.ID();
		final List<String> qualifyingIDs=getCurrentlyQualifyingIDs(studentM,AID);
		String mask=null;
		for(final Iterator<String> i=qualifyingIDs.iterator();i.hasNext();)
		{
			final String ID=i.next();
			mask=getExtraMask(ID,true,AID);
			if(mask!=null) return mask;
		}
		
		mask=getExtraMask(studentM.charStats().getCurrentClass().ID(),true,A.ID());
		return mask==null?"":mask;
	}

	public int qualifyingLevel(MOB studentM, Ability A)
	{
		if(studentM==null) return -1;
		int theLevel=-1;
		int greatestDiff=-1;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=studentM.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theLevel=level;
			}
		}
		int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		int charLevel=studentM.basePhyStats().level();
		if((raceLevel>=0)
		&&(charLevel>=raceLevel)
		&&((charLevel-raceLevel)>greatestDiff))
		{
			greatestDiff=charLevel-raceLevel;
			theLevel=raceLevel;
		}
		final Clan clan = studentM.getMyClan();
		if(clan != null)
		{
			int clanLevel=getQualifyingLevel(clan.getGovernment().ID(),false,A.ID());
			if((clanLevel>=0)
			&&(clan.getClanLevel()>=clanLevel)
			&&((charLevel-clanLevel)>greatestDiff))
			{
				greatestDiff=charLevel-clanLevel;
				theLevel=clanLevel;
			}
		}
		if(theLevel<0)
			return getQualifyingLevel(studentM.charStats().getCurrentClass().ID(),true,A.ID());
		return theLevel;
	}

	public int qualifyingClassLevel(MOB studentM, Ability A)
	{
		if(studentM==null) return -1;
		int greatestDiff=-1;
		CharClass theClass=null;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=studentM.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theClass=C;
			}
		}
		int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		int charLevel=studentM.basePhyStats().level();
		if((raceLevel>=0)
		&&(charLevel>=raceLevel)
		&&((charLevel-raceLevel)>greatestDiff))
			greatestDiff=charLevel-raceLevel;
		final Clan clan = studentM.getMyClan();
		if(clan != null)
		{
			int clanLevel=getQualifyingLevel(clan.getGovernment().ID(),false,A.ID());
			if((clanLevel>=0)
			&&(clan.getClanLevel()>=clanLevel)
			&&((charLevel-clanLevel)>greatestDiff))
				greatestDiff=charLevel-clanLevel;
		}
		if(theClass==null)
			return studentM.charStats().getClassLevel(studentM.charStats().getCurrentClass());
		return studentM.charStats().getClassLevel(theClass);
	}

	public CMObject lowestQualifyingClassRaceGovt(MOB studentM, Ability A)
	{
		if(studentM==null) return null;
		int theLevel=-1;
		CMObject theClass=null;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=studentM.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((theLevel<0)||(theLevel>=level)))
			{
				theLevel=level;
				theClass=C;
			}
		}
		int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		if((raceLevel>=0)
		&&((theClass==null)||((studentM.basePhyStats().level()>=raceLevel)&&(theLevel>raceLevel))))
			theClass=studentM.charStats().getMyRace();
		final Clan clan = studentM.getMyClan();
		if(clan != null)
		{
			int clanLevel=getQualifyingLevel(clan.getGovernment().ID(),false,A.ID());
			if((clanLevel>=0)&&(clan.getClanLevel()>=clanLevel)&&(theLevel>clanLevel))
				theClass=clan.getGovernment();
		}
		return theClass;
	}

	
	public boolean qualifiesByCurrentClassAndLevel(MOB studentM, Ability A)
	{
		if(studentM==null) return false;
		CharClass C=studentM.charStats().getCurrentClass();
		int level=getQualifyingLevel(C.ID(),true,A.ID());
		if((level>=0)
		&&(studentM.charStats().getClassLevel(C)>=level))
			return true;
		level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)&&(studentM.phyStats().level()>=level))
			return true;
		final Clan clan = studentM.getMyClan();
		if(clan != null)
		{
			level=getQualifyingLevel(clan.getGovernment().ID(),false,A.ID());
			if((level>=0)&&(clan.getClanLevel()>=level))
				return true;
		}
		return false;
	}
	
	public AbilityLimits getCommonSkillLimit(MOB studentM)
	{
		AbilityLimits aL=new AbilityLimits();
		if(studentM==null) return aL;
		CharClass C=studentM.charStats().getCurrentClass();
		if(C==null) return aL;
		aL.commonSkills = C.maxCommonSkills();
		if(aL.commonSkills == 0) aL.commonSkills = Integer.MAX_VALUE;
		aL.craftingSkills = C.maxCraftingSkills();
		if(aL.craftingSkills == 0) aL.craftingSkills = Integer.MAX_VALUE;
		aL.nonCraftingSkills = C.maxNonCraftingSkills();
		if(aL.nonCraftingSkills == 0) aL.nonCraftingSkills = Integer.MAX_VALUE;
		return aL;
	}
	
	public AbilityLimits getCommonSkillLimit(MOB studentM, Ability A)
	{
		AbilityLimits aL=getCommonSkillLimit(studentM);
		aL.specificSkillLimit = Integer.MAX_VALUE;
		if(A==null) return aL;
		if(A instanceof CommonSkill)
		{
			boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
			aL.specificSkillLimit = crafting ? aL.craftingSkills : aL.nonCraftingSkills;
		}
		return aL;
	}
	
	public AbilityLimits getCommonSkillRemainder(MOB studentM, Ability A)
	{
		AbilityLimits aL = getCommonSkillRemainders(studentM);
		aL.specificSkillLimit = Integer.MAX_VALUE;
		if(A==null) return aL;
		if(A instanceof CommonSkill)
		{
			boolean crafting = ((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL);
			aL.specificSkillLimit = crafting ? aL.craftingSkills : aL.nonCraftingSkills;
		}
		return aL;
	}
	
	public AbilityLimits getCommonSkillRemainders(MOB student)
	{
		AbilityLimits aL = getCommonSkillLimit(student);
		CharClass C=student.charStats().getCurrentClass();
		if(C==null) return aL;
		HashSet culturalAbilities=new HashSet();
		DVector culturalAbilitiesDV = student.baseCharStats().getMyRace().culturalAbilities();
		for(int i=0;i<culturalAbilitiesDV.size();i++)
			culturalAbilities.add(culturalAbilitiesDV.elementAt(i, 1).toString().toLowerCase());
		for(int a=0;a<student.numAbilities();a++)
		{
			Ability A2=student.fetchAbility(a);
			if(A2 instanceof CommonSkill)
			{
				if((CMLib.ableMapper().getQualifyingLevel(C.ID(), false, A2.ID())>=0)||(culturalAbilities.contains(A2.ID().toLowerCase())))
					continue;
				aL.commonSkills--;
				if((A2.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
					aL.craftingSkills--;
				else
					aL.nonCraftingSkills--;
			}
		}
		return aL;
	}

	public boolean qualifiesByLevel(MOB studentM, Ability A){return (A==null)?false:qualifiesByLevel(studentM,A.ID());}
	public boolean qualifiesByLevel(MOB studentM, String abilityID)
	{
		if(studentM==null) return false;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=studentM.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,abilityID);
			if((level>=0)
			&&(studentM.charStats().getClassLevel(C)>=level))
				return true;
		}
		int level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,abilityID);
		if((level>=0)&&(studentM.phyStats().level()>=level))
			return true;
		final Clan clan = studentM.getMyClan();
		if(clan != null)
		{
			level=getQualifyingLevel(clan.getGovernment().ID(),false,abilityID);
			if((level>=0)&&(clan.getClanLevel()>=level))
				return true;
		}
		return false;
	}

	public boolean getDefaultGain(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).autoGain;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).autoGain;
		}
		return false;
	}

	public boolean getAllQualified(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).isAllQualified;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).isAllQualified;
		}
		return false;
	}

	public AbilityMapping getAbleMap(String ID, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID);
		}
		return null;
	}
	public AbilityMapping getAllAbleMap(String abilityID){ return getAbleMap("All",abilityID);}

	public boolean getSecretSkill(String ID, boolean checkAll, String abilityID)
	{
		boolean secretFound=false;
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
			{
				if(!ableMap.get(abilityID).isSecret)
					return false;
				secretFound=true;
			}
		}
		if(checkAll)
		{
			AbilityMapping AB=getAllAbleMap(abilityID);
			if(AB!=null) return AB.isSecret;
		}
		return secretFound;
	}

	public boolean getAllSecretSkill(String abilityID)
	{
		AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null) return AB.isSecret;
		return false;
	}

	public final List<AbilityMapping> getAllAbilityMappings(final MOB mob, final String abilityID)
	{
		final List<AbilityMapping> list=new LinkedList<AbilityMapping>();
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			final String charClass=mob.charStats().getMyClass(c).ID();
			if(completeAbleMap.containsKey(charClass))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if(ableMap.containsKey(abilityID))
					list.add(ableMap.get(abilityID));
			}
		}
		if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(mob.charStats().getMyRace().ID());
			if(ableMap.containsKey(abilityID))
				list.add(ableMap.get(abilityID));
		}
		
		final Clan clan = mob.getMyClan();
		if(clan!=null)
		{
			if(completeAbleMap.containsKey(clan.getGovernment().ID()))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(clan.getGovernment().ID());
				if(ableMap.containsKey(abilityID))
					list.add(ableMap.get(abilityID));
			}
		}
		AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null)
			list.add(AB);
		return list;
	}
	
	public boolean getSecretSkill(MOB mob, String abilityID)
	{
		boolean secretFound=false;
		final List<AbilityMapping> mappings=getAllAbilityMappings(mob,abilityID);
		for(final Iterator<AbilityMapping> a=mappings.iterator();a.hasNext();)
		{
			final AbilityMapping ableMap=a.next();
			if(!ableMap.isSecret)
				return false;
			secretFound=true;
		}
		return secretFound;
	}

	public boolean getSecretSkill(String abilityID)
	{
		boolean secretFound=false;
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			String charClass=((CharClass)e.nextElement()).ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if(ableMap.containsKey(abilityID))
				{
					if(!ableMap.get(abilityID).isSecret)
						return false;
					secretFound=true;
				}
			}
		}
		for(Enumeration e=CMClass.races();e.hasMoreElements();)
		{
			String ID=((Race)e.nextElement()).ID();
			if(completeAbleMap.containsKey(ID))
			{
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if(ableMap.containsKey(abilityID))
				{
					if(!ableMap.get(abilityID).isSecret)
						return false;
					secretFound=true;
				}
			}
		}
		AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}

	public Integer[] getCostOverrides(String ID, boolean checkAll, String abilityID)
	{
		Integer[] found=null;
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				found=ableMap.get(abilityID).costOverrides;
		}
		if((checkAll)&&(found==null))
		{
			AbilityMapping AB=getAllAbleMap(abilityID);
			if(AB!=null) found=AB.costOverrides;
		}
		return found;
	}

	public Integer[] getAllCostOverrides(String abilityID)
	{
		AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null) return AB.costOverrides;
		return null;
	}

	public Map<String, int[]> getHardOverrideManaCache()
	{
		return hardOverrideCache;
	}

	public Integer[] getCostOverrides(MOB mob, String abilityID)
	{
		Integer[] found=null;
		final List<AbilityMapping> mappings=getAllAbilityMappings(mob,abilityID);
		for(final Iterator<AbilityMapping> a=mappings.iterator();a.hasNext();)
		{
			final AbilityMapping ableMap=a.next();
			found=ableMap.costOverrides;
			if(found!=null) break;
		}
		return found;
	}

	public Integer[] getCostOverrides(String abilityID)
	{
		Integer[] found=null;
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			String charClass=((CharClass)e.nextElement()).ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if((ableMap.containsKey(abilityID))&&(found==null))
					found=ableMap.get(abilityID).costOverrides;
			}
		}
		for(Enumeration e=CMClass.races();e.hasMoreElements();)
		{
			String ID=((Race)e.nextElement()).ID();
			if(completeAbleMap.containsKey(ID))
			{
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if((ableMap.containsKey(abilityID))&&(found==null))
					found=ableMap.get(abilityID).costOverrides;
			}
		}
		AbilityMapping AB=getAllAbleMap(abilityID);
		if((AB!=null)&&(found==null))
			return found=AB.costOverrides;
		return found;
	}

	public String getDefaultParm(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultParm;
		}

		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultParm;
		}
		return "";
	}

	public String getPreReqStrings(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).originalSkillPreReqList;
		}

		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).originalSkillPreReqList;
		}
		return "";
	}

	public int getMaxProficiency(MOB mob, boolean checkAll, String abilityID)
	{
		if(mob==null) return getMaxProficiency(abilityID);
		CharClass C=mob.charStats().getCurrentClass();
		if(C==null) return getMaxProficiency(abilityID);
		return getMaxProficiency(C.ID(),checkAll,abilityID);
	}
	
	public int getMaxProficiency(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).maxProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).maxProficiency;
		}
		return getMaxProficiency(abilityID);
	}
	public int getMaxProficiency(String abilityID)
	{
		if(maxProficiencyMap.containsKey(abilityID))
			return ((Integer)maxProficiencyMap.get(abilityID)).intValue();
		return 100;
	}
	public int getDefaultProficiency(String ID, boolean checkAll, String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultProficiency;
		}
		return 0;
	}

	public boolean IsItemComponent(MOB mob, AbilityComponent comp, int[] amt, Item I, List<Object> thisSet)
	{
		if(I==null)
			return false;
		Item container=null;
		if((comp.getType()==AbilityComponent.CompType.STRING)&&(!CMLib.english().containsString(I.name(),comp.getStringType())))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.RESOURCE)&&((!(I instanceof RawMaterial))||(I.material()!=comp.getLongType())))
			return false;
		else
		if((comp.getType()==AbilityComponent.CompType.MATERIAL)&&((!(I instanceof RawMaterial))||((I.material()&RawMaterial.MATERIAL_MASK)!=comp.getLongType())))
			return false;
		container=I.ultimateContainer(null);
		if(container==null) container=I;
		switch(comp.getLocation())
		{
		case INVENTORY:
			if((container.owner() instanceof Room)||(!container.amWearingAt(Wearable.IN_INVENTORY)))
				return false;
			break;
		case HELD:
			if((container.owner() instanceof Room)||(!container.amWearingAt(Wearable.WORN_HELD)))
				return false;
			break;
		case WORN:
			if((container.owner() instanceof Room)||(container.amWearingAt(Wearable.IN_INVENTORY)))
				return false;
			break;
		default:
		case NEARBY:
			if(!CMLib.flags().canBeSeenBy(container, mob))
				return false;
			break;
		case ONGROUND:
			if((!(container.owner() instanceof Room))||(!CMLib.flags().canBeSeenBy(container, mob)))
				return false;
			break;
		}
		if((comp.getType()!=AbilityComponent.CompType.STRING)
		&&(CMLib.flags().isOnFire(I)||CMLib.flags().enchanted(I)))
			return false;
		if(comp.getType()==AbilityComponent.CompType.STRING)
		{
			if(I instanceof PackagedItems)
				I=(Item)CMLib.materials().unbundle(I,amt[0]);
			amt[0]-=I.numberOfItems();
		}
		else
		if(I.phyStats().weight()>amt[0])
		{
			I=(Item)CMLib.materials().unbundle(I,amt[0]);
			if(I==null) 
				return false;
			amt[0]=amt[0]-I.phyStats().weight();
		}
		else
			amt[0]=amt[0]-I.phyStats().weight();
		thisSet.add(I);

		if(amt[0]<=0)
		{
			if(thisSet.size()>0)
				thisSet.add(Boolean.valueOf(comp.isConsumed()));
			return true;
		}
		return false;
	}
	
	// returns Vector of components found if all good, returns Integer of bad row if not.
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req)
	{
		if((mob==null)||(req==null)||(req.size()==0))
			return new Vector();
		boolean currentAND=false;
		boolean previousValue=true;
		final int[] amt={0};
		final List<Object> passes=new Vector<Object>();
		final List<Object> thisSet=new Vector<Object>();
		boolean found=false;
		AbilityComponent comp = null;
		Room room = mob.location();
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			currentAND=comp.getConnector()==AbilityComponent.CompConnector.AND;
			if(previousValue&&(!currentAND)) return passes;
			if((!previousValue)&&currentAND) return null;

			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
				continue;
			amt[0]=comp.getAmount();
			thisSet.clear();
			found=false;
			if(comp.getLocation()!=CompLocation.ONGROUND)
				for(int ii=0;ii<mob.numItems();ii++)
				{
					found=IsItemComponent(mob, comp, amt, mob.getItem(ii), thisSet);
					if(found)
						break;
				}
			if((!found)
			&&(room!=null)
			&&((comp.getLocation()==CompLocation.ONGROUND)||(comp.getLocation()==CompLocation.NEARBY)))
			{
				for(int ii=0;ii<room.numItems();ii++)
				{
					found=IsItemComponent(mob, comp, amt, room.getItem(ii), thisSet);
					if(found)
						break;
				}
			}
			if((amt[0]>0)&&(currentAND)&&(i>0)) return null;
			previousValue=amt[0]<=0;
			if(previousValue) passes.addAll(thisSet);
		}
		if(passes.size()==0) return null;
		return passes;
	}

	public List<AbilityComponent> getAbilityComponentDVector(String AID){ return (List<AbilityComponent>)getAbilityComponentMap().get(AID.toUpperCase().trim());}
	public List<DVector> getAbilityComponentDecodedDVectors(String AID){ return getAbilityComponentDecodedDVectors(getAbilityComponentDVector(AID));}
	public DVector getAbilityComponentDecodedDVector(AbilityComponent comp)
	{
		DVector curr=new DVector(2);
		String itemDesc=null;
		curr.addElement("ANDOR",comp.getConnector()==AbilityComponent.CompConnector.AND?"&&":"||");
		if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
			curr.addElement("DISPOSITION","held");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
			curr.addElement("DISPOSITION","worn");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.NEARBY)
			curr.addElement("DISPOSITION","nearby");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.ONGROUND)
			curr.addElement("DISPOSITION","onground");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)
			curr.addElement("DISPOSITION","inventory");
		
		if(comp.isConsumed())
			curr.addElement("FATE","consumed");
		else
			curr.addElement("FATE","kept");
		curr.addElement("AMOUNT",""+comp.getAmount());
		if(comp.getType()==AbilityComponent.CompType.STRING)
			itemDesc=comp.getStringType();
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			itemDesc=RawMaterial.MATERIAL_DESCS[(int)comp.getLongType()>>8].toUpperCase();
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
			itemDesc=RawMaterial.CODES.NAME((int)comp.getLongType()).toUpperCase();
		curr.addElement("COMPONENTID",itemDesc);
		curr.addElement("MASK",comp.getMaskStr());
		return curr;
	}

	public void setAbilityComponentCodedFromDecodedDVector(DVector decodedDV, AbilityComponent comp)
	{
		String[] s=new String[6];
		for(int i=0;i<6;i++)
			s[i]=(String)decodedDV.elementAt(i,2);
		if(s[0].equalsIgnoreCase("||"))
			comp.setConnector(AbilityComponent.CompConnector.OR);
		else
			comp.setConnector(AbilityComponent.CompConnector.AND);
		if(s[1].equalsIgnoreCase("held"))
			comp.setLocation(AbilityComponent.CompLocation.HELD);
		else
		if(s[1].equalsIgnoreCase("worn"))
			comp.setLocation(AbilityComponent.CompLocation.WORN);
		else
		if(s[1].equalsIgnoreCase("nearby"))
			comp.setLocation(AbilityComponent.CompLocation.NEARBY);
		else
		if(s[1].equalsIgnoreCase("onground"))
			comp.setLocation(AbilityComponent.CompLocation.ONGROUND);
		else
			comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
		if(s[2].equalsIgnoreCase("consumed"))
			comp.setConsumed(true);
		else
			comp.setConsumed(false);
		comp.setAmount(CMath.s_int(s[3]));
		int depth=CMLib.materials().getResourceCode(s[4],false);
		if(depth>=0)
			comp.setType(AbilityComponent.CompType.RESOURCE, Integer.valueOf(depth));
		else
		{
			depth=CMLib.materials().getMaterialCode(s[4],false);
			if(depth>=0)
				comp.setType(AbilityComponent.CompType.MATERIAL, Integer.valueOf(depth));
			else
				comp.setType(AbilityComponent.CompType.STRING, s[4].toUpperCase().trim());
		}
		comp.setMask(s[5]);
	}

	public List<DVector> getAbilityComponentDecodedDVectors(List<AbilityComponent> req)
	{
		if(req==null) return null;
		List<DVector> V=new Vector<DVector>();
		for(AbilityComponent comp : req)
			V.add(getAbilityComponentDecodedDVector(comp));
		return V;
	}

	public AbilityComponent createBlankAbilityComponent()
	{
		AbilityComponent comp = (AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
		comp.setConnector(AbilityComponent.CompConnector.AND);
		comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
		comp.setConsumed(false);
		comp.setAmount(1);
		comp.setType(AbilityComponent.CompType.STRING, "resource-material-item name");
		comp.setMask("");
		return comp;
	}

	public String getAbilityComponentCodedString(List<AbilityComponent> comps)
	{
		return getAbilityComponentCodedStringFromDVectors(getAbilityComponentDecodedDVectors(comps));
	}
	
	protected String getAbilityComponentCodedStringFromDVectors(List<DVector> comps)
	{
		StringBuilder buf=new StringBuilder("");
		DVector curr=null;
		for(int c=0;c<comps.size();c++)
		{
			curr=(DVector)comps.get(c);
			if(c>0) buf.append((String)curr.elementAt(0,2));
			buf.append("(");
			buf.append((String)curr.elementAt(1,2));
			buf.append(":");
			buf.append((String)curr.elementAt(2,2));
			buf.append(":");
			buf.append((String)curr.elementAt(3,2));
			buf.append(":");
			buf.append((String)curr.elementAt(4,2));
			buf.append(":");
			buf.append((String)curr.elementAt(5,2));
			buf.append(")");
		}
		return buf.toString();
	}
	
	public String getAbilityComponentCodedString(String AID)
	{
		StringBuffer buf=new StringBuffer("");
		List<DVector> comps=getAbilityComponentDecodedDVectors(AID);
		buf.append(getAbilityComponentCodedStringFromDVectors(comps));
		return AID+"="+buf.toString();
	}

	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req, int r)
	{
		int amt=0;
		String itemDesc=null;
		StringBuffer buf=new StringBuffer("");
		AbilityComponent comp = req.get(r);
		if(r>0) buf.append(comp.getConnector()==AbilityComponent.CompConnector.AND?", and ":", or ");
		if((mob!=null)
		&&(comp.getCompiledMask()!=null)
		&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
			return "";
		if(mob==null)
		{
			if(comp.getCompiledMask()!=null)
				buf.append("MASK: "+comp.getMaskStr()+": ");
		}
		amt=comp.getAmount();
		if(comp.getType()==AbilityComponent.CompType.STRING)
			itemDesc=((amt>1)?(amt+" "+comp.getStringType()+"s"):CMLib.english().startWithAorAn(comp.getStringType()));
		else
		if(comp.getType()==AbilityComponent.CompType.MATERIAL)
			itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.MATERIAL_DESCS[(int)comp.getLongType()>>8].toLowerCase();
		else
		if(comp.getType()==AbilityComponent.CompType.RESOURCE)
			itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
		if(comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)
			buf.append(itemDesc);
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
			buf.append(itemDesc+" held");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
			buf.append(itemDesc+" worn or wielded");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.NEARBY)
			buf.append(itemDesc+" nearby");
		else
		if(comp.getLocation()==AbilityComponent.CompLocation.ONGROUND)
			buf.append(itemDesc+" on the ground");
		return buf.toString();
	}

	public String getAbilityComponentDesc(MOB mob, String AID)
	{
		return getAbilityComponentDesc(mob,getAbilityComponentDVector(AID));
	}

	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req)
	{
		if(req==null) return null;
		StringBuffer buf=new StringBuffer("");
		for(int r=0;r<req.size();r++){ buf.append(getAbilityComponentDesc(mob,req,r));}
		return buf.toString();
	}

	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H)
	{
		int x=s.indexOf('=');
		if(x<0) return "Malformed component line (code 0): "+s;
		String id=s.substring(0,x).toUpperCase().trim();
		String parms=s.substring(x+1);

		String parmS=null;
		String rsc=null;
		List<AbilityComponent> parm=null;
		AbilityComponent build=null;
		int depth=0;
		parm=new Vector<AbilityComponent>();
		String error=null;
		while(parms.length()>0)
		{
			build=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
			build.setConnector(AbilityComponent.CompConnector.AND);
			if(parms.startsWith("||"))
			{ 
				build.setConnector(AbilityComponent.CompConnector.OR); 
				parms=parms.substring(2).trim();
			}
			else
			if(parms.startsWith("&&"))
			{ parms=parms.substring(2).trim();}

			if(!parms.startsWith("("))
			{error="Malformed component line (code 1): "+parms; break;}

			depth=0;
			x=1;
			for(;x<parms.length();x++)
				if((parms.charAt(x)==')')&&(depth==0))
					break;
				else
				if(parms.charAt(x)=='(')
					depth++;
				else
				if(parms.charAt(x)==')')
					depth--;
			if(x==parms.length()){error="Malformed component line (code 2): "+parms; break;}
			parmS=parms.substring(1,x).trim();
			parms=parms.substring(x+1).trim();

			build.setLocation(AbilityComponent.CompLocation.INVENTORY);
			x=parmS.indexOf(':');
			if(x<0)
			{
				error="Malformed component line (code 0-1): "+parmS; 
				continue;
			}
			if(parmS.substring(0,x).equalsIgnoreCase("held")) 
				build.setLocation(AbilityComponent.CompLocation.HELD);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("worn")) 
				build.setLocation(AbilityComponent.CompLocation.WORN);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("nearby")) 
				build.setLocation(AbilityComponent.CompLocation.NEARBY);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("onground")) 
				build.setLocation(AbilityComponent.CompLocation.ONGROUND);
			else
			if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("inventory")))
			{
				error="Malformed component line (code 0-2): "+parmS; 
				continue;
			}
			parmS=parmS.substring(x+1);

			build.setConsumed(true);
			x=parmS.indexOf(':');
			if(x<0){error="Malformed component line (code 1-1): "+parmS; continue;}
			if(parmS.substring(0,x).equalsIgnoreCase("kept")) 
				build.setConsumed(false);
			else
			if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("consumed")))
			{
				error="Malformed component line (code 1-2): "+parmS; 
				continue;
			}
			parmS=parmS.substring(x+1);

			build.setAmount(1);
			x=parmS.indexOf(':');
			if(x<0){error="Malformed component line (code 2-1): "+parmS; continue;}
			if((x>0)&&(!CMath.isInteger(parmS.substring(0,x))))
			{
				error="Malformed component line (code 2-2): "+parmS; 
				continue;
			}
			if(x>0) 
				build.setAmount(CMath.s_int(parmS.substring(0,x)));
			parmS=parmS.substring(x+1);

			build.setType(AbilityComponent.CompType.STRING, "");
			x=parmS.indexOf(':');
			if(x<=0){error="Malformed component line (code 3-1): "+parmS; continue;}
			rsc=parmS.substring(0,x);
			depth=CMLib.materials().getResourceCode(rsc,false);
			if(depth>=0)
				build.setType(AbilityComponent.CompType.RESOURCE, Long.valueOf(depth));
			else
			{
				depth=CMLib.materials().getMaterialCode(rsc,false);
				if(depth>=0)
					build.setType(AbilityComponent.CompType.MATERIAL, Long.valueOf(depth));
				else
					build.setType(AbilityComponent.CompType.STRING, rsc.toUpperCase().trim());
			}
			parmS=parmS.substring(x+1);

			build.setMask(parmS);

			parm.add(build);
		}
		if(parm instanceof Vector)
			((Vector)parm).trimToSize();
		if(parm instanceof SVector)
			((SVector)parm).trimToSize();
		if(error!=null) return error;
		if(parm.size()>0) H.put(id.toUpperCase(),parm);
		return null;
	}

	// format of each data entry is 1=ANDOR(B), 2=DISPO(I), 3=CONSUMED(B), 4=AMT(I), 5=MATERIAL(L)RESOURCE(I)NAME(S), 6=MASK(S)
	
	public Map<String, List<AbilityComponent>> getAbilityComponentMap()
	{
		Map<String, List<AbilityComponent>> H=(Map)Resources.getResource("COMPONENT_MAP");
		if(H==null)
		{
			H=new Hashtable();
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true).text();
			List<String> V=new Vector();
			if(buf!=null)
				V=Resources.getFileLineVector(buf);
			String s=null;
			String error=null;
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				s=((String)V.get(v)).trim();
				if(s.startsWith("#")||(s.length()==0)||s.startsWith(";")||s.startsWith(":")) continue;
				error=addAbilityComponent(s,H);
				if(error!=null) Log.errOut("CMAble",error);
			}
			Resources.submitResource("COMPONENT_MAP",H);
		}
		return H;
	}
	
	public int destroyAbilityComponents(List<Object> found)
	{
		int value=0;
		if(found==null)
		{
			return 0;
		}
		while(found.size()>0)
		{
			int i=0;
			boolean destroy=false;
			for(;i<found.size();i++)
				if(found.get(i) instanceof Boolean)
				{ destroy=((Boolean)found.get(i)).booleanValue(); break;}
			while(i>=0)
			{
				if((destroy)&&(found.get(0) instanceof Item))
				{
					value +=((Item)found.get(0)).value();
					((Item)found.get(0)).destroy();
				}
				found.remove(0);
				i--;
			}
		}
		return value;
	}
	
	public void alterAbilityComponentFile(String compID, boolean delete)
	{
		CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true);
		if(delete)
		{
			Resources.findRemoveProperty(F, compID);
			return;
		}
		String parms=CMLib.ableMapper().getAbilityComponentCodedString(compID);
		StringBuffer text=F.textUnformatted();
		boolean lastWasCR=true;
		boolean addIt=true;
		int delFromHere=-1;
		String upID=compID.toUpperCase();
		for(int t=0;t<text.length();t++)
		{
			if(text.charAt(t)=='\n')
				lastWasCR=true;
			else
			if(text.charAt(t)=='\r')
				lastWasCR=true;
			else
			if(Character.isWhitespace(text.charAt(t)))
				continue;
			else
			if((lastWasCR)&&(delFromHere>=0))
			{
				text.delete(delFromHere,t);
				text.insert(delFromHere,parms+'\n');
				delFromHere=-1;
				addIt=false;
				break;
			}
			else
			if((lastWasCR)&&(Character.toUpperCase(text.charAt(t))==upID.charAt(0)))
			{
				if((text.substring(t).toUpperCase().startsWith(upID))
				&&(text.substring(t+upID.length()).trim().startsWith("=")))
				{
					addIt=false;
					delFromHere=t;
				}
				lastWasCR=false;
			}
			else
				lastWasCR=false;
		}
		if(delFromHere>0)
		{
			text.delete(delFromHere,text.length());
			text.append(parms+'\n');
		}
		if(addIt)
		{
			if(!lastWasCR)
				text.append('\n');
			text.append(parms+'\n');
		}
		F.saveText(text.toString(),false);
	}
	
	public AbilityMapping makeAllQualifyMapping(String s)
	{
		int x=s.indexOf(' ');
		if(x<0) return null;
		String lvlStr = s.substring(0,x).trim();
		if(!CMath.isInteger(lvlStr))
			return null;
		s=s.substring(x+1).trim();
		int qualLevel=CMath.s_int(lvlStr);
		x=s.indexOf(' ');
		String abilityID;
		StringBuilder mask=new StringBuilder("");
		StringBuilder preReqs=new StringBuilder("");
		StringBuilder prof=new StringBuilder("");
		boolean autogain=false;
		if(x<0)
			abilityID=s;
		else
		{
			abilityID=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			String us=s.toUpperCase();
			int lastC=' ';
			StringBuilder cur=null;
			for(int i=0;i<s.length();i++)
			{
				if((lastC==' ')&&(Character.isLetter(us.charAt(i))))
				{
					String ss=us.substring(i);
					if(ss.startsWith("MASK="))
					{
						cur=mask;
						i+=4;
					}
					else
					if(ss.startsWith("PROF="))
					{
						cur=prof;
						i+=4;
					}
					else
					if(ss.startsWith("REQUIRES="))
					{
						cur=preReqs;
						i+=8;
					}
					else
					if(ss.startsWith("AUTOGAIN "))
					{
						cur=null;
						autogain=true;
						i+=8;
					}
					else
					if(ss.startsWith("AUTOGAIN") 
					&& (ss.length()==8))
					{
						cur=null;
						autogain=true;
						break;
					}
					else
					if(cur!=null)
						cur.append(s.charAt(i));
				}
				else
				if(cur!=null)
					cur.append(s.charAt(i));
				lastC=s.charAt(i);
			}
		}
		return
			makeAbilityMapping(abilityID,qualLevel,abilityID,CMath.s_int(prof.toString().trim()),100,"",autogain,false,
					true,CMParms.parseSpaces(preReqs.toString().trim(), true), mask.toString().trim(),null);
	}
	
	public Map<String, Map<String,AbilityMapping>> getAllQualifiesMap(final Map<String,Object> cache)
	{
		Map<String, Map<String,AbilityMapping>> bothMaps;
		if(cache!=null)
		{
			bothMaps=(Map)Resources.getResource("ALLQUALIFIES_MAP");
			if(bothMaps!=null) return bothMaps;
		}
		
		bothMaps=new TreeMap<String,Map<String,AbilityMapping>>();
		bothMaps.put("ALL", new TreeMap<String,AbilityMapping>());
		bothMaps.put("EACH", new TreeMap<String,AbilityMapping>());
		CMFile f = new CMFile(Resources.makeFileResourceName("skills/allqualifylist.txt"),null,false);
		if(f.exists() && f.canRead())
		{
			List<String> list = Resources.getFileLineVector(f.text());
			boolean eachMode = false;
			for(String s : list)
			{
				s=s.trim();
				if(s.equalsIgnoreCase("[EACH]"))
					eachMode=true;
				else
				if(s.equalsIgnoreCase("[ALL]"))
					eachMode=false;
				else
				if(s.startsWith("#")||s.length()==0)
					continue;
				else
				{
					AbilityMapping able=makeAllQualifyMapping(s);
					if(able==null)
						continue;
					if(eachMode)
					{
						Map<String, AbilityMapping> map=bothMaps.get("EACH");
						map.put(able.abilityID.toUpperCase().trim(),able);
					}
					else
					{
						Map<String, AbilityMapping> map=bothMaps.get("ALL");
						map.put(able.abilityID.toUpperCase().trim(),able);
					}
				}
			}
		}
		if(cache!=null)
			cache.put("ALLQUALIFIES_MAP",bothMaps);
		return bothMaps;
	}

	public String buildAllQualifysSection(Map<String,AbilityMapping> map)
	{
		TreeMap<Integer,List<AbilityMapping>> sortedMap=new TreeMap<Integer,List<AbilityMapping>>();
		for(AbilityMapping mapped : map.values())
		{
			List<AbilityMapping> subMap=sortedMap.get(Integer.valueOf(mapped.qualLevel));
			if(subMap==null)
			{
				subMap=new LinkedList<AbilityMapping>();
				sortedMap.put(Integer.valueOf(mapped.qualLevel), subMap);
			}
			subMap.add(mapped);
		}
		StringBuilder str=new StringBuilder("");
		for(Integer LEVEL : sortedMap.keySet())
		{
			List<AbilityMapping> subMap=sortedMap.get(LEVEL);
			for(AbilityMapping mapped : subMap)
			{
				str.append(LEVEL.toString()).append(" ");
				str.append(mapped.abilityID).append(" ");
				if(mapped.defaultProficiency>0) str.append("PROF="+mapped.defaultProficiency+" ");
				if(mapped.autoGain) str.append("AUTOGAIN ");
				if((mapped.extraMask!=null)&&(mapped.extraMask.length()>0))
					 str.append("MASK=").append(mapped.extraMask).append(" ");
				if((mapped.originalSkillPreReqList!=null)&&(mapped.originalSkillPreReqList.trim().length()>0))
					str.append("REQUIRES=").append(CMParms.combineWith(CMParms.parseCommas(mapped.originalSkillPreReqList,true), ' ')).append(" ");
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		return str.toString();
	}

	protected void undoAllQualifysList()
	{
		for(String abilityID : reverseAbilityMap.keySet())
		{
			Map<String, AbilityMapping> revT = reverseAbilityMap.get(abilityID);
			LinkedList<String> deleteThese=new LinkedList<String>();
			for(String ID : revT.keySet())
			{
				final AbilityMapping able = revT.get(ID);
				if(able.allQualifyFlag)
					deleteThese.add(ID);
			}
			for(String ID : deleteThese)
				revT.remove(ID);
		}
		
		for(String ID : completeAbleMap.keySet())
		{
			Map<String, AbilityMapping> ableMap = completeAbleMap.get(ID);
			LinkedList<String> deleteThese=new LinkedList<String>();
			for(String abilityID : ableMap.keySet())
			{
				final AbilityMapping able = ableMap.get(abilityID);
				if(able.allQualifyFlag)
					deleteThese.add(abilityID);
			}
			for(String abilityID : deleteThese)
				ableMap.remove(abilityID);
		}
	}
	
	public synchronized void saveAllQualifysFile(Map<String, Map<String,AbilityMapping>> newMap)
	{
		// undo and then reapply the all qualifys list
		undoAllQualifysList();
		eachClassSet=null;
		for(String ID : completeAbleMap.keySet())
			if((!ID.equalsIgnoreCase("All"))
			&&(!ID.equalsIgnoreCase("Archon")))
				handleEachAndClassAbility(completeAbleMap.get(ID), newMap, ID);

		// now just save it
		CMFile f = new CMFile(Resources.makeFileResourceName("skills/allqualifylist.txt"),null,false);
		List<String> set=new Vector<String>(0);
		if(f.exists() && f.canRead())
		{
			set=Resources.getFileLineVector(f.text());
		}
		StringBuilder str=new StringBuilder("");
		for(String line : set)
		{
			if(line.toUpperCase().startsWith("[ALL]")||line.toUpperCase().startsWith("[EACH]"))
			{
				str.append("\n\r\n\r");
				break;
			}
			else
			if(line.length()>0)
				str.append(line).append("\n\r");
		}
		Map<String,AbilityMapping> map;
		str.append("[EACH]").append("\n\r");
		map=newMap.get("EACH");
		if(map!=null) str.append(buildAllQualifysSection(map));
		str.append("[ALL]").append("\n\r");
		map=newMap.get("ALL");
		if(map!=null) str.append(buildAllQualifysSection(map));
		f.saveText(str.toString(),false);
	}
}
