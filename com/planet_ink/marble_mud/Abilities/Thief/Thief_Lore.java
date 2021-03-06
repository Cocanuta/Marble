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
public class Thief_Lore extends ThiefSkill
{
	public String ID() { return "Thief_Lore"; }
	public String name(){ return "Lore";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"LORE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ARCANELORE;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		levelDiff*=5;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,auto?"":"<S-NAME> stud(ys) <T-NAMESELF> and consider(s) for a moment.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String identity=target.secretIdentity();
				mob.tell(identity);

			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> stud(ys) <T-NAMESELF>, but can't remember a thing.");
		return success;
	}
}
