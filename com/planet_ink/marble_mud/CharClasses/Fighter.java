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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Fighter extends StdCharClass
{
	public String ID(){return "Fighter";}
	public String name(){return "Fighter";}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return -1;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 7;}
	public int getManaDivisor(){return 8;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int getMovementMultiplier(){return 12;}
	
	public Fighter()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=7;
	}
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_ArmorTweaking",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Disarm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Subdue",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Attack2",true); 
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_TrueShot",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_CritStrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_ShieldBlock",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_BlindFighting",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_WeaponBreak",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_DualParry",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Trip",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Sweep",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_CriticalShot",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_Whomp",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Attack3",true,CMParms.parseSemicolons("Skill_Attack2",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Endurance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_PointBlank",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Tumble",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_AutoBash",false,CMParms.parseSemicolons("Skill_Bash",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_SizeOpponent",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_Berzerk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_ImprovedShieldDefence",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_CoverDefence",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_WeaponCatch",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_CalledStrike",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_CounterAttack",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Heroism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Behead",0,"",false,true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_CoupDeGrace",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Fighter.");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
															 mob.charStats().getClassLevel(ID()),
															 false,
															 false);
			for(AbilityMapper.AbilityMapping able : V)
			{
				Ability A=CMClass.getAbility(able.abilityID);
				if((A!=null)
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public void executeMsg(Environmental host, CMMsg msg){ super.executeMsg(host,msg); Fighter.conquestExperience(this,host,msg);}
	public String getOtherBonusDesc(){return "Receives bonus conquest experience.";}
	public static void conquestExperience(CharClass C, Environmental host, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_AREAAFFECT)
		&&(msg.target() instanceof Area)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().equalsIgnoreCase("CONQUEST"))
		&&(host instanceof MOB)
		&&(((MOB)host).charStats().getCurrentClass().ID().equals(C.ID()))
		&&(msg.source().Name().equals(((MOB)host).getClanID()))
		)
		{
			Area A=(Area)msg.target();
			int xp=(int)Math.round(50.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],((MOB)host).phyStats().level()));
			if(xp>500) xp=500;
			if(xp>0)
			{
				((MOB)host).tell("^YVictory!!^N");
				CMLib.leveler().postExperience((MOB)host,null,null,xp,false);
			}
		}
	}
	
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.add(w);
		}
		return outfitChoices;
	}
}
