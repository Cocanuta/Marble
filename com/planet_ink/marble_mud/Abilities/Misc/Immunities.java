package com.planet_ink.marble_mud.Abilities.Misc;
import java.util.HashSet;
import java.util.Vector;

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
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

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

public class Immunities extends StdAbility
{
	public String ID() { return "Immunities"; }
	public String name(){ return "Immunities";}
	protected String displayText="";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return canBeUninvoked;}
	public int resistanceCode=0;
	public boolean canBeUninvoked = false;
	public HashSet<Integer> immunes=new HashSet<Integer>();

	public static SHashtable<String,Integer> immunityTypes=new SHashtable<String,Integer>(new Object[][]
	{
		{"ACID",Integer.valueOf(CMMsg.TYP_ACID)},
		{"WATER",Integer.valueOf(CMMsg.TYP_WATER)},
		{"COLD",Integer.valueOf(CMMsg.TYP_COLD)},
		{"DISEASE",Integer.valueOf(CMMsg.TYP_DISEASE)},
		{"ELECTRIC",Integer.valueOf(CMMsg.TYP_ELECTRIC)},
		{"FIRE",Integer.valueOf(CMMsg.TYP_FIRE)},
		{"GAS",Integer.valueOf(CMMsg.TYP_GAS)},
		{"JUSTICE",Integer.valueOf(CMMsg.TYP_JUSTICE)},
		{"MIND",Integer.valueOf(CMMsg.TYP_MIND)},
		{"PARALYZE",Integer.valueOf(CMMsg.TYP_PARALYZE)},
		{"POISON",Integer.valueOf(CMMsg.TYP_POISON)},
		{"UNDEAD",Integer.valueOf(CMMsg.TYP_UNDEAD)},
		{"LEGAL",Integer.valueOf(CMMsg.TYP_LEGALWARRANT)},
	});
	
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		immunes.clear();
		final Vector<String> immunities=CMParms.parse(text.toUpperCase());
		for(final String v : immunities)
			if(v.equalsIgnoreCase("ALL"))
			{
				for(final String key : immunityTypes.keySet())
					immunes.add(immunityTypes.get(key));
			}
			else
			if(immunityTypes.containsKey(v))
				immunes.add(immunityTypes.get(v));
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!mob.amDead())
		&&(immunes.contains(Integer.valueOf(msg.targetMinor()))
			|| immunes.contains(Integer.valueOf(msg.sourceMinor())))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)||(msg.targetMinor()==CMMsg.TYP_DAMAGE)||(msg.targetMinor()==CMMsg.TYP_LEGALWARRANT)))
		{
			String immunityName="certain";
			if(msg.tool()!=null)
				immunityName=msg.tool().name();
			if(!msg.sourceMajor(CMMsg.MASK_CNTRLMSG) && !msg.targetMajor(CMMsg.MASK_CNTRLMSG))
			{
				if(mob!=msg.source())
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+" attacks from <T-NAME>.");
				else
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+".");
			}
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel)
	{
		StringBuilder immunes=new StringBuilder("");
		int ticksOverride=0;
		if(commands.size()>0)
			for(final Object o : commands)
			{
				final String s=o.toString().toUpperCase();
				if(s.startsWith("TICKS=")&&(CMath.isInteger(s.substring(6).trim())))
					ticksOverride=CMath.s_int(s.substring(6).trim());
				else
				if(CMath.isInteger(s.trim()))
					ticksOverride=CMath.s_int(s.trim());
				else
				if(immunityTypes.containsKey(s))
					immunes.append(s).append(" ");
			}
		if(!super.invoke(mob, commands, target, auto, asLevel))
			return false;
		if(immunes.length()>0)
		{
			if(!beneficialAffect(mob, mob, asLevel, ticksOverride))
				return false;
			Immunities A=(Immunities)mob.fetchEffect(ID());
			if(A==null)
				return false;
			A.setMiscText(immunes.toString().trim());
			A.canBeUninvoked=true;
		}
		return true;
	}
}

