package com.planet_ink.marble_mud.Races;
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
public class Beholder extends StdRace
{
	public String ID(){	return "Beholder"; }
	public String name(){ return "Beholder"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unique";}
	private String[]racialAbilityNames={"Spell_Sleep","Spell_FloatingDisc","Spell_Fear","Spell_Slow","Spell_Charm","Prayer_CauseCritical","Spell_DispelMagic","Spell_FleshStone","Prayer_DeathFinger","Spell_Disintegrate"};
	private int[]racialAbilityLevels={1,1,1,5,10,10,15,20,30,30};
	private int[]racialAbilityProficiencies={50,50,50,50,50,50,100,50,50,50};
	private boolean[]racialAbilityQuals={false,false,false,false,false,false,false,false,false,false};
	public String[] racialAbilityNames(){return racialAbilityNames;}
	public int[] racialAbilityLevels(){return racialAbilityLevels;}
	public int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	public boolean[] racialAbilityQuals(){return racialAbilityQuals;}
	private String[]culturalAbilityNames={"Undercommon"};
	private int[]culturalAbilityProficiencies={100};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,10,-1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,5,20,110,325,500,850,950,1050};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,25);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,75);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,100);
	}

	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int x=0;x<10;x++)
					resources.addElement(makeResource
					("a "+name().toLowerCase()+" eye",RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
