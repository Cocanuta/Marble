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
public class Abjurer extends SpecialistMage
{
	public String ID(){return "Abjurer";}
	public String name(){return "Abjurer";}
	public int domain(){return Ability.DOMAIN_ABJURATION;}
	public int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_SongShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_ResistBludgeoning",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_MinManaShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_Counterspell",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_ResistPiercing",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_ManaShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_ChantShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_ResistSlashing",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_PrayerShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_ResistIndignities",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_KineticBubble",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_MajManaShield",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_AchillesArmor",25,true);
	}
}
