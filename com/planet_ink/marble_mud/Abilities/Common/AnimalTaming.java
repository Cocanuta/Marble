package com.planet_ink.marble_mud.Abilities.Common;
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

@SuppressWarnings("rawtypes")
public class AnimalTaming extends CommonSkill
{
	public String ID() { return "AnimalTaming"; }
	public String name(){ return "Animal Taming";}
	private static final String[] triggerStrings = {"TAME","TAMING","ANIMALTAMING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }

	protected Physical taming=null;
	protected boolean messedUp=false;
	public AnimalTaming()
	{
		super();
		displayText="You are taming...";
		verb="taming";
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((taming==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof MOB)&&(!mob.location().isInhabitant((MOB)taming)))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof Item)&&(!mob.location().isContent((Item)taming)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((taming!=null)&&(!aborted))
				{
					MOB animal=null;
					if(taming instanceof MOB)
						animal=(MOB)taming;
					else
					if((taming!=null)&&(taming instanceof CagedAnimal))
						animal=((CagedAnimal)taming).unCageMe();
					if((messedUp)||(animal==null))
						commonTell(mob,"You've failed to tame "+taming.name()+"!");
					else
					{
						if(animal.numBehaviors()==0)
							commonTell(mob,taming.name()+" is already tame.");
						else
						{
							int amount=1;
							amount=amount*abilityCode();
							if(amount>animal.numBehaviors())
								amount=animal.numBehaviors();
							String s="";
							if(amount>1)
								s="of "+amount+" ";
							s+="of "+animal.charStats().hisher()+" behaviors";
							mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to tame "+animal.name()+" "+s+".");
							for(int i=0;i<amount;i++)
							{
								if(animal.numBehaviors()==0) break;
								Behavior B=animal.fetchBehavior(CMLib.dice().roll(1,animal.numBehaviors(),-1));
								if(B!=null)	{
									animal.delBehavior(B);
								}
								animal.recoverCharStats();
								animal.recoverPhyStats();
								animal.recoverMaxState();
							}
							animal.resetToMaxState();
							if(taming instanceof CagedAnimal)
							{
								animal.text();
								((CagedAnimal)taming).cageMe(animal);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		verb="taming";
		taming=null;
		Item cage=null;
		String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		taming=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			if((!M.isMonster())
			   ||(!CMLib.flags().isAnimalIntelligence(M)))
			{
				commonTell(mob,"You can't tame "+M.name()+".");
				return false;
			}
			if((CMLib.flags().canMove(M))&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,M.name()+" doesn't seem willing to cooperate.");
				return false;
			}
			taming=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().getItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{ cage=I; break;}
			}
			if(commands.size()>0)
			{
				String last=(String)commands.lastElement();
				Item I=mob.location().findItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.removeElement(last);
				}
			}
			if(cage==null)
			{
				commonTell(mob,"You don't see anyone called '"+str+"' here.");
				return false;
			}
			taming=mob.location().findItem(cage,CMParms.combine(commands,0));
			if((taming==null)||(!CMLib.flags().canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTell(mob,"You don't see any creatures in "+cage.name()+" called '"+CMParms.combine(commands,0)+"'.");
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}
		else
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-taming.phyStats().level()+(2*getXLEVELLevel(mob)),auto);
		int duration=getDuration(35,mob,taming.phyStats().level(),10);
		verb="taming "+M.name();
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) taming "+M.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
