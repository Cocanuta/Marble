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
public class Evoker extends SpecialistMage
{
	public String ID(){return "Evoker";}
	public String name(){return "Evoker";}
	public int domain(){return Ability.DOMAIN_EVOCATION;}
	public int opposed(){return Ability.DOMAIN_ALTERATION;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_ContinualLight",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Shockshield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_IceLance",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Ignite",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_ForkedLightning",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Levitate",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_IceStorm",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_Shove",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Blademouth",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_LimbRack",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_MassDisintegrate",25,true);
	}
}
