package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.exceptions.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
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
import java.util.regex.*;

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
public class EnglishParser extends StdLibrary implements EnglishParsing
{
	public String ID(){return "EnglishParser";}
	private final static String[] articles={"a","an","all of","some one","a pair of","one of","all","the","some"};
	public static boolean[] PUNCTUATION_TABLE=null;
	public final static char[] ALL_CHRS="ALL".toCharArray();
	public final static String[] fwords={"calf", "half", "knife", "life", "wife", "elf", "self", "shelf", "leaf", "sheaf", "thief", "loaf", "wolf"};
	public final static List<Environmental> empty=new ReadOnlyVector<Environmental>(1);
	
	public String toEnglishStringList(final String[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		if(V.length==1) return V[0];
		final StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length-1;v++)
		{
			if(v>0) s.append(", ");
			s.append(V[v]);
		}
		s.append(" and ");
		s.append(V[V.length-1]);
		return s.toString();
	}

	
	public boolean isAnArticle(String s)
	{
		s=s.toLowerCase();
		for(int a=0;a<articles.length;a++)
			if(s.equals(articles[a]))
				return true;
		return false;
	}

	public String makePlural(String str)
	{
		if((str==null)||(str.length()==0))
			return str;
		final boolean uppercase=Character.isUpperCase(str.charAt(str.length()-1));
		final String lowerStr=str.toLowerCase();
		if(lowerStr.endsWith("is"))
			return str.substring(0,str.length()-2)+(uppercase?"ES":"es");
		if(lowerStr.endsWith("s")||lowerStr.endsWith("z")||lowerStr.endsWith("x")||lowerStr.endsWith("ch")||lowerStr.endsWith("sh"))
			return str+(uppercase?"ES":"es");
		if(lowerStr.endsWith("ay")||lowerStr.endsWith("ey")||lowerStr.endsWith("iy")||lowerStr.endsWith("oy")||lowerStr.endsWith("uy"))
			return str+(uppercase?"S":"s");
		if(lowerStr.endsWith("y"))
			return str.substring(0,str.length()-1)+(uppercase?"IES":"ies");
		if(CMStrings.contains(fwords, lowerStr))
			return str.substring(0,str.length()-1)+(uppercase?"VES":"ves");
		return str+(uppercase?"S":"s");
	}
	public String cleanArticles(String s)
	{
		boolean didSomething=true;
		while(didSomething)
		{
			didSomething=false;
			String lowStr=s.toLowerCase();
			for(int a=0;a<articles.length;a++)
				if(lowStr.startsWith(articles[a]+" "))
					return s.substring(articles[a].length()+1);
		}
		return s;
	}

	public String properIndefiniteArticle(String str)
	{
		int i=0;
		for(;i<str.length();i++)
		  switch(str.charAt(i))
		  {
		  case '^':
		  {
			  i++;
			  if(i<str.length())
			  {
				  switch(str.charAt(i))
				  {
				  case ColorLibrary.COLORCODE_FANSI256: i+=3; break;
				  case ColorLibrary.COLORCODE_BANSI256: i+=3; break;
				  case ColorLibrary.COLORCODE_BACKGROUND: i++; break;
				  case '<':
					while(i<str.length()-1)
					{
						if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
							i++;
						else
						{
							i++;
							break;
						}
					}
					break;
				  case '&':
					while(i<str.length())
					{
						if(str.charAt(i)!=';')
							i++;
						else
							break;
					}
					break;
				  }
			  }
			  break;
		  }
		  case 'a': case 'e': case 'i': case 'o': case 'u':
		  case 'A': case 'E': case 'I': case 'O': case 'U':
			  return "an";
		  default:
			  if(Character.isLetter(str.charAt(i)))
				  return "a";
			  else
				  return "";
		  }
		return "";
	}

	public String getFirstWord(final String str)
	{
	  int i=0;
	  int start=-1;
	  for(;i<str.length();i++)
		switch(str.charAt(i))
		{
		case '^':
		{
			i++;
			if(i<str.length())
			{
				switch(str.charAt(i))
				{
				case ColorLibrary.COLORCODE_FANSI256: i+=3; break;
				case ColorLibrary.COLORCODE_BANSI256: i+=3; break;
				case ColorLibrary.COLORCODE_BACKGROUND: i++; break;
				case '<':
				  while(i<str.length()-1)
				  {
					  if((str.charAt(i)!='^')||(str.charAt(i+1)!='>'))
						  i++;
					  else
					  {
						  i++;
						  break;
					  }
				  }
				  break;
				case '&':
				  while(i<str.length())
				  {
					  if(str.charAt(i)!=';')
						  i++;
					  else
						  break;
				  }
				  break;
				}
			}
			break;
		}
		case ' ':
			if(start>=0)
				return str.substring(start,i);
			break;
		default:
			if(Character.isLetter(str.charAt(i)))
				start=i;
			break;
		}
		return str;
	}

	public String startWithAorAn(final String str)
	{
		if((str==null)||(str.length()==0)) return str;
		final String uppStr=getFirstWord(str).toUpperCase();
		if((!uppStr.equals("A")) &&(!uppStr.equals("AN"))
		&&(!uppStr.equals("THE")) &&(!uppStr.equals("SOME")))
			return (properIndefiniteArticle(str)+" "+str.trim()).trim();
		return str;
	}

	public String insertUnColoredAdjective(String str, String adjective)
	{
		if(str.length()==0) 
			return str;
		str=CMStrings.removeColors(str.trim());
		final String uppStr=str.toUpperCase();
		if((uppStr.startsWith("A "))
		||(uppStr.startsWith("AN ")))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(2).trim();
		if(uppStr.startsWith("THE "))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(3).trim();
		if(uppStr.startsWith("SOME "))
			return properIndefiniteArticle(adjective)+" "+adjective+" "+str.substring(4).trim();
		return properIndefiniteArticle(adjective)+" "+adjective+" "+str.trim();
	}

