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
public class Unique extends StdRace
{
	public String ID(){	return "Unique"; }
	public String name(){ return "Unique"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unique";}
	public boolean fertile(){return false;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is pulsating in an unstable rage!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is massively and amazingly angry.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is very angry.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y is somewhat angry.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y is very irritated.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p is starting to show irritation.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is definitely serious and concerned.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g is growing serious and concerned.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g is definitely unamused and is starting to notice.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g is no longer amused, though still unconcerned.^N";
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
				("a "+name().toLowerCase()+" internal organ",RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
