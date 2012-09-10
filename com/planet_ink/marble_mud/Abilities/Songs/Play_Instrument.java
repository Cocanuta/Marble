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
@SuppressWarnings({"unchecked","rawtypes"})
public class Play_Instrument extends Play
{
	public String ID() { return "Play_Instrument"; }
	public String name(){ return "Instruments";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_WOODS;}
	public String mimicSpell(){return "";}

	protected void inpersistantAffect(MOB mob)
	{
		Ability A=getSpell();
		if((A!=null)
		&&((mob!=invoker())||(getSpell().abstractQuality()!=Ability.QUALITY_MALICIOUS)))
		{
			Vector chcommands=new Vector();
			chcommands.addElement(mob.name());
			A=(Ability)A.copyOf();
			A.invoke(invoker(),chcommands,mob,true,adjustedLevel(invoker(),0));
			if((A.abstractQuality()==Ability.QUALITY_MALICIOUS)
			&&(mob.isMonster())
			&&(!mob.isInCombat())
			&&(CMLib.flags().isMobile(mob))
			&&(!CMLib.flags().isATrackingMonster(mob))
			&&(mob.amFollowing()==null)
			&&(!mob.amDead())
			&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0)))
			{
				A=CMClass.getAbility("Thief_Assassinate");
				if(A!=null) A.invoke(mob,invoker(),true,0);
			}
		}
	}


	protected String songOf()
	{
		if(instrument!=null)
			return instrument.name();
		return name();
	}
	protected Ability getSpell()
	{
		return null;
	}
	public int abstractQuality()
	{
		if(getSpell()!=null) return getSpell().abstractQuality();
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}
	protected boolean persistantSong(){return false;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
}
