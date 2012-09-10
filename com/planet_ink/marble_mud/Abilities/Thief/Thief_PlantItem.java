package com.planet_ink.marble_mud.Abilities.Thief;
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
public class Thief_PlantItem extends ThiefSkill
{
	public String ID() { return "Thief_PlantItem"; }
	public String name(){ return "Plant Item";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	private static final String[] triggerStrings = {"PLANTITEM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("What would you like to plant on whom?");
			return false;
		}
		MOB target=mob.location().fetchInhabitant((String)commands.lastElement());
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+(String)commands.lastElement()+"' here.");
			return false;
		}
		if(target==mob)
		{
			mob.tell("You cannot plant anything on yourself!");
			return false;
		}
		commands.removeElement(commands.lastElement());

		Item item=super.getTarget(mob,null,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(item==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));
		if(levelDiff<0) levelDiff=0;
		levelDiff*=5;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,item,CMMsg.MSG_GIVE,"<S-NAME> plant(s) <O-NAME> on <T-NAMESELF>.",CMMsg.MASK_ALWAYS|CMMsg.MSG_GIVE,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_GIVE,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.isMine(item))
				{
					item.basePhyStats().setDisposition(item.basePhyStats().disposition()|PhyStats.IS_HIDDEN);
					item.recoverPhyStats();
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to plant "+item.name()+" on <T-NAMESELF>, but fail(s).");
		return success;
	}
}
