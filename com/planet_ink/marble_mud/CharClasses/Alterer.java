package com.planet_ink.marble_mud.CharClasses;
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
public class Alterer extends SpecialistMage
{
	public String ID(){return "Alterer";}
	public String name(){return "Alterer";}
	public int domain(){return Ability.DOMAIN_ALTERATION;}
	public int opposed(){return Ability.DOMAIN_EVOCATION;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().delCharAbilityMapping(ID(),"Spell_Shield");
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_MassFeatherfall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_IncreaseGravity",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_SlowProjectiles",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_MassSlow",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Timeport",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_GravitySlam",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_AlterSubstance",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_Duplicate",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_Wish",25,true);
	}
}
