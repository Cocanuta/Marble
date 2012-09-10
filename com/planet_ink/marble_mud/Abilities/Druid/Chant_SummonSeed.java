package com.planet_ink.marble_mud.Abilities.Druid;
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
public class Chant_SummonSeed extends Chant
{
	public String ID() { return "Chant_SummonSeed"; }
	public String name(){ return "Summon Seeds";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}

	public static final Integer[] NON_SEEDS={Integer.valueOf(RawMaterial.RESOURCE_ASH),
											Integer.valueOf(RawMaterial.RESOURCE_SOAP),
											Integer.valueOf(RawMaterial.RESOURCE_CHEESE),
											Integer.valueOf(RawMaterial.RESOURCE_BREAD),
											Integer.valueOf( RawMaterial.RESOURCE_CRACKER),
	};
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String s=CMParms.combine(commands,0);
		StringBuffer buf=new StringBuffer("Seed types known:\n\r");
		int material=0;
		String foundShortName=null;
		int col=0;
		List<Integer> codes = RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_VEGETATION);
		for(Integer code : codes)
		{
			if(!CMParms.contains(Chant_SummonSeed.NON_SEEDS,code))
			{
				String str=RawMaterial.CODES.NAME(code.intValue());
				if(str.toUpperCase().equalsIgnoreCase(s))
				{
					material=code.intValue();
					foundShortName=CMStrings.capitalizeAndLower(str);
					break;
				}
				if(col==4){ buf.append("\n\r"); col=0;}
				col++;
				buf.append(CMStrings.padRight(CMStrings.capitalizeAndLower(str),15));
			}
		}
		if(s.equalsIgnoreCase("list"))
		{
			mob.tell(buf.toString()+"\n\r\n\r");
			return true;
		}
		if(s.length()==0)
		{
			mob.tell("Summon what kind of seed?  Try LIST as a parameter...");
			return false;
		}
		if(foundShortName==null)
		{
			mob.tell("'"+s+"' is an unknown type of vegetation.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to <S-HIS-HER> hands.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=2;i<(2+(adjustedLevel(mob,asLevel)/4));i++)
				{
					Item newItem=CMClass.getBasicItem("GenResource");
					String name=foundShortName.toLowerCase();
					if(name.endsWith("ies")) name=name.substring(0,name.length()-3)+"y";
					if(name.endsWith("s")) name=name.substring(0,name.length()-1);
					newItem.setName(CMLib.english().startWithAorAn(name+" seed"));
					newItem.setDisplayText(newItem.name()+" is here.");
					newItem.setDescription("");
					newItem.setMaterial(material);
					newItem.basePhyStats().setWeight(0);
					newItem.recoverPhyStats();
					newItem.setMiscText(newItem.text());
					mob.addItem(newItem);
				}
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Some seeds appear!");
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIS-HER> hands, but nothing happens.");

		// return whether it worked
		return success;
	}
}
