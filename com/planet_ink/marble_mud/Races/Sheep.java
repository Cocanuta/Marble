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
public class Sheep extends StdRace
{
	public String ID(){	return "Sheep"; }
	public String name(){ return "Sheep"; }
	public int shortestMale(){return 36;}
	public int shortestFemale(){return 36;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 50;}
	public int weightVariance(){return 60;}
	public long forbiddenWornBits(){return ~(Wearable.WORN_HEAD|Wearable.WORN_FEET|Wearable.WORN_NECK|Wearable.WORN_EARS|Wearable.WORN_EYES);}
	public String racialCategory(){return "Ovine";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,2,4,7,15,20,21,22};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,5);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of hooves");
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is covered in blood and matted wool.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y has large patches of bloody matted wool.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y has some bloody matted wool.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p has a lot of cuts and gashes.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p has a few cut patches.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g has a cut patch of wool.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g has some disheveled wool.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g has some misplaced wool.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect health.^N";
	}
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<10;i++)
					resources.addElement(makeResource
					("some "+name().toLowerCase()+" wool",RawMaterial.RESOURCE_WOOL));
				for(int i=0;i<3;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_MUTTON));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		DeadBody body=super.getCorpseContainer(mob,room);
		if((body!=null)&&(CMLib.dice().roll(1,1000,0)==1)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
		{
			Ability A=CMClass.getAbility("Disease_Anthrax");
			if(A!=null) body.addNonUninvokableEffect(A);
		}
		return body;
	}
}
