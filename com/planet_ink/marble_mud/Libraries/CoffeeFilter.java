package com.planet_ink.marble_mud.Libraries;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.ColorLibrary.ColorState;
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
public class CoffeeFilter extends StdLibrary implements TelnetFilter
{
	public String ID(){return "CoffeeFilter";}
	public Hashtable<Object,Integer> tagTable=null;
	
	public Hashtable<Object, Integer> getTagTable()
	{
		if(tagTable==null) tagTable=CMStrings.makeNumericHash(TelnetFilter.FILTER_DESCS);
		return tagTable;
	}
	
	
	public String simpleOutFilter(String msg)
	{
		if(msg==null) return null;
		StringBuffer buf=new StringBuffer(msg);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case '`':
				buf.setCharAt(i,'\'');
				break;
			case '\\':
				if(i<buf.length()-1)
				{
					switch(buf.charAt(i+1))
					{
					case 'n':
					case 'r':
						{
						buf.setCharAt(i,(char)13);
						if((i>=buf.length()-2)||((i<buf.length()-2)&&((buf.charAt(i+2))!=10)))
							buf.setCharAt(i+1,(char)10);
						else
						if(i<buf.length()-2)
							buf.deleteCharAt(i+1);
						}
						break;
					case '\'':
					case '`':
						{
						buf.setCharAt(i,'\'');
						buf.deleteCharAt(i+1);
						}
						break;
					}
				}
				break;
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.OUTPUT))
			Log.debugOut("CoffeeFilter","OUTPUT: ?: "+buf.toString());
		return buf.toString();
	}
	
	public String[] wrapOnlyFilter(String msg, int wrap)
	{
		int loop=0;
		StringBuilder buf=new StringBuilder(msg);
		int len=(wrap>0)?wrap:Integer.MAX_VALUE;
		int lastSpace=0;
		int firstAlpha=-1;
		int amperStop = -1;

		while(buf.length()>loop)
		{
			int lastSp=-1;
			while((loop<len)&&(buf.length()>loop))
			{
				switch(buf.charAt(loop))
				{
				case ' ':
					{
						if(lastSp>lastSpace)
							lastSpace=lastSp;
						lastSp=loop;
					}
					break;
				case (char)13:
					{
						if(((loop<buf.length()-1)&&((buf.charAt(loop+1))!=10))
						&&((loop>0)&&((buf.charAt(loop-1))!=10)))
							buf.insert(loop+1,(char)10);
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case (char)10:
					{
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case '`': break;
				case '!':
					if((loop<buf.length()-10)
					&&(buf.charAt(loop+1)=='!')
					&&((buf.substring(loop+2,loop+7).equalsIgnoreCase("sound"))
					   ||(buf.substring(loop+2,loop+7).equalsIgnoreCase("music"))))
					{
						int x=buf.indexOf("(",loop+7);
						int y=buf.indexOf(")",loop+7);
						if((x>=0)&&(y>=x))
						{
							buf.delete(loop,y+1);
							loop--;
						}
					}
					break;
				case '>': break;
				case '"': break;
				case '&':
					if(loop < amperStop)
						break;
					else
					if(loop<buf.length()-3)
					{
						if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
							buf.replace(loop,loop+3,"<");
						else
						if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
							buf.replace(loop,loop+3,">");
					}
					break;
				case '%':
					if(loop<buf.length()-2)
					{
						int dig1=hexStr.indexOf(buf.charAt(loop+1));
						int dig2=hexStr.indexOf(buf.charAt(loop+2));
						if((dig1>=0)&&(dig2>=0))
						{
							buf.setCharAt(loop,(char)((dig1*16)+dig2));
							buf.deleteCharAt(loop+1);
							if((buf.charAt(loop))==13)
								buf.setCharAt(loop+1,(char)10);
							else
								buf.deleteCharAt(loop+1);
							loop--; // force a retry of this char
						}
					}
					break;
				case '(': break;
				case '\\':
					if(loop<buf.length()-1)
					{
						switch(buf.charAt(loop+1))
						{
						case 'n':
						case 'r':
							{
							buf.setCharAt(loop,(char)13);
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&((buf.charAt(loop+2))!=10)))
								buf.setCharAt(loop+1,(char)10);
							else
							if(loop<buf.length()-2)
								buf.deleteCharAt(loop+1);
							}
							break;
						case '\'':
						case '`':
							{
							buf.setCharAt(loop,'\'');
							buf.deleteCharAt(loop+1);
							}
							break;
						}
					}
					break;
				case '<': break;
				case '\033': // skip escapes
					if((loop < buf.length()-1) && (buf.charAt(loop+1)=='['))
					{
						char loopc=buf.charAt(loop);
						while( (loop < buf.length()-1) && (loopc!='m') && (loopc!='z') )
						{
							len++;
							loop++;
							loopc=buf.charAt(loop);
						}
						len++; // and one more for the 'm'.
					}
					break;
				case '^':
				{
					len++;
					loop++;
					if(loop<buf.length())
					{
						final char c=buf.charAt(loop);
						switch(c)
						{
						case ColorLibrary.COLORCODE_BACKGROUND:
							if(loop+1<buf.length())
							{
								len++;
								loop++;
							}
							break;
						case ColorLibrary.COLORCODE_FANSI256:
						case ColorLibrary.COLORCODE_BANSI256:
						  if(loop+3<buf.length())
						  {
							  len+=3;
							  loop+=3;
						  }
						  break;
						case '<': case '&':
						{
							len++;
							loop++;
							while(loop<(buf.length()-1))
							{
								if(((c=='<')&&((buf.charAt(loop)!='^')||(buf.charAt(loop+1)!='>')))
								||((c=='&')&&(buf.charAt(loop)!=';')))
								{
									len++;
									loop++;
								}
							}
							len++;
							break;
						}
						default: break;
						}
					}
					break;
				}
				default:
					if((firstAlpha < 0)&&(Character.isLetter(buf.charAt(loop))))
						firstAlpha = loop;
					break;
				}
				loop++;
			}

			if((len<buf.length())
			&&(loop!=lastSp)
			&&(lastSp>=0)
			&&(loop>=0)
			&&(loop<buf.length())
			&&(buf.charAt(loop)!=13)
			&&(buf.charAt(loop)!=10))
			{
				amperStop=loop;
				if(buf.charAt(lastSp+1)==' ')
				{
					buf.setCharAt(lastSp,(char)13);
					buf.setCharAt(lastSp+1,(char)10);
				}
				else
				{
					buf.setCharAt(lastSp,(char)13);
					buf.insert(lastSp,(char)10);
				}
				loop=lastSp+2;
			}
			if(wrap>0)len=loop+wrap;
		}

		if(firstAlpha<0) firstAlpha=0;
		if(firstAlpha<buf.length())
			buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		return buf.toString().split("\n\r");
	}

	protected int convertEscape(final Session S, final StringBuffer str, final int index) 
	{
		int enDex = index + 1;
		final char c = str.charAt(enDex);
		switch (c) 
		{
		case '?':
		{
			if((S!=null)&&(S.clientTelnetMode(Session.TELNET_ANSI)))
			{
				ColorState lastColor=S.lastColor(null);
				ColorState currColor=S.currentColor(null);
				if((lastColor.foregroundCode==currColor.foregroundCode)
				&&(lastColor.backgroundCode==currColor.backgroundCode))
					lastColor=ColorLibrary.COLORSTATE_NORMAL;
				final String[] clookup = S.clookup();
				String escapeSequence;
				if(lastColor.foregroundCode < 256)
					escapeSequence=clookup[lastColor.foregroundCode];
				else
					escapeSequence="\033[38;5;"+(lastColor.foregroundCode & 0xff)+"m";
				if(lastColor.backgroundCode=='.')
					escapeSequence=ColorLibrary.COLOR_NONE+escapeSequence;
				else
				{
					String bgEscapeSequence;
					if(lastColor.backgroundCode < 256)
					{
						bgEscapeSequence=clookup[lastColor.backgroundCode];
						if(bgEscapeSequence==null)
							bgEscapeSequence=ColorLibrary.COLOR_BGBLACK;
						else
						if(ColorLibrary.MAP_COLOR_TO_BGCOLOR.containsKey(bgEscapeSequence))
							bgEscapeSequence=ColorLibrary.MAP_COLOR_TO_BGCOLOR.get(bgEscapeSequence);
					}
					else
						bgEscapeSequence="\033[48;5;"+(lastColor.backgroundCode & 0xff)+"m";
					escapeSequence+=bgEscapeSequence;
				}
				S.lastColor(S.currentColor(null));
				S.currentColor(lastColor);
				str.insert(index+2, escapeSequence);
				str.delete(index, index+2);
				return index+escapeSequence.length()-1;
			}
			str.delete(index, index+2);
			return index-1;
		}
		case ColorLibrary.COLORCODE_FANSI256:
		case ColorLibrary.COLORCODE_BANSI256:
		{
			if(((S!=null)&&(!S.clientTelnetMode(Session.TELNET_ANSI)))
			||(enDex>str.length()-5))
			{
				str.delete(index, index+5);
				return index-1;
			}
			enDex++;
			int finalNum=-1;
			int num=str.charAt(enDex)-'0';
			if((num>=0)&&(num<=5))
			{
				int buildNum=(num*36);
				num=str.charAt(enDex+1)-'0';
				if((num>=0)&&(num<=5))
				{
					buildNum+=(num*6);
					num=str.charAt(enDex)-'0';
					if((num>=0)&&(num<=5))
					{
						finalNum=buildNum + num + 16;
					}
				}
			}
			if((finalNum < 0) && (str.charAt(enDex)==str.charAt(enDex-1)))
			{
				enDex++;
				num=CMath.hexDigit(str.charAt(enDex));
				int num2=CMath.hexDigit(str.charAt(enDex+1));
				if((num>=0)&&(num2>=0))
					finalNum=(num*16)+num2;
			}
			if(finalNum >=0)
			{
				final boolean isFg=(c==ColorLibrary.COLORCODE_FANSI256);
				String escapeSequence; 
				if(isFg)
				{
					escapeSequence="\033[38;5;"+finalNum+"m";
					if((S!=null)&&(S.currentColor(null).backgroundCode!='.'))
						escapeSequence=ColorLibrary.COLOR_NONE+escapeSequence;
				}
				else
					escapeSequence="\033[48;5;"+finalNum+"m";
				str.insert(index+5, escapeSequence);
				str.delete(index, index+5);
				if(S!=null)
				{
					if(isFg)
					{
						S.lastColor(S.currentColor(null));
						S.currentColor(ColorState.valueOf((char)(256 | finalNum), '.'));
					}
					else
					{
						S.currentColor(ColorState.valueOf(S.currentColor(null).foregroundCode, (char)(256 | finalNum)));
					}
				}
				return index+escapeSequence.length()-1;
			}
			str.delete(index, index+5);
			return index-1;
		}
		case ColorLibrary.COLORCODE_BACKGROUND:
		{
			enDex++;
			if((S!=null)&&(!S.clientTelnetMode(Session.TELNET_ANSI)))
			{
				str.delete(index, index+2);
				return index-1;
			}
			final char bc=str.charAt(enDex);
			final String[] clookup = (S==null)?CMLib.color().standardColorLookups():S.clookup();
			final String escapeSequence=clookup[bc];
			final String bgEscapeSequence=ColorLibrary.MAP_COLOR_TO_BGCOLOR.get(escapeSequence);
			if(bgEscapeSequence != null)
			{
				str.insert(index+3, bgEscapeSequence);
				str.delete(index, index+3);
				if(S!=null)
				{
					final ColorState curColor=S.currentColor(null);
					S.currentColor(ColorState.valueOf(curColor.foregroundCode, bc));
				}
				return index+bgEscapeSequence.length()-1;
			}
			else
			if(escapeSequence != null)
			{
				str.insert(index+3, escapeSequence);
				str.delete(index, index+3);
				return index+escapeSequence.length()-1;
			}
			else
			{
				str.delete(index, index+3);
				return index-1;
			}
		}
		case '>':
			/* why was this here?
			if (currentColor > 0) 
			{
				if (clookup()[c] == null)
					escapeCodes = clookup()[currentColor];
				else if (clookup()[currentColor] == null)
					escapeCodes = clookup[c];
				else
					escapeCodes = clookup()[c] + clookup()[currentColor];
			} 
			else
			*/
			str.delete(index, index+1);
			return index;
		case '<':
		{
			final boolean noMXP=((S==null)||(!S.clientTelnetMode(Session.TELNET_MXP)));
			while(enDex<(str.length()-1))
			{
				if((str.charAt(enDex)!='^')||(str.charAt(enDex+1)!='>'))
					enDex++;
				else
				if(noMXP)
				{
					str.delete(index,enDex+2);
					enDex=index-1;
					return enDex;
				}
				else
				{
					str.delete(enDex, enDex+1);
					str.delete(index, index+1);
					return enDex-1;
				}
			}
			str.delete(index, index+1);
			return index;
		}
		case '&':
		{
			  final boolean noMXP=((S==null)||(!S.clientTelnetMode(Session.TELNET_MXP)));
			while(enDex<(str.length()-1))
			{
				if(str.charAt(enDex)!=';')
					enDex++;
				else
				if(noMXP)
				{
					str.delete(index,enDex+1);
					enDex=index-1;
					return enDex;
				}
				else
				{
					str.delete(index, index+1);
					return enDex;
				}
			}
			str.delete(index, index+1);
			return index;
		}
		case '"':
			str.delete(index, index+1);
			return index;
		case '0': case '1': case '2': case '3': case '4': 
		case '5': case '6': case '7': case '8': case '9': 
			if((S==null)||(S.clientTelnetMode(Session.TELNET_MSP)))
			{
				final String escapeSequence=CMProps.getVar(CMProps.SYSTEM_ESC0 + (c - ('0')));
				str.insert(index+2, escapeSequence);
				str.delete(index, index+2);
				return index+escapeSequence.length()-1;
			}
			else
			{
				str.delete(index, index+2);
				return index-1;
			}
		case '.':
		  if((S==null)||(S.clientTelnetMode(Session.TELNET_ANSI)))
		  {
			  final String[] clookup = (S==null)?CMLib.color().standardColorLookups():S.clookup();
			  String escapeSequence=clookup[c];
			  if(escapeSequence==null) escapeSequence="";
			  str.insert(index+2, escapeSequence);
			  str.delete(index, index+2);
			  return index+escapeSequence.length()-1;
		  }
		  else
		  {
			  str.delete(index, index+2);
			  return index-1;
		  }
		default:
			if((c>=0)&&(c<256)&&((S==null)||(S.clientTelnetMode(Session.TELNET_ANSI))))
			{
				final String[] clookup = (S==null)?CMLib.color().standardColorLookups():S.clookup();
				String escapeSequence=clookup[c];
				if(escapeSequence==null) escapeSequence="";
				if((S!=null)&&(escapeSequence.length()>0)&&(escapeSequence.charAt(0)=='\033'))
				{
					final ColorState state=S.currentColor(null);
					if(state.backgroundCode!='.')
						escapeSequence=ColorLibrary.COLOR_NONE+escapeSequence;
					S.lastColor(state);
					S.currentColor(ColorState.valueOf(c,'.'));
				}
				str.insert(index+2, escapeSequence);
				str.delete(index, index+2);
				return index+escapeSequence.length()-1;
			}
			else
			{
				str.delete(index, index+2);
				return index-1;
			}
		}
	}

	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	// (it's not a member of the interface either so probably shouldn't be public)
	public String colorOnlyFilter(String msg, Session S)
	{
		if(msg==null) return null;

		if(msg.length()==0) return msg;
		StringBuffer buf=new StringBuffer(msg);
		Session CS=S;
		//if(CS==null) CS=(Session)CMClass.getCommon("DefaultSession");
		int loop=0;

		while(buf.length()>loop)
		{
			switch(buf.charAt(loop))
			{
			case '>':
				if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				{
					buf.delete(loop,loop+1);
					buf.insert(loop,"&gt;".toCharArray());
					loop+=3;
				}
				break;
			case '"':
				if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				{
					buf.delete(loop,loop+1);
					buf.insert(loop,"&quot;".toCharArray());
					loop+=5;
				}
				break;
			case '&':
				if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				{
					if((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
					&&(buf.substring(loop,loop+3).equalsIgnoreCase("gt;")))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&amp;".toCharArray());
						loop+=4;
					}
				}
				else
				if(loop<buf.length()-3)
				{
					if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
						buf.replace(loop,loop+3,"<");
					else
					if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
						buf.replace(loop,loop+3,">");
				}
				break;
				case '<':
					if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&lt;".toCharArray());
						loop+=3;
					}
					break;
				case '^':
				{
					loop=convertEscape(CS, buf, loop);
					break;
				}
			default:
				break;
			}
			loop++;
		}

		if ((S!=null)
		&&(!ColorLibrary.COLORSTATE_NORMAL.equals(S.currentColor(null)))
		&&(S.clientTelnetMode(Session.TELNET_ANSI)))
		{
			buf.append(S.clookup()['N']);
			S.lastColor(S.currentColor(null));
			S.currentColor(ColorLibrary.COLORSTATE_NORMAL);
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.OUTPUT))
			Log.debugOut("CoffeeFilter","OUTPUT: "+(((S!=null)&&(S.mob()!=null))?S.mob().Name():"")+": "+buf.toString());
		return buf.toString();
	}
	
	
	public String getLastWord(StringBuffer buf, int lastSp, int lastSpace)
	{
		String lastWord="";
		if(lastSp>lastSpace)
		{
			lastWord=CMStrings.removeColors(buf.substring(lastSpace,lastSp)).trim().toUpperCase();
			while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(0))))
				  lastWord=lastWord.substring(1);
			while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(lastWord.length()-1))))
				  lastWord=lastWord.substring(0,lastWord.length()-1);
			for(int i=lastWord.length()-1;i>=0;i--)
				if(!Character.isLetterOrDigit(lastWord.charAt(i)))
				{ lastWord=lastWord.substring(i+1); break;}
		}
		else
		{
			for(int i=(lastSpace-1);((i>=0)&&(!Character.isLetterOrDigit(buf.charAt(i))));i--)
				lastWord=buf.charAt(i)+lastWord;
			lastWord=CMStrings.removeColors(lastWord).trim().toUpperCase();
		}
		return lastWord;
	}
	
	public String fullOutFilter(Session S,
								MOB mob,
								Physical source,
								Environmental target,
								Environmental tool,
								String msg,
								boolean wrapOnly)
	{
		if(msg==null) return null;

		if(msg.length()==0) return msg;
		String newMsg=(S==null)?null:CMLib.lang().sessionTranslation(msg);
		if(newMsg!=null) msg=newMsg;
		
		boolean doSagain=false;
		boolean firstSdone=false;
		StringBuffer buf=new StringBuffer(msg);

		int wrap=(S!=null)?S.getWrap():78;
		int len=(wrap>0)?wrap:Integer.MAX_VALUE;
		int loop=0;
		int lastSpace=0;
		int firstAlpha=-1;
		int amperStop = -1;

		while(buf.length()>loop)
		{
			int lastSp=-1;
			while((loop<len)&&(buf.length()>loop))
			{
				switch(buf.charAt(loop))
				{
				case ' ':
					{
						if(lastSp>lastSpace)
							lastSpace=lastSp;
						lastSp=loop;
					}
					break;
				case (char)13:
					{
						if(((loop<buf.length()-1)&&((buf.charAt(loop+1))!=10))
						&&((loop>0)&&((buf.charAt(loop-1))!=10)))
							buf.insert(loop+1,(char)10);
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case (char)10:
					{
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case '`':
					buf.setCharAt(loop,'\'');
					break;
				case '!':
					if((loop<buf.length()-10)
					&&(buf.charAt(loop+1)=='!')
					&&((buf.substring(loop+2,loop+7).equalsIgnoreCase("sound"))
					   ||(buf.substring(loop+2,loop+7).equalsIgnoreCase("music"))))
					{
						int x=buf.indexOf("(",loop+7);
						int y=buf.indexOf(")",loop+7);
						if((x>=0)&&(y>=x))
						{
							if((S!=null)
							&&(S.clientTelnetMode(Session.TELNET_MSP))
							&&((source==null)
							   ||(source==mob)
							   ||(CMLib.flags().canBeHeardSpeakingBy(source,mob))))
							{
								if(wrap>0)
									len=len+(y-loop)+1;
								loop=y;
							}
							else
							{
								buf.delete(loop,y+1);
								loop--;
							}
						}
					}
					break;
				case '>':
					if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&gt;".toCharArray());
						loop+=3;
					}
					break;
				case '"':
					if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&quot;".toCharArray());
						loop+=5;
					}
					break;
				case '&':
					if(loop < amperStop)
						break;
					else
					if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
					{
						if((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
						&&(!buf.substring(loop,loop+3).equalsIgnoreCase("gt;")))
						{
							buf.delete(loop,loop+1);
							buf.insert(loop,"&amp;".toCharArray());
							loop+=4;
						}
						else
							loop+=3;
					}
					else
					if(loop<buf.length()-3)
					{
						if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
							buf.replace(loop,loop+3,"<");
						else
						if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
							buf.replace(loop,loop+3,">");
					}
					break;
				case '%':
					if(loop<buf.length()-2)
					{
						int dig1=hexStr.indexOf(buf.charAt(loop+1));
						int dig2=hexStr.indexOf(buf.charAt(loop+2));
						if((dig1>=0)&&(dig2>=0))
						{
							buf.setCharAt(loop,(char)((dig1*16)+dig2));
							buf.deleteCharAt(loop+1);
							if((buf.charAt(loop))==13)
								buf.setCharAt(loop+1,(char)10);
							else
								buf.deleteCharAt(loop+1);
							if(buf.charAt(loop)=='\033')
								loop--; // force a retry of this char
						}
					}
					break;
				case '(':
					if((!wrapOnly)&&(loop<(buf.length()-1)))
					{
						char c2=Character.toUpperCase(buf.charAt(loop+1));
						if(((loop<buf.length()-2)&&(buf.charAt(loop+2)==')')&&(c2=='S'))
						||((loop<buf.length()-3)&&(buf.charAt(loop+3)==')')&&(Character.toUpperCase(buf.charAt(loop+2))=='S')&&((c2=='Y')||(c2=='E'))))
						{
							String lastWord=getLastWord(buf,lastSp,lastSpace);
							int lastParen=(c2=='S')?loop+2:loop+3;
							if(lastWord.equals("A")
							||lastWord.equals("YOU")
							||lastWord.equals("1")
							||doSagain)
							{
								if(c2=='Y')
									buf.replace(loop,lastParen+1,CMStrings.sameCase("y",buf.charAt(loop+1)));
								else
									buf.delete(loop,lastParen+1);
								doSagain=true;
								loop--;
							}
							else
							{
								if(c2=='Y')
									buf.replace(loop,lastParen+1,CMStrings.sameCase("ies",buf.charAt(loop+1)));
								else
								{
									buf.deleteCharAt(lastParen);
									buf.deleteCharAt(loop);
								}
							}
							firstSdone=true;
						}
					}
					break;
				case '\\':
					if(loop<buf.length()-1)
					{
						switch(buf.charAt(loop+1))
						{
						case 'n':
						case 'r':
							{
							buf.setCharAt(loop,(char)13);
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&((buf.charAt(loop+2))!=10)))
								buf.setCharAt(loop+1,(char)10);
							else
							if(loop<buf.length()-2)
								buf.deleteCharAt(loop+1);
							}
							break;
						case '\'':
						case '`':
							{
							buf.setCharAt(loop,'\'');
							buf.deleteCharAt(loop+1);
							}
							break;
						}
					}
					break;
				case '<':
					if((!wrapOnly)&&((loop+1)<buf.length()))
					{
						// supported here <?-HIS-HER>, <?-HIM-HER>, <?-NAME>,
						// <?-NAMESELF>, <?-HE-SHE>, <?-IS-ARE>, <?-HAS-HAVE>
						//int endDex=loop;
						StringBuffer cmd=new StringBuffer("");
						int ldex=loop+1;
						char lc=' ';
						for(;(ldex<buf.length())&&(cmd!=null);ldex++)
						{
							lc=buf.charAt(ldex);
							if(lc=='>') 
								break;
							switch(lc)
							{
								case '<':
								case '\n':
								case '\r':
									cmd=null;
									break;
								default:
									cmd.append(Character.toUpperCase(lc));
									break;
							}
						}
						if((cmd!=null)&&(ldex<buf.length())&&(buf.charAt(ldex)=='>')&&(cmd.length()>1)&&(cmd.length()<14))
						{
							Environmental regarding=null;
							switch(cmd.charAt(0))
							{
							case 'S': regarding=source; break;
							case 'T': regarding=target; break;
							case 'O': regarding=tool; break;
							}
							String replacement=null;
							Integer I=(Integer)getTagTable().get(cmd.substring(1));
							if(I==null)
							{
								if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
								{
									buf.delete(loop,loop+1);
									buf.insert(loop,"&lt;".toCharArray());
								}
							}
							else
							switch(I.intValue())
							{
							case NAME:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
									{
										replacement="you";
										if(!firstSdone) doSagain=true;
									}
									else
									if((mob!=null)
									&&((!CMLib.flags().canSee(mob))||(!CMLib.flags().canBeSeenBy(regarding,mob)))
									&&(regarding.Name().trim().length()>0))
										replacement=((regarding instanceof MOB)?"someone":"something");
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).displayName(mob);
									else
										replacement=regarding.name();
								}
								break;
							case NAMENOART:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
									{
										replacement="you";
										if(!firstSdone) doSagain=true;
									}
									else
									if((mob!=null)
									&&((!CMLib.flags().canSee(mob))||(!CMLib.flags().canBeSeenBy(regarding,mob)))
									&&(regarding.Name().trim().length()>0))
										replacement=((regarding instanceof MOB)?"someone":"something");
									else
									if(regarding instanceof MOB)
										replacement=CMLib.english().cleanArticles(((MOB)regarding).displayName(mob));
									else
										replacement=CMLib.english().cleanArticles(regarding.name());
								}
							break;
							case NAMESELF:
								{
									if(regarding==null)
										replacement="";
									else
									if(((source==target)||(target==null))&&(mob==regarding))
										replacement="yourself";
									else
									if(mob==regarding)
									{
										replacement="you";
										if(!firstSdone) doSagain=true;
									}
									else
									if((mob!=null)
									&&((!CMLib.flags().canSee(mob))||(!CMLib.flags().canBeSeenBy(regarding,mob)))
									&&(regarding.Name().trim().length()>0))
										replacement=((regarding instanceof MOB)?"someone":"something");
									else
									if(source==target)
										replacement=((regarding instanceof MOB)?(((MOB)regarding).charStats().himher()+"self"):"itself");
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).displayName(mob);
									else
										replacement=regarding.name();
								}
								break;
							case YOUPOSS:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="your";
									else
									if((mob!=null)
									&&((!CMLib.flags().canSee(mob))||(!CMLib.flags().canBeSeenBy(regarding,mob)))
									&&(regarding.Name().trim().length()>0))
										replacement=((regarding instanceof MOB)?"someone's":"something's");
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).displayName(mob)+"'s";
									else
										replacement=regarding.name()+"'s";
								}
								break;
							case HISHER:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="your";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().hisher();
									else
										replacement="its";

								}
								break;
							case HIMHER:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
									{
										replacement="you";
										if(!firstSdone) doSagain=true;
									}
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().himher();
									else
										replacement="it";

								}
								break;
							case HIMHERSELF:
								{
									if(regarding==null)
										replacement="themself";
									else
									if(mob==regarding)
										replacement="yourself";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().himher()+"self";
									else
										replacement="itself";

								}
								break;
							case HISHERSELF:
								{
									if(regarding==null)
										replacement="themself";
									else
									if(mob==regarding)
										replacement="yourself";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().hisher()+"self";
									else
										replacement="itself";
								}
								break;
							case HESHE:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
									{
										replacement="you";
										if(!firstSdone) doSagain=true;
									}
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().heshe();
									else
										replacement="its";
								}
								break;
							case SIRMADAM:
								{
									if(regarding==null)
										replacement="";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().sirmadam();
									else
										replacement="sir";
								}
								break;
							case ISARE:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="are";
									else
									if(regarding instanceof MOB)
										replacement="is";
								}
								break;
							case ISARE2:
								{
									String lastWord=getLastWord(buf,lastSp,lastSpace);
									if((lastWord.equals("A")||lastWord.equals("YOU")||lastWord.equals("1")||doSagain))
										replacement="is";
									else
										replacement="are";
								}
								break;
							case HASHAVE:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="have";
									else
									if(regarding instanceof MOB)
										replacement="has";
								}
								break;
							}
							if(replacement!=null)
							{
								String newReplacement=CMLib.lang().filterTranslation(replacement);
								if(newReplacement!=null) replacement=newReplacement;
								buf.delete(loop,ldex+1);
								buf.insert(loop,replacement.toCharArray());
								loop--;
							}
						}
						else
						if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
						{
							buf.delete(loop,loop+1);
							buf.insert(loop,"&lt;".toCharArray());
							loop+=3;
						}
					}
					break;
					case '\033': // skip escapes
						if((loop < buf.length()-1) && (buf.charAt(loop+1)=='['))
						{
							char loopc=buf.charAt(loop);
							while( (loop < buf.length()-1) && (loopc!='m') && (loopc!='z') )
							{
								len++;
								loop++;
								loopc=buf.charAt(loop);
							}
							if(buf.charAt(loop)=='\033')
								loop--; // force a retry of this char
						}
						break;
					case '^':
					{
						final int oldLoop=loop;
						loop=convertEscape(S, buf, loop);
						if(wrap>0) len=(loop-oldLoop)+len+1;
						break;
					}
				default:
					if((firstAlpha < 0)&&(Character.isLetter(buf.charAt(loop))))
						firstAlpha = loop;
					break;
				}
				loop++;
			}


			if((len<buf.length())
			&&(loop!=lastSp)
			&&(lastSp>=0)
			&&(loop>=0)
			&&(loop<buf.length())
			&&(buf.charAt(loop)!=13)
			&&(buf.charAt(loop)!=10))
			{
				amperStop=loop;
				if(buf.charAt(lastSp+1)==' ')
				{
					buf.setCharAt(lastSp,(char)13);
					buf.setCharAt(lastSp+1,(char)10);
				}
				else
				{
					buf.setCharAt(lastSp,(char)13);
					buf.insert(lastSp,(char)10);
				}
				loop=lastSp+2;
			}
			if(wrap>0)len=loop+wrap;
		}

		if(firstAlpha<0) firstAlpha=0;
		if(firstAlpha<buf.length())
			buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		if((S!=null)
		&&(!ColorLibrary.COLORSTATE_NORMAL.equals(S.currentColor(null)))
		&&(S.clientTelnetMode(Session.TELNET_ANSI)))
		{
			buf.append(S.clookup()['N']);
			S.lastColor(S.currentColor(null));
			S.currentColor(ColorLibrary.COLORSTATE_NORMAL);
		}

		/* fabulous debug code
		for(int i=0;i<buf.length();i+=25)
		{
			for(int x=0;x<25;x++)
			{
				if((i+x)<buf.length())
				{
					char c=buf.charAt(i+x);
					if((c!='\r')&&(c!='\n'))
						System.out.print(c);
					else
						System.out.print("?");
				}
			}
			System.out.print(" ");
			for(int x=0;x<25;x++)
			{
				if((i+x)<buf.length())
				{
					int c=(int)buf.charAt(i+x);
					int a=c/16;
					int b=c%16;
					System.out.print(("0123456789ABCDEF").charAt(a));
					System.out.print(("0123456789ABCDEF").charAt(b));
				}
			}
			System.out.print(" \n");
		}
		//*/
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.OUTPUT))
			Log.debugOut("CoffeeFilter","OUTPUT: "+(((S!=null)&&(S.mob()!=null))?S.mob().Name():"")+": "+buf.toString());
		return buf.toString();
	}


	public StringBuffer simpleInFilter(StringBuffer input, boolean allowMXP)
	{
		if(input==null) return null;

		int x=0;
		while(x<input.length())
		{
			char c=input.charAt(x);
			if(c=='\'')
				input.setCharAt(x,'`');
			else
			if((c=='^')&&(x<(input.length()-1))&&(!allowMXP))
			{
				switch(input.charAt(x+1))
				{
				case '<':
				case '>':
				case '&':
					input.deleteCharAt(x);
					break;
				}
			}
			else
			if(c==8)
			{
				String newStr=input.toString();
				if(x==0)
					input=new StringBuffer(newStr.substring(x+1));
				else
				{
					input=new StringBuffer(newStr.substring(0,x-1)+newStr.substring(x+1));
					x--;
				}
				x--;
			}
			x++;
		}
		return new StringBuffer(input.toString());
	}

	public String fullInFilter(String input, boolean allowMXP)
	{
		if(input==null) return null;
		StringBuffer buf=new StringBuffer(input);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case (char)10:
				buf.setCharAt(i,'r');
				buf.insert(i,'\\');
				break;
			case (char)13:
				buf.setCharAt(i,'n');
				buf.insert(i,'\\');
				break;
			}
		}
		return simpleInFilter(buf,allowMXP).toString();
	}
	
	public String safetyFilter(String s)
	{
		StringBuffer s1=new StringBuffer(s);
		
		int x=-1;
		while((++x)<s1.length())
		{
			if(s1.charAt(x)=='\r')
			{
				s1.deleteCharAt(x);
				x--;
			}
			else
			if(s1.charAt(x)=='\n')
			{
				s1.setCharAt(x,'\\');
				s1.insert(x+1,'n');
				x++;
			}
			else
			if(s1.charAt(x)=='\'')
				s1.setCharAt(x,'`');
		}
		return s1.toString();
	}
}
