package com.planet_ink.marble_mud.Abilities.Spells;
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
public class Spell_TrueSight extends Spell
{
	public String ID() { return "Spell_TrueSight"; }
	public String name(){return "True Sight";}
	public String displayText(){return "(True Sight)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You no longer have true sight.");
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return true;
		if((msg.amISource((MOB)affected))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(msg.target()!=null)
		&&(!msg.target().name().equals(msg.target().Name())))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,msg.target().name()+" is truly "+msg.target().Name()+".",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		return true;
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_OVERLOOKING,affected.phyStats().level()+(2*getXLEVELLevel(invoker()))+200+affectableStats.getStat(CharStats.STAT_SAVE_OVERLOOKING));
	}
	
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_HIDDEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INVISIBLE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_SNEAKERS);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat()&&(!CMLib.flags().canBeSeenBy(mob.getVictim(),mob)))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE>  true sight.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> gain(s) true sight!":"^S<S-NAME> incant(s) softly, and gain(s) true sight!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes, but the spell fizzles.");

		return success;
	}
}
