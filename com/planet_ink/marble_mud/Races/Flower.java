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
public class Flower extends Vine
{
	public String ID(){	return "Flower"; }
	public String name(){ return "Flower"; }
	public int shortestMale(){return 4;}
	public int shortestFemale(){return 4;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 1;}
	public int weightVariance(){return 1;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Vegetation";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,8 ,8 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_TASTE|PhyStats.CAN_NOT_MOVE);
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_GENDER,'N');
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_GAS,affectableStats.getStat(CharStats.STAT_SAVE_GAS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
	}
	public String arriveStr()
	{
		return "floats in";
	}
	public String leaveStr()
	{
		return "floats";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a nasty bloom");
			naturalWeapon.setRanges(0,3);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB)
		&&(msg.amISource((MOB)myHost)))
		{
			if((msg.targetMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL)
				||(msg.sourceMinor()==CMMsg.TYP_RETREAT))
			{
				msg.source().tell("You can't really go anywhere -- you are rooted!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
			((MOB)ticking).curState().recoverTick(((MOB)ticking),((MOB)ticking).maxState());
		return true;
	}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is massively shredded and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is extremely shredded and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y is very shredded and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y is shredded and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p is shredded and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p has lost numerous petals.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g has lost some petals.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g has lost a few petals.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect condition.^N";
	}
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some petals",RawMaterial.RESOURCE_HERBS));
			}
		}
		return resources;
	}
}
