package com.planet_ink.marble_mud.Abilities.Druid;
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
public class Chant_EelShock extends Chant
{
	public String ID() { return "Chant_EelShock"; }
	public String name(){return "Eel Shock";}
	public String displayText(){return "(Stunned)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int maxRange() {return 3;}
	public int minRange() {return 0;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
	public long flags(){return Ability.FLAG_AIRBASED;}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell("<S-YOUPOSS> are no longer stunned.");
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(PhyStats.IS_SITTING);
	}


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.sourceMajor()>0))
		{
			mob.tell("You are stunned.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
	
	private boolean roomWet(Room location)
	{
		if(location.domainType() == Room.DOMAIN_INDOORS_UNDERWATER ||
		   location.domainType() == Room.DOMAIN_INDOORS_WATERSURFACE ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_UNDERWATER ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_WATERSURFACE ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_SWAMP)
			return true;

		Area currentArea = location.getArea();
		if(currentArea.getClimateObj().weatherType(location) == Climate.WEATHER_RAIN ||
		   currentArea.getClimateObj().weatherType(location) == Climate.WEATHER_THUNDERSTORM)
			return true;
		return false;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			Set<MOB> h=CMLib.combat().properTargets(this,mob,false);
			if(h==null)
				return Ability.QUALITY_INDIFFERENT;
			Room location=mob.location();
			if(location!=null)
			{
				if(!roomWet(location))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=CMLib.combat().properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth shocking.");
			return false;
		}

		Room location = mob.location();

		if(!roomWet(location))
		{
				mob.tell("It's too dry to invoke this chant.");
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
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),"^S<S-NAME> chant(s) and electrical sparks dance across <S-HIS-HER> skin.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_ELECTRIC,"<T-NAME> is stunned.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
						maliciousAffect(mob,target,asLevel,3+super.getXLEVELLevel(mob)+(2*super.getX1Level(mob)),-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> sees tiny sparks dance across <S-HIS-HER> skin, but nothing more happens.");
		// return whether it worked
		return success;
	}
}
