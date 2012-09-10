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
public class Thief_AnalyzeMark extends ThiefSkill
{
	public String ID() { return "Thief_AnalyzeMark"; }
	public String name(){ return "Analyze Mark";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;}

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}
	public int getMarkTicks(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target()!=null)
			&&(getMark(mob)==msg.target())
			&&(getMarkTicks(mob)>15)
			&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false)))
			{
				if(CMLib.dice().rollPercentage()>50) helpProficiency((MOB)affected, 0);
				StringBuilder str=CMLib.commands().getScore((MOB)msg.target());
				if(!mob.isMonster())
					mob.session().wraplessPrintln(str.toString());
			}
		}
		super.executeMsg(myHost,msg);
	}
}
