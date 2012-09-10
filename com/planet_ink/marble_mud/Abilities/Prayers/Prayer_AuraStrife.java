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
public class Prayer_AuraStrife extends Prayer
{
	public String ID() { return "Prayer_AuraStrife"; }
	public String name(){ return "Aura of Strife";}
	public String displayText(){ return "(Aura of Strife)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((invoker()!=null)&&(affected!=invoker())&&(CMLib.flags().isEvil(invoker())))
		{
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-(adjustedLevel(invoker(),0)/5));
			if(affectableStats.getStat(CharStats.STAT_CHARISMA)<=0)
				affectableStats.setStat(CharStats.STAT_CHARISMA,1);
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(M!=null)&&(!M.amDead())&&(M.location()!=null))
			M.location().show(M,null,CMMsg.MSG_OK_VISUAL,"The aura of strife around <S-NAME> fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(invoker()!=null)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			Set<MOB> invokerGroup=invoker().getGroupMembers(new HashSet<MOB>());
			if(mob!=invoker())
			{
				if(mob.location()!=invoker().location())
					unInvoke();
				else
				{
					if(invokerGroup.contains(mob))
						unInvoke();
					else
					if(mob.isInCombat())
					{
						int levels=invoker().charStats().getClassLevel("Templar");
						if(levels<0) levels=invoker().phyStats().level();
						if(CMLib.dice().rollPercentage()>=levels)
						{
							MOB newvictim=mob.location().fetchRandomInhabitant();
							if(newvictim!=mob) mob.setVictim(newvictim);
						}
					}
				}
			}
			else
			if((mob.location()!=null)&&(CMLib.flags().isEvil(invoker())))
			for(int m=0;m<mob.location().numInhabitants();m++)
			{
				MOB M=mob.location().fetchInhabitant(m);
				if((M!=null)&&(M!=invoker())&&(!invokerGroup.contains(M))&&(!M.Name().equals(mob.getLiegeID())))
					beneficialAffect(invoker,M,0,Ability.TICKS_FOREVER);
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		Room targetRoom=target.location();
		if(targetRoom==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			beneficialAffect(mob,target,asLevel,0);
			target.recoverPhyStats();
			targetRoom.recoverRoomStats();
		}
		// return whether it worked
		return success;
	}

	public boolean autoInvocation(MOB mob)
	{
		if(mob.charStats().getCurrentClass().ID().equals("Archon"))
			return false;
		return super.autoInvocation(mob);
	}
}
