package com.planet_ink.marble_mud.Commands;
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
import com.planet_ink.marble_mud.Libraries.interfaces.JournalsLibrary;
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
public class Shell extends StdCommand
{
	public Shell(){}

	private final String[] access={"SHELL","CMFS","."};
	public String[] getAccessWords(){return access;}
	
	protected static DVector pwds=new DVector(2);
	protected String[][] SUB_CMDS={
			{"$","DIRECTORY","LS"},
			{">","COPY","CP"},
			{".","CHANGEDIRECTORY","CD","GO"},
			{"-","DELETE","RM","RD"},
			{"\\","TYPE","CAT","TP"},
			{"+","MAKEDIRECTORY","MKDIR","MD"},
			{"*","FINDFILE","FF"},
			{"&","SEARCHTEXT","GREP","ST"},
			{"/","EDIT"},
			{"~","MOVE","MV"},
			//{"?","COMPAREFILES","DIFF","CF"},
	};
	
	protected final static String[] badTextExtensions={
		".ZIP",".JPE",".JPG",".GIF",".CLASS",".WAV",".BMP",".JPEG",".GZ",".TGZ",".JAR"
	};

	private class cp_options
	{
		boolean recurse=false;
		boolean forceOverwrites=false;
		boolean preservePaths=false;
		public cp_options(Vector cmds)
		{
			for(int c=cmds.size()-1;c>=0;c--)
			{
				String s=(String)cmds.elementAt(c);
				if(s.startsWith("-"))
				{
					for(int c2=1;c2<s.length();c2++)
					switch(s.charAt(c2))
					{
					case 'r':
					case 'R':
						recurse=true;
						break;
					case 'f':
					case 'F':
						forceOverwrites=true;
						break;
					case 'p':
					case 'P':
						preservePaths=true;
						break;
					}
					cmds.removeElementAt(c);
				}
			}
		}
	}
	
