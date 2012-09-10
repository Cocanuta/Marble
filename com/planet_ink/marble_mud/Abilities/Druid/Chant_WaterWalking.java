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
public class Chant_WaterWalking extends Chant
{
	public String ID() { return "Chant_WaterWalking"; }
	public String name(){ return "Water Walking";}
	public String displayText(){return "(Water Walking)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected boolean triggerNow=false;

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(triggerNow||
			   ((mob.location()!=null)
				&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
		}
	}


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(affected==null) return true;
		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(mob.location()!=null)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Room))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
			&&(msg.target()==mob.location().getRoomInDir(Directions.UP)))
			{
				msg.source().tell("Your water walking magic prevents you from ascending from the water surface.");
				return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_LEAVE)
			&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Exit))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R=mob.location().getRoomInDir(d);
					if((R!=null)
					&&(mob.location().getReverseExit(d)==msg.tool())
					&&((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
					||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)))
					{
						triggerNow=true;
						msg.source().recoverPhyStats();
						return true;
					}
				}
			}
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(triggerNow)triggerNow=false;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You have a sinking feeling that your water walking ability is gone.");
	}



	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already a water walker.");
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> feel(s) a little lighter!");
					success=beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
