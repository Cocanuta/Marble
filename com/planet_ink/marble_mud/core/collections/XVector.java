package com.planet_ink.marble_mud.core.collections;
import java.io.Serializable;
import java.util.*;

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

/*
 * A version of the Vector class that provides to "safe" adds
 * and removes by copying the underlying vector whenever those
 * operations are done.
 */
public class XVector<T> extends Vector<T> 
{
	private static final long serialVersionUID = 6687178785122563992L;
	
	public XVector(List<T> V)
	{
		super();
		if(V!=null)
			addAll(V);
	}
	
	public XVector(T... E)
	{
		super();
		if(E!=null)
			for(T o : E)
				add(o);
	}
	
	public XVector(Set<T> E)
	{
		super();
		if(E!=null)
			for(T o : E)
				add(o);
	}
	
	public XVector(Enumeration<T> E)
	{
		super();
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}
	
	public XVector(Iterator<T> E)
	{
		super();
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}
	
	public synchronized void addAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}
	
	public synchronized void addAll(T[] E)
	{
		if(E!=null)
			for(T e : E)
				add(e);
	}
	
	public synchronized void addAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}
	
	public synchronized void removeAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				remove(E.nextElement());
	}
	
	public synchronized void removeAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				remove(E.next());
	}
	
	public synchronized void removeAll(List<T> E)
	{
		if(E!=null)
			for(T o : E)
				remove(o);
	}
	
	public synchronized void sort()
	{
		Vector<T> V2=new Vector<T>(new TreeSet<T>(this));
		clear();
		addAll(V2);
	}
}
