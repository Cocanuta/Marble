package com.planet_ink.marble_mud.Abilities.Fighter;
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
public class Fighter_CircleTrip extends FighterSkill
{
	boolean doneTicking=false;
	public String ID() { return "Fighter_CircleTrip"; }
	public String name(){ return "Circle Trip";}
	public String displayText(){ return "(Tripped)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CIRCLETRIP","CTRIP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CMLib.commands().postStand(mob,true);
				}
			}
			else
				mob.tell("You regain your feet.");
		}
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
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
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to circle trip!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
		{
			mob.tell("You need at least two legs to do this.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth tripping.");
			return false;
		}

		boolean success=true;
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":"^F^<FIGHT^><S-NAME> slide(s) into a circle trip!^<FIGHT^>^?");
		CMLib.color().fixSourceFightColor(msg);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(Iterator e=h.iterator();e.hasNext();)
			{
				MOB target=(MOB)e.next();

				if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				{
					mob.tell(target.name()+" is already on the floor!");
					return false;
				}

				if(target.riding()!=null)
				{
					mob.tell("You can't trip someone "+target.riding().stateString(target)+" "+target.riding().name()+"!");
					return false;
				}
				if(CMLib.flags().isInFlight(target))
				{
					mob.tell(target.name()+" is flying and can't be tripped!");
					return false;
				}

				int levelDiff=target.phyStats().level()-(((2*getXLEVELLevel(mob))+mob.phyStats().level()));
				if(levelDiff>0)
					levelDiff=levelDiff*5;
				else
					levelDiff=0;
				int adjustment = ( -levelDiff ) +
								 ( -( 35 + ((int)Math.round( ( ((double) target.charStats().getStat( CharStats.STAT_DEXTERITY ) ) - 9.0 ) * 3.0 ) ) ) );
				success=proficiencyCheck(mob,adjustment,auto);
				success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
				if(success)
				{
					msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> trip(s)!":"^F^<FIGHT^><S-NAME> trip(s) <T-NAMESELF>!^</FIGHT^>^?");
					CMLib.color().fixSourceFightColor(msg);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						maliciousAffect(mob,target,asLevel,2,-1);
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the floor!");
					}
				}
				else
					return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to circle trip <T-NAMESELF>, but fail(s).");
			}
		}
		else
			success=false;
		return success;
	}
}