	private java.util.List<CMFile> sortDirsUp(CMFile[] files)
	{
		Vector<CMFile> dirs=new Vector<CMFile>();
		CMFile CF=null;
		Vector<CMFile> finalList=new Vector<CMFile>();
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.addElement(CF);
						break;
					}
					else
					if(((CMFile)dirs.elementAt(x)).getVFSPathAndName().length()<CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						break;
					}
				}
			}
			else
				finalList.add(CF);
				
		}
		finalList.addAll(dirs);
		return finalList;
	}
	
	private java.util.List<CMFile>  sortDirsDown(CMFile[] files)
	{
		Vector<CMFile> dirs=new Vector<CMFile>();
		HashSet<CMFile> dirsH=new HashSet<CMFile>();
		CMFile CF=null;
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.addElement(CF);
						dirsH.add(CF);
						break;
					}
					else
					if(((CMFile)dirs.elementAt(x)).getVFSPathAndName().length()>CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						dirsH.add(CF);
						break;
					}
				}
			}
		}
		for(CMFile F : files)
			if(!dirsH.contains(F))
				dirs.addElement(F);
		return dirs;
	}
	
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String pwd=(pwds.contains(mob))?(String)pwds.elementAt(pwds.indexOf(mob),2):"";
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("Current directory: /"+pwd);
			return false;
		}
		int cmd=-1;
		String first=((String)commands.firstElement()).toUpperCase();
		StringBuffer allcmds=new StringBuffer("");
		for(int i=0;i<SUB_CMDS.length;i++)
		{
			String shortcut=SUB_CMDS[i][0];
			if(first.startsWith(shortcut))
			{
				first=first.substring(shortcut.length()).trim();
				if(first.length()>0)
				{
					if(commands.size()>1)
						commands.setElementAt(first,1);
					else
						commands.addElement(first);
				}
				cmd=i;
				break;
			}
			for(int x=1;x<SUB_CMDS[i].length;x++)
			{
				if(SUB_CMDS[i][x].startsWith(first.toUpperCase()))
				{
					cmd=i;
					break;
				}
				if(x==1)
				{
					allcmds.append(SUB_CMDS[i][x]+" (");
					for(int x2=0;x2<SUB_CMDS[i].length;x2++)
						if(x2!=x)
						{
							allcmds.append(SUB_CMDS[i][x2]);
							if(x2<SUB_CMDS[i].length-1)allcmds.append("/");
						}
					allcmds.append("), ");
				}
			}
			if(cmd>=0) break;
		}
		switch(cmd)
		{
		case 0: // directory
		{
			cp_options opts=new cp_options(commands);
			CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell("^xError: invalid directory!^N");
				return false;
			}
			StringBuffer msg=new StringBuffer("\n\r^y .\n\r^y ..\n\r");
			for(int d=0;d<dirs.length;d++)
			{
				CMFile entry=dirs[d];
				if(entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^y"+CMStrings.padRight(entry.getName(),25));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
					msg.append("^w"+CMStrings.padRight(entry.author(),20));
					msg.append("\n\r");
				}
			}
			for(int d=0;d<dirs.length;d++)
			{
				CMFile entry=dirs[d];
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+CMStrings.padRight(entry.getName(),25));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
					msg.append("^w"+CMStrings.padRight(entry.author(),20));
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			break;
		}
		case 1: // copy
		{
			cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.addElement(".");
			if(commands.size()<3)
			{
				mob.tell("^xError  : source and destination must be specified!^N");
				mob.tell("^xOptions: -r = recurse into directories.^N");
				mob.tell("^x       : -p = preserve paths.^N");
				return false;
			}
			String source=(String)commands.elementAt(1);
			String target=CMParms.combine(commands,2);
			CMFile[] dirs=CMFile.getFileList(pwd,source,mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell("^xError: invalid source!^N");
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell("^xError: no source files matched^N");
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
				target=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
			CMFile DD=new CMFile(pwd,target,mob,false);
			java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			for(CMFile SF: ddirs)
			{
				if((SF==null)||(!SF.exists())){ mob.tell("^xError: source "+desc(SF)+" does not exist!^N"); return false;}
				if(!SF.canRead()){mob.tell("^xError: access denied to source "+desc(SF)+"!^N"); return false;}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell("^xError: source can not be a directory!^N"); 
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0) 
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob,false);
				}
				else
				if(dirs.length>1)
				{
					mob.tell("^xError: destination must be a directory!^N"); 
					return false;
				}
				if(DF.mustOverwrite()){ mob.tell("^xError: destination "+desc(DF)+" already exists!^N"); return false;}
				if(!DF.canWrite()){ mob.tell("^xError: access denied to destination "+desc(DF)+"!^N"); return false;}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if(!DF.mkdir())
						mob.tell("^xWarning: failed to mkdir "+desc(DF)+" ^N");
					else
						mob.tell(desc(SF)+" copied to "+desc(DF));
				}
				else
				{
					byte[] O=SF.raw();
					if(O.length==0){ mob.tell("^xWarning: "+desc(SF)+" file had no data^N");}
					if(!DF.saveRaw(O))
						mob.tell("^xWarning: write failed to "+desc(DF)+" ^N");
					else
						mob.tell(desc(SF)+" copied to "+desc(DF));
				}
			}
			break;
		}
		case 2: // cd
		{
			CMFile newDir=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
			String changeTo=newDir.getVFSPathAndName();
			if(!newDir.exists())
			{
				mob.tell("^xError: Directory '"+CMParms.combine(commands,1)+"' does not exist.^N");
				return false;
			}
			if((!newDir.canRead())||(!newDir.isDirectory()))
			{
				mob.tell("^xError: You are not authorized enter that directory.^N");
				return false;
			}
			pwd=changeTo;
			mob.tell("Directory is now: /"+pwd);
			pwds.removeElement(mob);
			pwds.addElement(mob,pwd);
			return true;
		}
		case 3: // delete
		{
			cp_options opts=new cp_options(commands);
			CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,opts.recurse,false);
			if(dirs==null)
			{
				mob.tell("^xError: invalid filename!^N");
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell("^xError: no files matched^N");
				return false;
			}
			java.util.List<CMFile> ddirs=sortDirsDown(dirs);
			for(int d=0;d<ddirs.size();d++)
			{
				CMFile CF=(CMFile)ddirs.get(d);
				if((CF==null)||(!CF.exists()))
				{
					mob.tell("^xError: "+desc(CF)+" does not exist!^N");
					return false;
				}
				if(!CF.canWrite())
				{
					mob.tell("^xError: access denied to "+desc(CF)+"!^N");
					return false;
				}
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell("^xError: delete of "+desc(CF)+" failed.^N");
					return false;
				}
				mob.tell(desc(CF)+" deleted.");
			}
			break;
		}
		case 4: // type
		{
			CMFile[] dirs=CMFile.getFileList(pwd,CMParms.combine(commands,1),mob,false,false);
			if(dirs==null)
			{
				mob.tell("^xError: invalid filename!^N");
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell("^xError: no files matched^N");
				return false;
			}
			for(int d=0;d<dirs.length;d++)
			{
				CMFile CF=dirs[d];
				if((CF==null)||(!CF.exists()))
				{
					mob.tell("^xError: file does not exist!^N");
					return false;
				}
				if(!CF.canRead())
				{
					mob.tell("^xError: access denied!^N");
					return false;
				}
				if(mob.session()!=null)
				{
					mob.session().colorOnlyPrintln("\n\r^xFile /"+CF.getVFSPathAndName()+"^.^N\n\r");
					mob.session().rawPrint(CF.text().toString());
				}
			}
			break;
		}
		case 5: // makedirectory
		{
			CMFile CF=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
			if(CF.exists())
			{
				mob.tell("^xError: file already exists!^N");
				return false;
			}
			if(!CF.canWrite())
			{
				mob.tell("^xError: access denied!^N");
				return false;
			}
			if(!CF.mkdir())
			{
				mob.tell("^xError: makedirectory failed.^N");
				return false;
			}
			mob.tell("Directory '/"+CF.getAbsolutePath()+"' created.");
			break;
		}
		case 6: // findfiles
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0) substring="*";
			CMFile[] dirs=CMFile.getFileList(pwd,substring,mob,true,true);
			StringBuffer msg=new StringBuffer("");
			if(dirs.length==0)
			{
				mob.tell("^xError: no files matched^N");
				return false;
			}
			for(int d=0;d<dirs.length;d++)
			{
				CMFile entry=dirs[d];
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+entry.getVFSPathAndName());
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case 7: // searchtext
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0)
			{
				mob.tell("^xError: you must specify a search string^N");
				return false;
			}
			CMFile[] dirs=CMFile.getFileList(pwd,"*",mob,true,true);
			if(dirs.length==0)
			{
				mob.tell("^xError: no files found!^N");
				return false;
			}
			mob.session().print("\n\rSearching...");
			substring=substring.toUpperCase();
			Vector dirs2=new Vector();
			for(int d=0;d<dirs.length;d++)
			{
				CMFile entry=dirs[d];
				if(!entry.isDirectory())
				{
					boolean proceed=true;
					for(int i=0;i<badTextExtensions.length;i++)
						if(entry.getName().toUpperCase().endsWith(badTextExtensions[i]))
						{ proceed=false; break;}
					if(proceed)
					{
						StringBuffer text=entry.textUnformatted();
						if(text.toString().toUpperCase().indexOf(substring)>=0)
							dirs2.addElement(entry);
					}
				}
			}
			if(dirs2.size()==0)
			{
				mob.tell("\n\r^xError: no files matched^N");
				return false;
			}
			StringBuffer msg=new StringBuffer("\n\r");
			for(int d=0;d<dirs2.size();d++)
			{
				CMFile entry=(CMFile)dirs2.elementAt(d);
				if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
					msg.append(" ");
				else
				if((entry.isLocalFile()&&(entry.canVFSEquiv()))
				||((entry.isVFSFile())&&(entry.canLocalEquiv())))
					msg.append("^R+");
				else
					msg.append("^r-");
				msg.append("^w"+entry.getVFSPathAndName());
				msg.append("\n\r");
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case 8: // edit
		{
			CMFile file=new CMFile(pwd,CMParms.combine(commands,1),mob,false);
			if((!file.canWrite())
			||(file.isDirectory()))
			{
				mob.tell("^xError: You are not authorized to create/modify that file.^N");
				return false;
			}
			StringBuffer buf=file.textUnformatted();
			String CR=Resources.getLineMarker(buf);
			List<String> vbuf=Resources.getFileLineVector(buf);
			buf=null;
			mob.tell(desc(file)+" has been loaded.\n\r\n\r");
			final String messageTitle="File: "+file.getVFSPathAndName();
			JournalsLibrary.MsgMkrResolution resolution=CMLib.journals().makeMessage(mob, messageTitle, vbuf, false);
			if(resolution==JournalsLibrary.MsgMkrResolution.SAVEFILE)
			{
				StringBuffer text=new StringBuffer("");
				for(int i=0;i<vbuf.size();i++)
					text.append(((String)vbuf.get(i))+CR);
				if(file.saveText(text))
				{
					for(final Iterator<String> i=Resources.findResourceKeys(file.getName());i.hasNext();)
						Resources.removeResource(i.next());
					mob.tell("File saved.");
				}
				else
					mob.tell("^XError: could not save the file!^N^.");
				return true;
			}
			if(resolution==JournalsLibrary.MsgMkrResolution.CANCELFILE)
				return true;
			return false;
		}
		case 9: // move
		{
			cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.addElement(".");
			if(commands.size()<3)
			{
				mob.tell("^xError  : source and destination must be specified!^N");
				mob.tell("^xOptions: -r = recurse into directories.^N");
				mob.tell("^x       : -f = force overwrites.^N");
				mob.tell("^x       : -p = preserve paths.^N");
				return false;
			}
			String source=(String)commands.elementAt(1);
			String target=CMParms.combine(commands,2);
			CMFile[] dirs=CMFile.getFileList(pwd,source,mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell("^xError: invalid source!^N");
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell("^xError: no source files matched^N");
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
				target=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
			CMFile DD=new CMFile(pwd,target,mob,false);
			java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			java.util.List<CMFile> dirsLater=new Vector<CMFile>();
			for(int d=0;d<ddirs.size();d++)
			{
				CMFile SF=(CMFile)ddirs.get(d);
				if((SF==null)||(!SF.exists())){ mob.tell("^xError: source "+desc(SF)+" does not exist!^N"); return false;}
				if(!SF.canRead()){mob.tell("^xError: access denied to source "+desc(SF)+"!^N"); return false;}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell("^xError: source can not be a directory!^N"); 
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0) 
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob,false);
				}
				else
				if(dirs.length>1)
				{
					mob.tell("^xError: destination must be a directory!^N"); 
					return false;
				}
				if(DF.mustOverwrite() && (!opts.forceOverwrites)){ mob.tell("^xError: destination "+desc(DF)+" already exists!^N"); return false;}
				if(!DF.canWrite()){ mob.tell("^xError: access denied to destination "+desc(DF)+"!^N"); return false;}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if((!DF.mustOverwrite())&&(!DF.mkdir()))
						mob.tell("^xWarning: failed to mkdir "+desc(DF)+" ^N");
					else
						mob.tell(desc(SF)+" copied to "+desc(DF));
					dirsLater.add(SF);
				}
				else
				{
					byte[] O=SF.raw();
					if(O.length==0){ mob.tell("^xWarning: "+desc(SF)+" file had no data^N");}
					if(!DF.saveRaw(O))
						mob.tell("^xWarning: write failed to "+desc(DF)+" ^N");
					else
						mob.tell(desc(SF)+" moved to "+desc(DF));
					if((!SF.delete())&&(SF.exists()))
					{
						mob.tell("^xError: Unable to delete file "+desc(SF));
						break;
					}
				}
			}
			dirsLater=sortDirsDown(dirsLater.toArray(new CMFile[0]));
			for(int d=0;d<dirsLater.size();d++)
			{
				CMFile CF=(CMFile)dirsLater.get(d);
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell("^xError: Unable to delete dir "+desc(CF));
					break;
				}
			}
			break;
		}
		case 10: // compare files
		{
			mob.tell("^xNot yet implemented.^N");
			return false;
		}
		default:
			mob.tell("'"+first+"' is an unknown command.  Valid commands are: "+allcmds.toString()+"and SHELL alone to check your current directory.");
			return false;
		}
		return true;
	}
	
	public String desc(CMFile CF){ return (CF.isLocalFile()?"Local file ":"VFS file ")+"'/"+CF.getVFSPathAndName()+"'";}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.hasAccessibleDir(mob,null);}
	
}
