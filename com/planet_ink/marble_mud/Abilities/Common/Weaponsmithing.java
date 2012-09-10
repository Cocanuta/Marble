package com.planet_ink.marble_mud.Abilities.Common;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.Common.CraftingSkill.CraftingActivity;
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
public class Weaponsmithing extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "Weaponsmithing"; }
	public String name(){ return "Weaponsmithing";}
	private static final String[] triggerStrings = {"WEAPONSMITH","WEAPONSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "METAL|MITHRIL";}
	protected int displayColumns(){return 2;}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tWEAPON_CLASS\tWEAPON_TYPE\tBASE_DAMAGE\tATTACK_MODIFICATION\t"
		+"WEAPON_HANDS_REQUIRED\tMAXIMUM_RANGE\tOPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_WEAPONCLASS=6;
	protected static final int RCP_WEAPONTYPE=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_ATTACK=9;
	protected static final int RCP_HANDS=10;
	protected static final int RCP_MAXRANGE=11;
	protected static final int RCP_EXTRAREQ=12;
	protected static final int RCP_SPELL=13;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}

	public String parametersFile(){ return "weaponsmith.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+building.name()+".");
							building.destroy();
						}
						else
							commonEmote(mob,"<S-NAME> mess(es) up smithing "+building.name()+".");
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( building, recipeHolder );
						building.destroy();
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							building.setUsesRemaining(100);
						else
							dropAWinner(mob,building);
					}
				}
				building=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}


	protected int specClass(String weaponClass)
	{
		for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
		{
			if(Weapon.CLASS_DESCS[i].equalsIgnoreCase(weaponClass))
				return i;
		}
		return -1;
	}
	protected int specType(String weaponType)
	{
		for(int i=0;i<Weapon.TYPE_DESCS.length;i++)
		{
			if(Weapon.TYPE_DESCS[i].equalsIgnoreCase(weaponType))
				return i;
		}
		return -1;
	}
	protected boolean canDo(String weaponClass, MOB mob)
	{
		if((mob.isMonster())&&(!CMLib.flags().isAnimalIntelligence(mob)))
			return true;

		if(mob.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Commoner"))
			return true;
		String specialization="";
		switch(specClass(weaponClass))
		{
		case Weapon.CLASS_AXE: specialization="Specialization_Axe"; break;
		case Weapon.CLASS_STAFF:
		case Weapon.CLASS_HAMMER:
		case Weapon.CLASS_BLUNT: specialization="Specialization_BluntWeapon"; break;
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED: specialization="Specialization_EdgedWeapon"; break;
		case Weapon.CLASS_FLAILED: specialization="Specialization_FlailedWeapon"; break;
		case Weapon.CLASS_POLEARM: specialization="Specialization_Polearm"; break;
		case Weapon.CLASS_SWORD: specialization="Specialization_Sword"; break;
		case Weapon.CLASS_THROWN:
		case Weapon.CLASS_RANGED: specialization="Specialization_Ranged"; break;
		default: return false;
		}
		if(mob.fetchAbility(specialization)==null) return false;
		return true;
	}

	protected boolean masterCraftCheck(final Item I)
	{
		if(I.basePhyStats().level()>30)
			return false;
		if(I.name().toUpperCase().startsWith("MASTER")||(I.name().toUpperCase().indexOf(" MASTER ")>0))
			return false;
		return true;
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
		&&((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if(!masterCraftCheck(I))
			return (isANativeItem(I.Name()));
		if(!(I instanceof Weapon))
			return false;
		Weapon W=(Weapon)I;
		if(W.requiresAmmunition())
			return false;
		return true;
	}

	public boolean supportsMending(Physical I){ return canMend(null,I,true);}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,"That's not "+CMLib.english().startWithAorAn(Name().toLowerCase())+" item.");
			return false;
		}
		return true;
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;
		fireRequired=true;
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		DVector enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"weaponsmith list\" for a list, \"weaponsmith scan\", \"weaponsmith learn <item>\", or \"weaponsmith mend <item>\".");
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		bundling=false;
		String startStr=null;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("Weapons <S-NAME> <S-IS-ARE> skilled at making:\n\r");
			int toggler=1;
			int toggleTop=displayColumns();
			int itemWidth=(78/toggleTop)-9;
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",itemWidth)+" Lvl "+CMStrings.padRight("Amt",3)+((r<(toggleTop-1)?" ":"")));
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
					{
						if(toggler>1) buf.append("\n\r");
						toggler=toggleTop;
					}
					if(((autoGenerate>0)
						||((level<=xlevel(mob))&&((canDo((String)V.get(RCP_WEAPONCLASS),mob)))))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,itemWidth)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRightPreserve(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonEmote(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			building=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob,building,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		{
			activity = CraftingActivity.CRAFTING;
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			building=null;
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
			{
				amount=CMath.s_int((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				List<String> V=matches.get(r);
				if(V.size()>0)
				{
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					if((autoGenerate>0)||((level<=mob.phyStats().level())
										&&(canDo((String)V.get(RCP_WEAPONCLASS),mob))))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try 'list' instead.");
				return false;
			}
			
			final String woodRequiredStr = (String)foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);
			
			if(amount>woodRequired) woodRequired=amount;
			String otherRequired=(String)foundRecipe.get(RCP_EXTRAREQ);
			int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.get(RCP_SPELL)).trim():"";
			bundling=spell.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												otherRequired.length()>0?1:0,otherRequired,null,
												false,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			woodRequired=data[0][FOUND_AMT];

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null)
				+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
			building=CMClass.getItem((String)foundRecipe.get(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(CMath.s_int((String)foundRecipe.get(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.get(RCP_LEVEL)),4);
			String itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) smithing "+building.name()+".";
			displayText="You are smithing "+building.name();
			verb="smithing "+building.name();
			playSound="tinktinktink2.wav";
			int hardness=RawMaterial.CODES.HARDNESS(data[0][FOUND_CODE])-6;
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.basePhyStats().setWeight((int)Math.round( (double)woodRequired * this.getItemWeightMultiplier( bundling )));
			building.setBaseValue((CMath.s_int((String)foundRecipe.get(RCP_VALUE))/4)+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			building.setMaterial(data[0][FOUND_CODE]);
			building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL))+(hardness*3));
			building.setSecretIdentity(getBrand(mob));
			if(bundling) building.setBaseValue(lostValue);
			addSpells(building,spell);
			if(building instanceof Weapon)
			{
				Weapon w=(Weapon)building;
				w.setWeaponClassification(specClass((String)foundRecipe.get(RCP_WEAPONCLASS)));
				w.setWeaponType(specType((String)foundRecipe.get(RCP_WEAPONTYPE)));
				w.setRanges(w.minRange(),CMath.s_int((String)foundRecipe.get(RCP_MAXRANGE)));
			}
			if(CMath.s_int((String)foundRecipe.get(RCP_HANDS))==2)
				building.setRawLogicalAnd(true);
			building.basePhyStats().setAttackAdjustment(CMath.s_int((String)foundRecipe.get(RCP_ATTACK))+(hardness*5)+(abilityCode()-1));
			building.basePhyStats().setDamage(CMath.s_int((String)foundRecipe.get(RCP_ARMORDMG))+hardness);

			building.recoverPhyStats();
			building.text();
			building.recoverPhyStats();
		}

		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb="bundling "+RawMaterial.CODES.NAME(building.material()).toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,building,enhancedTypes);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
