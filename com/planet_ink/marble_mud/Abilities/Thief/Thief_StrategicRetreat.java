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
public class Thief_StrategicRetreat extends ThiefSkill
{
	public String ID() { return "Thief_StrategicRetreat"; }
	public String name(){ return "Strategic Retreat";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"FREEFLEE","STRATEGICRETREAT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You can only retreat from combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String where=CMParms.combine(commands,0);
		if(!success)
		{
			mob.tell("Your attempt to flee with grace and honor FAILS!");
			CMLib.commands().postFlee(mob,where);
		}
		else
		{
			int directionCode=-1;
			if(!where.equals("NOWHERE"))
			{
				if(where.length()==0)
				{
					Vector directions=new Vector();
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit thisExit=mob.location().getExitInDir(d);
						Room thisRoom=mob.location().getRoomInDir(d);
						if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
							directions.addElement(Integer.valueOf(d));
					}
					// up is last resort
					if(directions.size()>1)
						directions.removeElement(Integer.valueOf(Directions.UP));
					if(directions.size()>0)
					{
						directionCode=((Integer)directions.elementAt(CMLib.dice().roll(1,directions.size(),-1))).intValue();
						where=Directions.getDirectionName(directionCode);
					}
				}
				else
					directionCode=Directions.getGoodDirectionCode(where);
				if(directionCode<0)
				{
					mob.tell("Flee where?!");
					return false;
				}
				mob.makePeace();
				CMLib.tracking().walk(mob,directionCode,true,false);
			}
		}
		return success;
	}
}
