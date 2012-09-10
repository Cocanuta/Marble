package com.planet_ink.marble_mud.Abilities.Ranger;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
import com.planet_ink.marble_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Ranger_Sneak extends StdAbility
{
	public String ID() { return "Ranger_Sneak"; }
	public String name(){ return "Woodland Sneak";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"WSNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_STEALTHY;}
	public int usageType(){return USAGE_MOVEMENT;}

	public int getMOBLevel(MOB meMOB)
	{
		if(meMOB==null) return 0;
		return meMOB.phyStats().level();
	}
	public MOB getHighestLevelMOB(MOB meMOB, Vector not)
	{
		if(meMOB==null) return null;
		Room R=meMOB.location();
		if(R==null) return null;
		int highestLevel=0;
		MOB highestMOB=null;
		Set<MOB> H=meMOB.getGroupMembers(new HashSet<MOB>());
		if(not!=null) H.addAll(not);
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=meMOB)
			&&(!H.contains(M))
			&&(highestLevel<M.phyStats().level()))
			{
				highestLevel=M.phyStats().level();
				highestMOB=M;
			}
		}
		return highestMOB;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String dir=CMParms.combine(commands,0);
		if(commands.size()>0) dir=(String)commands.lastElement();
		int dirCode=Directions.getGoodDirectionCode(dir);
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}

		if((((mob.location().domainType()&Room.INDOORS)>0))&&(!auto))
		{
			mob.tell("You must be outdoors to do this.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		&&(!auto))
		{
			mob.tell("You don't know how to sneak around a place like this.");
			return false;
		}

		if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("Sneak where?");
			return false;
		}

		MOB highestMOB=getHighestLevelMOB(mob,null);
		int levelDiff=(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)))-getMOBLevel(highestMOB);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*10;
			else
				levelDiff=levelDiff*5;
			success=proficiencyCheck(mob,levelDiff,auto);

			if(success)
			{
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SNEAKING);
				mob.recoverPhyStats();
			}
			CMLib.tracking().walk(mob,dirCode,false,false);
			if(success)
			{

				int disposition=mob.basePhyStats().disposition();
				if((disposition&PhyStats.IS_SNEAKING)>0)
				{
					mob.basePhyStats().setDisposition(disposition-PhyStats.IS_SNEAKING);
					mob.recoverPhyStats();
				}
			}
		}
		return success;
	}

}
