package com.planet_ink.marble_mud.Libraries.interfaces;

import java.util.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
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

public interface SessionsList extends CMLibrary, Runnable
{
	public void stopSessionAtAllCosts(Session S);
	public Session findPlayerSessionOnline(String srchStr, boolean exactOnly);
	public MOB findPlayerOnline(String srchStr, boolean exactOnly);
	public Iterator<Session> all();
	public Iterable<Session> allIterable();
	public Iterator<Session> localOnline();
	public Iterable<Session> localOnlineIterable();
	public int getCountLocalOnline();
	public int getCountAll();
	public Session getAllSessionAt(int index);
	public void add(Session s);
	public void remove(Session s);
}