	public CMObject findCommand(MOB mob, List<String> commands)
	{
		if((mob==null)
		||(commands==null)
		||(mob.location()==null)
		||(commands.size()==0))
			return null;

		String firstWord=((String)commands.get(0)).toUpperCase();

		if((firstWord.length()>1)&&(!Character.isLetterOrDigit(firstWord.charAt(0))))
		{
			commands.add(1,((String)commands.get(0)).substring(1));
			commands.set(0,""+firstWord.charAt(0));
			firstWord=""+firstWord.charAt(0);
		}

		// first, exacting pass
		Command C=CMClass.findCommandByTrigger(firstWord,true);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isCommandDisabled(CMClass.classID(C).toUpperCase())))
			return C;

		Ability A=getToEvoke(mob,new XVector<String>(commands));
		if((A!=null)
		&&(!CMSecurity.isAbilityDisabled(A.ID().toUpperCase())))
			return A;

		if(getAnEvokeWord(mob,firstWord)!=null)
			return null;

		Social social=CMLib.socials().fetchSocial(commands,true,true);
		if(social!=null) return social;

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}

		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().equalsIgnoreCase(firstWord))
			{
				C=CMClass.getCommand("CommandJournal");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}

		// second, inexacting pass
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			HashSet<String> tried=new HashSet<String>();
			if((A!=null)&&(A.triggerStrings()!=null))
				for(int t=0;t<A.triggerStrings().length;t++)
					if((A.triggerStrings()[t].toUpperCase().startsWith(firstWord))
					&&(!tried.contains(A.triggerStrings()[t])))
					{
						Vector<String> commands2=new XVector<String>(commands);
						commands2.setElementAt(A.triggerStrings()[t],0);
						Ability A2=getToEvoke(mob,commands2);
						if((A2!=null)&&(!CMSecurity.isAbilityDisabled(A2.ID().toUpperCase())))
						{
							commands.set(0,A.triggerStrings()[t]);
							return A;
						}
					}
		}
		//commands comes inexactly after ables
		//because of CA, PR, etc..
		C=CMClass.findCommandByTrigger(firstWord,false);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isCommandDisabled(CMClass.classID(C).toUpperCase())))
			return C;


		social=CMLib.socials().fetchSocial(commands,false,true);
		if(social!=null)
		{
			commands.set(0,social.baseName());
			return social;
		}

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).startsWith(firstWord))
			{
				commands.set(0,CMLib.channels().getChannelName(c));
				C=CMClass.getCommand("Channel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).startsWith(firstWord))
			{
				commands.set(0,"NO"+CMLib.channels().getChannelName(c));
				C=CMClass.getCommand("NoChannel");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}

		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().startsWith(firstWord))
			{
				C=CMClass.getCommand("CommandJournal");
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}
		return null;
	}

	public boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private String collapsedName(Ability thisAbility)
	{
		int x=thisAbility.name().indexOf(' ');
		if(x>=0)
			return CMStrings.replaceAll(thisAbility.name()," ","");
		return thisAbility.Name();
	}

	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
			{
				if(((thisAbility.name().toUpperCase().startsWith(secondWord)))
				||(collapsedName(thisAbility).toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	public String getAnEvokeWord(MOB mob, String word)
	{
		if(mob==null) return null;
		Ability A=null;
		HashSet<String[]> done=new HashSet<String[]>();
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if((A!=null)
			&&(A.triggerStrings()!=null)
			&&(!done.contains(A.triggerStrings())))
			{
				done.add(A.triggerStrings());
				for(int t=0;t<A.triggerStrings().length;t++)
					if(word.equals(A.triggerStrings()[t]))
						return A.triggerStrings()[t];
			}
		}
		return null;
	}

	public Ability getToEvoke(MOB mob, List<String> commands)
	{
		String evokeWord=((String)commands.get(0)).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokableAbility=null;
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if((A!=null)
			&&(evokedBy(A,evokeWord)))
			{
				if(evokableAbility!=null)
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				evokableAbility=A;
			}
		}

		if((evokableAbility!=null)&&(commands.size()>1))
		{
			int classCode=evokableAbility.classificationCode()&Ability.ALL_ACODES;
			switch(classCode)
			{
			case Ability.ACODE_SPELL:
			case Ability.ACODE_SONG:
			case Ability.ACODE_PRAYER:
			case Ability.ACODE_CHANT:
				evokableAbility=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokableAbility!=null)
			commands.remove(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.remove(0);
			foundMoreThanOne=false;
			String secondWord=((String)commands.get(0)).toUpperCase();
			for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
				if((A!=null)
				&&(evokedBy(A,evokeWord,secondWord.toUpperCase())))
				{
					if((A.name().equalsIgnoreCase(secondWord))
					||(collapsedName(A).equalsIgnoreCase(secondWord)))
					{
						evokableAbility=A;
						foundMoreThanOne=false;
						break;
					}
					else
					if(evokableAbility!=null)
						foundMoreThanOne=true;
					else
						evokableAbility=A;
				}
			}
			if((evokableAbility!=null)&&(!foundMoreThanOne))
				commands.remove(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				String secondAndThirdWord=secondWord+" "+((String)commands.get(1)).toUpperCase();

				for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					Ability A=a.nextElement();
					if((A!=null) && (evokedBy(A,evokeWord,secondAndThirdWord.toUpperCase())))
					{
						evokableAbility=A;
						break;
					}
				}
				if(evokableAbility!=null)
				{
					commands.remove(0);
					commands.remove(0);
				}
			}
			else
			{
				for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
				{
					Ability A=a.nextElement();
					if((A!=null)
					&&(evokedBy(A,evokeWord))
					&&(A.name().toUpperCase().indexOf(" "+secondWord.toUpperCase())>0))
					{
						evokableAbility=A;
						commands.remove(0);
						break;
					}
				}
			}
		}
		return evokableAbility;
	}

	public boolean preEvoke(MOB mob, List<String> commands, int secondsElapsed, double actionsRemaining)
	{
		commands=new Vector<String>(commands);
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell("You don't know how to do that.");
			return false;
		}
		if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS)))
		{
			mob.tell("You are not high enough level to do that.");
			return false;
		}
		return evokableAbility.preInvoke(mob,commands,null,false,0,secondsElapsed,actionsRemaining);
	}
	public void evoke(MOB mob, Vector<String> commands)
	{
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell("You don't know how to do that.");
			return;
		}
		if((CMLib.ableMapper().qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMLib.ableMapper().qualifiesByLevel(mob,evokableAbility))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS)))
		{
			mob.tell("You are not high enough level to do that.");
			return;
		}
		evokableAbility.invoke(mob,commands,null,false,0);
	}

	public boolean[] PUNCTUATION_TABLE() {
		if(PUNCTUATION_TABLE==null)
		{
			boolean[] PUNCTUATION_TEMP_TABLE=new boolean[255];
			for(int c=0;c<255;c++)
				switch(c)
				{
				case '`': case '~': case '!': case '@': case '#': case '$': case '%':
				case '^': case '&': case '*': case '(': case ')': case '_': case '-':
				case '+': case '=': case '[': case ']': case '{': case '}': case '\\':
				case '|': case ';': case ':': case '\'': case '\"': case ',': case '<':
				case '.': case '>': case '/': case '?':
					PUNCTUATION_TEMP_TABLE[c]=true;
					break;
				default:
					PUNCTUATION_TEMP_TABLE[c]=false;
				}
			PUNCTUATION_TABLE=PUNCTUATION_TEMP_TABLE;
		}
		return PUNCTUATION_TABLE;
	}
	
	public String stripPunctuation(String str)
	{
		if((str==null)||(str.length()==0)) return str;
		boolean puncFound=false;
		PUNCTUATION_TABLE();
		for(int x=0;x<str.length();x++)
			if(isPunctuation((byte)str.charAt(x)))
			{
				puncFound=true;
				break;
			}
		if(!puncFound) return str;
		char[] strc=str.toCharArray();
		char[] str2=new char[strc.length];
		int s=0;
		for(int x=0;x<strc.length;x++)
			if(!isPunctuation((byte)strc[x]))
			{
				str2[s]=strc[x];
				s++;
			}
		return new String(str2,0,s);
	}
	
	private boolean isPunctuation(byte b) {
		if((b<0)||(b>255)) return false;
		return PUNCTUATION_TABLE[b];
	}
	
	public boolean equalsPunctuationless(char[] strC, char[] str2C)
	{
		if((strC.length==0)&&(str2C.length==0)) return true;
		PUNCTUATION_TABLE();
		int s1=0;
		int s2=0;
		int s1len=strC.length;
		while((s1len>0)&&(Character.isWhitespace(strC[s1len-1])||isPunctuation((byte)strC[s1len-1])))
			s1len--;
		int s2len=str2C.length;
		while((s2len>0)&&(Character.isWhitespace(str2C[s2len-1])||isPunctuation((byte)str2C[s2len-1])))
			s2len--;
		while(s1<s1len)
		{
			while((s1<s1len)&&(isPunctuation((byte)strC[s1])))
				s1++;
			while((s2<s2len)&&(isPunctuation((byte)str2C[s2])))
				s2++;
			if(s1==s1len)
			{
				if(s2==s2len)
					return true;
				return false;
			}
			if(s2==s2len)
				return false;
			if(strC[s1]!=str2C[s2])
				return false;
			s1++;
			s2++;
		}
		if(s2==s2len)
			return true;
		return false;
	}
	
	public boolean containsString(final String toSrchStr, final String srchStr)
	{
		if((toSrchStr==null)||(srchStr==null)) return false;
		if((toSrchStr.length()==0)&&(srchStr.length()>0)) return false;
		char[] srchC=srchStr.toCharArray();
		char[] toSrchC=toSrchStr.toCharArray();
		for(int c=0;c<srchC.length;c++)
			srchC[c]=Character.toUpperCase(srchC[c]);
		for(int c=0;c<toSrchC.length;c++)
			toSrchC[c]=Character.toUpperCase(toSrchC[c]);
		if(java.util.Arrays.equals(srchC,ALL_CHRS)) return true;
		if(java.util.Arrays.equals(srchC,toSrchC)) return true;
		if(equalsPunctuationless(srchC,toSrchC)) return true;
		
		boolean topOnly=false;
		if((srchC.length>1)&&(srchC[0]=='$'))
		{
			srchC=new String(srchC,1,srchC.length-1).toCharArray();
			topOnly=true;
		}
		int tos=0;
		boolean found=false;
		while((!found)&&(tos<toSrchC.length))
		{
			for(int x=0;x<srchC.length;x++)
			{
				if(tos>=toSrchC.length)
				{
					if(srchC[x]=='$')
						found=true;
					break;
				}

				switch(toSrchC[tos])
				{
				case '^':
					tos++;
					if(tos<toSrchC.length)
						switch(toSrchC[tos])
						{
						case ColorLibrary.COLORCODE_BACKGROUND:
							if(tos < toSrchC.length-1)
								tos+=2;
							break;
						case ColorLibrary.COLORCODE_FANSI256:
						case ColorLibrary.COLORCODE_BANSI256:
						  if(tos < toSrchC.length-4)
							  tos+=4;
						  break;
						default:
						  tos++;
						  break;
						}
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					tos++;
					break;
				}
				switch(srchC[x])
				{
				case '^': 
				  x++;
				  if(tos<srchC.length)
					  switch(srchC[x])
					  {
					  case ColorLibrary.COLORCODE_BACKGROUND:
						  if(x < srchC.length-1)
							  x+=2;
						  break;
					  case ColorLibrary.COLORCODE_FANSI256:
					  case ColorLibrary.COLORCODE_BANSI256:
						if(x < srchC.length-4)
							x+=4;
						break;
					  default:
						x++;
						break;
					  }
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';': x++;
					break;
				}
				if(x<srchC.length)
				{
					if(tos<toSrchC.length)
					{
						if(srchC[x]!=toSrchC[tos])
							break;
						else
						if(x==(srchC.length-1))
						   found=true;
						else
							tos++;
					}
					else
					if(srchC[x]=='$')
						found=true;
					else
						break;
				}
				else
				{
					found=true;
					break;
				}
			}
			if((topOnly)&&(!found)) break;
			while((!found)&&(tos<toSrchC.length)&&(Character.isLetter(toSrchC[tos])))
				tos++;
			tos++;
		}
		return found;
	}

	public String bumpDotNumber(String srchStr)
	{
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return srchStr;
		if(flags.allFlag)
			return srchStr;
		if(flags.occurrance==0)
			return "1."+flags.srchStr;
		return (flags.occurrance+1)+"."+flags.srchStr;
	}

	public int getContextNumber(ItemCollection cont, Environmental E){ return getContextNumber(toCollection(cont),E);}
	public int getContextNumber(Environmental[] list, Environmental E){ return getContextNumber(new XVector<Environmental>(list),E);}
	public int getContextNumber(Collection<? extends Environmental> list, Environmental E)
	{
		if(list==null) return 0;
		int context=1;
		for(Object O : list)
			if((((Environmental)O).Name().equalsIgnoreCase(E.Name()))
			||(((Environmental)O).name().equalsIgnoreCase(E.name())))
			{
				if(O==E)
					return context<2?0:context;
				if((!(O instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)O).container()))
					context++;
			}
		return -1;
	}
	private Collection<? extends Environmental> toCollection(ItemCollection cont)
	{
		LinkedList<Item> list=new LinkedList<Item>();
		for(Enumeration<Item> i=cont.items();i.hasMoreElements();)
			list.add(i.nextElement());
		return list;
	}
	
	public int getContextSameNumber(ItemCollection cont, Environmental E){ return getContextSameNumber(toCollection(cont),E);}
	public int getContextSameNumber(Environmental[] list, Environmental E){ return getContextSameNumber(new XVector<Environmental>(list),E);}
	public int getContextSameNumber(Collection<? extends Environmental> list, Environmental E)
	{
		if(list==null) return 0;
		int context=1;
		for(Object O : list)
			if((((Environmental)O).Name().equalsIgnoreCase(E.Name()))
			||(((Environmental)O).name().equalsIgnoreCase(E.name())))
			{
				if(E.sameAs((Environmental)O))
					return context<2?0:context;
				if((!(O instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)O).container()))
					context++;
			}
		return -1;
	}
	public String getContextName(ItemCollection cont, Environmental E){ return getContextName(toCollection(cont),E);}
	public String getContextName(Environmental[] list, Environmental E){ return getContextName(new XVector<Environmental>(list),E);}
	public String getContextName(Collection<? extends Environmental> list, Environmental E)
	{
		if(list==null) return E.name();
		int number=getContextNumber(list,E);
		if(number<0) return null;
		if(number<2) return E.name();
		return E.name()+"."+number;
	}

	public String getContextSameName(ItemCollection cont, Environmental E){ return getContextSameName(toCollection(cont),E);}
	public String getContextSameName(Environmental[] list, Environmental E){ return getContextSameName(new XVector<Environmental>(list),E);}
	public String getContextSameName(Collection<? extends Environmental> list, Environmental E)
	{
		if(list==null) return E.name();
		int number=getContextSameNumber(list,E);
		if(number<0) return null;
		if(number<2) return E.name();
		return E.name()+"."+number;
	}

	public Environmental parseShopkeeper(MOB mob, List<String> commands, String error)
	{
		if(commands.size()==0)
		{
			if(error.length()>0) mob.tell(error);
			return null;
		}
		commands.remove(0);

		List<Environmental> V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		if(V.size()==0)
		{
			if(error.length()>0) mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
				if(error.length()>0) mob.tell(error);
				return null;
			}
			String what=(String)commands.get(commands.size()-1);

			Environmental shopkeeper=fetchEnvironmental(V,what,false);
			if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
				for(int v=0;v<V.size();v++)
					if(V.get(v) instanceof Area)
					{ shopkeeper=(Environmental)V.get(v); break;}
			if((shopkeeper!=null)&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				commands.remove(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.get(commands.size()-1)+"' here buying or selling.");
				return null;
			}
			return shopkeeper;
		}
		Environmental shopkeeper=(Environmental)V.get(0);
		if(commands.size()>1)
		{
			MOB M=mob.location().fetchInhabitant((String)commands.get(commands.size()-1));
			if((M!=null)&&(CMLib.coffeeShops().getShopKeeper(M)!=null)&&(CMLib.flags().canBeSeenBy(M,mob)))
			{
				shopkeeper=M;
				commands.remove(commands.size()-1);
			}
		}
		return shopkeeper;
	}

	public List<Item> fetchItemList(Environmental from,
									MOB mob,
									Item container,
									List<String> commands,
									int preferredLoc,
									boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		List<Item> V=new Vector<Item>();

		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.get(0))>0))
		{
			maxToItem=CMath.s_int((String)commands.get(0));
			commands.set(0,"all");
		}

		String name=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.get(0)).equalsIgnoreCase("all"):false;
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToItem)))
		{
			doBugFix=false;
			Environmental item=null;
			if(from instanceof MOB)
			{
				if(preferredLoc==Wearable.FILTER_UNWORNONLY)
					item=((MOB)from).fetchCarried(container,name+addendumStr);
				else
				if(preferredLoc==Wearable.FILTER_WORNONLY)
					item=((MOB)from).fetchWornItem(name+addendumStr);
				else
					item=((MOB)from).findItem(null,name+addendumStr);
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,preferredLoc);
			if((item!=null)
			&&(item instanceof Item)
			&&((!visionMatters)||(CMLib.flags().canBeSeenBy(item,mob))||(item instanceof Light))
			&&(!V.contains(item)))
				V.add((Item)item);
			if(item==null) break;
			addendumStr="."+(++addendum);
		}
		
		if(preferredLoc==Wearable.FILTER_WORNONLY)
		{
			Vector<Item> V2=new Vector<Item>();
			short topLayer=0;
			short curLayer=0;
			int which=-1;
			while(V.size()>0)
			{
				Item I=(Item)V.get(0);
				topLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
				which=0;
				for(int v=1;v<V.size();v++)
				{
					I=(Item)V.get(v);
					curLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
					if(curLayer>topLayer)
					{ which=v; topLayer=curLayer;}
				}
				V2.addElement(V.get(which));
				V.remove(which);
			}
			V=V2;
		}
		else
		if(preferredLoc==Wearable.FILTER_UNWORNONLY)
		{
			Vector<Item> V2=new Vector<Item>();
			short topLayer=0;
			short curLayer=0;
			int which=-1;
			while(V.size()>0)
			{
				Item I=(Item)V.get(0);
				topLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
				which=0;
				for(int v=1;v<V.size();v++)
				{
					I=(Item)V.get(v);
					curLayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
					if(curLayer<topLayer)
					{ which=v; topLayer=curLayer;}
				}
				V2.addElement(V.get(which));
				V.remove(which);
			}
			V=V2;
		}
		return V;
	}

	public long numPossibleGold(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
					if(((Coins)V.get(v)).getNumberOfCoins()>=num)
						return num;
				V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.get(v)).getNumberOfCoins()>=num)
						return num;
			}
			return CMath.s_long(itemID);
		}
		Vector<String> V=CMParms.parse(itemID);
		if((V.size()>1)
		&&((CMath.isInteger((String)V.firstElement()))
		&&(matchAnyCurrencySet(CMParms.combine(V,1))!=null)))
			return CMath.s_long((String)V.firstElement());
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
		{
			String currency=matchAnyCurrencySet(CMParms.combine(V,1));
			if(currency!=null)
			{
				if(mine instanceof MOB)
				{
					List<Coins> V2=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
					double denomination=matchAnyDenomination(currency,CMParms.combine(V,1));
					Coins C=null;
					for(int v2=0;v2<V2.size();v2++)
					{
						C=(Coins)V2.get(v2);
						if(C.getDenomination()==denomination)
							return C.getNumberOfCoins();
					}
				}
				return 1;
			}
		}
		else
		if((V.size()>0)&&(matchAnyCurrencySet(CMParms.combine(V,0))!=null))
			return 1;
		return 0;
	}
	public String numPossibleGoldCurrency(Environmental mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
					if(((Coins)V.get(v)).getNumberOfCoins()>=num)
						return ((Coins)V.get(v)).getCurrency();
				V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.get(v)).getNumberOfCoins()>=num)
						return ((Coins)V.get(v)).getCurrency();
			}
			return CMLib.beanCounter().getCurrency(mine);
		}
		Vector<String> V=CMParms.parse(itemID);
		if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if(V.size()>0)
			return matchAnyCurrencySet(CMParms.combine(V,0));
		return CMLib.beanCounter().getCurrency(mine);
	}

	public long getMillisMultiplierByName(String timeName)
	{
		timeName=timeName.toLowerCase();
		if("ticks".startsWith(timeName))
			return CMProps.getTickMillis();
		else
		if("seconds".startsWith(timeName))
			return 1000;
		else
		if("minutes".startsWith(timeName))
			return 1000*60;
		else
		if("hours".startsWith(timeName))
			return 1000*60*60;
		else
		if("days".startsWith(timeName))
			return 1000*60*60*24;
		else
		if("weeks".startsWith(timeName))
			return 1000*60*60*24*7;
		else
			return -1;
	}

	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				List<Coins> V=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.get(v)).getNumberOfCoins()>=num)
						return ((Coins)V.get(v)).getDenomination();
			}
			return CMLib.beanCounter().getLowestDenomination(currency);
		}
		Vector<String> V=CMParms.parse(itemID);
		if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if(V.size()>0)
			return matchAnyDenomination(currency,CMParms.combine(V,0));
		return 0;
	}

	public String matchAnyCurrencySet(String itemID)
	{
		List<String> V=CMLib.beanCounter().getAllCurrencies();
		List<String> V2=null;
		for(int v=0;v<V.size();v++)
		{
			V2=CMLib.beanCounter().getDenominationNameSet((String)V.get(v));
			for(int v2=0;v2<V2.size();v2++)
			{
				String s=(String)V2.get(v2);
				if(s.toLowerCase().endsWith("(s)"))
					s=s.substring(0,s.length()-3)+"s";
				if(containsString(s,itemID))
					return (String)V.get(v);
			}
		}
		return null;
	}

	public double matchAnyDenomination(String currency, String itemID)
	{
		MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(currency);
		itemID=itemID.toUpperCase();
		String s=null;
		if(DV!=null)
		for(int v2=0;v2<DV.length;v2++)
		{
			s=DV[v2].name.toUpperCase();
			if(s.endsWith("(S)"))
				s=s.substring(0,s.length()-3)+"S";
			if(containsString(s,itemID))
				return DV[v2].value;
			else
			if((s.length()>0)
			&&(containsString(s,itemID)))
				return DV[v2].value;
		}
		return 0.0;
	}

	public Item possibleRoomGold(MOB seer, Room room, Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		if(CMath.isInteger(itemID))
		{
			gold=CMath.s_long(itemID);
			itemID="";
		}
		else
		{
			Vector<String> V=CMParms.parse(itemID);
			if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
				gold=CMath.s_long((String)V.firstElement());
			else
				return null;
			itemID=CMParms.combine(V,1);
		}
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.getItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(CMLib.flags().canBeSeenBy(I,seer))
				&&((itemID.length()==0)||(containsString(I.name(),itemID))))
				{
					if(((Coins)I).getNumberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).getNumberOfCoins()-gold);
					Coins C=(Coins)CMClass.getItem("StdCoins");
					C.setCurrency(((Coins)I).getCurrency());
					C.setNumberOfCoins(gold);
					C.setDenomination(((Coins)I).getDenomination());
					C.setContainer(container);
					C.recoverPhyStats();
					room.addItem(C);
					C.setExpirationDate(I.expirationDate());
					return C;
				}
			}
		}
		return null;
	}

	public Item bestPossibleGold(MOB mob, Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		double denomination=0.0;
		String currency=CMLib.beanCounter().getCurrency(mob);
		if(CMath.isInteger(itemID))
		{
			gold=CMath.s_long(itemID);
			double totalAmount=CMLib.beanCounter().getTotalAbsoluteValue(mob,currency);
			double bestDenomination=CMLib.beanCounter().getBestDenomination(currency,(int)gold,totalAmount);
			if(bestDenomination==0.0)
			{
				bestDenomination=CMLib.beanCounter().getBestDenomination(null,(int)gold,totalAmount);
				if(bestDenomination>0.0)
					currency=null;
			}
			if(bestDenomination==0.0)
				return null;
			denomination=bestDenomination;
		}
		else
		{
			Vector<String> V=CMParms.parse(itemID);
			if(V.size()<1) return null;
			if((!CMath.isInteger((String)V.firstElement()))
			&&(!((String)V.firstElement()).equalsIgnoreCase("all")))
				V.insertElementAt("1",0);
			Item I=mob.findItem(container,CMParms.combine(V,1));
			if(I instanceof Coins)
			{
				if(((String)V.firstElement()).equalsIgnoreCase("all"))
					gold=((Coins)I).getNumberOfCoins();
				else
					gold=CMath.s_long((String)V.firstElement());
				currency=((Coins)I).getCurrency();
				denomination=((Coins)I).getDenomination();
			}
			else
				return null;
		}
		if(gold>0)
		{
			double amt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
			if(amt>=CMath.mul(denomination,gold))
			{
				double expectedAmt = amt - CMath.mul(denomination,gold);
				CMLib.beanCounter().subtractMoney(mob,currency,denomination,CMath.mul(denomination,gold));
				double newAmt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
				if(newAmt > expectedAmt)
					CMLib.beanCounter().subtractMoney(mob,currency,(newAmt - expectedAmt));
				Coins C=(Coins)CMClass.getItem("StdCoins");
				C.setCurrency(currency);
				C.setDenomination(denomination);
				C.setNumberOfCoins(gold);
				C.recoverPhyStats();
				mob.addItem(C);
				return C;
			}
			mob.tell("You don't have that much "+CMLib.beanCounter().getDenominationName(currency,denomination)+".");
			List<Coins> V=CMLib.beanCounter().getStandardCurrency(mob,currency);
			for(int v=0;v<V.size();v++)
				if(((Coins)V.get(v)).getDenomination()==denomination)
					return (Item)V.get(v);
		}
		return null;
	}

	public List<Container> possibleContainers(MOB mob, List<String> commands, int wornFilter, boolean withContentOnly)
	{
		Vector<Container> V=new Vector<Container>();
		if(commands.size()==1)
			return V;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>0;i--)
			if(((String)commands.get(i)).equalsIgnoreCase("from"))
			{
				fromDex=i;
				containerDex=i+1;
				if(((containerDex+1)<commands.size())
				&&((((String)commands.get(containerDex)).equalsIgnoreCase("all"))
				||(CMath.s_int((String)commands.get(containerDex))>0)))
					containerDex++;
				break;
			}

		String possibleContainerID=CMParms.combine(commands,containerDex);

		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(containerDex>1)
			preWord=(String)commands.get(containerDex-1);

		int maxContained=Integer.MAX_VALUE;
		if(CMath.s_int(preWord)>0)
		{
			maxContained=CMath.s_int(preWord);
			commands.set(containerDex-1,"all");
			containerDex--;
			preWord="all";
		}

		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}

		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxContained)))
		{
			doBugFix=false;
			Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,wornFilter);
			if((E!=null)
			&&(E instanceof Item)
			&&(((Item)E) instanceof Container)
			&&((!withContentOnly)||(((Container)E).getContents().size()>0))
			&&(CMLib.flags().canBeSeenBy(E,mob)||mob.isMine(E)))
			{
				V.addElement((Container)E);
				if(V.size()==1)
				{
					while((fromDex>=0)&&(commands.size()>fromDex))
						commands.remove(fromDex);
					while(commands.size()>containerDex)
						commands.remove(containerDex);
					preWord="";
				}
			}
			if(E==null)
				return V;
			addendumStr="."+(++addendum);
		}
		return V;
	}

	public Item possibleContainer(MOB mob, List<String> commands, boolean withStuff, int wornFilter)
	{
		if(commands.size()==1)
			return null;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>=1;i--)
			if(((String)commands.get(i)).equalsIgnoreCase("from"))
			{ fromDex=i; containerDex=i+1;  break;}
		String possibleContainerID=CMParms.combine(commands,containerDex);

		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornFilter);
		if((E!=null)
		&&(E instanceof Item)
		&&(((Item)E) instanceof Container)
		&&((!withStuff)||(((Container)E).getContents().size()>0)))
		{
			while((fromDex>=0)&&(commands.size()>fromDex))
				commands.remove(fromDex);
			while(commands.size()>containerDex)
				commands.remove(containerDex);
			return (Item)E;
		}
		return null;
	}

	public String returnTime(long millis, long ticks)
	{
		String avg="";
		if(ticks>0)
			avg=", Average="+(millis/ticks)+"ms";
		if(millis<1000) return millis+"ms"+avg;
		long seconds=millis/1000;
		millis-=(seconds*1000);
		if(seconds<60) return seconds+"s "+millis+"ms"+avg;
		long minutes=seconds/60;
		seconds-=(minutes*60);
		if(minutes<60) return minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long hours=minutes/60;
		minutes-=(hours*60);
		if(hours<24) return hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long days=hours/24;
		hours-=(days*24);
		return days+"d "+hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
	}

	public Object[] parseMoneyStringSDL(MOB mob, String amount, String correctCurrency)
	{
		double b=0;
		String myCurrency=CMLib.beanCounter().getCurrency(mob);
		double denomination=1.0;
		if(correctCurrency==null) correctCurrency=myCurrency;
		if(amount.length()>0)
		{
			myCurrency=CMLib.english().numPossibleGoldCurrency(mob,amount);
			if(myCurrency!=null)
			{
				denomination=CMLib.english().numPossibleGoldDenomination(null,correctCurrency,amount);
				long num=CMLib.english().numPossibleGold(null,amount);
				b=CMath.mul(denomination,num);
			}
			else
				myCurrency=CMLib.beanCounter().getCurrency(mob);
		}
		return new Object[]{myCurrency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination))};
	}
	
	public int calculateMaxToGive(MOB mob, List<String> commands, boolean breakPackages, Environmental checkWhat, boolean getOnly)
	{
		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMLib.english().numPossibleGold(mob,CMParms.combine(commands,0))==0)
		&&(CMath.s_int((String)commands.get(0))>0))
		{
			maxToGive=CMath.s_int((String)commands.get(0));
			commands.set(0,"all");
			if(breakPackages)
			{
				boolean throwError=false;
				if((commands.size()>2)&&("FROM".startsWith(((String)commands.get(1)).toUpperCase())))
				{
					throwError=true;
					commands.remove(1);
				}
				String packCheckName=CMParms.combine(commands,1);
				Environmental fromWhat=null;
				if(checkWhat instanceof MOB)
					fromWhat=mob.findItem(null,packCheckName);
				else
				if(checkWhat instanceof Room)
					fromWhat=((Room)checkWhat).fetchFromMOBRoomFavorsItems(mob,null,packCheckName,Wearable.FILTER_UNWORNONLY);
				if(fromWhat instanceof Item)
				{
					int max=mob.maxCarry();
					if(max>3000) max=3000;
					if(maxToGive>max)
					{
						mob.tell("You can only handle "+max+" at a time.");
						return -1;
					}
					Environmental toWhat=CMLib.materials().unbundle((Item)fromWhat,maxToGive);
					if(toWhat==null)
					{
						if(throwError)
						{
							mob.tell("You can't get anything from "+fromWhat.name()+".");
							return -1;
						}
					}
					else
					if(getOnly&&mob.isMine(fromWhat)&&mob.isMine(toWhat))
					{
						mob.tell("Ok");
						return -1;
					}
					else
					if(commands.size()==1)
						commands.add(toWhat.name());
					else
					{
						String O=commands.get(0);
						commands.clear();
						commands.add(O);
						commands.add(toWhat.name());
					}
				}
				else
				if(throwError)
				{
					mob.tell("You don't see '"+packCheckName+"' here.");
					return -1;
				}
			}
		}
		return maxToGive;
	}
	
	protected class FetchFlags
	{
		public String srchStr;
		public int occurrance;
		public boolean allFlag;
		public FetchFlags(String ss, int oc, boolean af)
		{ srchStr=ss; occurrance=oc; allFlag=af;}
	}
	
	public FetchFlags fetchFlags(String srchStr)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equals("THE")))
		   return null;

		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equals("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf('.');
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=CMath.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf('.');
				sub=srchStr.substring(0,dot);
				occurrance=CMath.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		return new FetchFlags(srchStr,occurrance,allFlag);
	}

	protected String cleanExtraneousDollarMarkers(String srchStr)
	{
		if(srchStr.startsWith("$"))
		{
			if(srchStr.endsWith("$")&&(srchStr.length()>1))
				return srchStr.substring(1,srchStr.length()-1);
			else
				return srchStr.substring(1);
		}
		else
		if(srchStr.endsWith("$")) 
			return srchStr.substring(0,srchStr.length()-1);
		return srchStr;
	}
	
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if(E!=null)
						if(E.ID().equalsIgnoreCase(srchStr)
						   ||E.name().equalsIgnoreCase(srchStr)
						   ||E.Name().equalsIgnoreCase(srchStr))
							if((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0)))
								if((--myOccurrance)<=0)
									return E;
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if((E!=null)
					   &&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					   &&((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return E;
				}
				myOccurrance=flags.occurrance;
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if((E!=null)
					&&(!(E instanceof Ability))
					&&(containsString(E.displayText(),srchStr)
						||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
							if((--myOccurrance)<=0)
								return E;
				}
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}

	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		Vector<Environmental> matches=new Vector<Environmental>(1);
		if(list.size()==0) return matches;
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return matches;
		
		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if(E!=null)
						if(E.ID().equalsIgnoreCase(srchStr)
						   ||E.name().equalsIgnoreCase(srchStr)
						   ||E.Name().equalsIgnoreCase(srchStr))
							if((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0)))
								if((--myOccurrance)<=0)
									matches.addElement(E);
				}
			}
			else
			{
				myOccurrance=flags.occurrance;
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					   &&((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0))))
						if((--myOccurrance)<=0)
							matches.addElement(E);
				}
				if(matches.size()==0)
				{
					myOccurrance=flags.occurrance;
					for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
					{
						final Environmental E=e.next();
						if((E!=null)
						&&(!(E instanceof Ability))
						&&(containsString(E.displayText(),srchStr)
							||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr))))
								if((--myOccurrance)<=0)
									matches.addElement(E);
					}
				}
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return matches;
	}

	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly)
	{
		if(list.size()==0) return null;
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;

		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		Environmental E=null;
		if(exactOnly)
		{
			srchStr=cleanExtraneousDollarMarkers(srchStr);
			for(Iterator<String> k =list.keySet().iterator();k.hasNext();)
			{
				E=(Environmental)list.get(k.next());
				if(E!=null)
					if(E.ID().equalsIgnoreCase(srchStr)
					||E.Name().equalsIgnoreCase(srchStr)
					||E.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0)))
							if((--myOccurrance)<=0)
								return E;
			}
		}
		else
		{
			myOccurrance=flags.occurrance;
			for(Iterator<String> k =list.keySet().iterator();k.hasNext();)
			{
				E=(Environmental)list.get(k.next());
				if((E!=null)
				&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
				&&((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0))))
					if((--myOccurrance)<=0)
						return E;
			}
			myOccurrance=flags.occurrance;
			for(Iterator<String> k =list.keySet().iterator();k.hasNext();)
			{
				E=(Environmental)list.get(k.next());
				if(E!=null)
					if((containsString(E.displayText(),srchStr))
					||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr)))
						if((--myOccurrance)<=0)
							return E;
			}
		}
		return null;
	}

	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		if(list.size()==0) return null;
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;
		if(exactOnly)
		{
			try
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for(final Iterator<Item> i=list.iterator();i.hasNext();)
				{
					final Item I=i.next();
					boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);

					if((I!=null)
					&&(I.container()==goodLocation)
					&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
					&&(I.ID().equalsIgnoreCase(srchStr)
					   ||(I.Name().equalsIgnoreCase(srchStr))
					   ||(I.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							if((--myOccurrance)<=0)
								return I;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(final Iterator<Item> i=list.iterator();i.hasNext();)
				{
					final Item I=i.next();
					boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);

					if((I!=null)
					&&(I.container()==goodLocation)
					&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
					&&((containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
					   &&((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))))
						if((--myOccurrance)<=0)
							return I;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=flags.occurrance;
			try
			{
				for(final Iterator<Item> i=list.iterator();i.hasNext();)
				{
					final Item I=i.next();
					boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
					if((I!=null)
					&&(I.container()==goodLocation)
					&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
					&&(containsString(I.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return I;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		Vector<Item> matches=new Vector<Item>(1);
		if(list.size()==0) return matches;
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return matches;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for(final Iterator<Item> i=list.iterator();i.hasNext();)
				{
					final Item I=i.next();
					boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
					if((I!=null)
					&&(I.container()==goodLocation)
					&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
					&&(I.ID().equalsIgnoreCase(srchStr)
					   ||(I.Name().equalsIgnoreCase(srchStr))
					   ||(I.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))
							if((--myOccurrance)<=0)
								matches.addElement(I);
				}
			}
			else
			{
				for(final Iterator<Item> i=list.iterator();i.hasNext();)
				{
					final Item I=i.next();
					boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
					if((I!=null)
					&&(I.container()==goodLocation)
					&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
					&&((containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
					   &&((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))))
						if((--myOccurrance)<=0)
							matches.addElement(I);
				}
				if(matches.size()==0)
				{
					myOccurrance=flags.occurrance;
					for(final Iterator<Item> i=list.iterator();i.hasNext();)
					{
						final Item I=i.next();
						boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
						if((I!=null)
						&&(I.container()==goodLocation)
						&&((wornReqCode==Wearable.FILTER_ANY)||(beingWorn&&(wornReqCode==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornReqCode==Wearable.FILTER_UNWORNONLY)))
						&&(containsString(I.displayText(),srchStr)))
							if((--myOccurrance)<=0)
								matches.addElement(I);
					}
				}
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return matches;
	}

	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, int wornFilter, boolean exactOnly)
	{
		if(list.size()==0) return null;
		FetchFlags flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=flags.srchStr;
		int myOccurrance=flags.occurrance;
		boolean allFlag=flags.allFlag;

		Item I=null;
		try
		{
			if(exactOnly)
			{
				srchStr=cleanExtraneousDollarMarkers(srchStr);
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if(E instanceof Item)
					{
						I=(Item)E;
						boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
						if((I.container()==goodLocation)
						&&((wornFilter==Wearable.FILTER_ANY)||(beingWorn&&(wornFilter==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornFilter==Wearable.FILTER_UNWORNONLY)))
						&&(I.ID().equalsIgnoreCase(srchStr)
						   ||(I.Name().equalsIgnoreCase(srchStr))
						   ||(I.name().equalsIgnoreCase(srchStr))))
							if((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))
								if((--myOccurrance)<=0)
									return I;
					}
					else
					if(E!=null)
						if(E.ID().equalsIgnoreCase(srchStr)
						||E.Name().equalsIgnoreCase(srchStr)
						||E.name().equalsIgnoreCase(srchStr))
							if((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0)))
								if((--myOccurrance)<=0)
									return E;
				}
			}
			else
			{
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if(E instanceof Item)
					{
						I=(Item)E;
						boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);

						if((I.container()==goodLocation)
						&&((wornFilter==Wearable.FILTER_ANY)||(beingWorn&&(wornFilter==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornFilter==Wearable.FILTER_UNWORNONLY)))
						&&((containsString(I.name(),srchStr)||containsString(I.Name(),srchStr))
						   &&((!allFlag)||((I.displayText()!=null)&&(I.displayText().length()>0)))))
							if((--myOccurrance)<=0)
								return I;
					}
					else
					if((E!=null)
					&&(containsString(E.name(),srchStr)||containsString(E.Name(),srchStr))
					&&((!allFlag)||((E.displayText()!=null)&&(E.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return E;
				}

				myOccurrance=flags.occurrance;
				for(final Iterator<? extends Environmental> e=list.iterator();e.hasNext();)
				{
					final Environmental E=e.next();
					if(E instanceof Item)
					{
						I=(Item)E;
						boolean beingWorn=!I.amWearingAt(Wearable.IN_INVENTORY);
						if((I.container()==goodLocation)
						&&((wornFilter==Wearable.FILTER_ANY)||(beingWorn&&(wornFilter==Wearable.FILTER_WORNONLY))||((!beingWorn)&&(wornFilter==Wearable.FILTER_UNWORNONLY)))
						&&(containsString(I.displayText(),srchStr)))
							if((--myOccurrance)<=0)
								return I;
					}
					else
					if(E!=null)
						if((containsString(E.displayText(),srchStr))
						||((E instanceof MOB)&&containsString(((MOB)E).genericName(),srchStr)))
							if((--myOccurrance)<=0)
								return E;
				}
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
}
