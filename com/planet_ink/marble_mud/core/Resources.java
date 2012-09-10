package com.planet_ink.marble_mud.core;
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
public class Resources
{
	private static final Resources[] rscs=new Resources[256];
	private static boolean 	 compress=false;
	
	private final Map<String,Object> resources=new STreeMap<String,Object>(new Comparator<String>(){
		public int compare(String o1, String o2) {
			if(o1==null)
			{
				if(o2==null) return 0;
				return -1;
			}
			else
			if(o2==null)
				return 1;
			return o1.compareToIgnoreCase(o2);
		}
	});
	
	private static class CompressedResource
	{
		public byte[] data;
		public CompressedResource(byte[] d) { data=d;}
	}
	
	public Resources()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(rscs[c]==null) rscs[c]=this;
	}
	public static final Resources instance()
	{
		final Resources r=r();
		if(r==null) return new Resources();
		return r;
	}
	public static final Resources instance(final char c){ return rscs[c];}
	private static final Resources r(){ return rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	
	public static final Resources newResources(){ return new Resources();}

	public static final void clearResources(){r()._clearResources();}
	public static final void removeResource(final String ID){ r()._removeResource(ID);}
	public static final Iterator<String> findResourceKeys(final String srch){return r()._findResourceKeys(srch);}
	public static final Object getResource(final String ID){return r()._getResource(ID);}
	public static final void submitResource(final String ID, final Object obj){r()._submitResource(ID,obj);}
	public static final boolean isFileResource(final String filename){return r()._isFileResource(filename);}
	public static final StringBuffer getFileResource(final String filename, final boolean reportErrors){return r()._getFileResource(filename,reportErrors);}
	public static final boolean saveFileResource(final String filename, final MOB whom, final StringBuffer myRsc){return r()._saveFileResource(filename,whom,myRsc);}
	public static final boolean updateFileResource(final String filename, final Object obj){return r()._updateFileResource(filename,obj);}
	public static final boolean findRemoveProperty(final CMFile F, final String match){return r()._findRemoveProperty(F,match);}

	public static final String getLineMarker(final StringBuffer buf)
	{
		for(int i=0;i<buf.length()-1;i++)
			switch(buf.charAt(i))
			{
			case '\n':
				if(buf.charAt(i+1)=='\r')
					return "\n\r";
				return "\n";
			case '\r':
				if(buf.charAt(i+1)=='\n')
					return "\r\n";
				return "\r";
			}
		return "\n\r";
	}
	
	public static final List<String> getFileLineVector(final StringBuffer buf)
	{
		final Vector<String> V=new Vector<String>();
		if(buf==null) return V;
		final StringBuffer str=new StringBuffer("");
		for(int i=0;i<buf.length();i++)
		{
			if(((buf.charAt(i)=='\n')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\r'))
			   ||((buf.charAt(i)=='\r')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\n')))
			{
				i++;
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
			if(((buf.charAt(i)=='\r'))
			||((buf.charAt(i)=='\n')))
			{
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
				str.append(buf.charAt(i));
		}
		if(str.length()>0)
			V.addElement(str.toString());
		V.trimToSize();
		return V;
	}

	public static final String buildResourcePath(final String path)
	{
		if((path==null)||(path.length()==0)) return "resources/";
		return "resources/"+path+"/";
	}

	public static final void updateMultiList(String filename, final Map<String, List<String>> lists)
	{
		final StringBuffer str=new StringBuffer("");
		for(String ml : lists.keySet())
		{
			final List<String> V=lists.get(ml);
			str.append(ml+"\r\n");
			if(V!=null)
			for(int v=0;v<V.size();v++)
				str.append(((String)V.get(v))+"\r\n");
			str.append("\r\n");
		}
		String prefix="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			prefix=filename.substring(0,2);
			filename=filename.substring(2);
		}
		new CMFile(prefix+buildResourcePath(filename),null,false).saveText(str);
	}

	public static final boolean removeMultiLists(final String filename)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		removeResource(key);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static final Map<String, List<String>> getCachedMultiLists(final String filename, boolean createIfNot)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null)
		{
			H=Resources.getMultiLists(filename);
			if((H==null) && (createIfNot))
				H=new Hashtable<String,List<String>>();
			if(H!=null)
				Resources.submitResource(key,H);
		}
		return H;
	}
	
	@SuppressWarnings("unchecked")
	public static final boolean updateCachedMultiLists(final String filename)
	{
		final String key;
		if(filename.startsWith("::")||filename.startsWith("//"))
			key = "PARSED_MULTI: "+filename.substring(2).toUpperCase();
		else
			key = "PARSED_MULTI: "+filename.toUpperCase();
		Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null) return false;
		updateMultiList(filename, H);
		return true;
	}
	
	public static final Map<String, List<String>> getMultiLists(String filename)
	{
		final Hashtable<String,List<String>> oldH=new Hashtable<String,List<String>>();
		List<String> V=new Vector<String>();
		try{
			String prefix="";
			if(filename.startsWith("::")||filename.startsWith("//"))
			{
				prefix=filename.substring(0,2);
				filename=filename.substring(2);
			}
			V=getFileLineVector(new CMFile(prefix+"resources/"+filename,null,false).text());
		}catch(Exception e){}
		if((V!=null)&&(V.size()>0))
		{
			String journal="";
			List<String> set=new Vector<String>();
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.get(v);
				if(s.trim().length()==0)
					journal="";
				else
				if(journal.length()==0)
				{
					journal=s;
					set=new Vector<String>();
					oldH.put(journal,set);
				}
				else
					set.add(s);
			}
		}
		return oldH;
	}

	public static final String makeFileResourceName(final String filename)
	{
		return "resources/"+filename;
	}

	public static final void setCompression(final boolean truefalse)
	{   compress=truefalse;}

	public static final boolean _compressed(){return compress;}

	public final void _clearResources()
	{
		resources.clear();
	}

	public final Iterator<String> _findResourceKeys(final String srch)
	{
		final String lowerSrch=srch.toLowerCase();
		final boolean allOfThem=(lowerSrch.length()==0);
		return new FilteredIterator<String>(resources.keySet().iterator(), new Filterer<String>(){
			public boolean passesFilter(String obj) {
				return (allOfThem) || (obj.toLowerCase().indexOf(lowerSrch)>=0);
			}
		});
	}

	public final Object _getResource(final String ID)
	{
		final Object O = resources.get(ID);
		if(O!=null)
		{
			if(compress && (O instanceof CompressedResource))
				return new StringBuffer(CMLib.encoder().decompressString(((CompressedResource)O).data));
			return O;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static final Object prepareObject(final Object obj)
	{
		if(obj instanceof Vector) ((Vector)obj).trimToSize();
		if(obj instanceof DVector) ((DVector)obj).trimToSize();
		if(!compress) return obj;
		if(obj instanceof StringBuffer)
			return CMLib.encoder().compressString(((StringBuffer)obj).toString());
		return obj;
	}

	public final Object _submitResource(final String ID, final Object obj)
	{
		final Object prepared=prepareObject(obj);
		if(prepared != obj)
			resources.put(ID,new CompressedResource((byte[])prepared));
		else
			resources.put(ID,prepared);
		return prepared;
	}

	private final Object _updateResource(final String ID, final Object obj)
	{
		return _submitResource(ID, obj);
	}

	public final void _removeResource(final String ID)
	{
		resources.remove(ID);
	}

	public final boolean _isFileResource(final String filename)
	{
		if(_getResource(filename)!=null) return true;
		if(new CMFile(makeFileResourceName(filename),null,false).exists())
			return true;
		return false;
	}

	public final StringBuffer _toStringBuffer(final Object o)
	{
		if(o!=null)
		{
			if(o instanceof StringBuffer)
				return (StringBuffer)o;
			else
			if(o instanceof String)
				return new StringBuffer((String)o);
			else
			if(o instanceof StringBuilder)
				return new StringBuffer((StringBuilder)o);
		}
		return null;
	}
	
	public final StringBuffer _getFileResource(final String filename, final boolean reportErrors)
	{
		Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		StringBuffer buf=new CMFile(makeFileResourceName(filename),null,reportErrors).text();
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_FILERESOURCENOCACHE))
			_submitResource(filename,buf);
		return buf;
	}

	public final boolean _updateFileResource(final String filename, final Object obj)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_FILERESOURCENOCACHE))
			_updateResource(CMFile.vfsifyFilename(filename), obj);
		return _saveFileResource(filename,null,_toStringBuffer(obj));
	}

	public final boolean _saveFileResource(String filename, final MOB whom, final StringBuffer myRsc)
	{
		final boolean vfsFile=filename.trim().startsWith("::");
		final boolean localFile=filename.trim().startsWith("//");
		filename=CMFile.vfsifyFilename(filename);
		if(!filename.startsWith("resources/"))
			filename="resources/"+filename;
		filename=(vfsFile?"::":localFile?"//":"")+filename;
		return new CMFile(filename,whom,false).saveRaw(myRsc);
	}

	public final boolean _findRemoveProperty(final CMFile F, final String match)
	{
		boolean removed=false;
		final StringBuffer text=F.textUnformatted();
		int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
		while(x>=0)
		{
			if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
			&&(text.substring(x+match.length()).trim().startsWith("=")))
			{
				int zb1=text.lastIndexOf("\n",x);
				int zb2=text.lastIndexOf("\r",x);
				int zb=(zb2>zb1)?zb2:zb1;
				if(zb<0) zb=0; else zb++;
				int ze1=text.indexOf("\n",x);
				int ze2=text.indexOf("\r",x);
				int ze=ze2+1;
				if((ze1>zb)&&(ze1==ze2+1)) ze=ze1+1;
				else
				if((ze2<0)&&(ze1>0)) ze=ze1+1;
				if(ze<=0) ze=text.length();
				if(!text.substring(zb).trim().startsWith("#"))
				{
					text.delete(zb,ze);
					x=-1;
					removed=true;
				}
			}
			x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
		}
		if(removed) F.saveRaw(text);
		return removed;
	}
}
