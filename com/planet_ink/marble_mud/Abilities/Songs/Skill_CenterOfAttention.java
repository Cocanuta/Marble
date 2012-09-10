package com.planet_ink.marble_mud.Abilities.Songs;
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
public class Skill_CenterOfAttention extends BardSkill
{
	public String ID() { return "Skill_CenterOfAttention"; }
	public String name(){ return "Center of Attention";}
	public String displayText(){ return "(Watching "+(invoker()==null?"a crazy bard":invoker().name())+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CENTEROFATTENTION"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int getTicksBetweenCasts() { return (int)(CMProps.getMillisPerMudHour() / CMProps.getTickMillis() / (long)2); }

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if(CMLib.flags().canBeSeenBy(invoker(), (MOB)affected))
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((!CMLib.flags().canBeSeenBy(invoker(), mob))
			||((invoker()!=null)&&(mob.location()!=invoker().location())))
				unInvoke();
			String verbStr;
			String targetStr;
			switch(CMLib.dice().roll(1, 10, 0))
			{
			case 1: verbStr="<S-IS-ARE> entranced by"; break;
			case 2: verbStr="remain(s) captivated by"; break;
			case 3: verbStr="<S-IS-ARE> captivated by"; break;
			case 4: verbStr="remain(s) entranced by"; break;
			case 5: verbStr="can't stop watching"; break;
			case 6: verbStr="stare(s) amazed at"; break;
			case 7: verbStr="<S-IS-ARE> hypnotized by"; break;
			case 8: verbStr="remain(s) enthralled by"; break;
			case 9: verbStr="<S-IS-ARE> delighted by"; break;
			default: verbStr="remain(s) enchanted by"; break;
			}
			switch(CMLib.dice().roll(1, 10, 0))
			{
			case 1: targetStr="<T-YOUPOSS> performance"; break;
			case 2: targetStr="<T-YOUPOSS> antics"; break;
			case 3: targetStr="<T-YOUPOSS> flailing about"; break;
			case 4: targetStr="<T-YOUPOSS> drama"; break;
			case 5: targetStr="<T-YOUPOSS> show"; break;
			case 6: targetStr="the ongoing spectacle"; break;
			case 7: targetStr="<T-YOUPOSS> comedy"; break;
			case 8: targetStr="<T-YOUPOSS> tomfoolery"; break;
			case 9: targetStr="<T-YOUPOSS> escapades"; break;
			default: targetStr="<T-YOUPOSS> stunts"; break;
			}
			mob.location().show(mob, invoker(), CMMsg.MSG_OK_VISUAL, "<S-NAME> "+verbStr+" "+targetStr+".");
		}
		
		return true;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth performing for.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
										auto?"":"<S-NAME> begin(s) flailing about while making loud silly noises.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Iterator e=h.iterator();e.hasNext();)
				{
					MOB target=(MOB)e.next();
					if(CMLib.flags().canBeSeenBy(mob, target))
					{
						int levelDiff=target.phyStats().level()-(((2*getXLEVELLevel(mob))+mob.phyStats().level()));
						if(levelDiff>0)
							levelDiff=levelDiff*5;
						else
							levelDiff=0;
						CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
						if(mob.location().okMessage(mob,msg2))
						{
							mob.location().send(mob,msg2);
							if((msg.value()<=0)&&(msg2.value()<=0))
							{
								maliciousAffect(mob,target,asLevel,3,-1);
								target.location().show(target,mob,CMMsg.MSG_OK_ACTION,"<S-NAME> begin(s) watching <T-NAME> with an amused expression.");
							}
						}
					}
				}
			}
			setTimeOfNextCast(mob);
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to become the center of attention, but fail(s).");
		return success;
	}
}
