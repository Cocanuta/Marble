package com.planet_ink.marble_mud.Abilities.Paladin;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class PaladinSkill extends StdAbility
{
	public String ID() { return "PaladinSkill"; }
	public String name(){ return "Paladin Skill";}
	public String displayText(){return "";}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected Vector paladinsGroup=null;
	public int classificationCode(){ return Ability.ACODE_SKILL;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		if(invoker==null) invoker=(MOB)affected;
		if(!(CMLib.flags().isGood(invoker)))
			return false;
		if(paladinsGroup!=null)
		{
			Set<MOB> H=((MOB)affected).getGroupMembers(new HashSet<MOB>());
			for(Iterator e=H.iterator();e.hasNext();)
			{
				MOB mob=(MOB)e.next();
				if(!paladinsGroup.contains(mob))
					paladinsGroup.addElement(mob);
			}
			for(int i=paladinsGroup.size()-1;i>=0;i--)
			{
				try
				{
					MOB mob=(MOB)paladinsGroup.elementAt(i);
					if((!H.contains(mob))
					||(mob.location()!=invoker.location()))
						paladinsGroup.removeElement(mob);
				}
				catch(java.lang.ArrayIndexOutOfBoundsException e)
				{
				}
			}
		}
		if(CMLib.dice().rollPercentage()==1)
			helpProficiency(invoker, 0);
		return true;
	}

	public boolean autoInvocation(MOB mob)
	{
		if(mob.charStats().getCurrentClass().ID().equals("Archon"))
			return false;
		return super.autoInvocation(mob);
	}
}
