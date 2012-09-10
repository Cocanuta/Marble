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
import com.planet_ink.marble_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Spell_Scribe extends Spell
{
	public String ID() { return "Spell_Scribe"; }
	public String name(){return "Scribe";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	protected int overridemana(){return Ability.COST_ALL;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Scribe which spell onto what?");
			return false;
		}
		Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Scroll))
		{
			mob.tell("You can't scribe onto that.");
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		Scroll scroll=(Scroll)target;

		String spellName=CMParms.combine(commands,0).trim();
		Spell scrollThis=null;
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if((A!=null)
			&&(A instanceof Spell)
			&&(A.name().equalsIgnoreCase(spellName))
			&&(!A.ID().equals(this.ID())))
				scrollThis=(Spell)A;
		}
		if(scrollThis==null)
			for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
				if((A!=null)
				&&(A instanceof Spell)
				&&(CMLib.english().containsString(A.name(),spellName))
				&&(!A.ID().equals(this.ID())))
					scrollThis=(Spell)A;
			}
		if(scrollThis==null)
		{
			mob.tell("You don't know how to scribe '"+spellName+"'.");
			return false;
		}
		if(CMLib.ableMapper().lowestQualifyingLevel(scrollThis.ID())>24)
		{
			mob.tell("That spell is too powerful to scribe.");
			return false;
		}
		
		int numSpells=(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().qualifyingLevel(mob,this));
		if(numSpells<0) numSpells=0;
		if(scroll.getSpells().size()>numSpells)
		{
			mob.tell("You aren't powerful enough to scribe any more spells onto "+scroll.name()+".");
			return false;
		}

		List<Ability> spells=scroll.getSpells();
		for(Ability spell: spells)
			if(spell.ID().equals(scrollThis.ID()))
			{
				mob.tell("That spell is already scribed onto "+scroll.name()+".");
				return false;
			}

		int experienceToLose=10*CMLib.ableMapper().lowestQualifyingLevel(scrollThis.ID());
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell("You don't have enough experience to cast this spell.");
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
		mob.tell("You lose "+experienceToLose+" experience points for the effort.");

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(scrollThis.ID());
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(scroll.getSpellList().trim().length()==0)
					scroll.setSpellList(scrollThis.ID());
				else
					scroll.setSpellList(scroll.getSpellList()+";"+scrollThis.ID());
				if((scroll.usesRemaining()==Integer.MAX_VALUE)||(scroll.usesRemaining()<0))
					scroll.setUsesRemaining(0);
				scroll.setUsesRemaining(scroll.usesRemaining()+1);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
