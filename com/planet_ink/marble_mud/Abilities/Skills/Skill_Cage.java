package com.planet_ink.marble_mud.Abilities.Skills;
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
public class Skill_Cage extends StdSkill
{
	public String ID() { return "Skill_Cage"; }
	public String name(){ return "Cage";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CAGE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null)&&(mob.isInCombat()))
			return Ability.QUALITY_INDIFFERENT;
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item cage=null;
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().getItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Item I=mob.location().findItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.removeElement(last);
				}
			}
		}

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!auto)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ORDER)||target.willFollowOrdersOf(mob))))
		{
			boolean ok=false;
			if((target.isMonster())
			&&(CMLib.flags().isAnimalIntelligence(target)))
			{
				if(CMLib.flags().isSleeping(target)
				||(!CMLib.flags().canMove(target))
				||((target.amFollowing()==mob))
				||(CMLib.flags().isBoundOrHeld(target)))
					ok=true;
			}
			if(!ok)
			{
				mob.tell(target.name()+" won't seem to let you.");
				return false;
			}

			if(cage==null)
			{
				mob.tell("Cage "+target.name()+" where?");
				return false;
			}

			if((mob.isInCombat())&&(mob.getVictim()!=target))
			{
				mob.tell("Not while you are fighting!");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CagedAnimal caged=(CagedAnimal)CMClass.getItem("GenCaged");
		if((success)&&(caged.cageMe(target)))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((cage!=null)&&(cage.owner()!=null))
				{
					if(cage.owner() instanceof MOB)
						((MOB)cage.owner()).addItem((Item)caged);
					else
					if(cage.owner() instanceof Room)
						((Room)cage.owner()).addItem((Item)caged);
				}
				else
					mob.addItem((Item)caged);
				CMMsg putMsg=CMClass.getMsg(mob,cage,(Item)caged,CMMsg.MSG_PUT,"<S-NAME> cage(s) <O-NAME> in <T-NAME>.");
				if(mob.location().okMessage(mob,putMsg))
				{
					mob.location().send(mob,putMsg);
					target.killMeDead(false);
				}
				else
					((Item)caged).destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to cage <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}
