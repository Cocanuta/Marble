package com.planet_ink.marble_mud.Items.Weapons;
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
public class Cestus extends StdWeapon
{
	public String ID(){	return "Cestus";}
	public Cestus()
	{
		super();

		setName("a mean looking cestus");
		setDisplayText("a cestus is on the gound.");
		setDescription("It\\`s a glove covered in long spikes and blades.");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(2);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(4);
		material=RawMaterial.RESOURCE_LEATHER;
		baseGoldValue=5;
		recoverPhyStats();
		weaponType=Weapon.TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_EDGED;

	}



}
