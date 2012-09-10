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
public class Fishing extends GatheringSkill
{
	public String ID() { return "Fishing"; }
	public String name(){ return "Fishing";}
	private static final String[] triggerStrings = {"FISH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;}
	public String supportedResourceString(){return "FLESH";}

	protected Item found=null;
	protected String foundShortName="";
	public Fishing()
	{
		super();
		displayText="You are fishing...";
		verb="fishing";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
					commonTell(mob,"You got a tug on the line!");
				else
				{
					StringBuffer str=new StringBuffer("Nothing is biting around here.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

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
				if((found!=null)&&(!aborted)&&(!helping))
				{
					int amount=CMLib.dice().roll(1,3,0)*(abilityCode());
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to catch "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItem(newFound,ItemPossessor.Expire.Player_Drop);
						if((mob.riding()!=null)&&(mob.riding() instanceof Container))
							newFound.setContainer((Container)mob.riding());
						CMLib.commands().postGet(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		
		int foundFish=-1;
		boolean maybeFish=false;
		if(mob.location()!=null)
		{
			for(int fishCode : RawMaterial.CODES.FISHES())
				if(mob.location().myResource()==fishCode)
				{
					foundFish=fishCode;
					maybeFish=true;
				}
				else
				if((mob.location().resourceChoices()!=null)
				&&(mob.location().resourceChoices().contains(Integer.valueOf(fishCode))))
					maybeFish=true;
		}
		if(!maybeFish)
		{
			commonTell(mob,"The fishing doesn't look too good around here.");
			return false;
		}
		verb="fishing";
		found=null;
		playSound="fishreel.wav";
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((proficiencyCheck(mob,0,auto))
		   &&(foundFish>0))
		{
			found=(Item)CMLib.materials().makeResource(foundFish,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		int duration=getDuration(45,mob,1,15);
		CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) fishing.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
