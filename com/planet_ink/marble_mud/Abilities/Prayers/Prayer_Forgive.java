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
public class Prayer_Forgive extends Prayer
{
	public String ID() { return "Prayer_Forgive"; }
	public String name(){return "Forgive";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		LegalBehavior B=null;
		if(mob.location()!=null) B=CMLib.law().getLegalBehavior(mob.location());

		String name=CMParms.combine(commands,0);
		if(name.startsWith("$")) name=name.substring(1);
		if(name.endsWith("$")) name=name.substring(0,name.length()-1);
		if((name.trim().length()==0)&&(givenTarget!=null)) name=givenTarget.Name(); 
		if(name.trim().length()==0)
		{
			mob.tell("Forgive whom?");
			return false;
		}
		List<LegalWarrant> warrants=new Vector();
		List<MOB> criminals=new Vector();
		if(B!=null)
		{
			criminals=B.getCriminals(CMLib.law().getLegalObject(mob.location()),name);
			if(criminals.size()>0)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),(MOB)criminals.get(0));
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(warrants.size()==0)
				beneficialWordsFizzle(mob,null,"<S-NAME> "+prayForWord(mob)+" to forgive "+name+" for no reason at all.");
			else
			{
				CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to forgive "+name+".^?");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					for(int i=0;i<warrants.size();i++)
					{
						LegalWarrant W=(LegalWarrant)warrants.get(i);
						W.setCrime("pardoned");
						W.setOffenses(0);
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayForWord(mob)+" to forgive "+name+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
