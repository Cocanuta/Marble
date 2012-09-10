package com.planet_ink.marble_mud.core;
import com.planet_ink.marble_mud.core.database.DBConnections;
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/*
Copyright 2012 Ben Cherrington

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
public class CMFile
{
	public static final int  VFS_MASK_MASKSAVABLE=1+2+4;

	//private static final int VFS_MASK_BINARY=1;
	public static final int  VFS_MASK_DIRECTORY=2;
	private static final int VFS_MASK_HIDDEN=4;
	private static final int VFS_MASK_ISLOCAL=8;
	private static final int VFS_MASK_NOWRITEVFS=16;
	private static final int VFS_MASK_NOWRITELOCAL=32;
	private static final int VFS_MASK_NOREADVFS=64;
	private static final int VFS_MASK_NOREADLOCAL=128;

	private static final char pathSeparator=File.separatorChar;
	
	private static final String inCharSet = Charset.defaultCharset().name();
	private static final String outCharSet = Charset.defaultCharset().name();
	
	private static CMVFSDir[] vfs=new CMVFSDir[256];
	private boolean logErrors=false;

	private int vfsBits=0;
	private String localPath=null;
	private String path=null;
	private String name=null;
	private String author=null;
	private MOB accessor=null;
	private long modifiedDateTime=System.currentTimeMillis();
	private File localFile=null;
	private boolean demandVFS=false;
	private boolean demandLocal=false;
	private String parentDir=null;

	public CMFile(final String filename, final MOB user, final boolean pleaseLogErrors)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,false);}
	public CMFile(final String filename, final MOB user, final boolean pleaseLogErrors, final boolean forceAllow)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,forceAllow);}
	public CMFile (final String currentPath, final String filename, final MOB user, final boolean pleaseLogErrors)
	{ super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,false); }
	public CMFile (final String currentPath, final String filename, final MOB user, final boolean pleaseLogErrors, final boolean forceAllow)
	{ super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,forceAllow); }

	public static class CMVFSFile 
	{
		public String fName;
		public String uName;
		public String path;
		public int mask;
		public long modifiedDateTime;
		public String author;
		public Object data = null;
		
		public CMVFSFile(final String path, final int mask, final long modifiedDateTime, final String author) 
		{
			this.path=path;
			this.fName=path;
			int x=path.lastIndexOf('/');
			if(x==path.length()-1) x=path.lastIndexOf('/',path.length()-2);
			if(x>=0) this.fName=path.substring(x+1);
			while(this.fName.startsWith("/")) this.fName=this.fName.substring(1);
			while(this.fName.endsWith("/")) this.fName=this.fName.substring(0,this.fName.length()-1);
			this.uName=this.fName.toUpperCase();
			this.mask=mask;
			this.modifiedDateTime=modifiedDateTime;
			this.author=author;
		}
		
		public void copyInto(final CMVFSFile f2)
		{
			f2.author=author;
			f2.data=data;
			f2.fName=fName;
			f2.mask=mask;
			f2.modifiedDateTime=modifiedDateTime;
			f2.path=path;
			f2.uName=uName;
			if((this instanceof CMVFSDir)&&(f2 instanceof CMVFSDir))
			{
				((CMVFSDir)f2).parent=((CMVFSDir)this).parent;
				if((((CMVFSDir)this).files!=null)
				&&(((CMVFSDir)this).files.length>0)
				&&(((CMVFSDir)f2).files==null))
					((CMVFSDir)f2).files=((CMVFSDir)this).files;
			}
		}
	}
	
	public static final class CMVFSDir extends CMVFSFile
	{
		private CMVFSFile[] files=null;
		public CMVFSDir parent=null;
		
		private static Comparator<CMVFSFile> fcomparator=new Comparator<CMVFSFile>() {
			public int compare(final CMVFSFile arg0, final CMVFSFile arg1) { return arg0.uName.compareTo(arg1.uName); }
		};
		
		public CMVFSDir(final CMVFSDir parent, final String path, final int mask, final long modifiedDateTime, final String author)
		{
			super(path,mask,modifiedDateTime,author);
			this.parent=parent;
		}

		public synchronized CMVFSDir fetchSubDir(final String path, final boolean create)
		{
			final String[] ppath=path.split("/");
			CMVFSDir currDir=this;
			for(final String p : ppath)
				if(p.length()>0)
				{
					synchronized(currDir)
					{
						final CMVFSDir key = new CMVFSDir(currDir,currDir.path+p+"/",VFS_MASK_DIRECTORY,System.currentTimeMillis(),"SYS");
						int dex = -1;
						if(currDir.files!=null)
							dex=Arrays.binarySearch(currDir.files, key, fcomparator);
						if(dex>=0)
						{
							if(currDir.files[dex] instanceof CMVFSDir)
							{
								currDir=(CMVFSDir)currDir.files[dex];
								continue;
							}
							return null; // found a step, but its a file, not a subdir
						}
						else
						if(!create)
							return null;
						if(currDir.files==null) currDir.files=new CMVFSFile[0];
						currDir.files=Arrays.copyOf(currDir.files, currDir.files.length+1);
						currDir.files[currDir.files.length-1]=key;
						Arrays.sort(currDir.files,fcomparator);
						currDir=key;
					}
				}
			return currDir;
		}

		public final boolean add(CMVFSFile f)
		{
			int x=f.path.lastIndexOf('/');
			if(x==f.path.length()-1)
				x=f.path.lastIndexOf('/',x-1);
			CMVFSDir subDir=this;
			if(x>0)
				subDir=fetchSubDir(f.path.substring(0,x),true);
			if(subDir!=null)
			synchronized(subDir)
			{
				if(subDir.files==null) subDir.files=new CMVFSFile[0];
				final CMVFSFile old = subDir.get(f.uName);
				if(old!=null)
					f.copyInto(old);
				else
				{
					subDir.files=Arrays.copyOf(subDir.files, subDir.files.length+1);
					if(CMath.bset(f.mask, CMFile.VFS_MASK_DIRECTORY))
					{
						CMVFSDir d = new CMVFSDir(subDir,f.path,f.mask,f.modifiedDateTime,f.author);
						f.copyInto(d);
						f=d;
					}
					subDir.files[subDir.files.length-1]=f;
				}
				Arrays.sort(subDir.files,fcomparator);
			}
			else
				return false;
			return true;
		}
		
		public final boolean delete(final CMVFSFile file)
		{
			final CMVFSDir dir = vfsV();
			if(dir==null) return false;
			int x=file.path.lastIndexOf('/');
			CMVFSDir subDir=dir;
			if(x>0)
				subDir=dir.fetchSubDir(file.path.substring(0,x),true);
			synchronized(subDir)
			{
				if((x==file.path.length()-1)&&(subDir.parent!=null))
				{
					if(subDir.parent.files!=null)
					{
						final List<CMVFSFile> list = new Vector<CMVFSFile>();
						list.addAll(Arrays.asList(subDir.parent.files));
						if(!list.remove(subDir))
							return false;
						subDir.parent.files = list.toArray(new CMVFSFile[0]);
						return true;
					}
					else
						return false;
				}
				else
				{
					final List<CMVFSFile> list = new Vector<CMVFSFile>();
					list.addAll(Arrays.asList(subDir.files));
					if(!list.remove(file))
						return false;
					subDir.files = list.toArray(new CMVFSFile[0]);
					return true;
				}
			}
		}
		
		private final synchronized CMVFSFile get(final String fileName)
		{
			if(files==null) return null;
			final CMVFSFile key = new CMVFSFile(fileName, 0, 0, "");
			int dex = Arrays.binarySearch(files, key, fcomparator);
			if(dex>=0) return files[dex];
			return null;
		}
		
		public final synchronized CMVFSFile fetch(final String filePath)
		{
			if(files==null) return null;
			final int x=filePath.lastIndexOf('/');
			CMVFSDir dir = this;
			if(x>=0)
				dir = this.fetchSubDir(filePath.substring(0,x), false);
			if(dir==null) return null;
			if(x==filePath.length()-1) return dir;
			String fileName=(x<0)?filePath:filePath.substring(x+1).trim();
			if(fileName.length()==0)
				return dir;
			return dir.get(fileName);
		}
	}
	
	private static final CMVFSDir vfsV() 
	{
		return vfs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}
	
	private final void buildCMFile(String absolutePath, final MOB user, final boolean pleaseLogErrors, final boolean forceAllow)
	{
		accessor=user;
		localFile=null;
		logErrors=pleaseLogErrors;
		demandLocal=absolutePath.trim().startsWith("//");
		demandVFS=absolutePath.trim().startsWith("::");
		if(accessor!=null) author=accessor.Name();
		absolutePath=vfsifyFilename(absolutePath);
		name=absolutePath;
		int x=absolutePath.lastIndexOf('/');
		path="";
		if(x>=0)
		{
			path=absolutePath.substring(0,x);
			name=absolutePath.substring(x+1);
		}
		parentDir=path;
		localPath=path.replace('/',pathSeparator);
		// fill in all we can
		vfsBits=0;
		CMVFSFile info=getVFSInfo(absolutePath);
		String ioPath=getIOReadableLocalPathAndName();
		localFile=new File(ioPath);
		if(!localFile.exists())
		{
			File localDir=new File(".");
			int endZ=-1;
			boolean found=true;
			if((localDir.exists())&&(localDir.isDirectory()))
				parentDir="//"+localPath;
			while((!localFile.exists())&&(endZ<ioPath.length())&&(localDir.exists())&&(localDir.isDirectory())&&(found))
			{
				int startZ=endZ+1;
				endZ=ioPath.indexOf(pathSeparator,startZ);
				if(endZ<0)
					endZ=ioPath.length();
				String[] files=localDir.list();
				found=false;
				for(int f=0;f<files.length;f++)
				{
					if(files[f].equalsIgnoreCase(ioPath.substring(startZ,endZ)))
					{
						if(!files[f].equals(ioPath.substring(startZ,endZ)))
							ioPath=ioPath.substring(0,startZ)+files[f]+((endZ<ioPath.length())?ioPath.substring(endZ):"");
						found=true;
						break;
					}
				}
				if(found)
				{
					if(endZ==ioPath.length())
					{
						int lastSep=ioPath.lastIndexOf(pathSeparator);
						if(lastSep>=0)
						{
							localPath=ioPath.substring(0,lastSep);
							name=ioPath.substring(lastSep+1);
						}
						else
							name=ioPath;
						localFile=new File(getIOReadableLocalPathAndName());
					}
					else
						localDir=new File(localDir.getAbsolutePath()+pathSeparator+ioPath.substring(startZ,endZ));
				}
			}
		}
		else
			parentDir="::"+parentDir;

		if((info!=null)&&((!demandLocal)||(!localFile.exists())))
		{
			vfsBits=vfsBits|info.mask;
			author=info.author;
			modifiedDateTime=info.modifiedDateTime;
		}
		else
		{
			modifiedDateTime=localFile.lastModified();
			if(localFile.isHidden()) vfsBits=vfsBits|CMFile.VFS_MASK_HIDDEN;
		}

		boolean isADirectory=((localFile!=null)&&(localFile.exists())&&(localFile.isDirectory()))
						   ||doesExistAsPathInVFS(absolutePath);
		boolean allowedToTraverseAsDirectory=isADirectory
										   &&((accessor==null)||CMSecurity.canTraverseDir(accessor,accessor.location(),absolutePath));
		boolean allowedToWriteVFS=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,true)));
		boolean allowedToReadVFS=(doesFilenameExistInVFS(absolutePath)&&allowedToWriteVFS)||allowedToTraverseAsDirectory;
		boolean allowedToWriteLocal=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,false)));
		boolean allowedToReadLocal=(localFile!=null)
									&&(localFile.exists())
									&&(allowedToWriteLocal||allowedToTraverseAsDirectory);

		if(!allowedToReadVFS) vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;

		if(!allowedToReadLocal) vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;

		if(!allowedToWriteVFS) vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;

		if(!allowedToWriteLocal) vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;

		if(allowedToTraverseAsDirectory)  vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;

		if((demandVFS)&&(!allowedToReadVFS))  vfsBits=vfsBits|CMFile.VFS_MASK_NOREADLOCAL;
		if((demandVFS)&&(!allowedToWriteVFS))  vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITELOCAL;

		if((demandLocal)&&(!allowedToReadLocal))  vfsBits=vfsBits|CMFile.VFS_MASK_NOREADVFS;
		if((demandLocal)&&(!allowedToWriteLocal))  vfsBits=vfsBits|CMFile.VFS_MASK_NOWRITEVFS;

		if((!demandVFS)
		&&(demandLocal||((!allowedToReadVFS) && (allowedToWriteLocal))))
			vfsBits=vfsBits|CMFile.VFS_MASK_ISLOCAL;
	}

	public final CMFile getParent(){return new CMFile(path,accessor,false,false);}

	public final boolean mustOverwrite()
	{
		if(!isDirectory())
			return canRead();
		if(isLocalFile())
			return ((localFile!=null)&&(localFile.isDirectory()));
		String filename=getVFSPathAndName();
		CMVFSFile info=getVFSInfo(filename);
		if(!filename.endsWith("/")) filename=filename+"/";
		if(info==null) info=getVFSInfo(filename);
		return (info!=null);
	}

	public final boolean canRead()
	{
		if(!exists()) return false;
		if(CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL))
		{
			if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL))
				return false;
		}
		else
		{
			if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS))
				return false;
		}
		return true;
	}
	public final boolean canWrite()
	{
		if(CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL))
		{
			if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOWRITELOCAL))
				return false;
		}
		else
		{
			if(CMath.bset(vfsBits,CMFile.VFS_MASK_NOWRITEVFS))
				return false;
		}
		return true;
	}

	public final boolean demandedVFS(){return demandVFS;}
	public final boolean demandedLocal(){return demandLocal;}
	public final boolean isDirectory(){return exists()&&CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY);}
	public final boolean exists(){ return !(CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS)&&CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL));}
	public final boolean isFile(){return canRead()&&(!CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY));}
	public final long lastModified(){return modifiedDateTime;}
	public final String author(){return ((author!=null))?author:"SYS_UNK";}
	public final boolean isLocalFile(){return CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL);}
	public final boolean isVFSFile(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_ISLOCAL));}
	public final boolean canVFSEquiv(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADVFS));}
	public final boolean canLocalEquiv(){return (!CMath.bset(vfsBits,CMFile.VFS_MASK_NOREADLOCAL));}
	public final String getName(){return name;}
	public final String getAbsolutePath(){return "/"+getVFSPathAndName();}
	public final String getCanonicalPath(){return getVFSPathAndName();}
	public final String getLocalPathAndName()
	{
		if(path.length()==0)
			return name;
		return localPath+pathSeparator+name;
	}
	public final String getIOReadableLocalPathAndName()
	{
		final String s=getLocalPathAndName();
		if(s.trim().length()==0) return ".";
		return s;
	}
	public final String getVFSPathAndName()
	{
		if(path.length()==0)
			return name;
		return path+'/'+name;
	}

	public final boolean mayDeleteIfDirectory()
	{
		if(!isDirectory()) return true;
		if((localFile!=null)&&(localFile.isDirectory())&&(localFile.list().length>0))
			return false;
		return getVFSDirectory().fetchSubDir(getVFSPathAndName().toUpperCase(), false)!=null;
	}

	public final boolean deleteLocal()
	{
		if(!exists()) return false;
		if(!canWrite()) return false;
		if(!mayDeleteIfDirectory()) return false;
		if((canLocalEquiv())&&(localFile!=null))
			return localFile.delete();
		return false;
	}
	public final boolean deleteVFS()
	{
		if(!exists()) return false;
		if(!canWrite()) return false;
		if(!mayDeleteIfDirectory()) return false;
		if(canVFSEquiv())
		{
			String name=getVFSPathAndName();
			if(isDirectory()) name=name+"/";
			final CMVFSFile info=getVFSInfo(name);
			if(info==null)
				return false;
			CMLib.database().DBDeleteVFSFile(info.path);
			getVFSDirectory().delete(info);
			return true;
		}
		return false;
	}

	public final boolean delete()
	{
		if(!exists()) return false;
		if(!canWrite()) return false;
		if(!mayDeleteIfDirectory()) return false;
		if((isVFSDirectory())&&(!demandLocal))
			return deleteVFS();
		if((isLocalDirectory())&&(!demandVFS))
			return deleteLocal();
		if(isVFSFile())
			return deleteVFS();
		if(isLocalFile())
			return deleteLocal();
		return false;
	}

	public final StringBuffer text()
	{
		final StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
			return buf;
		}
		if(isVFSFile())
		{
			final CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				final Object data=getVFSData(getVFSPathAndName());
				if(data==null) return buf;
				if(data instanceof String)
					return new StringBuffer((String)data);
				if(data instanceof StringBuffer)
					return (StringBuffer)data;
				if(data instanceof byte[])
					return new StringBuffer(CMStrings.bytesToStr((byte[])data));
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		BufferedReader reader = null;
		try
		{
			String charSet=CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=inCharSet;
			reader=new BufferedReader(
				   new InputStreamReader(
				   new FileInputStream(
					   getIOReadableLocalPathAndName()
			),charSet));
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
				{
					buf.append(line);
					buf.append("\n\r");
				}
			}
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
			return buf;
		}
		finally
		{
			try
			{
				if ( reader != null )
				{
					reader.close();
					reader = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public final StringBuffer textUnformatted()
	{
		final StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
			return buf;
		}
		if(isVFSFile())
		{
			final CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				final Object data=getVFSData(getVFSPathAndName());
				if(data==null) return buf;
				if(data instanceof String)
					return new StringBuffer((String)data);
				if(data instanceof StringBuffer)
					return (StringBuffer)data;
				if(data instanceof byte[])
					return new StringBuffer(CMStrings.bytesToStr((byte[])data));
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		Reader F = null;
		try
		{
			String charSet=CMProps.getVar(CMProps.SYSTEM_CHARSETINPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=inCharSet;
			F=new InputStreamReader(
			   new FileInputStream(
				   getIOReadableLocalPathAndName()
			 ),charSet);
			char c=' ';
			while(F.ready())
			{
				c=(char)F.read();
				if(c<0) break;
				buf.append(c);
			}
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			try
			{
				if ( F != null )
				{
					F.close();
					F = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public final byte[] raw()
	{
		byte[] buf=new byte[0];
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getVFSPathAndName()+"'.");
			return buf;
		}
		if(isVFSFile())
		{
			CMVFSFile info=getVFSInfo(getVFSPathAndName());
			if(info!=null)
			{
				Object data=getVFSData(getVFSPathAndName());
				if(data==null) return buf;
				if(data instanceof byte[])
					return (byte[])data;
				if(data instanceof String)
					return CMStrings.strToBytes((String)data);
				if(data instanceof StringBuffer)
					return CMStrings.strToBytes(((StringBuffer)data).toString());
			}
			else
			if(logErrors)
				Log.errOut("CMFile","VSF File '"+getVFSPathAndName()+"' not found.");
			return buf;
		}

		DataInputStream fileIn = null;
		try
		{
			fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(getIOReadableLocalPathAndName()) ) );
			buf = new byte [ fileIn.available() ];
			fileIn.readFully(buf);
			fileIn.close();
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			try
			{
				if ( fileIn != null )
				{
					fileIn.close();
					fileIn = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public final StringBuffer textVersion(byte[] bytes)
	{
		final StringBuffer text=new StringBuffer(CMStrings.bytesToStr(bytes));
		for(int i=0;i<text.length();i++)
			if((text.charAt(i)<0)||(text.charAt(i)>127))
				return null;
		return text;
	}

	public final boolean saveRaw(Object data)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getVFSPathAndName()+"': No Data.");
			return false;
		}
		if((CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY))
		||(!canWrite()))
		{
			Log.errOut("CMFile","Access error saving file '"+getVFSPathAndName()+"'.");
			return false;
		}

		Object O=null;
		if(data instanceof String)
			O=new StringBuffer((String)data);
		else
		if(data instanceof StringBuffer)
			O=(StringBuffer)data;
		else
		if(data instanceof byte[])
		{
			StringBuffer test=textVersion((byte[])data);
			if(test!=null)
				O=test;
			else
				O=(byte[])data;
		}
		else
			O=new StringBuffer(data.toString());
		if(!isLocalFile())
		{
			String filename=getVFSPathAndName();
			CMVFSFile info=getVFSInfo(filename);
			if(info!=null)
			{
				filename=info.path;
				getVFSDirectory().delete(info);
				CMLib.database().DBDeleteVFSFile(filename);
			}
			if(vfsBits<0) vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			info = new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author());
			getVFSDirectory().add(info);
			CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),O);
			return true;
		}

		FileOutputStream FW = null;
		try
		{
			File F=new File(getIOReadableLocalPathAndName());
			if(O instanceof StringBuffer)
				O=CMStrings.strToBytes(((StringBuffer)O).toString());
			if(O instanceof String)
				O=CMStrings.strToBytes(((String)O));
			if(O instanceof byte[])
			{
				FW=new FileOutputStream(F,false);
				FW.write((byte[])O);
				FW.flush();
				FW.close();
			}
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	public final boolean saveText(Object data){ return saveText(data,false);}
	public final boolean saveText(Object data, boolean append)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getVFSPathAndName()+"': No Data.");
			return false;
		}
		if((CMath.bset(vfsBits,CMFile.VFS_MASK_DIRECTORY))
		||(!canWrite()))
		{
			Log.errOut("CMFile","Access error saving file '"+getVFSPathAndName()+"'.");
			return false;
		}

		StringBuffer O=null;
		if(data instanceof String)
			O=new StringBuffer((String)data);
		else
		if(data instanceof StringBuffer)
			O=(StringBuffer)data;
		else
		if(data instanceof byte[])
			O=new StringBuffer(CMStrings.bytesToStr((byte[])data));
		else
			O=new StringBuffer(data.toString());
		if(!isLocalFile())
		{
			if(append) O=new StringBuffer(text().append(O).toString());
			String filename=getVFSPathAndName();
			CMVFSFile info=getVFSInfo(filename);
			if(info!=null)
			{
				filename=info.path;
				if(vfsV()!=null) vfsV().delete(info);
				CMLib.database().DBDeleteVFSFile(filename);
			}
			if(vfsBits<0) vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			info=new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author());
			vfsV().add(info);
			CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),O);
			return true;
		}

		Writer FW = null;
		try
		{
			String charSet=CMProps.getVar(CMProps.SYSTEM_CHARSETOUTPUT);
			if((charSet==null)||(charSet.length()==0))
				charSet=outCharSet;
			File F=new File(getIOReadableLocalPathAndName());
			FW=new OutputStreamWriter(new FileOutputStream(F,append),charSet);
			FW.write(saveBufNormalize(O).toString());
			FW.close();
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	public final boolean mkdir()
	{
		if(mustOverwrite())
		{
			Log.errOut("CMFile","File exists '"+getVFSPathAndName()+"'.");
			return false;
		}
		if(!canWrite())
		{
			Log.errOut("CMFile","Access error making directory '"+getVFSPathAndName()+"'.");
			return false;
		}
		if(!isLocalFile())
		{
			String filename=getVFSPathAndName();
			CMVFSFile info=getVFSInfo(filename);
			if(!filename.endsWith("/")) filename=filename+"/";
			if(info==null) info=getVFSInfo(filename);
			if(info!=null)
			{
				Log.errOut("CMFile","File exists '"+getVFSPathAndName()+"'.");
				return false;
			}
			if(vfsBits<0) vfsBits=0;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADVFS);
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOWRITEVFS);
			vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
			info=new CMVFSFile(filename,vfsBits&VFS_MASK_MASKSAVABLE,System.currentTimeMillis(),author());
			vfsV().add(info);
			CMLib.database().DBCreateVFSFile(filename,vfsBits,author(),new StringBuffer(""));
			return true;
		}
		String fullPath=getIOReadableLocalPathAndName();
		File F=new File(fullPath);
		File PF=F.getParentFile();
		Vector<File> parents=new Vector<File>();
		while(PF!=null)
		{
			parents.addElement(PF);
			PF=PF.getParentFile();
		}
		for(int p=parents.size()-1;p>=0;p--)
		{
			PF=(File)parents.elementAt(p);
			if((PF.exists())&&(PF.isDirectory())) continue;
			if((PF.exists()&&(!PF.isDirectory()))||(!PF.mkdir()))
			{
				Log.errOut("CMFile","Unable to mkdir '"+PF.getAbsolutePath()+"'.");
				return false;
			}
		}
		if(F.exists())
		{
			Log.errOut("CMFile","File exists '"+fullPath+"'.");
			return false;
		}
		if(F.mkdir())
		{
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOREADLOCAL);
			vfsBits=vfsBits|CMFile.VFS_MASK_DIRECTORY;
			vfsBits=CMath.unsetb(vfsBits,CMFile.VFS_MASK_NOWRITELOCAL);
			return true;
		}
		return false;
	}

	public final String[] list()
	{
		if(!isDirectory()) return new String[0];
		final CMFile[] CF=listFiles();
		final String[] list=new String[CF.length];
		for(int f=0;f<CF.length;f++)
			list[f]=CF[f].getName();
		return list;
	}

	private static final CMVFSFile getVFSInfo(final String filename)
	{
		final CMVFSDir vfs=getVFSDirectory();
		if(vfs==null) return null;
		return vfs.fetch(vfsifyFilename(filename));
	}

	private static final Object getVFSData(final String filename)
	{
		final CMVFSFile file=getVFSInfo(filename);
		if(file!=null)
		{
			final CMVFSFile f=CMLib.database().DBReadVFSFile(file.path);
			if((f!=null)&&(f.data != null))
				return f.data;
		}
		return null;
	}

	private static final boolean doesFilenameExistInVFS(final String filename)
	{
		return getVFSInfo(filename)!=null;
	}

	private static boolean doesExistAsPathInVFS(String filename)
	{
		CMVFSFile file=getVFSInfo(filename);
		if(file==null) return false;
		if((file instanceof CMVFSDir)||(CMath.bset(file.mask, CMFile.VFS_MASK_DIRECTORY)))
			return true;
		return false;
	}

	public final boolean isVFSDirectory()
	{
		String dir=getVFSPathAndName().toLowerCase();
		if(!dir.endsWith("/")) dir+="/";
		final CMVFSFile file=getVFSInfo(dir);
		return (file instanceof CMVFSDir);
	}
	
	public final boolean isLocalDirectory()
	{
		return (localFile!=null)&&(localFile.isDirectory());
	}

	public final CMFile[] listFiles()
	{
		if((!isDirectory())||(!canRead()))
			return new CMFile[0];
		final String prefix=demandLocal?"//":(demandVFS?"::":"");
		final Vector<CMFile> dir=new Vector<CMFile>();
		final Vector<String> fcheck=new Vector<String>();
		String thisDir=getVFSPathAndName();
		CMVFSDir vfs=getVFSDirectory();
		String vfsSrchDir=thisDir+"/";
		if(thisDir.length()==0) vfsSrchDir="";
		if(!demandLocal)
		{
			if(vfsSrchDir.length()>0)
				vfs=vfs.fetchSubDir(vfsSrchDir, false);
			if((vfs!=null)&&(vfs.files!=null)&&(vfs.files.length>0))
				for(CMVFSFile file : vfs.files)
				{
					CMFile CF=new CMFile(prefix+file.path,accessor,false);
					if((CF.canRead())
					&&(!fcheck.contains(file.uName)))
					{
						fcheck.addElement(file.uName);
						dir.addElement(CF);
					}
				}
		}

		if(!demandVFS)
		{
			thisDir=getIOReadableLocalPathAndName();
			final File F=new File(thisDir);
			if(F.isDirectory())
			{
				final File[] list=F.listFiles();
				File F2=null;
				for(int l=0;l<list.length;l++)
				{
					F2=list[l];
					String thisPath=vfsifyFilename(thisDir)+"/"+F2.getName();
					String thisName=F2.getName();
					CMFile CF=new CMFile(prefix+thisPath,accessor,false);
					if((CF.canRead())
					&&(!fcheck.contains(thisName.toUpperCase())))
						dir.addElement(CF);
				}
			}
		}
		final CMFile[] finalDir=new CMFile[dir.size()];
		for(int f=0;f<dir.size();f++)
			finalDir[f]=(CMFile)dir.elementAt(f);
		return finalDir;
	}

	public static final CMVFSDir getVFSDirectory()
	{
		CMVFSDir vvfs=vfsV();
		if(vvfs==null)
		{
			if(CMLib.database()==null) return new CMVFSDir(null,"",CMFile.VFS_MASK_DIRECTORY,System.currentTimeMillis(),"SYS");
			char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
			if(threadCode==MudHost.MAIN_HOST)
				vvfs=vfs[threadCode]=CMLib.database().DBReadVFSDirectory();
			else
			{
				Vector<String> privateV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true);
				if(privateV.contains("DBVFS"))
					vvfs=vfs[threadCode]=CMLib.database().DBReadVFSDirectory();
				else
				if(vfs[MudHost.MAIN_HOST]!=null)
				{
					vfs[threadCode]=vfs[MudHost.MAIN_HOST];
					return vfs[threadCode];
				}
				else
					vvfs=vfs[threadCode]=vfs[MudHost.MAIN_HOST]=CMLib.database().DBReadVFSDirectory();
			}
		}
		return vvfs;
	}

	public static final String vfsifyFilename(String filename)
	{
		filename=filename.trim();
		if(filename.startsWith("::"))
			filename=filename.substring(2);
		if(filename.startsWith("//"))
			filename=filename.substring(2);
		if((filename.length()>3)
		&&(Character.isLetter(filename.charAt(0))
		&&(filename.charAt(1)==':')))
			filename=filename.substring(2);
		while(filename.startsWith("/")) filename=filename.substring(1);
		while(filename.startsWith("\\")) filename=filename.substring(1);
		while(filename.endsWith("/"))
			filename=filename.substring(0,filename.length()-1);
		while(filename.endsWith("\\"))
			filename=filename.substring(0,filename.length()-1);
		return filename.replace(pathSeparator,'/');
	}

	private final StringBuffer saveBufNormalize(final StringBuffer myRsc)
	{
		for(int i=0;i<myRsc.length();i++)
			if(myRsc.charAt(i)=='\n')
			{
				for(i=myRsc.length()-1;i>=0;i--)
					if(myRsc.charAt(i)=='\r')
						myRsc.deleteCharAt(i);
				return myRsc;
			}
		for(int i=0;i<myRsc.length();i++)
			if(myRsc.charAt(i)=='\r')
				myRsc.setCharAt(i,'\n');
		return myRsc;
	}

	private static final String incorporateBaseDir(String currentPath, String filename)
	{
		String starter="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			starter=filename.substring(0,2);
			filename=filename.substring(2);
		}
		if(!filename.startsWith("/"))
		{
			boolean didSomething=true;
			while(didSomething)
			{
				didSomething=false;
				if(filename.startsWith(".."))
				{
					filename=filename.substring(2);
					int x=currentPath.lastIndexOf('/');
					if(x>=0)
						currentPath=currentPath.substring(0,x);
					else
						currentPath="";
					didSomething=true;
				}
				if((filename.startsWith("."))&&(!(filename.startsWith(".."))))
				{
					filename=filename.substring(1);
					didSomething=true;
				}
				while(filename.startsWith("/")) filename=filename.substring(1);
			}
			if((currentPath.length()>0)&&(filename.length()>0))
				filename=currentPath+"/"+filename;
			else
			if(currentPath.length()>0)
				filename=currentPath;
		}
		return starter+filename;
	}

	public static final CMFile[] getFileList(final String currentPath, final String filename, final MOB user, final boolean recurse, final boolean expandDirs)
	{ return getFileList(incorporateBaseDir(currentPath,filename),user,recurse,expandDirs);}
	public static final CMFile[] getFileList(final String parse, final MOB user, final boolean recurse, final boolean expandDirs)
	{
		final boolean demandLocal=parse.trim().startsWith("//");
		final boolean demandVFS=parse.trim().startsWith("::");
		CMFile dirTest=new CMFile(parse,user,false);
		if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
		{ return expandDirs?dirTest.listFiles():new CMFile[]{dirTest};}
		final String vsPath=vfsifyFilename(parse);
		String fixedName=vsPath;
		int x=vsPath.lastIndexOf('/');
		String fixedPath="";
		if(x>=0)
		{
			fixedPath=vsPath.substring(0,x);
			fixedName=vsPath.substring(x+1);
		}
		final CMFile dir=new CMFile((demandLocal?"//":demandVFS?"::":"")+fixedPath,user,false);
		if((!dir.exists())||(!dir.isDirectory())||(!dir.canRead()))
			return null;
		final Vector<CMFile> set=new Vector<CMFile>();
		CMFile[] cset=dir.listFiles();
		fixedName=fixedName.toUpperCase();
		for(int c=0;c<cset.length;c++)
		{
			if((recurse)&&(cset[c].isDirectory())&&(cset[c].canRead()))
			{
				final CMFile[] CF2=getFileList(cset[c].getVFSPathAndName()+"/"+fixedName,user,true,expandDirs);
				for(int cf2=0;cf2<CF2.length;cf2++)
					set.addElement(CF2[cf2]);
			}
			final String name=cset[c].getName().toUpperCase();
			boolean ismatch=true;
			if((!name.equalsIgnoreCase(fixedName))
				&&(fixedName.length()>0))
			for(int f=0,n=0;f<fixedName.length();f++,n++)
				if(fixedName.charAt(f)=='?')
				{
					if(n>=name.length()){ ismatch=false; break; }
				}
				else
				if(fixedName.charAt(f)=='*')
				{
					if(f==fixedName.length()-1) break;
					int endOfMatchStr=fixedName.indexOf('*',f+1);
					if(endOfMatchStr<0) endOfMatchStr=fixedName.indexOf('?',f+1);
					int mbEnd = fixedName.length();
					if(endOfMatchStr>f)
						mbEnd = endOfMatchStr;
					String matchBuf = fixedName.substring(f+1,mbEnd);
					int found = name.indexOf(matchBuf,n);
					if(found < 0)
					{
						ismatch=false;
						break;
					}
					else
					{
						n=found + matchBuf.length() - 1;
						f+=matchBuf.length();
					}
				}
				else
				if((n>=name.length())
				||(fixedName.charAt(f)!=name.charAt(n))
				||((f==fixedName.length()-1)&&(n<name.length()-1)))
				{ 
					ismatch=false; 
					break; 
				}
			if(ismatch) set.addElement(cset[c]);
		}
		if(set.size()==1)
		{
			dirTest=(CMFile)set.firstElement();
			if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
			{ return expandDirs?dirTest.listFiles():new CMFile[]{dirTest};}
		}
		cset=new CMFile[set.size()];
		for(int s=0;s<set.size();s++)
			cset[s]=(CMFile)set.elementAt(s);
		return cset;
	}
}
