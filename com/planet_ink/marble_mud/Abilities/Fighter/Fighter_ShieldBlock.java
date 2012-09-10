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

public class Fighter_ShieldBlock extends FighterSkill
{
	public int hits=0;
	public String ID() { return "Fighter_ShieldBlock"; }
	public String name(){ return "Shield Block";}
	public String displayText(){return "";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_SHIELDUSE;}
	private boolean enabledFlag=true;

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		&&(enabledFlag)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(proficiencyCheck(null,mob.charStats().getStat(CharStats.STAT_DEXTERITY)-90+(2*getXLEVELLevel(mob)),false))
		&&(msg.source().getVictim()==mob))
		{
			CMMsg msg2=CMClass.getMsg(msg.source(),mob,mob.fetchHeldItem(),CMMsg.MSG_QUIETMOVEMENT,"<T-NAME> block(s) <S-YOUPOSS> attack with <O-NAME>!");
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				helpProficiency(mob, 0);
				return false;
			}
		}
		return true;
	}
	
	public void affectPhyStats(Physical affected, PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if(affected instanceof MOB)
		{
			Item shield=((MOB)affected).fetchHeldItem();
			enabledFlag=(shield instanceof Shield);
			if(enabledFlag)
			{
				stats.setArmor(stats.armor()+(int)Math.round(
					CMath.mul(shield.phyStats().armor(),
						CMath.mul(getXLEVELLevel((MOB)affected),0.5))));
			}
		}

	}
}
