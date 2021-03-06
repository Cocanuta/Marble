package com.planet_ink.marble_mud.Items.MiscMagic;
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
public class Wand_Advancement extends StdWand implements ArchonOnly
{
	public String ID(){	return "Wand_Advancement";}
	public Wand_Advancement()
	{
		super();

		setName("a platinum wand");
		setDisplayText("a platinum wand is here.");
		setDescription("A wand made out of platinum");
		secretIdentity="The wand of Advancement.  Hold the wand say `level up` to it.";
		this.setUsesRemaining(50);
		material=RawMaterial.RESOURCE_OAK;
		baseGoldValue=20000;
		recoverPhyStats();
		secretWord="LEVEL UP";
	}


	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="LEVEL UP";
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="LEVEL UP";
	}


	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if((mob.isMine(this))
			   &&(!amWearingAt(Wearable.IN_INVENTORY))
			   &&(msg.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)msg.target())))
			{
				MOB target=(MOB)msg.target();
				int x=msg.targetMessage().toUpperCase().indexOf("LEVEL UP");
				if((!mob.isMonster())
				&&(x>=0)
				&&(mob.session().previousCMD()!=null)
				&&(CMParms.combine(mob.session().previousCMD(),0).toUpperCase().indexOf("LEVEL UP")<0))
					mob.tell("The wand fizzles in an irritating way.");
				else
				if(x>=0)
				{
					if((usesRemaining()>0)&&(useTheWand(CMClass.getAbility("Falling"),mob,0)))
					{
						this.setUsesRemaining(this.usesRemaining()-1);
						CMMsg msg2=CMClass.getMsg(mob,msg.target(),null,CMMsg.MSG_HANDS,CMMsg.MSG_OK_ACTION,CMMsg.MSG_OK_ACTION,"<S-NAME> point(s) "+this.name()+" at <T-NAMESELF>, who begins to glow softly.");
						if(mob.location().okMessage(mob,msg2))
						{
							mob.location().send(mob,msg2);
							if((target.charStats().getCurrentClass().leveless())
							||(target.charStats().isLevelCapped(target.charStats().getCurrentClass()))
							||(target.charStats().getMyRace().leveless())
							||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
								mob.tell("The wand will not work on such as "+target.name()+".");
							else
							if((target.getExpNeededLevel()==Integer.MAX_VALUE)
							||(target.charStats().getCurrentClass().expless())
							||(target.charStats().getMyRace().expless()))
								CMLib.leveler().level(target);
							else
								CMLib.leveler().postExperience(target,null,null,target.getExpNeededLevel()+1,false);
						}

					}
				}
			}
			return;
		default:
			break;
		}
	}
}
