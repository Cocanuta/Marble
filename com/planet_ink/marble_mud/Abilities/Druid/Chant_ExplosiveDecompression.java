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
public class Chant_ExplosiveDecompression extends Chant
{
	public String ID() { return "Chant_ExplosiveDecompression"; }
	public String name(){ return "Explosive Decompression";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public boolean bubbleAffect(){return true;}

	public void affectPhyStats(Physical affecting, PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if(affecting instanceof MOB)
			stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.location().domainType()&Room.INDOORS)==0)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((!auto)&&((target.domainType()&Room.INDOORS)==0))
		{
			mob.tell("This chant only works indoors.");
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) loudly.  A ball of fire forms around <S-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ball of fire **EXPLODES**!");
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(M!=mob))
						{
							CMMsg msg2=CMClass.getMsg(mob,M,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_FIRE,null);
							if(mob.location().okMessage(mob,msg2))
							{
								mob.location().send(mob,msg2);
								invoker=mob;
								int numDice = adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob));
								int damage = CMLib.dice().roll(numDice, 5, 25);
								if(msg2.value()>0)
									damage = (int)Math.round(CMath.div(damage,2.0));
								CMLib.combat().postDamage(mob,M,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flaming blast <DAMAGE> <T-NAME>!");
							}
							if((M.charStats().getBodyPart(Race.BODY_FOOT)>0)
							&&(!CMLib.flags().isFlying(M))&&(CMLib.flags().isStanding(M)))
								mob.location().show(M,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_SIT,"<S-NAME> <S-IS-ARE> blown off <S-HIS-HER> feet!");
						}
					}
					maliciousAffect(mob,target,asLevel,20,-1);
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The fire burns off all the air here!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) loudly, but nothing happens.");
		// return whether it worked
		return success;
	}
}
