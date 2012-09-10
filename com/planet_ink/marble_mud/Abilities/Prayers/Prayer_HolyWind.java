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
public class Prayer_HolyWind extends Prayer
{
	public String ID() { return "Prayer_HolyWind"; }
	public String name(){ return "Holy Wind";}
	public String displayText(){return "(Blown Down)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int maxRange(){return adjustedMaxInvokerRange(4);}
	public boolean doneTicking=false;
	public long flags(){return Ability.FLAG_MOVING;}

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

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth blowing around.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"A horrendous wind gust blows through here.":"^S<S-NAME> "+prayWord(mob)+" for the holy wind to blow through here.^?")+CMProps.msp("wind.wav",40)))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"<T-NAME> get(s) blown back!");
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					if((msg.value()<=0)&&(target.location()==mob.location()))
					{
						int howLong=2;
						if((mob.location().getArea().getClimateObj().weatherType(mob.location())==Climate.WEATHER_WINDY)
						||(mob.location().getArea().getClimateObj().weatherType(mob.location())==Climate.WEATHER_DUSTSTORM)
						||(mob.location().getArea().getClimateObj().weatherType(mob.location())==Climate.WEATHER_THUNDERSTORM))
							howLong=4;

						MOB victim=target.getVictim();
						if((victim!=null)&&(target.rangeToTarget()>=0))
							target.setAtRange(target.rangeToTarget()+(howLong/2));
						if(target.rangeToTarget()>target.location().maxRange())
							target.setAtRange(target.location().maxRange());
						mob.location().send(mob,msg);
						if((!CMLib.flags().isInFlight(target))
						&&(CMLib.dice().rollPercentage()>(((target.charStats().getStat(CharStats.STAT_DEXTERITY)*2)+target.phyStats().level()))-(5*howLong))
						&&(target.charStats().getBodyPart(Race.BODY_LEG)>0))
						{
							mob.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) down!");
							doneTicking=false;
							success=maliciousAffect(mob,target,asLevel,howLong,-1);
						}
						if(target.getVictim()!=null)
							target.getVictim().setAtRange(target.rangeToTarget());
						if(mob.getVictim()==null) mob.setVictim(null); // correct range
						if(target.getVictim()==null) target.setVictim(null); // correct range
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
