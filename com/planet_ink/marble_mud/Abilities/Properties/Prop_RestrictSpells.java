package com.planet_ink.marble_mud.Abilities.Properties;
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
public class Prop_RestrictSpells extends Property
{
	public String ID() { return "Prop_RestrictSpells"; }
	public String name(){ return "Specific Spell Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(text().toUpperCase().indexOf(msg.tool().ID().toUpperCase())>=0))
		{
			Room roomS=null;
			Room roomD=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB)&&(((MOB)msg.target()).location()!=null))
				roomD=((MOB)msg.target()).location();
			else
			if((msg.source()!=null)&&(msg.source().location()!=null))
				roomS=msg.source().location();
			else
			if((msg.target()!=null)&&(msg.target() instanceof Room))
				roomD=(Room)msg.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			Ability A=(Ability)msg.tool();
			if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
			{
				if(roomS!=null)
					roomS.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
				if(roomD!=null)
					roomD.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
				if((msg.source()!=null)
				&&(msg.source().location()!=null)
				&&(msg.source().location()!=roomS)
				&&(msg.source().location()!=roomD))
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			}
			return false;
		}
		return true;
	}
}
