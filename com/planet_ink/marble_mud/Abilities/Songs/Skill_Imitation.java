package com.planet_ink.marble_mud.Abilities.Songs;
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
public class Skill_Imitation extends BardSkill
{
	public String ID() { return "Skill_Imitation"; }
	public String name(){ return "Imitate";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"IMITATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE;}
	public String lastID="";
	public int craftType(){return Ability.ACODE_SPELL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public STreeMap<String,String> immitations=new STreeMap<String,String>();
	public String[] lastOnes=new String[2];

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
		   return;
		MOB mob=(MOB)myHost;
		if(msg.tool()!=null)
		{
			if((msg.amISource(mob))
			&&((msg.tool().ID().equals("Skill_Spellcraft"))
				||(msg.tool().ID().equals("Skill_Songcraft"))
				||(msg.tool().ID().equals("Skill_Chantcraft"))
				||(msg.tool().ID().equals("Skill_Prayercraft")))
			&&(msg.tool().text().equals(lastOnes[0]))
			&&(msg.tool().text().length()>0)
			&&(!immitations.containsKey(msg.tool().text())))
			{
				Ability A=CMClass.getAbility(msg.tool().text());
				if(A!=null)	immitations.put(A.name(),lastOnes[1]);
			}
			else
			if((msg.tool() instanceof Ability)
			&&(!msg.amISource(mob))
			&&(msg.othersMessage()!=null))
			{
				lastOnes[0]=msg.tool().ID();
				lastOnes[1]=msg.othersMessage();
			}
		}

	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Environmental target=null;
		if(commands.size()>1)
		{
			target=mob.location().fetchFromRoomFavorMOBs(null,(String)commands.lastElement());
			if(target==null) target=mob.findItem(null,(String)commands.lastElement());
			if(target!=null) commands.removeElementAt(commands.size()-1);
		}
		String cmd=(commands.size()>0)?CMParms.combine(commands,0).toUpperCase():"";
		StringBuffer str=new StringBuffer("");
		String found=null;
		for(String key : immitations.keySet())
		{
			if((cmd.length()>0)&&(key.toUpperCase().startsWith(cmd)))
				found=key;
			str.append(key+" ");
		}
		if((cmd.length()==0)||(found==null))
		{
			if(found!=null) mob.tell("'"+cmd+"' is not something you know how to imitate.");
			mob.tell("Spells/Skills you may imitate: "+str.toString()+".");
			return true;
		}
		if(target==null) target=mob.getVictim();
		if(target==null) target=mob;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_DELICATE|(auto?CMMsg.MASK_ALWAYS:0),(String)immitations.get(found));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to imitate "+found+", but fail(s).");

		return success;
	}
}
