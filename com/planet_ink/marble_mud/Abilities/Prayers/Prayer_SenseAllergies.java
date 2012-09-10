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
public class Prayer_SenseAllergies extends Prayer
{
	public String ID() { return "Prayer_SenseAllergies"; }
	public String name(){ return "Sense Allergies";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":(mob==target)?"^S<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own nostrils.^?":"^S<S-NAME> peer(s) into the nostrils of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect("Allergies");
				if(A==null)
					mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is not allergic to anything.");
				else
				{
					
					Vector allergies=new Vector();
					Vector<String> V=CMParms.parse(A.text().toUpperCase().trim());
					for(int i=0;i<V.size();i++)
					{
						if(CMParms.contains(RawMaterial.CODES.NAMES(), (String)V.elementAt(i)))
							allergies.addElement(((String)V.elementAt(i)).toLowerCase());
						else
						{
							Race R=CMClass.getRace((String)V.elementAt(i));
							if(R!=null)
								allergies.addElement(R.name());
						}
					}
					mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is allergic to "+CMParms.toStringList(V) +".");
				}
			}
		}
		else
		if(mob==target)
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own nostrils, but then blink(s).");
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> peer(s) into the nostrils of <T-NAMESELF>, but then blink(s).");


		// return whether it worked
		return success;
	}
}
