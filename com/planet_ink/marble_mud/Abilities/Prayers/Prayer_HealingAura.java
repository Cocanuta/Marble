package com.planet_ink.marble_mud.Abilities.Prayers;
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
public class Prayer_HealingAura extends Prayer
{
	public String ID() { return "Prayer_HealingAura"; }
	public String name(){ return "Healing Aura";}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Healing Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean  canBeUninvoked(){return false;}
	public boolean  isAutoInvoked(){return true;}
	protected int fiveDown=5;
	protected int tenDown=10;
	protected int twentyDown=20;

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.isInCombat())&&(!mob.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead")))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return false;
		if(tickID!=Tickable.TICKID_MOB) return true;
		MOB myChar=(MOB)affected;
		if(((--fiveDown)>0)&&((--tenDown)>0)&&((--twentyDown)>0)) return true;

		Set<MOB> followers=myChar.getGroupMembers(new HashSet<MOB>());
		if(myChar.location()!=null)
			for(int i=0;i<myChar.location().numInhabitants();i++)
			{
				MOB M=myChar.location().fetchInhabitant(i);
				if((M!=null)
				&&((M.getVictim()==null)||(!followers.contains(M.getVictim()))))
					followers.add(M);
			}
		if((fiveDown)<=0)
		{
			fiveDown=5;
			Ability A=CMClass.getAbility("Prayer_CureLight");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		if((tenDown)<=0)
		{
			tenDown=10;
			Ability A=CMClass.getAbility("Prayer_RemovePoison");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		if((twentyDown)<=0)
		{
			twentyDown=20;
			Ability A=CMClass.getAbility("Prayer_CureDisease");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		return true;
	}
}
