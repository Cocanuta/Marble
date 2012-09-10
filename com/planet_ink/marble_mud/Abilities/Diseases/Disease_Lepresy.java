package com.planet_ink.marble_mud.Abilities.Diseases;
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

public class Disease_Lepresy extends Disease
{
	public String ID() { return "Disease_Lepresy"; }
	public String name(){ return "Leprosy";}
	public String displayText(){ return "(Leprosy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 10;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your leprosy is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) pale!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	public int difficultyLevel(){return 4;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.targetMessage()!=null))
		{
			if((msg.targetMessage().indexOf("<DAMAGE>")>=0)
			||(msg.targetMessage().indexOf("<DAMAGES>")>=0))
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),CMLib.combat().replaceDamageTag(msg.targetMessage(),1,0,'T'),
						  msg.othersCode(),msg.othersMessage());
			else
			if((msg.tool()!=null)&&(msg.tool() instanceof Weapon))
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),"^e^<FIGHT^>"+((Weapon)msg.tool()).hitString(1)+"^</FIGHT^>^?",
						  msg.othersCode(),msg.othersMessage());
		}
		return super.okMessage(myHost,msg);
	}

}
