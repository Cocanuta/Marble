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
@SuppressWarnings({"unchecked","rawtypes"})
public class Thief_Palm extends ThiefSkill
{
	public String ID() { return "Thief_Palm"; }
	public String name(){ return "Palm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"PALM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public double castingTime(final MOB mob, final List<String> cmds){return CMProps.getActionSkillCost(ID(),0.0);}
	public double combatCastingTime(final MOB mob, final List<String> cmds){return CMProps.getCombatActionSkillCost(ID(),0.0);}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to palm something and fail(s).");
		else
		{
			if((commands.size()>0)&&(!((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
			   commands.addElement("UNOBTRUSIVELY");
			try
			{
				Command C=CMClass.getCommand("Get");
				commands.insertElementAt("GET",0);
				if(C!=null) C.execute(mob,commands,0);
			}
			catch(Exception e)
			{}
		}
		return success;
	}
}
