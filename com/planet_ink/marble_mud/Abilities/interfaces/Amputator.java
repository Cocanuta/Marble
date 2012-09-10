package com.planet_ink.marble_mud.Abilities.interfaces;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.Misc.Amputation;
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
/**
 * An Amputator is a kind of ability that denotes missing parts.  Usually
 * the missing parts are body parts from mobs, but they can technically be
 * anything that can go missing as parts from the hosted object.
 */
public interface Amputator extends Ability
{
	/**
	 * Returns a fully-qualified list of those parts of the given object which
	 * have not yet gone missing from it.  This would be a string set denoting the names
	 * of the specific parts not yet missing.
	 * @param P the object to scrutinize 
	 * @return the set of the name of the remaining pieces. 
	 */
	public List<String> remainingLimbNameSet(Physical P);
	/**
	 * Performs the very dirty business of amputating the item of the given
	 * name from the given target.  An existing instanceof of the amputator
	 * which will act as a property for the target must also be passed in.
	 * It will generate messages if necessary, toss the piece on the groud
	 * if that is appropriate, and do all thats needed.
	 * @param target the thing to take the part away from
	 * @param A the instanceof this object to use as a marker
	 * @param gone the name of the piece to remove, fully qualified.
	 * @return the item object representing the newly missing piece.
	 */
	public Item amputate(Physical target, Amputator A, String gone);
	/**
	 * The opposite of the remainingLimbNameSet method, this method returns
	 * the list of the names of those parts which have been amputated.
	 * @return the list of the names of the parts that are GONE!
	 */
	public List<String> missingLimbNameSet();
	/**
	 * Often losing one part means that other parts are instantly affected, like
	 * removing an engine includes the spark plugs.  This method is called to
	 * generate the list of those parts which also must go due to the parts
	 * described by the missing string, but which are not currently included
	 * in the given missingLimbs set.
	 * @param O the mob/race/object frame of reference to use
	 * @param missing the name of the part that was removed
	 * @param missingLimbs the parts already missing from the target
	 * @return the set of parts that are not yet missing, but now should be.
	 */
	public List<String> affectedLimbNameSet(Object O, String missing, List<String> missingLimbs);
	/**
	 * Restores a missing part, denoted by the given string, and managed by the
	 * given Amputator property
	 * @param target the unfortunate target
	 * @param A the amputator object managing the targets missing stuff
	 * @param gone the name of the part to restore.
	 */
	public void unamputate(Physical target, Amputator A, String gone);

}
