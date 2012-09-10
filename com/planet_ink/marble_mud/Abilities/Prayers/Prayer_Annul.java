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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Prayer_Annul extends Prayer
{
	public String ID() { return "Prayer_Annul"; }
	public String name(){ return "Annul";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!target.isMarriedToLiege())
		{
			mob.tell(target.name()+" is not married!");
			return false;
		}
		if(target.fetchWornItem("wedding band")!=null)
		{
			mob.tell(target.name()+" must remove the wedding band first.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> annul(s) the marriage between <T-NAMESELF> and "+target.getLiegeID()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				if((!target.isMonster())&&(target.soulMate()==null))
					CMLib.coffeeTables().bump(target,CoffeeTableRow.STAT_DIVORCES);
				mob.location().send(mob,msg);
				List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DIVORCES);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel((String)channels.get(i),mob.getClanID(),target.name()+" and "+target.getLiegeID()+" just had their marriage annulled.",true);
				MOB M=CMLib.players().getPlayer(target.getLiegeID());
				if(M!=null) M.setLiegeID("");
				target.setLiegeID("");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
