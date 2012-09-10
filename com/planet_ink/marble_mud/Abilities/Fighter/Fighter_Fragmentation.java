package com.planet_ink.marble_mud.Abilities.Fighter;
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

public class Fighter_Fragmentation extends FighterSkill
{
	public String ID() { return "Fighter_Fragmentation"; }
	public String name(){ return "Fragmentation";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target() instanceof Weapon)
		&&(msg.tool()==this))
			((Weapon)msg.target()).destroy();
		super.executeMsg(myHost,msg);
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof Weapon)
		&&(msg.value()>0)
		&&(msg.target() instanceof MOB)
		&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))
		{
			if(CMLib.dice().rollPercentage()<25) helpProficiency((MOB)affected, 0);
			CMMsg msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),this,CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^><T-NAME> fragment(s) in <S-NAME>!^</FIGHT^>^?");
			CMLib.color().fixSourceFightColor(msg2);
			msg.addTrailerMsg(msg2);
			msg.setValue(msg.value()+(int)Math.round(CMath.mul(3.0 * msg.value(),CMath.div(proficiency(),100.0-(10.0*getXLEVELLevel(invoker()))))));
		}

		return super.okMessage(myHost,msg);
	}

}
