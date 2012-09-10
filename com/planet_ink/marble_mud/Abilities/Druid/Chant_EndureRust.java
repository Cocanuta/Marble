package com.planet_ink.marble_mud.Abilities.Druid;
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
public class Chant_EndureRust extends Chant
{
	public String ID() { return "Chant_EndureRust"; }
	public String name(){ return "Endure Rust";}
	public String displayText(){return "(Endure Rust)";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;}
	protected HashSet dontbother=new HashSet();

	public void unInvoke()
	{
		if((affected instanceof MOB)&&(canBeUninvoked()))
			((MOB)affected).tell("Your rust endurance fades.");
		super.unInvoke();
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((((msg.target()==affected)&&(affected instanceof Item))
			||(msg.target() instanceof Item)&&(affected instanceof MOB)&&(((MOB)affected).isMine(msg.target())))
		&&(msg.targetMinor()==CMMsg.TYP_WATER))
		{
			if(!dontbother.contains(msg.target()))
			{
				Room R=CMLib.map().roomLocation(affected);
				dontbother.add(msg.target());
				if(R!=null)
					R.show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,"<T-NAME> resist(s) the oxidizing affects.");
			}
			return false;
		}
		return super.okMessage(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=this.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;
		if(target instanceof Item)
		{
		}
		else
		if(target instanceof MOB)
		{
		}
		else
		{
			mob.tell("This chant won't affect "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, causing a rust proof film to envelope <T-HIM-HER>!^?");
		if(mob.location().okMessage(mob,msg))
		{
			dontbother.clear();
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but fail(s).");

		return success;
	}
}
