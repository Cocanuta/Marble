package com.planet_ink.marble_mud.core.intermud.i3.net;
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
/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The Input class accepts user input.
 */


/**
 * This class is designed to accept user input
 * outside of any command parsing structure.
 * You will generally subclass this to provide a
 * temporary place to direct user input when asking
 * the user questions and so on.  When the user types
 * something that has been redirected to a subclass of
 * Input, the string they type gets passed to the
 * input() method for processing.<BR>
 * Created: 28 September 1996<BR>
 * Last modified: 28 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 * @see com.planet_ink.marble_mud.core.intermud.i3.net.Interactive#redirectInput
 */

public interface Input {
	/**
	 * Implementations of this interface will define this
	 * method so that it processes user input.
	 * @param arg the input string
	 */
	public abstract void input(Interactive user, String arg);
}
