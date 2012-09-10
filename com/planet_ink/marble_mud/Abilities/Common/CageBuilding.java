package com.planet_ink.marble_mud.Abilities.Common;
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

@SuppressWarnings("rawtypes")
public class CageBuilding extends Wainwrighting
{
	public String ID() { return "CageBuilding"; }
	public String name(){ return "Cage Building";}
	private static final String[] triggerStrings = {"BUILDCAGE","CAGEBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "WOODEN";}

	public String parametersFile(){ return "cagebuilding.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(!(I instanceof Container))
			return false;
		Container C=(Container)I;
		if((C.containTypes()==Container.CONTAIN_CAGED)
		||(C.containTypes()==(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED)))
			return true;
		if(isANativeItem(I.Name()))
			return true;
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Build what? Enter \"buildcage list\" for a list, \"buildcage learn <item>\" to gain recipes.");
			return false;
		}
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
