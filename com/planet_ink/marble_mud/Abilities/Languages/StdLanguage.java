package com.planet_ink.marble_mud.Abilities.Languages;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class StdLanguage extends StdAbility implements Language
{
	public String ID() { return "StdLanguage"; }
	public String name(){ return "Languages";}
	public String writtenName() { return name();}
	private static final String[] triggerStrings = {"SPEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getLangTrainCostFormula(ID()); }
	public int classificationCode(){return Ability.ACODE_LANGUAGE;}

	private static Hashtable emptyHash=new Hashtable();
	private static Vector emptyVector=new Vector();
	protected boolean spoken=false;
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	public boolean beingSpoken(String language){return spoken;}
	public void setBeingSpoken(String language, boolean beingSpoken){spoken=beingSpoken;}
	public Map<String, String> translationHash(String language){ return emptyHash; }
	public List<String[]> translationVector(String language){ return emptyVector; }
	
	public List<String> languagesSupported() {return new XVector(ID());}
	public boolean translatesLanguage(String language) { return ID().equalsIgnoreCase(language);}
	public int getProficiency(String language) { 
		if(ID().equalsIgnoreCase(language))
			return proficiency();
		return 0;
	}
	
	public String displayText()
	{
		if(beingSpoken(ID())) return "(Speaking "+name()+")";
		return "";
	}

	protected String fixCase(String like,String make)
	{
		StringBuffer s=new StringBuffer(make);
		char lastLike=' ';
		for(int x=0;x<make.length();x++)
		{
			if(x<like.length()) lastLike=like.charAt(x);
			s.setCharAt(x,fixCase(lastLike,make.charAt(x)));
		}
		return s.toString();
	}
	protected char fixCase(char like,char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		return Character.toLowerCase(make);
	}
	public String translate(String language, String word)
	{
		if(translationHash(language).containsKey(word.toUpperCase()))
			return fixCase(word,(String)translationHash(language).get(word.toUpperCase()));
		MOB M=CMLib.players().getPlayer(word);
		if(M!=null) return word;
		List<String[]> translationVector=translationVector(language);
		if(translationVector.size()>0)
		{
			String[] choices=null;
			try{ choices=(String[])translationVector.get(word.length()-1);}catch(Exception e){}
			if(choices==null) choices=(String[])translationVector.get(translationVector.size()-1);
			return choices[CMath.abs(word.toLowerCase().hashCode()) % choices.length];
		}
		return word;
	}

	protected int numChars(String words)
	{
		int num=0;
		for(int i=0;i<words.length();i++)
		{
			if(Character.isLetter(words.charAt(i)))
				num++;
		}
		return num;
	}

	public String messChars(String language, String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0) return words;
		StringBuffer w=new StringBuffer(words);
		while(numToMess>0)
		{
			int x=CMLib.dice().roll(1,words.length(),-1);
			char c=words.charAt(x);
			if(Character.isLetter(c))
			{
				if(vowels.indexOf(c)>=0)
					w.setCharAt(x,fixCase(c,vowels.charAt(CMLib.dice().roll(1,vowels.length(),-1))));
				else
					w.setCharAt(x,fixCase(c,consonants.charAt(CMLib.dice().roll(1,consonants.length(),-1))));
				numToMess--;
			}
		}
		return w.toString();
	}

	public String scrambleAll(String language, String str, int numToMess)
	{
		StringBuffer newStr=new StringBuffer("");
		int start=0;
		int end=0;
		int state=-1;
		while(start<=str.length())
		{
			char c='\0';
			if(end>=str.length())
				c=' ';
			else
				c=str.charAt(end);
			switch(state)
			{
			case -1:
				if(Character.isLetter(c))
				{ state=0; end++;}
				else
				{ newStr.append(c); end++;start=end;}
				break;
			case 0:
				if(Character.isLetter(c))
				{ end++;}
				else
				if(Character.isDigit(c))
				{ newStr.append(str.substring(start,end+1)); end++; start=end; state=1; }
				else
				{ newStr.append(translate(language,str.substring(start,end))+c); end++; start=end; state=-1; }
				break;
			case 1:
				if(Character.isLetterOrDigit(c))
				{ newStr.append(c); end++; start=end;}
				else
				{ newStr.append(c); end++; start=end; state=-1; }
				break;
			}
		}
		return newStr.toString();
	}
	

	
	protected Language getMyTranslator(String id, Physical P, Language winner) 
	{
		if(P==null) return winner;
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof Language) 
			&& ((Language)A).translatesLanguage(id)
			&& ((winner==null)
					||((Language)A).getProficiency(id) > winner.getProficiency(id)))
			{
				winner = (Language)A;
			}
		}
		return winner;
	}
	
	protected Language getAnyTranslator(String id, MOB mob) 
	{
		Language winner = null;
		winner = getMyTranslator(id,mob,winner);
		winner = getMyTranslator(id,mob.location(),winner);
		for(int i=0;i<mob.numItems();i++)
			winner=getMyTranslator(id,mob.getItem(i),winner);
		return winner;
	}

	protected boolean processSourceMessage(CMMsg msg, String str, int numToMess)
	{
		String smsg=CMStrings.getSayFromMessage(msg.sourceMessage());
		if(numToMess>0) smsg=messChars(ID(),smsg,numToMess);
		msg.modify(msg.source(),
					  msg.target(),
					  this,
					  msg.sourceCode(),
					  CMStrings.substituteSayInMessage(msg.sourceMessage(),smsg),
					  msg.targetCode(),
					  msg.targetMessage(),
					  msg.othersCode(),
					  msg.othersMessage());
		return true;
	}
	
	protected boolean processNonSourceMessages(CMMsg msg, String str, int numToMess)
	{
		str=scrambleAll(ID(),str,numToMess);
		msg.modify(msg.source(),
					  msg.target(),
					  this,
					  msg.sourceCode(),
					  msg.sourceMessage(),
					  msg.targetCode(),
					  CMStrings.substituteSayInMessage(msg.targetMessage(),str),
					  msg.othersCode(),
					  CMStrings.substituteSayInMessage(msg.othersMessage(),str));
		return true;
	}

	protected boolean tryLinguisticWriting(CMMsg msg)
	{
		if(msg.target() instanceof Physical)
		{
			Physical P = (Physical)msg.target();
			for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof Language)&&(!A.ID().equals(ID())))
				{
					msg.source().tell(msg.target().name()+" is already written in "+A.name()+" and can not have "+writtenName()+" writing added.");
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)&&(beingSpoken(ID())))
		{
			if((msg.source()==affected)
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))))
			{
				String str=CMStrings.getSayFromMessage(msg.othersMessage());
				if(str==null) str=CMStrings.getSayFromMessage(msg.targetMessage());
				if(str!=null)
				{
					int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-getProficiency(ID()),100)));
					if(!processSourceMessage(msg, str, numToMess))
						return false;
					if(!processNonSourceMessages(msg,str,numToMess))
						return false;
					if(CMLib.flags().aliveAwakeMobile((MOB)affected,true))
						helpProficiency((MOB)affected, 0);
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WRITE)
			&&(msg.source()==affected)
			&&(msg.target() instanceof Item)
			&&(((Item)msg.target()).isReadable())
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				if(!tryLinguisticWriting(msg))
					return false;
			}
			else
			if((msg.target()==affected)&&(msg.source()!=affected))
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_ORDER:
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_BID:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_WITHDRAW:
				case CMMsg.TYP_DEPOSIT:
				{
					// yes, this means that a mob speaking Common to a marketing player will get failed,
					// however, remember that the LISTer language doesn't matter, only the responding (this) language.
					// also, think about muds where there is no Common (an interesting mud!)
					if((!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.ORDER))
					&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDMOBS)||(!((MOB)msg.target()).isMonster()))
					&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDROOMS)||(!((MOB)msg.target()).isMonster())))
					{
						Language L=getAnyTranslator(ID(),msg.source());
						if((L==null)
						||(!L.beingSpoken(ID()))
						||((CMLib.dice().rollPercentage()*2)>(L.getProficiency(ID())+getProficiency(ID()))))
						{
							msg.setTargetCode(CMMsg.TYP_SPEAK);
							msg.setSourceCode(CMMsg.TYP_SPEAK);
							msg.setOthersCode(CMMsg.TYP_SPEAK);
							String reply=null;
							if((L==null)||(!L.beingSpoken(ID())))
								reply="<S-NAME> <S-IS-ARE> speaking "+name()+" and <T-NAME> would not understand <S-HIM-HER>.";
							else
								reply="<T-NAME> <T-IS-ARE> having trouble understanding <S-YOUPOSS> pronunciation.";
							msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),msg.source(),null,CMMsg.MSG_OK_VISUAL,reply));
						}
					}
					break;
				}
				default:
					break;
				}
		}
		return super.okMessage(myHost,msg);
	}
	
	private int numLanguagesKnown(MOB student)
	{
		int numLanguages=0;
		if(student==null) return Integer.MAX_VALUE;
		CharClass C=student.charStats().getCurrentClass();
		DVector culturalAbilitiesDV = student.baseCharStats().getMyRace().culturalAbilities();
		HashSet culturalAbilities=new HashSet();
		for(int i=0;i<culturalAbilitiesDV.size();i++)
			culturalAbilities.add(culturalAbilitiesDV.elementAt(i, 1).toString().toLowerCase());
		for(int a=0;a<student.numAbilities();a++)
		{
			Ability A=student.fetchAbility(a);
			if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
			&&(!(A instanceof Common))
			&&(!culturalAbilities.contains(A.ID())))
			{
				if((CMLib.ableMapper().getQualifyingLevel(C.ID(), false, A.ID())>=0)||(culturalAbilities.contains(A.ID().toLowerCase())))
					continue;
				numLanguages++;
			}
		}
		return numLanguages;
	}
	
	
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		CharClass C=student.charStats().getCurrentClass();
		if(C.maxLanguages()==0) return true;
		if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
			return true;
		int numLanguages=numLanguagesKnown(student);
		if((C.maxLanguages()>0)&&(C.maxLanguages()<=numLanguages))
		{
			teacher.tell(student.name()+" can not learn any more languages.");
			student.tell("You may only learn " + C.maxLanguages() + " languages.");
			return false;
		}
		return true;
	}
	
	public void teach(MOB teacher, MOB student)
	{
		super.teach(teacher, student);
		if((student!=null)&&(student.fetchAbility(ID())!=null))
		{
			CharClass C=student.charStats().getCurrentClass();
			if(C.maxLanguages()==0) return;
			if(CMLib.ableMapper().getQualifyingLevel(C.ID(), false, ID())>=0)
				return;
			int numLanguages=numLanguagesKnown(student);
			int remaining = C.maxLanguages() - numLanguages;
			if(remaining<=0)
				student.tell(student.name()+" may not learn any more languages.");
			else
			if(remaining<Integer.MAX_VALUE/2)
				student.tell(student.name()+" may learn "+remaining+" more languages.");
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A instanceof Language))
				{
					if(mob.isMonster())
						A.setProficiency(100);
					if(A.ID().equals(ID()))
						((Language)A).setBeingSpoken(ID(),true);
					else
						((Language)A).setBeingSpoken(ID(),false);
				}
			}
			isAnAutoEffect=false;
			mob.tell("You are now speaking "+name()+".");
		}
		else
			setBeingSpoken(ID(),true);
		return true;
	}

	protected boolean translateOthersMessage(CMMsg msg, String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf('\'')>0))
		{
			String otherMes=msg.othersMessage();
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}
	
	protected boolean translateTargetMessage(CMMsg msg, String sourceWords)
	{
		if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
		{
			String otherMes=msg.targetMessage();
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateChannelMessage(CMMsg msg, String sourceWords)
	{
		if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		{
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),CMStrings.substituteSayInMessage(msg.othersMessage(),sourceWords)+" (translated from "+name()+")"));
			return true;
		}
		return false;
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Language)
		&&(msg.tool().ID().equals(ID())))
		{
			String str=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(str!=null)
			{
				int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-getProficiency(ID()),100)));
				if(numToMess>0)
					str=messChars(ID(),str,numToMess);
				if(!translateChannelMessage(msg,str))
					if(!translateTargetMessage(msg,str))
						translateOthersMessage(msg, str);
			}
		}
		else
		if((affected instanceof MOB)
		&&(msg.source()==affected)
		&&(beingSpoken(ID()))
		&&(msg.target() instanceof Item)
		&&(msg.sourceMinor()==CMMsg.TYP_WRITE)
		&&(((Item)msg.target()).isReadable())
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			Item I = (Item)msg.target();
			Ability L=null;
			for(int i=I.numEffects()-1;i>=0;i--) // reverse enumeration
			{
				L=I.fetchEffect(i);
				if(L instanceof Language)
				{
					I.delEffect(L);
					break;
				}
			}
			I.addNonUninvokableEffect((Ability)this.copyOf());
		}
		else
		if((affected instanceof Item)
		&&(!canBeUninvoked())
		&&(msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_READ)
		&&((msg.targetMessage()==null)||(!msg.targetMessage().equals("CANCEL")))
		&&(!(affected instanceof LandTitle))
		&&(CMLib.flags().canBeSeenBy(this,msg.source()))
		&&(((Item)affected).isReadable())
		&&(((Item)affected).readableText()!=null)
		&&(((Item)affected).readableText().length()>0))
		{
			// first make sure the Item does not handle it,
			// since THIS item is in another language.
			msg.modify(msg.source(),
					   msg.target(),
					   msg.tool(),
					   msg.sourceCode(),
					   msg.sourceMessage(),
					   msg.targetCode(),
					   "CANCEL",
					   msg.othersCode(),
					   msg.othersMessage());
			Language L=(Language)msg.source().fetchEffect(ID());
			String str=((Item)affected).readableText();
			if(str.startsWith("FILE=")
			||str.startsWith("FILE="))
			{
				StringBuffer buf=Resources.getFileResource(str.substring(5),true);
				if((buf!=null)&&(buf.length()>0))
					str=buf.toString();
				else
					str="";
			}
			int numToMess=numChars(str);
			if(numToMess==0)
				msg.source().tell("There is nothing written on "+affected.name()+".");
			else
			{
				if(L!=null)
					numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-L.getProficiency(ID()),100)));
				String original=messChars(ID(),str,numToMess);
				str=scrambleAll(ID(),str,numToMess);
				msg.source().tell("It says '"+str+"'");
				if((L!=null)&&(!original.equals(str)))
					msg.source().tell("It says '"+original+"' (translated from "+L.writtenName()+").");
			}
		}
	}
}
