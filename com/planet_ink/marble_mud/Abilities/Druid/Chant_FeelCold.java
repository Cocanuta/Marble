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
public class Chant_FeelCold extends Chant
{
	public String ID() { return "Chant_FeelCold"; }
	public String name(){ return "Feel Cold";}
	public String displayText(){return "(Feel Cold)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your cold feeling is gone.");

		super.unInvoke();

	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&(msg.sourceMinor()==CMMsg.TYP_COLD))
		{
			int recovery=(int)Math.round(CMath.mul((msg.value()),2.0));
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STAT_SAVE_COLD,affectedStats.getStat(CharStats.STAT_SAVE_COLD)-100);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID!=Tickable.TICKID_MOB) return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			MOB M=(MOB)affecting();
			Room room=M.location();
			if(room!=null)
			{
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINDY)
				&&((room.getArea().climateType()&Area.CLIMASK_COLD)>0)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
					CMLib.combat().postDamage(invoker,M,null,1,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The cold biting wind <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_WINTER_COLD)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
					CMLib.combat().postDamage(invoker,M,null,1,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The biting cold <DAMAGE> <T-NAME>!");
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_SNOW)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					int damage=CMLib.dice().roll(1,8,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The blistering snow <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_BLIZZARD)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					int damage=CMLib.dice().roll(1,16,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The blizzard <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().getClimateObj().weatherType(room)==Climate.WEATHER_HAIL)
				&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_COLD)))
				{
					int damage=CMLib.dice().roll(1,8,0);
					CMLib.combat().postDamage(invoker,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The biting hail <DAMAGE> <T-NAME>!");
				}
				else
					return true;
				if((!M.isInCombat())&&(M!=invoker)&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
					CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) very cold");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
