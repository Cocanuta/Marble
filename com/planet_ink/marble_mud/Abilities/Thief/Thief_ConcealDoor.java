package com.planet_ink.marble_mud.Abilities.Thief;
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
public class Thief_ConcealDoor extends ThiefSkill
{
	public String ID() { return "Thief_ConcealDoor"; }
	public String name(){ return "Conceal Door";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}
	private static final String[] triggerStrings = {"DOORCONCEAL","DCONCEAL","CONCEALDOOR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=Integer.MIN_VALUE;

	public int abilityCode(){
		if(code<0) code=CMath.s_int(text());
		return code;
	}
	public void setAbilityCode(int newCode){code=newCode; super.miscText=""+newCode;}

	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		if((host instanceof Exit)&&(!((Exit)host).isOpen()))
		{
			stats.setDisposition(stats.disposition()|PhyStats.IS_HIDDEN);
			// cant affect level because will make it unpickable, therefore unopenable
			// need some other way to designate its hiddenitude.
			//stats.setLevel(stats.level()+abilityCode());
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_OPEN)
		&&(super.canBeUninvoked()))
		{
			unInvoke();
			affected.delEffect(this);
			affected.recoverPhyStats();
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Which door would you like to conceal?");
			return false;
		}
		Environmental chkE=null;
		String typed=CMParms.combine(commands,0);
		if(Directions.getGoodDirectionCode(typed)<0)
			chkE=mob.location().fetchFromMOBRoomItemExit(mob,null,typed,Wearable.FILTER_WORNONLY);
		else
			chkE=mob.location().getExitInDir(Directions.getGoodDirectionCode(typed));
		if((!(chkE instanceof Exit))||(!CMLib.flags().canBeSeenBy(chkE,mob)))
		{
			mob.tell("You don't see any doors called '"+typed+"' here.");
			return false;
		}
		Exit X=(Exit)chkE;
		if(!X.hasADoor())
		{
			mob.tell(mob,X,null,"<T-NAME> is not a door!");
			return false;
		}
		if((!auto)&&(X.phyStats().level()>((adjustedLevel(mob,asLevel)*2))))
		{
			mob.tell("You aren't good enough to conceal that door.");
			return false;
		}
		if(X.isOpen())
		{
			mob.tell(mob,X,null,"You'd better close <T-NAME> first.");
			return false;
		}
		

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,X,null,CMMsg.MSG_THIEF_ACT,"<S-NAME> conceal(s) <T-NAME>.",CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=(Ability)super.copyOf();
				A.setInvoker(mob);
				A.setAbilityCode((adjustedLevel(mob,asLevel)*2)-X.phyStats().level());
				Room R=mob.location();
				Room R2=null;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(R.getExitInDir(d)==X)
					{ R2=R.getRoomInDir(d); break;}
				if((CMLib.law().doesOwnThisProperty(mob,R))
				||((R2!=null)&&(CMLib.law().doesOwnThisProperty(mob,R2))))
				{
					X.addNonUninvokableEffect(A);
					CMLib.database().DBUpdateExits(mob.location());
				}
				else
					A.startTickDown(mob,X,15*(adjustedLevel(mob,asLevel)));
				X.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,X,"<S-NAME> attempt(s) to coneal <T-NAME>, but obviously fail(s).");
		return success;
	}
}
