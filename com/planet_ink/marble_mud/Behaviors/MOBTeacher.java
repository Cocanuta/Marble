package com.planet_ink.marble_mud.Behaviors;
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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
public class MOBTeacher extends CombatAbilities
{
	public String ID(){return "MOBTeacher";}
	protected MOB myMOB=null;
	protected boolean teachEverything=true;
	protected boolean noCommon=false;
	protected boolean noExpertises=false; // doubles as a "done ticking" flag
	protected boolean noHLExpertises=false;
	protected int tickDownToKnowledge=4;
	protected List<ExpertiseDefinition> trainableExpertises=null;

	public String accountForYourself()
	{ 
		return "skill teaching";
	}
	
	public void startBehavior(PhysicalAgent forMe)
	{
		if(forMe instanceof MOB)
			myMOB=(MOB)forMe;
		setParms(parms);
	}

	protected void setTheCharClass(MOB mob, CharClass C)
	{
		if((mob.baseCharStats().numClasses()==1)
		&&(mob.baseCharStats().getMyClass(0).ID().equals("StdCharClass"))
		&&(!C.ID().equals("StdCharClass")))
		{
			mob.baseCharStats().setMyClasses(C.ID());
			mob.baseCharStats().setMyLevels(""+mob.phyStats().level());
			mob.recoverCharStats();
			return;
		}
		for(int i=0;i<mob.baseCharStats().numClasses();i++)
		{
			CharClass C1=mob.baseCharStats().getMyClass(i);
			if((C1!=null)
			&&(mob.baseCharStats().getClassLevel(C1)>0))
				mob.baseCharStats().setClassLevel(C1,1);
		}
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,mob.phyStats().level());
		mob.recoverCharStats();
	}

	protected void classAbles(MOB mob, Hashtable myAbles, int pct)
	{
		boolean stdCharClass=mob.charStats().getCurrentClass().ID().equals("StdCharClass");
		String className=mob.charStats().getCurrentClass().ID();
		Ability A=null;
		for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			A=(Ability)a.nextElement();
			if((((stdCharClass&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>0)))
				||(CMLib.ableMapper().qualifiesByLevel(mob,A)&&(!CMLib.ableMapper().getSecretSkill(className,true,A.ID()))))
			&&((!noCommon)||((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL))
			&&((!stdCharClass)||(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))))
				addAbility(mob,A,pct,myAbles);
		}
	}

	public boolean tryTeach(MOB teacher, MOB student, String teachWhat)
	{
		CMMsg msg2=CMClass.getMsg(teacher,student,null,CMMsg.MSG_SPEAK,null);
		if(!teacher.location().okMessage(teacher,msg2))
			return false;
		msg2=CMClass.getMsg(teacher,student,null,CMMsg.MSG_OK_ACTION,"<S-NAME> teach(es) <T-NAMESELF> '"+teachWhat+"'.");
		if(!teacher.location().okMessage(teacher,msg2))
			return false;
		teacher.location().send(teacher,msg2);
		return true;
	}
	
	
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(!noExpertises)
		&&((--tickDownToKnowledge)==0)
		&&(ticking instanceof MOB))
		{
			noExpertises=true;
			MOB mob=(MOB)ticking;
			if(teachEverything)
			{
				for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
				{
					ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
					if(mob.fetchExpertise(def.ID)==null)
						mob.addExpertise(def.ID);
				}
				trainableExpertises=null;
			}
			else
			{
				boolean someNew=true;
				CharStats oldBase=(CharStats)mob.baseCharStats().copyOf();
				for(int i: CharStats.CODES.BASE())
					mob.baseCharStats().setStat(i,100);
				for(int i=0;i<mob.baseCharStats().numClasses();i++)
					mob.baseCharStats().setClassLevel(mob.baseCharStats().getMyClass(i),100);
				mob.recoverCharStats();
				while(someNew)
				{
					someNew=false;
					List<ExpertiseDefinition> V=CMLib.expertises().myQualifiedExpertises(mob);
					ExpertiseLibrary.ExpertiseDefinition def=null;
					for(int v=0;v<V.size();v++)
					{
						def=V.get(v);
						if(mob.fetchExpertise(def.ID)==null)
						{
							mob.addExpertise(def.ID);
							someNew=true;
						}
					}
					if(someNew)
						trainableExpertises=null;
				}
				mob.setBaseCharStats(oldBase);
				mob.recoverCharStats();
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public void addAbility(MOB mob, Ability A, int pct, Hashtable myAbles)
	{
		if(CMLib.dice().rollPercentage()<=pct)
		{
			Ability A2=(Ability)myAbles.get(A.ID());
			if(A2==null)
			{
				A=(Ability)A.copyOf();
				A.setSavable(false);
				A.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A.ID()));
				myAbles.put(A.ID(),A);
				mob.addAbility(A);
			}
			else
				A2.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,A2.ID()));
		}
	}

	protected void ensureCharClass()
	{
		myMOB.baseCharStats().setMyClasses("StdCharClass");
		myMOB.baseCharStats().setMyLevels(""+myMOB.phyStats().level());
		myMOB.recoverCharStats();

		Hashtable myAbles=new Hashtable();
		Ability A=null;
		for(Enumeration<Ability> a=myMOB.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A!=null) myAbles.put(A.ID(),A);
		}
		myMOB.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
		myMOB.baseCharStats().setStat(CharStats.STAT_WISDOM,19);

		int pct=100;
		Vector V=null;
		A=CMClass.getAbility(getParms());
		if(A!=null)
		{
			addAbility(myMOB,A,pct,myAbles);
			teachEverything=false;
		}
		else
			V=CMParms.parse(getParms());

		if(V!=null)
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=(String)V.elementAt(v);
			if(s.equalsIgnoreCase("NOCOMMON"))
			{
				noCommon=true;
				V.removeElementAt(v);
			}
			if(s.equalsIgnoreCase("NOEXPS")||s.equalsIgnoreCase("NOEXP"))
			{
				noExpertises=true;
				V.removeElementAt(v);
			}
			if(s.equalsIgnoreCase("NOHLEXPS")||s.equalsIgnoreCase("NOHLEXP"))
			{
				noHLExpertises=true;
				V.removeElementAt(v);
			}
		}

		if(V!=null)
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.endsWith("%"))
			{
				pct=CMath.s_int(s.substring(0,s.length()-1));
				continue;
			}

			A=CMClass.getAbility(s);
			CharClass C=CMClass.findCharClass(s);
			if((C!=null)&&(!C.ID().equals("StdCharClass")))
			{
				teachEverything=false;
				setTheCharClass(myMOB,C);
				classAbles(myMOB,myAbles,pct);
				myMOB.recoverCharStats();
			}
			else
			if(A!=null)
			{
				addAbility(myMOB,A,pct,myAbles);
				teachEverything=false;
			}
			else
			{
				ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(s);
				if(def!=null)
				{
					myMOB.addExpertise(def.ID);
					teachEverything=false;
				}
			}
		}
		myMOB.recoverCharStats();
		if((myMOB.charStats().getCurrentClass().ID().equals("StdCharClass"))
		&&(teachEverything))
			classAbles(myMOB,myAbles,pct);
		int lvl=myMOB.phyStats().level()/myMOB.baseCharStats().numClasses();
		if(lvl<1) lvl=1;
		for(int i=0;i<myMOB.baseCharStats().numClasses();i++)
		{
			CharClass C=myMOB.baseCharStats().getMyClass(i);
			if((C!=null)&&(myMOB.baseCharStats().getClassLevel(C)>=0))
				myMOB.baseCharStats().setClassLevel(C,lvl);
		}
		myMOB.recoverCharStats();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		if(myMOB==null) return;
		teachEverything=true;
		noCommon=false;
		noExpertises=false;
		tickDownToKnowledge=4;
		trainableExpertises=null;
		ensureCharClass();
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			if(CMath.bset(((MOB)host).getBitmap(),MOB.ATT_NOTEACH))
				((MOB)host).setBitmap(CMath.unsetb(((MOB)host).getBitmap(),MOB.ATT_NOTEACH));
		}
		return super.okMessage(host,msg);
	}
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		if(myMOB==null) return;
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=myMOB;
		MOB student=msg.source();

		if((!msg.amISource(monster))
		&&(!student.isMonster())
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||msg.amITarget(monster))
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK))
		{
			String sayMsg=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(sayMsg==null)
			{
				int start=msg.sourceMessage().indexOf('\'');
				if(start>0)
					sayMsg=msg.sourceMessage().substring(start+1);
				else
					sayMsg=msg.sourceMessage();
			}
			int x=sayMsg.toUpperCase().indexOf("TEACH");
			if(x<0)
				x=sayMsg.toUpperCase().indexOf("GAIN ");
			if(x>=0)
			{
				boolean giveABonus=false;
				String s=sayMsg.substring(x+5).trim();
				x=s.lastIndexOf("\'");
				if(x>0)
					s=s.substring(0,x);
				else
				{
					x=s.lastIndexOf('`');
					if(x>0) 
						s=s.substring(0,x);
				}

				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().endsWith("PLEASE"))
					s=s.substring(0,s.length()-6).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.toUpperCase().startsWith("PLEASE "))
				{
					giveABonus=true;
					s=s.substring(6).trim();
				}
				if(s.toUpperCase().startsWith("ME "))
					s=s.substring(3).trim();
				if(s.startsWith("\"")) s=s.substring(1).trim();
				if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
				if(s.trim().equalsIgnoreCase("LIST"))
				{
					CMLib.commands().postSay(monster,student,"Try the QUALIFY command.",true,false);
					return;
				}
				if(s.trim().toUpperCase().equals("ALL"))
				{
					CMLib.commands().postSay(monster,student,"I can't teach you everything at once. Try the QUALIFY command.",true,false);
					return;
				}
				Ability myAbility=CMClass.findAbility(s.trim().toUpperCase(),monster);
				if(myAbility==null)
				{
					ExpertiseLibrary.ExpertiseDefinition theExpertise=null;
					if(trainableExpertises==null)
					{
						trainableExpertises=new LinkedList<ExpertiseLibrary.ExpertiseDefinition>();
						trainableExpertises.addAll(CMLib.expertises().myListableExpertises(monster));
						for(int exi=0;exi<monster.numExpertises();exi++)
						{
							final String experID=monster.fetchExpertise(exi);
							if(experID!=null)
							{
								ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(experID);
								if((def != null) && (!trainableExpertises.contains(def))) 
									trainableExpertises.add(def);
							}
						}
						HashSet<String> allExperParentsIDs=new HashSet();
						for(int v=0;v<trainableExpertises.size();v++)
						{
							ExpertiseLibrary.ExpertiseDefinition def=trainableExpertises.get(v);
							if(!allExperParentsIDs.contains(def.baseName))
								allExperParentsIDs.add(def.baseName);
						}
						for(String experParentID : allExperParentsIDs)
						{
							List<String> childrenIDs=CMLib.expertises().getStageCodes(experParentID);
							for(String experID : childrenIDs)
							{
							  ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(experID);
							  if((def != null) && (!trainableExpertises.contains(def))) 
								  trainableExpertises.add(def);
							}
						}
					}
					for(ExpertiseLibrary.ExpertiseDefinition def : trainableExpertises)
					{
						if((def.name.equalsIgnoreCase(s))
						&&(theExpertise==null))
							theExpertise=def;
					}
					if(theExpertise==null)
					for(ExpertiseLibrary.ExpertiseDefinition def : trainableExpertises)
					{
						if((CMLib.english().containsString(def.name,s)
						&&(theExpertise==null)))
							theExpertise=def;
					}
					if(theExpertise!=null)
					{
						if(student.fetchExpertise(theExpertise.ID)!=null)
						{
							monster.tell(student.name()+" already knows "+theExpertise.name);
							CMLib.commands().postSay(monster,student,"You already know "+theExpertise.name,true,false);
							return;
						}
						if(!CMLib.expertises().myQualifiedExpertises(student).contains(theExpertise))
						{
							monster.tell(student.name()+" does not yet fully qualify for the expertise '"+theExpertise.name+"'.\n\rRequirements: "+CMLib.masking().maskDesc(theExpertise.allRequirements()));
							CMLib.commands().postSay(monster,student,"I'm sorry, you do not yet fully qualify for the expertise '"+theExpertise.name+"'.\n\rRequirements: "+CMLib.masking().maskDesc(theExpertise.allRequirements()),true,false);
							return;
						}
						if(!theExpertise.meetsCostRequirements(student))
						{
							monster.tell("Training for that expertise requires "+theExpertise.costDescription()+".");
							CMLib.commands().postSay(monster,student,"I'm sorry, but to learn the expertise '"+theExpertise.name+"' requires: "+theExpertise.costDescription(),true,false);
							return ;
						}
						if(!tryTeach(monster,student,theExpertise.name))
							return;
						theExpertise.spendCostRequirements(student);
						student.addExpertise(theExpertise.ID);
					}
					else
					if((CMClass.findCharClass(s.trim())!=null))
						CMLib.commands().postSay(monster,student,"I've heard of "+s+", but that's an class-- try TRAINing  for it.",true,false);
					else
					{
						for(Enumeration e=CMLib.expertises().definitions(); e.hasMoreElements();)
						{
							ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
							if(def.name.equalsIgnoreCase(s))
							{
								theExpertise=def;
								break;
							}
						}
						if(theExpertise==null)
							CMLib.commands().postSay(monster,student,"I'm sorry, but I've never heard of "+s,true,false);
						else
							CMLib.commands().postSay(monster,student,"I'm sorry, but I do not know "+theExpertise.name+".");
					}
					return;
				}
				if(giveABonus)
				{
					monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,25);
					monster.baseCharStats().setStat(CharStats.STAT_WISDOM,25);
					monster.recoverCharStats();
				}

				if(student.fetchAbility(myAbility.ID())!=null)
				{
					CMLib.commands().postSay(monster,student,"But you already know '"+myAbility.name()+"'.",true,false);
					return;
				}
				int prof75=(int)Math.round(CMath.mul(CMLib.ableMapper().getMaxProficiency(student,true,myAbility.ID()),0.75));
				myAbility.setProficiency(prof75/2);
				if(!myAbility.canBeTaughtBy(monster,student))
					return;
				if(!myAbility.canBeLearnedBy(monster,student))
					return;
				if(!tryTeach(monster,student,myAbility.name()))
					return ;
				myAbility.teach(monster,student);
				monster.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,19);
				monster.baseCharStats().setStat(CharStats.STAT_WISDOM,19);
				monster.recoverCharStats();
			}
		}

	}
}
