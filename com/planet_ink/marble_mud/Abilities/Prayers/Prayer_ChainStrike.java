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
public class Prayer_ChainStrike extends Prayer
{
	public String ID() { return "Prayer_ChainStrike"; }
	public String name(){return "Chain Strike";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY|Ability.FLAG_AIRBASED;}
	public int maxRange(){return adjustedMaxInvokerRange(2);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null) h=new HashSet();

		Set<MOB> myGroup=mob.getGroupMembers(new HashSet<MOB>());
		Vector targets=new Vector();
		for(Iterator e=h.iterator();e.hasNext();)
			targets.addElement(e.next());
		for(Iterator e=myGroup.iterator();e.hasNext();)
		{
			MOB M=(MOB)e.next();
			if((M!=mob)&&(!targets.contains(M))) targets.addElement(M);
		}
		targets.addElement(mob);

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int damage = CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/2,1+(2*super.getX1Level(mob)));

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"A thunderous crack of electricity erupts!":"^S<S-NAME> "+prayForWord(mob)+" to send down a thunderous crack of electricity.^?")+CMProps.msp("lightning.wav",40)))
			{
				while(damage>0)
				for(int i=0;i<targets.size();i++)
				{
					MOB target=(MOB)targets.elementAt(i);
					if(target.amDead()||(target.location()!=mob.location()))
					{
						int count=0;
						for(int i2=0;i2<targets.size();i2++)
						{
							MOB M2=(MOB)targets.elementAt(i2);
							if((!M2.amDead())
							   &&(mob.location()!=null)
							   &&(mob.location().isInhabitant(M2))
							   &&(M2.location()==mob.location()))
								 count++;
						}
						if(count<2)
							return true;
						continue;
					}

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					boolean oldAuto=auto;
					if((target==mob)||(myGroup.contains(target)))
					   auto=true;
					CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_ALWAYS:0),null);
					auto=oldAuto;
					if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
					{
						mob.location().send(mob,msg);
						mob.location().send(mob,msg2);
						invoker=mob;

						int dmg=damage;
						if((msg.value()>0)||(msg2.value()>0))
							dmg = (int)Math.round(CMath.div(dmg,2.0));
						if(target.location()==mob.location())
						{
							CMLib.combat().postDamage(mob,target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The strike <DAMAGE> <T-NAME>!");
							damage = (int)Math.round(CMath.div(damage,2.0));
							if(damage<2){ damage=0; break;}
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a ferocious spell, but nothing happens.");


		// return whether it worked
		return success;
	}
}
