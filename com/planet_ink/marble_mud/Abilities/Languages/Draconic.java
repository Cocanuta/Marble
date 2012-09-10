package com.planet_ink.marble_mud.Abilities.Languages;
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

@SuppressWarnings({"unchecked","rawtypes"})
public class Draconic extends StdLanguage
{
	public String ID() { return "Draconic"; }
	public String name(){ return "Draconic";}
	public static List<String[]> wordLists=null;
	public Draconic()
	{
		super();
	}

	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"y"};
			String[] two={"ve","ov","iv","si","es","se"};
			String[] three={"see","sev","ave","ces","ven","sod"};
			String[] four={"nirg","avet","sav`e","choc","sess","sens","vent","vens","sven","yans","vays"};
			String[] five={"splut","svets","fruite","dwagg","vrers","verrs","srens","swath","senys","varen"};
			String[] six={"choccie","svenren","yorens","vyrues","whyrie","vrysenso","forin","sinnes","sessis","uroven","xorers","nosees"};
			wordLists=new Vector();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
		}
		return wordLists;
	}
}
