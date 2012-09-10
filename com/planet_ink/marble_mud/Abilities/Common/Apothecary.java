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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Apothecary extends Cooking
{
	public String ID() { return "Apothecary"; }
	public String name(){ return "Apothecary";}
	private static final String[] triggerStrings = {"APOTHECARY","MIX"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "MISC";}
	public String cookWordShort(){return "mix";}
	public String cookWord(){return "mixing";}
	public boolean honorHerbs(){return false;}
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }

	public String parametersFile(){ return "poisons.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public Apothecary()
	{
		super();

		defaultFoodSound = "hotspring.wav";
		defaultDrinkSound = "hotspring.wav";
	}

	public boolean supportsDeconstruction() { return false; }

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I instanceof Perfume)
		{
			return true;
		}
		else
		if(I instanceof Drink)
		{
			Drink D=(Drink)I;
			if(D.liquidType()!=RawMaterial.RESOURCE_POISON)
				return false;
			if(CMLib.flags().flaggedAffects(D, Ability.FLAG_INTOXICATING).size()>0)
				return false;
			if(CMLib.flags().domainAffects(D, Ability.ACODE_POISON).size()>0)
				return true;
			return true;
		}
		else
		if(I instanceof MagicDust)
		{
			MagicDust M=(MagicDust)I;
			List<Ability> spells=M.getSpells();
			if((spells == null)||(spells.size()==0))
				return false;
			return true;
		}
		else
			return false;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(building==null))
			return false;
		Ability A2=building.fetchEffect(0);
		if((A2!=null)
		&&(building instanceof Drink))
		{
			((Drink)building).setLiquidType(RawMaterial.RESOURCE_POISON);
			if(building instanceof Item)
				((Item)building).setMaterial(RawMaterial.RESOURCE_POISON);
		}
		return true;
	}
}
