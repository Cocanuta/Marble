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
public class Burglar extends Thief
{
	public String ID(){return "Burglar";}
	public String name(){return "Burglar";}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_BURGLAR;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	
	public Burglar()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
	}
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false,"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Appraise",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Palm",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_Intimidate",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_TagTurf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Peek",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Observation",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Forgery",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Listen",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_ImprovedHiding",false,CMParms.parseSemicolons("Thief_Hide",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Steal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_TurfWar",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_SlipItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_ImprovedPeek",false,CMParms.parseSemicolons("Thief_Peek",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_PlantItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Detection",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Bribe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_ImprovedSwipe",false,CMParms.parseSemicolons("Thief_Swipe",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_SilentGold",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_Safecracking",false,CMParms.parseSemicolons("Thief_Pick",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_SilentLoot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_BlindFighting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Robbery",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Map",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_SenseLaw",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Mug",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Racketeer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_StripItem",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_UsePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_ImprovedSteal",false,CMParms.parseSemicolons("Thief_Steal",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_AnalyzeDweomer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Tumble",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Con",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Comprehension",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_Embezzle",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_ContractHit",true);
	}
	public String getStatQualDesc(){return "Dexterity 9+ Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Burglar.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Burglar.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
}
