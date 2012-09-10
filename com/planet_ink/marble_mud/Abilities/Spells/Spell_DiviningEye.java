package com.planet_ink.marble_mud.Abilities.Spells;
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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Spell_DiviningEye extends Spell
{
	public String ID() { return "Spell_DiviningEye"; }
	public String name(){return "Divining Eye";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("You must specify a divining spell and any parameters for it.");
			return false;
		}
		
		Ability pryingEyeA=mob.fetchEffect("Spell_PryingEye");
		if(pryingEyeA==null)
		{
			mob.tell("This spell requires an active prying eye.");
			return false;
		}
		
		String commandStr=CMParms.combine(commands);
		commands.insertElementAt("CAST",0);
		Ability A=CMLib.english().getToEvoke(mob, commands);
		if(A==null)
		{
			mob.tell("'"+commandStr+"' does not refer to any diviner spell you know.");
			return false;
		}
		if(((A.classificationCode() & Ability.ALL_ACODES)!=Ability.ACODE_SPELL)
		||((A.classificationCode() & Ability.ALL_DOMAINS)!=Ability.DOMAIN_DIVINATION))
		{
			mob.tell("'"+A.name()+"' is not a diviner spell you know.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":"^S<S-NAME> invoke(s) a remote divination!^?");
			final Room room=mob.location();
			if(room.okMessage(mob,msg))
			{
				final MOB eyeM=(MOB)pryingEyeA.affecting();
				room.send(mob,msg);
				try
				{
					final Room eyeRoom=eyeM.location();
					if(eyeRoom!=null)
					{
						eyeM.addAbility(A);
						A.invoke(eyeM, commands, null, false, 0);
					}
				}
				finally
				{
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke something, but fail(s).");

		// return whether it worked
		return success;
	}
}
