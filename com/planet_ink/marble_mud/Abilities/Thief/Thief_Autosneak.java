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
public class Thief_Autosneak extends ThiefSkill
{
	public String ID() { return "Thief_Autosneak"; }
	public String displayText() {return "(AutoSneak)";}
	public String name(){ return "AutoSneak";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"AUTOSNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_STEALTHY; }
	protected boolean noRepeat=false;

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected instanceof MOB)
		&&(!noRepeat)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(((MOB)affected).location()!=null))
		{
			int dir=-1;
			MOB mob=(MOB)affected;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if((mob.location().getRoomInDir(d)==msg.target())
				||(mob.location().getReverseExit(d)==msg.tool())
				||(mob.location().getExitInDir(d)==msg.tool()))
				{ dir=d; break;}
			if(dir>=0)
			{
				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A==null) A=mob.fetchAbility("Ranger_Sneak");
				if(A!=null)
				{
					noRepeat=true;
					if(A.invoke(mob,CMParms.parse(Directions.getDirectionName(dir)),null,false,0))
					{
						int[] usage=A.usageCost(mob,false);
						if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS)&&(usage[USAGEINDEX_HITPOINTS]>0))
							mob.curState().adjHitPoints(usage[USAGEINDEX_HITPOINTS]/2,mob.maxState());
						if(CMath.bset(A.usageType(),Ability.USAGE_MANA)&&(usage[USAGEINDEX_MANA]>0))
							mob.curState().adjMana(usage[USAGEINDEX_MANA]/2,mob.maxState());
						if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT)&&(usage[USAGEINDEX_MOVEMENT]>0))
							mob.curState().adjMovement(usage[USAGEINDEX_MOVEMENT]/2,mob.maxState());
					}
					if(CMLib.dice().rollPercentage()<10)
						helpProficiency(mob, 0);
					noRepeat=false;
				}
				return false;
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell("You are no longer automatically sneaking around.");
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell("You will now automatically sneak around while you move.");
			beneficialAffect(mob,mob,asLevel,adjustedLevel(mob,asLevel));
			Ability A=mob.fetchEffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to get into <S-HIS-HER> sneaking stance, but fail(s).");
		return success;
	}

}
