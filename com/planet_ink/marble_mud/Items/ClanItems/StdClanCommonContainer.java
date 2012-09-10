package com.planet_ink.marble_mud.Items.ClanItems;
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
public class StdClanCommonContainer extends StdClanContainer
{
	public String ID(){	return "StdClanCommonContainer";}
	protected int workDown=0;
	public StdClanCommonContainer()
	{
		super();

		setName("a clan workers container");
		basePhyStats.setWeight(1);
		setDisplayText("an workers container belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		capacity=100;
		setCIType(ClanItem.CI_GATHERITEM);
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
		&&(((MOB)owner()).getClanID().equals(clanID()))
		&&((--workDown)<=0)
		&&(!CMLib.flags().isAnimalIntelligence((MOB)owner())))
		{
			workDown=CMLib.dice().roll(1,5,0);
			MOB M=(MOB)owner();
			if(M.fetchEffect(readableText())==null)
			{
				Ability A=CMClass.getAbility(readableText());
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				{
					A.setProficiency(100);
					if(M.numItems()>1)
					{
						Item I=null;
						int tries=0;
						while((I==null)&&((++tries)<20))
						{
							I=M.getRandomItem();
							if((I==null)||(I==this)||(!I.amWearingAt(Wearable.IN_INVENTORY)))
								I=null;
						}
						Vector V=new Vector();
						if(I!=null)	V.addElement(I.name());
						A.invoke(M,V,null,false,phyStats().level());
					}
					else
						A.invoke(M,new Vector(),null,false,phyStats().level());
				}

			}
		}
		return true;
	}
}
