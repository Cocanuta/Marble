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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prayer_MassCureDisease extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_MassCureDisease"; }
	public String name(){ return "Mass Cure Disease";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean supportsMending(Physical item)
	{ 
		if(!(item instanceof MOB)) return false;
		boolean canMend=returnOffensiveAffects(item).size()>0;
		return canMend;
	}
	
	public List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A instanceof DiseaseAffect))
				offenders.addElement(A);
		}
		return offenders;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(supportsMending((MOB)target))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"A healing glow surrounds this place.":"^S<S-NAME> "+prayWord(mob)+" to cure disease here.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean worked=false;
				TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
				List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,100);
				for(Iterator<Room> r=checkSet.iterator();r.hasNext();)
				{
					Room R=CMLib.map().getRoom(r.next());
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB target=R.fetchInhabitant(m);
						if(target!=null)
						{
							List<Ability> offensiveAffects=returnOffensiveAffects(target);
							if(offensiveAffects.size()>0)
							{
								boolean badOnes=false;
								for(int a=offensiveAffects.size()-1;a>=0;a--)
								{
									Ability A=((Ability)offensiveAffects.get(a));
									if(A instanceof DiseaseAffect)
									{
										if((A.invoker()!=mob)
										&&((((DiseaseAffect)A).difficultyLevel()*10)>adjustedLevel(mob,asLevel)))
											badOnes=true;
										else
											A.unInvoke();
									}
									else
										A.unInvoke();
									if(target.fetchEffect(A.ID())==null)
										worked=true;
								}
								if(badOnes)
									mob.tell(mob,target,null,"<T-NAME> had diseases too powerful for this magic.");
								if(!CMLib.flags().stillAffectedBy(target,offensiveAffects,false))
									target.tell("You feel much better!");
							}
						}
					}
				}
				if((worked)&&(!auto))
					mob.tell("Your healing prayer has cured the sick.");
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),auto?"":"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
