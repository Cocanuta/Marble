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
public class Spell_FoolsGold extends Spell
{
	public String ID() { return "Spell_FoolsGold"; }
	public String name(){return "Fools Gold";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	boolean destroyOnNextTick=false;
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!destroyOnNextTick) return super.tick(ticking,tickID);
		((Item)affected).destroy();
		destroyOnNextTick=false;
		return false;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(affected instanceof Item))
		{
			if((msg.amITarget(affected))&&(msg.targetMinor()==CMMsg.TYP_GET)&&(msg.source()!=invoker))
				destroyOnNextTick=true;
			else
			if((msg.tool()!=null)&&(msg.tool()==affected)&&(msg.targetMinor()==CMMsg.TYP_GIVE))
				destroyOnNextTick=true;
		}
	}
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()==0)||(CMath.s_int(CMParms.combine(commands,0))==0))
		{
			mob.tell("You must specify how big of a pile of gold to create.");
			return false;
		}
		int amount=CMath.s_int(CMParms.combine(commands,0));
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item gold=CMClass.getItem("GenItem");
				switch(amount)
				{
				case 1:
					gold.setName("a gold coin");
					gold.setDisplayText("a gold coin sits here");
					break;
				case 2:
					gold.setName("two gold coins");
					gold.setDisplayText("two gold coins sit here");
					break;
				default:
					gold.setName("a pile of "+amount+" gold coins");
					gold.setDisplayText(gold.name()+" sit here");
					break;
				}
				gold.basePhyStats().setWeight(0);
				gold.recoverPhyStats();
				mob.addItem(gold);
				mob.location().show(mob,null,gold,CMMsg.MSG_OK_ACTION,"Suddenly, <S-NAME> hold(s) <O-NAME>.");
				destroyOnNextTick=false;
				beneficialAffect(mob,gold,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms around dramatically, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
