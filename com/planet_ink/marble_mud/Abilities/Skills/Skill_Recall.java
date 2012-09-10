package com.planet_ink.marble_mud.Abilities.Skills;
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
public class Skill_Recall extends StdSkill
{
	public String ID() { return "Skill_Recall"; }
	public String name(){ return "Recall";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"RECALL","/"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean group=false;//"GROUP".startsWith(CMParms.combine(commands,0).toUpperCase());
		boolean success=(!mob.isInCombat())||proficiencyCheck(mob,getXLEVELLevel(mob)*10,auto);
		if(success)
		{
			Room recalledRoom=mob.location();
			if(recalledRoom == null) return false;
			Room recallRoom=CMLib.map().getStartRoom(mob);
			if((recallRoom==null)&&(!mob.isMonster()))
			{
				mob.setStartRoom(CMLib.login().getDefaultStartRoom(mob));
				recallRoom=CMLib.map().getStartRoom(mob);
			}
			
			if(recallRoom == null)
			{
				mob.tell("You've nowhere to recall TO!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,auto?"<S-NAME> disappear(s) into the Java Plane!":"<S-NAME> recall(s) body and spirit to the Java Plane!");
			CMMsg msg2=CMClass.getMsg(mob,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
			if(((recalledRoom.okMessage(mob,msg))&&(recallRoom.okMessage(mob,msg2)))
			||CMSecurity.isAllowed(mob,recalledRoom,CMSecurity.SecFlag.GOTO))
			{
				if(mob.isInCombat())
					CMLib.commands().postFlee(mob,"NOWHERE");
				recalledRoom.send(mob,msg);
				recallRoom.send(mob,msg2);
				if(recalledRoom.isInhabitant(mob))
					recallRoom.bringMobHere(mob,false);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					
					if((follower!=null)
					&&(follower.isMonster())
					&&(!follower.isPossessing())
					&&(CMLib.flags().isInTheGame(follower,true))
					&&(!CMath.bset(follower.getBitmap(),MOB.ATT_AUTOGUARD)))
					{
						Room fRecalledRoom=recalledRoom;
						if(group)fRecalledRoom=follower.location();
						msg=CMClass.getMsg(follower,fRecalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,auto?"<S-NAME> disappear(s) into the Java Plane!":"<S-NAME> <S-IS-ARE> sucked into the vortex created by "+mob.name()+"s recall.");
						if(((follower.location()==fRecalledRoom))
						&&(fRecalledRoom.isInhabitant(follower))
						&&(fRecalledRoom.okMessage(follower,msg)||CMSecurity.isAllowed(mob,recalledRoom,CMSecurity.SecFlag.GOTO)))
						{
							msg2=CMClass.getMsg(follower,fRecalledRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
							if(recallRoom.okMessage(follower,msg2)||CMSecurity.isAllowed(mob,recalledRoom,CMSecurity.SecFlag.GOTO))
							{
								if(follower.isInCombat())
									CMLib.commands().postFlee(follower,("NOWHERE"));
								recallRoom.send(follower,msg2);
								if(fRecalledRoom.isInhabitant(follower))
									recallRoom.bringMobHere(follower,false);
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to recall, but <S-HIS-HER> plea goes unheard.");

		// return whether it worked
		return success;
	}

}
