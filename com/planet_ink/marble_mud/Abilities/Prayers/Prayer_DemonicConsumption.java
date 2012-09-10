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
public class Prayer_DemonicConsumption extends Prayer
{
	public String ID() { return "Prayer_DemonicConsumption"; }
	public String name(){return "Demonic Consumption";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_VEXING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public long flags(){return Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=false;
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(!(target instanceof Item))
		{
			if(!auto)
				affectType=affectType|CMMsg.MASK_MALICIOUS;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(target instanceof MOB) levelDiff+=6;
		if(levelDiff<0) levelDiff=0;
		success=proficiencyCheck(mob,-(levelDiff*15),auto);

		if(auto)affectType=affectType|CMMsg.MASK_ALWAYS;

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+" treacherously!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Hashtable V=new Hashtable();
					for(int i=0;i<mob.location().numItems();i++)
					{
						Item item=mob.location().getItem(i);
						if((item!=null)&&(item instanceof DeadBody))
							V.put(item,item);
					}

					if(target instanceof MOB)
					{
						if(((MOB)target).curState().getHitPoints()>0)
							CMLib.combat().postDamage(mob,(MOB)target,this,(((MOB)target).curState().getHitPoints()*100),CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"^SThe evil <DAMAGE> <T-NAME>!^?");
						if(((MOB)target).amDead())
							mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> <T-IS-ARE> consumed!");
						else
							return false;
					}
					else
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> is consumed!");

					if(target instanceof Item)
						((Item)target).destroy();
					else
					{
						int i=0;
						while(i<mob.location().numItems())
						{
							int s=mob.location().numItems();
							Item item=mob.location().getItem(i);
							if((item!=null)&&(item instanceof DeadBody)&&(V.get(item)==null))
								item.destroy();
							if(s==mob.location().numItems())
								i++;
						}
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+" treacherously, but fizzle(s) the magic!");


		// return whether it worked
		return success;
	}
}
