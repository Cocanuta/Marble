package com.planet_ink.marble_mud.Abilities.Properties;
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
public class Prop_AstralSpirit extends Property
{
	public String ID() { return "Prop_AstralSpirit"; }
	public String name(){ return "Astral Spirit";}
	public String displayText(){ return "(Spirit Form)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	private Race race=null;
	
	
	public long flags(){return Ability.FLAG_ADJUSTER|Ability.FLAG_IMMUNER;}

	public Race spiritRace() {
		if(race==null)
			race=CMClass.getRace("Spirit");
		return race;
	}
	public boolean autoInvocation(MOB mob)
	{
		if((mob!=null)&&(mob.fetchEffect(ID())==null))
		{
			mob.addNonUninvokableEffect(this);
			return true;
		}
		return false;
	}

	public String accountForYourself()
	{ return "an astral spirit";	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DISPOSSESS)&&(msg.source().soulMate()!=null))
			{
				Ability A=msg.source().fetchEffect("Chant_AstralProjection");
				if(A==null) A=msg.source().soulMate().fetchEffect("Chant_AstralProjection");
				if(A!=null)
				{
					A.unInvoke();
					return false;
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_SIT)&&(msg.target() instanceof DeadBody))
			{
				Vector<String> V=CMParms.parse(text().toUpperCase());
				if(!V.contains("SELF-RES"))
				{
					mob.tell("You lack that power");
					return false;
				}
			}
			if((msg.tool()!=null)&&(msg.tool().ID().equalsIgnoreCase("Skill_Revoke")))
			   return super.okMessage(myHost,msg);
			else
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH)))
			{
				if(msg.sourceMajor(CMMsg.MASK_SOUND))
					mob.tell("You are unable to make sounds in this incorporeal form.");
				else
					mob.tell("You are unable to do that this incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((msg.amITarget(mob))&&(!msg.amISource(mob))
		   &&(!msg.targetMajor(CMMsg.MASK_ALWAYS)))
		{
			mob.tell(mob.name()+" doesn't seem to be here.");
			return false;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setMyRace(spiritRace());
		super.affectCharStats(affected, affectableStats);
	}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
		affectableStats.setDisposition(affectableStats.disposition()&~PhyStats.IS_SITTING);
		affectableStats.setDisposition(affectableStats.disposition()&~PhyStats.IS_SLEEPING);
		affectableStats.setSensesMask(affectableStats.sensesMask()&~PhyStats.CAN_NOT_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
	}
}
