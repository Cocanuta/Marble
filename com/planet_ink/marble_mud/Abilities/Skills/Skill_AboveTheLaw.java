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
public class Skill_AboveTheLaw extends StdSkill
{
	public String ID() { return "Skill_AboveTheLaw"; }
	public String name(){ return "Above The Law";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected LegalBehavior B=null;
	protected Area O=null;
	protected Area A=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room room=mob.location();
			if(room!=null)
			{
				if((A==null)||(room.getArea()!=A))
				{
					if (isSavable()
					|| ((mob.getStartRoom() != null) && (room.getArea() == mob.getStartRoom().getArea())))
					{
						A=room.getArea();
						if(isSavable() || proficiencyCheck(mob,0,false))
						{
							O=CMLib.law().getLegalObject(A);
							B=CMLib.law().getLegalBehavior(room);
						}
					}
				}
			}
			if(B!=null)
			{
				final List<LegalWarrant> warrants=B.getWarrantsOf(O,mob);
				for(final LegalWarrant W : warrants)
				{
					W.setCrime("pardoned");
					W.setOffenses(0);
					if((!isSavable())&&(CMLib.dice().rollPercentage()<10))
						helpProficiency(mob, 0);
				}
			}
		}
		return true;
	}
}