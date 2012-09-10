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
@SuppressWarnings({"unchecked","rawtypes"})
public class Skill_Swim extends StdSkill
{
	public String ID() { return "Skill_Swim"; }
	public String name(){ return "Swim";}
	public String displayText(){ return "(Swimming)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"SWIM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS; }
	public int usageType(){return USAGE_MOVEMENT;}
	public double castingTime(final MOB mob, final List<String> cmds){return CMProps.getActionSkillCost(ID(),CMath.greater(CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFABLETIME),50.0),1.0));}
	public double combatCastingTime(final MOB mob, final List<String> cmds){return CMProps.getCombatActionSkillCost(ID(),CMath.greater(CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMABLETIME),50.0),1.0));}

	public boolean placeToSwim(Room r2)
	{
		if((r2==null)
		||((r2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(r2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		&&(r2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)))
			return false;
		return true;
	}
	public boolean placeToSwim(Environmental E)
	{ return placeToSwim(CMLib.map().roomLocation(E));}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}

	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		if(secondsElapsed==0)
		{
			int dirCode=Directions.getDirectionCode(CMParms.combine(commands,0));
			if(dirCode<0)
			{
				mob.tell("Swim where?");
				return false;
			}
			Room r=mob.location().getRoomInDir(dirCode);
			if(!placeToSwim(mob.location()))
			{
				if(!placeToSwim(r))
				{
					mob.tell("There is no water to swim on that way.");
					return false;
				}
			}
			else
			if((r!=null)
			&&(r.domainType()==Room.DOMAIN_OUTDOORS_AIR)
			&&(r.domainType()==Room.DOMAIN_INDOORS_AIR))
			{
				mob.tell("There is no water to swim on that way.");
				return false;
			}
	
			if((mob.riding()!=null)
			&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_WATER)
			&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_AIR))
			{
				mob.tell("You need to get off "+mob.riding().name()+" first!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) swimming "+Directions.getDirectionName(dirCode)+".");
			Room R=mob.location();
			if((R!=null)&&(R.okMessage(mob,msg)))
				R.send(mob,msg);
			else
				return false;
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int dirCode=Directions.getDirectionCode(CMParms.combine(commands,0));
		if(!preInvoke(mob,commands,givenTarget,auto,asLevel,0,0.0))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		Room R=mob.location();
		if((R!=null)
		&&(R.okMessage(mob,msg)))
		{
			R.send(mob,msg);
			success=proficiencyCheck(mob,0,auto);
			if(!success)
				R.show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> struggle(s) against the water, making no progress.");
			else
			{
				if(mob.fetchEffect(ID())==null)
					mob.addEffect(this);
				mob.recoverPhyStats();

				CMLib.tracking().walk(mob,dirCode,false,false);
			}
			mob.delEffect(this);
			mob.recoverPhyStats();
			if(mob.location()!=R)
				mob.location().show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		}
		return success;
	}
}
