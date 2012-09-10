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
public class Chant_Stonewalking extends Chant
{
	public String ID() { return "Chant_Stonewalking"; }
	public String name(){return "Stonewalking";}
	public String displayText(){return "(Stonewalking spell)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MALICIOUS))
		   ||((msg.sourceMinor()==CMMsg.TYP_GET)&&((msg.target()==null)||(!mob.isMine(msg.target()))))))
			unInvoke();
		return;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			Room R=((MOB)affected).location();
			if((R.domainType()==Room.DOMAIN_INDOORS_CAVE)
			   ||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
			   ||(R.domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
			   ||(R.domainType()==Room.DOMAIN_OUTDOORS_ROCKS))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
				affectableStats.setWeight(0);
				affectableStats.setHeight(-1);
			}
		}
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				Room R=mob.location();
				if((R.domainType()==Room.DOMAIN_INDOORS_CAVE)||(R.domainType()==Room.DOMAIN_INDOORS_STONE))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> drawn out of the walls.");
				else
				if((R.domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)||(R.domainType()==Room.DOMAIN_OUTDOORS_ROCKS))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> drawn out of the rocks.");
				else
					mob.tell("Your stone walk has ended.");
			}
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
				&&(R.domainType()!=Room.DOMAIN_INDOORS_STONE)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already "+name()+".");
			return false;
		}

		Room R=mob.location();
		if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		   &&(R.domainType()!=Room.DOMAIN_INDOORS_STONE)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
		{
			mob.tell("You must be near walls of stone or massive rock to use this chant.");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) quietly to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fade(s) into the walls!");
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) quietly to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
