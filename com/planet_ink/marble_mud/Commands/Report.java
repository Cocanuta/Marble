package com.planet_ink.marble_mud.Commands;
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
public class Report extends Skills
{
	public Report(){}

	private final String[] access={"REPORT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			StringBuffer buf=new StringBuffer(
								"say \"I have "+mob.curState().getHitPoints()
							   +"/"+mob.maxState().getHitPoints()+" hit points, "
							   +mob.curState().getMana()+"/"+mob.maxState().getMana()
							   +" mana, "+mob.curState().getMovement()
							   +"/"+mob.maxState().getMovement()+" move");
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()
			&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			   buf.append(", and need "+mob.getExpNeededLevel()+" to level");
			buf.append(".\"");
			Command C=CMClass.getCommand("Say");
			if(C!=null) C.execute(mob,CMParms.parse(buf.toString()),metaFlags);
		}
		else
		{
			int level=parseOutLevel(commands);
			String s=CMParms.combine(commands,1).toUpperCase();
			StringBuffer say=new StringBuffer("");
			if("AFFECTS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{
				
				StringBuffer aff=new StringBuffer("\n\r^!I am affected by:^? ");
				Command C=CMClass.getCommand("Affect");
				if(C!=null) aff.append(C.executeInternal(mob,metaFlags,mob).toString());
				say.append(aff.toString());
			}
			if("STATS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{
				StringBuffer stats=new StringBuffer("");
				int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
				CharStats CT=mob.charStats();
				for(int i : CharStats.CODES.BASE())
					stats.append("^c" + CMStrings.capitalizeAndLower(CMStrings.limit(CharStats.CODES.NAME(i),3))+": ^w"
							+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)
							+"/"+(max+CT.getStat(CharStats.CODES.toMAXBASE(i)))+", ");
				say.append("\n\r^NMy stats:^? "+stats.toString());
			}
			if(s.equalsIgnoreCase("ALL"))
			{
				
				Vector V=new Vector();
				V.addElement(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
				V.addElement(Integer.valueOf(Ability.ACODE_SKILL));
				V.addElement(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
				V.addElement(Integer.valueOf(Ability.ACODE_SPELL));
				V.addElement(Integer.valueOf(Ability.ACODE_PRAYER));
				V.addElement(Integer.valueOf(Ability.ACODE_SUPERPOWER));
				V.addElement(Integer.valueOf(Ability.ACODE_CHANT));
				V.addElement(Integer.valueOf(Ability.ACODE_SONG));
				say.append("\n\r^NMy skills:^? "+getAbilities(null,mob,V,Ability.ALL_ACODES,false,level));
			}
			else
			if("SPELLS".startsWith(s))
				say.append("\n\r^NMy spells:^? "+getAbilities(null,mob,Ability.ACODE_SPELL,-1,false,level));
			else
			if("SKILLS".startsWith(s))
			{
				Vector V=new Vector();
				V.addElement(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
				V.addElement(Integer.valueOf(Ability.ACODE_SKILL));
				V.addElement(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
				say.append("\n\r^NMy skills:^? "+getAbilities(null,mob,V,Ability.ALL_ACODES,false,level));
			}
			else
			if("PRAYERS".startsWith(s))
				say.append("\n\r^NMy prayers:^? "+getAbilities(null,mob,Ability.ACODE_PRAYER,-1,false,level));
			else
			if(("POWERS".startsWith(s))||("SUPER POWERS".startsWith(s)))
				say.append("\n\r^NMy super powers:^? "+getAbilities(null,mob,Ability.ACODE_SUPERPOWER,-1,false,level));
			else
			if("CHANTS".startsWith(s))
				say.append("\n\r^NMy chants:^? "+getAbilities(null,mob,Ability.ACODE_CHANT,-1,false,level));
			else
			if("SONGS".startsWith(s))
				say.append("\n\r^NMy songs:^? "+getAbilities(null,mob,Ability.ACODE_SONG,-1,false,level));
			
			
			if(say.length()==0)
				mob.tell("'"+s+"' is unknown.  Try SPELLS, SKILLS, PRAYERS, CHANTS, SONGS, STATS, or ALL.");
			else
				CMLib.commands().postSay(mob,null,say.toString(),false,false);
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	
}
