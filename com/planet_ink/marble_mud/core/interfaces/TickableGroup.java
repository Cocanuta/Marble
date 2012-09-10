package com.planet_ink.marble_mud.core.interfaces;
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

/*
Copyright 2002-2012 Bo Zimmerman

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
/**
 * This class represents a thread, consisting of a group of Tickable objects  receiving periodic calls
 * to their tick(Tickable,int) methods by this thread object
 * @see Tickable
 * @see Tickable#tick(Tickable, int)
 * @author Bo Zimmerman
 *
 */
public interface TickableGroup
{
	/**
	 * Returns the current or last Tickable object which this thread made a tick(Tickable,int) method
	 * call to.
	 * @see Tickable
	 * @see Tickable#tick(Tickable, int)
	 * @return the Tickable object last accessed
	 */ 
	public Tickable lastTicked();
}
