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
public class Pachyderm extends StdRace
{
	public String ID(){	return "Pachyderm"; }
	public String name(){ return "Pachyderm"; }
	public int shortestMale(){return 60;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 850;}
	public int weightVariance(){return 300;}
	public long forbiddenWornBits(){return ~(Wearable.WORN_HEAD|Wearable.WORN_EARS|Wearable.WORN_EYES);}
	public String racialCategory(){return "Pachyderm";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,4,8,16,28,60,80,82,84};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,18);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,3);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of tusks");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is almost dead!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is cut and bruised heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g has a few bruises and scratched scales.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g has a few small bruises.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect health.^N";
	}

	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<12;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",RawMaterial.RESOURCE_LEATHER));
				for(int i=0;i<52;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" tusk",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
