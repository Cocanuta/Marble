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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Chant_PlantSelf extends Chant
{
	public String ID() { return "Chant_PlantSelf"; }
	public String name(){ return "Plant Self";}
	public String displayText(){return "(Planted)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	long lastTime=0;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> uproot(s) <S-HIM-HERSELF>.");
				else
					mob.tell("You uproot yourself.");
			}
		}
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.tool()!=this)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
				||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))))
			unInvoke();
		return;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB) return true;
		if(!mob.isInCombat())
		{
			if(!mob.location().getArea().getClimateObj().canSeeTheSun(mob.location()))
			{
				unInvoke();
				return false;
			}
			if((System.currentTimeMillis()-lastTime)<60000) 
				return true;
			if(!proficiencyCheck(null,0,false)) 
				return true;
			lastTime=System.currentTimeMillis();
			Room room=mob.location();
			int myAlignment=mob.fetchFaction(CMLib.factions().AlignID());
			int total=CMLib.factions().getTotal(CMLib.factions().AlignID());
			int oneHalfPct=(int)Math.round(CMath.mul(total,.015));
			if(CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_INDIFF)<99)
			{
				if(CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_EVIL)<CMLib.factions().getAlignPurity(myAlignment,Faction.ALIGN_GOOD))
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), oneHalfPct);
				else
					CMLib.factions().postFactionChange(mob,this, CMLib.factions().AlignID(), -oneHalfPct);
				switch(CMLib.dice().roll(1,10,0))
				{
				case 0: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> whisper(s) to the wind."); break;
				case 1: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> lean(s) towards the sun."); break;
				case 2: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> feel(s) the life of the insects."); break;
				case 3: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> feed(s) on the moisture of the earth."); break;
				case 4: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> take(s) in the energy of the sun."); break;
				case 5: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> feel(s) <S-HIM-HERSELF> grow."); break;
				case 6: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> become(s) one with the earth."); break;
				case 7: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> seek(s) the inner beauty of the natural order."); break;
				case 8: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> expunge(s) <S-HIS-HER> unnatural thoughts."); break;
				case 9: room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> find(s) clarity in the natural world."); break;
				}
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("You can't commune while in combat!");
			return false;
		}
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!mob.location().getArea().getClimateObj().canSeeTheSun(mob.location()))
		{
			mob.tell("You won't feel the sun here.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_DESERT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_ROCKS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		
		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),"^S<S-NAME> plant(s) <S-HIM-HERSELF> in the earth while chanting softly...^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				helpProficiency(mob, 0);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to the earth, but lose(s) concentration.");

		// return whether it worked
		return success;
	}
}
