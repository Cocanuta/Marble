package com.planet_ink.marble_mud.Abilities.Properties;
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

/**
 * <p>Title: False Realities Flavored marblemud</p>
 * <p>Description: The False Realities Version of marblemud</p>
 * <p>Copyright: Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

@SuppressWarnings("rawtypes")
public class Prop_ScrapExplode extends Property {

	public String ID() { return "Prop_ScrapExplode"; }
	public String name() { return "Scrap Explode"; }
	protected int canAffectCode() { return Ability.CAN_ITEMS; }

	public void executeMsg(Environmental myHost, CMMsg affect)
	{
		super.executeMsg(myHost, affect);
		if((affect.target()!=null)&&(affect.target().equals(affected))
		   &&(affect.tool()!=null)&&(affect.tool().ID().equals("Scrapping")))
		{
			Item item=(Item)affect.target();
			MOB mob = affect.source();
			Room room = mob.location();
			int damage = 3 * item.phyStats().weight();
			if (mob != null)
			{
				CMLib.combat().postDamage(mob, mob, item, damage*2,  CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE, Weapon.TYPE_PIERCING,
						"Scrapping " + item.Name() + " causes an explosion which <DAMAGE> <T-NAME>!!!");
				Set<MOB> theBadGuys=mob.getGroupMembers(new HashSet<MOB>());
				for(Iterator e=theBadGuys.iterator();e.hasNext();)
				{
					MOB inhab=(MOB)e.next();
					if (mob != inhab)
						CMLib.combat().postDamage(inhab, inhab, item, damage, CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE, Weapon.TYPE_PIERCING,
								"Fragments from " + item.Name() + " <DAMAGE> <T-NAME>!");
				}
			}
			item.destroy();
			room.recoverRoomStats();
		}
	}
}
