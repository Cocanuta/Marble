package com.planet_ink.marble_mud.Abilities.Prayers;
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
import com.planet_ink.marble_mud.Libraries.interfaces.TrackingLibrary;
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
public class Prayer_LowerLaw extends Prayer
{
	public String ID() { return "Prayer_LowerLaw"; }
	public String name(){ return "Lower Law";}
	public String displayText(){ return "";}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}

	
	public void possiblyAddLaw(Law L, Vector<String> V, String code)
	{
		if(L.basicCrimes().containsKey(code))
		{
			final String name=((String[])L.basicCrimes().get(code))[Law.BIT_CRIMENAME];
			if(!V.contains(name)) V.add(name);
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for knowledge of the lower law here.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Area O=CMLib.law().getLegalObject(mob.location());
				LegalBehavior B=CMLib.law().getLegalBehavior(mob.location());
				if((B==null)||(O==null))
					mob.tell("No lower law is established here.");
				else
				{
					Law L=B.legalInfo(O);
					Vector<String> crimes=new Vector<String>();
					possiblyAddLaw(L,crimes,"TRESPASSING");
					possiblyAddLaw(L,crimes,"ASSAULT");
					possiblyAddLaw(L,crimes,"MURDER");
					possiblyAddLaw(L,crimes,"NUDITY");
					possiblyAddLaw(L,crimes,"ARMED");
					possiblyAddLaw(L,crimes,"RESISTINGARREST");
					possiblyAddLaw(L,crimes,"PROPERTYROB");
					for(String key : L.abilityCrimes().keySet())
						if(key.startsWith("$"))
							crimes.add(key.substring(1));
					if(L.taxLaws().containsKey("TAXEVASION"))
						crimes.add(((String[])L.taxLaws().get("TAXEVASION"))[Law.BIT_CRIMENAME]);
					for(int x=0;x<L.bannedSubstances().size();x++)
					{
						final String name=((String[])L.bannedBits().get(x))[Law.BIT_CRIMENAME];
						if(!crimes.contains(name)) crimes.add(name);
					}
					for(int x=0;x<L.otherCrimes().size();x++)
					{
						final String name=((String[])L.otherBits().get(x))[Law.BIT_CRIMENAME];
						if(!crimes.contains(name)) crimes.add(name);
					}
					mob.tell("The following lower crimes are divinely revealed to you: "+CMLib.english().toEnglishStringList(crimes.toArray(new String[0]))+".");
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but nothing is revealed.");

		return success;
	}
}
