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
public class FoodPrep extends Cooking
{
	public String ID() { return "FoodPrep"; }
	public String name(){ return "Food Prep";}
	private static final String[] triggerStrings = {"FOODPREPPING","FPREP"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "make";}
	public String cookWord(){return "making";}
	public boolean honorHerbs(){return false;}
	public boolean requireFire(){return false;}

	public String parametersFile(){ return "foodprep.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public FoodPrep()
	{
		super();

		defaultFoodSound = "chopchop.wav";
	}

}
