package com.planet_ink.marble_mud.Abilities.Common;
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
public class FireBuilding extends CommonSkill
{
	public String ID() { return "FireBuilding"; }
	public String name(){ return "Fire Building";}
	private static final String[] triggerStrings = {"LIGHT","FIREBUILD","FIREBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_NATURELORE; }

	public Item lighting=null;
	protected int durationOfBurn=0;
	protected boolean failed=false;

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(failed)
					commonTell(mob,"You failed to get the fire started.");
				else
				{
					if(lighting==null)
					{
						Item I=CMClass.getItem("GenItem");
						I.basePhyStats().setWeight(50);
						I.setName("a roaring campfire");
						I.setDisplayText("A roaring campfire has been built here.");
						I.setDescription("It consists of dry wood, burning.");
						I.recoverPhyStats();
						I.setMaterial(RawMaterial.RESOURCE_WOOD);
						mob.location().addItem(I);
						lighting=I;
					}
					Ability B=CMClass.getAbility("Burning");
					B.invoke(mob,lighting,true,durationOfBurn);
				}
				lighting=null;
			}
		}
		super.unInvoke();
	}

	public boolean fireHere(Room R)
	{
		for(int i=0;i<R.numItems();i++)
		{
			Item I2=R.getItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
				return true;
		}
		return false;
	}
	
	public Vector resourceHere(Room R, int material)
	{
		Vector here=new Vector();
		for(int i=0;i<R.numItems();i++)
		{
			Item I2=R.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(I2 instanceof RawMaterial)
			&&(((I2.material()&RawMaterial.RESOURCE_MASK)==material)
				||(((I2.material())&RawMaterial.MATERIAL_MASK)==material))
			&&(!CMLib.flags().enchanted(I2)))
				here.addElement(I2);
		}
		return here;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			if((!fireHere(mob.location()))
			&&(resourceHere(mob.location(),RawMaterial.MATERIAL_WOODEN).size()>0))
				commands.addElement(((Environmental)resourceHere(mob.location(),RawMaterial.MATERIAL_WOODEN).firstElement()).Name());
			else
				commands.addElement("fire");
		}

		if(commands.size()==0)
		{
			commonTell(mob,"Light what?  Try light fire, or light torch...");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String name=CMParms.combine(commands,0);
		int proficiencyAdjustment=0;
		int duration=6;
		if(name.equalsIgnoreCase("fire"))
		{
			lighting=null;
			if((mob.location().domainType()&Room.INDOORS)>0)
			{
				commonTell(mob,"You can't seem to find any deadwood around here.");
				return false;
			}
			switch(mob.location().domainType())
			{
			case Room.DOMAIN_OUTDOORS_HILLS:
			case Room.DOMAIN_OUTDOORS_JUNGLE:
			case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			case Room.DOMAIN_OUTDOORS_PLAINS:
			case Room.DOMAIN_OUTDOORS_WOODS:
				break;
			default:
				commonTell(mob,"You can't seem to find any dry deadwood around here.");
				return false;
			}
			duration=getDuration(25,mob,1,3);
			durationOfBurn=150+(xlevel(mob)*5);
			verb="building a fire";
			displayText="You are building a fire.";
		}
		else
		{
			lighting=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
			if(lighting==null) return false;
			
			if((lighting.displayText().length()==0)
			||(!CMLib.flags().isGettable(lighting)))
			{
				commonTell(mob,"For some reason, "+lighting.name()+" just won't catch.");
				return false;
			}
			if(lighting instanceof Light)
			{
				Light l=(Light)lighting;
				if(l.isLit())
				{
					commonTell(mob,l.name()+" is already lit!");
					return false;
				}
				if(CMLib.flags().isGettable(lighting))
					commonTell(mob,"Just hold this item to light it.");
				else
				{
					l.light(true);
					mob.location().show(mob,lighting,CMMsg.TYP_HANDS,"<S-NAME> light(s) <T-NAMESELF>.");
					return true;
				}
				return false;
			}
			if(!(lighting instanceof RawMaterial))
			{
				LandTitle t=CMLib.law().getLandTitle(mob.location());
				if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
				{
					mob.tell("You are not allowed to burn anything here.");
					return false;
				}
			}
			durationOfBurn=CMLib.flags().burnStatus(lighting); 
			if(durationOfBurn<0)
			{
				commonTell(mob,"You need to cook that, if you can.");
				return false;
			}
			else
			if(durationOfBurn==0)
			{
				commonTell(mob,"That won't burn.");
				return false;
			}
			if((lighting.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
				duration=getDuration(25,mob,1,3);
			verb="lighting "+lighting.name();
			displayText="You are lighting "+lighting.name()+".";
		}

		switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_THUNDERSTORM:
			proficiencyAdjustment=-80;
			break;
		case Climate.WEATHER_DROUGHT:
			proficiencyAdjustment=50;
			break;
		case Climate.WEATHER_DUSTSTORM:
		case Climate.WEATHER_WINDY:
			proficiencyAdjustment=-10;
			break;
		case Climate.WEATHER_HEAT_WAVE:
			proficiencyAdjustment=10;
			break;
		case Climate.WEATHER_RAIN:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_HAIL:
			proficiencyAdjustment=-50;
			break;
		}
		failed=!proficiencyCheck(mob,proficiencyAdjustment,auto);

		durationOfBurn=durationOfBurn*abilityCode();
		if(duration<4) duration=4;

		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> start(s) building a fire.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
			FireBuilding fireBuild = (FireBuilding)mob.fetchEffect(ID());
			if(fireBuild!=null)
				fireBuild.durationOfBurn = this.durationOfBurn;
			
		}
		return true;
	}
}
