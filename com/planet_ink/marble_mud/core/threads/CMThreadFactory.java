package com.planet_ink.marble_mud.core.threads;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

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
public class CMThreadFactory implements ThreadFactory  
{
	private String 						serverName;
	private final AtomicInteger 		counter		=new AtomicInteger();
	private final SLinkedList<Thread> 	active 		= new SLinkedList<Thread>();
	
	public CMThreadFactory(String serverName)
	{
		this.serverName=serverName;
	}
	public void setServerName(String newName)
	{
		this.serverName=newName;
	}
	public Thread newThread(Runnable r) 
	{
		final Thread t = new Thread(r,serverName+"#"+counter.addAndGet(1));
		active.add(t);
		return t;
	}
	public Collection<Thread> getThreads() 
	{ 
		return active;
	}
}
