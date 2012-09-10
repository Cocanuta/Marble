package com.planet_ink.marble_mud.Abilities.Archon;
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

@SuppressWarnings({"unchecked","rawtypes"})
public class Archon_Metacraft extends ArchonSkill
{
	public String ID() { return "Archon_Metacraft"; }
	public String name(){ return "Metacrafting";}
	private static final String[] triggerStrings = {"METACRAFT"};
	public String[] triggerStrings(){return triggerStrings;}
	
	public static List<Ability> craftingSkills=new Vector<Ability>();
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(craftingSkills.size()==0)
		{
			Vector V=new Vector();
			for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=(Ability)e.nextElement();
				if(A instanceof ItemCraftor)
					V.addElement(A.copyOf());
			}
			while(V.size()>0)
			{
				int lowest=Integer.MAX_VALUE;
				Ability lowestA=null;
				for(int i=0;i<V.size();i++)
				{
					Ability A=(Ability)V.elementAt(i);
					int ii=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					if(ii<lowest)
					{ 
						lowest=ii; 
						lowestA=A;
					}
				}
				if(lowestA==null) 
					lowestA=(Ability)V.firstElement();
				if(lowestA!=null)
				{
					V.removeElement(lowestA);
					craftingSkills.add(lowestA);
				}
				else
					break;
			}
		}
		if(commands.size()<1)
		{
			mob.tell("Metacraft what (recipe, everything, every x), (optionally) out of what material, and (optionally) to self, to here, or to file [FILENAME]?");
			return false;
		}
		String mat=null;
		String toWHERE = "SELF";
		if(commands.size()>1)
		{
			for(int x=1;x<commands.size()-1;x++)
			{
				if(((String)commands.elementAt(x)).equalsIgnoreCase("to"))
				{
					if(((String)commands.elementAt(x+1)).equalsIgnoreCase("self"))
					{
						toWHERE="SELF";
						commands.removeElementAt(x);
						commands.removeElementAt(x);
						break;
					}
					if(((String)commands.elementAt(x+1)).equalsIgnoreCase("here"))
					{
						toWHERE="HERE";
						commands.removeElementAt(x);
						commands.removeElementAt(x);
						break;
					}
					if(((String)commands.elementAt(x+1)).equalsIgnoreCase("file")&&(x<commands.size()-2))
					{
						toWHERE=(String)commands.elementAt(x+2);
						commands.removeElementAt(x);
						commands.removeElementAt(x);
						commands.removeElementAt(x);
						break;
					}
				}
			}
			if(commands.size()>1)
			{
				mat=((String)commands.lastElement()).toUpperCase();
				commands.removeElementAt(commands.size()-1);
			}
		}
		int material=-1;
		if(mat!=null)
			material=RawMaterial.CODES.FIND_StartsWith(mat);
		if((mat!=null)&&(material<0))
		{
			mob.tell("'"+mat+"' is not a recognized material.");
			return false;
		}
		ItemCraftor skill=null;
		String recipe=CMParms.combine(commands,0);
		List<Ability> skillsToUse=new Vector<Ability>();
		boolean everyFlag=false;
		if(recipe.equalsIgnoreCase("everything"))
		{
			skillsToUse=new XVector<Ability>(craftingSkills);
			everyFlag=true;
			recipe=null;
		}
		else
		if(recipe.toUpperCase().startsWith("EVERY "))
		{
			everyFlag=true;
			recipe=recipe.substring(6).trim();
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				List<List<String>> V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0)) skillsToUse.add(skill);
			}
			if(skillsToUse.size()==0)
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				List<List<String>> V=skill.matchingRecipeNames(recipe,true);
				if((V!=null)&&(V.size()>0)) skillsToUse.add(skill);
			}
		}
		else
		{
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				List<List<String>> V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0)){ skillsToUse.add(skill);}
			}
			if(skillsToUse.size()==0)
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.get(i);
				List<List<String>> V=skill.matchingRecipeNames(recipe,true);
				if((V!=null)&&(V.size()>0)){ skillsToUse.add(skill);}
			}
		}
		if(skillsToUse.size()==0)
		{
			mob.tell("'"+recipe+"' can not be made with any of the known crafting skills.");
			return false;
		}
		
		boolean success=false;
		StringBuffer xml = new StringBuffer("<ITEMS>");
		HashSet files = new HashSet();
		for(int s=0;s<skillsToUse.size();s++)
		{
			skill=(ItemCraftor)skillsToUse.get(s);
			List<Item> items=new Vector<Item>();
			if(everyFlag)
			{
				if(recipe==null)
				{
					List<ItemCraftor.ItemKeyPair> V=null;
					if(material>=0)
						V=skill.craftAllItemSets(material);
					else
						V=skill.craftAllItemSets();
					
					if(V!=null)
						for(ItemCraftor.ItemKeyPair L: V)
						{
							items.add(L.item);
							if(L.key!=null)
								items.add(L.key);
						}
				}
				else
				if(material>=0)
				{
					ItemCraftor.ItemKeyPair pair = skill.craftItem(recipe,material);
					if(pair!=null) items.addAll(pair.asList());
				}
				else
				{
					ItemCraftor.ItemKeyPair pair = skill.craftItem(recipe);
					if(pair!=null) items.addAll(pair.asList());
				}
			}
			else
			if(material>=0)
			{
				ItemCraftor.ItemKeyPair pair = skill.craftItem(recipe,material);
				if(pair!=null) items.addAll(pair.asList());
			}
			else
			{
				ItemCraftor.ItemKeyPair pair = skill.craftItem(recipe);
				if(pair!=null) items.addAll(pair.asList());
			}
			if((items==null)||(items.size()==0)) continue;
			success=true;
			if(toWHERE.equals("SELF")||toWHERE.equals("HERE"))
				for(Item building : items)
				{
					if((building instanceof ClanItem)&&(mob.getClanID().length()>0))
					{
						Clan C=CMLib.clans().getClan(mob.getClanID());
						String clanName = (C==null)?" Clan "+mob.getClanID():(" "+C.getGovernmentName()+" "+C.name());
						building.setName(CMStrings.replaceFirst(building.Name(), " Clan None", clanName));
						building.setDisplayText(CMStrings.replaceFirst(building.displayText(), " Clan None", clanName));
						building.setDescription(CMStrings.replaceFirst(building.description(), " Clan None", clanName));
						((ClanItem)building).setClanID(mob.getClanID());
					}
					if(toWHERE.equals("HERE"))
					{
						mob.location().addItem(building,ItemPossessor.Expire.Player_Drop);
						mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,building.name()+" appears here.");
					}
					else
					{
						mob.moveItemTo(building);
						mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,building.name()+" appears in <S-YOUPOSS> hands.");
					}
				}
			else
				xml.append(CMLib.coffeeMaker().getItemsXML(items,new Hashtable(),files,0));
			mob.location().recoverPhyStats();
			if(!everyFlag) break;
		}
		if(success
		&&(!toWHERE.equals("SELF"))
		&&(!toWHERE.equals("HERE")))
		{
			CMFile file = new CMFile(toWHERE,mob,false);
			if(!file.canWrite())
				mob.tell("Unable to open file '"+toWHERE+"' for writing.");
			else
			{
				xml.append("</ITEMS>");
				if(files.size()>0)
				{
					StringBuffer str=new StringBuffer("<FILES>");
					for(Iterator i=files.iterator();i.hasNext();)
					{
						Object O=i.next();
						String filename=(String)O;
						StringBuffer buf=new CMFile(Resources.makeFileResourceName(filename),null,true).text();
						if((buf!=null)&&(buf.length()>0))
						{
							str.append("<FILE NAME=\""+filename+"\">");
							str.append(buf);
							str.append("</FILE>");
						}
					}
					str.append("</FILES>");
					xml.append(str);
				}
				file.saveText(xml);
				mob.tell("File '"+file.getAbsolutePath()+"' written.");
			}
		}
		if(!success)
		{
			mob.tell("The metacraft failed.");
			return false;
		}
		return true;
	}
}
