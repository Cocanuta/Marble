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
public class Carpentry extends EnhancedCraftingSkill implements ItemCraftor
{
	public String ID() { return "Carpentry"; }
	public String name(){ return "Carpentry";}
	private static final String[] triggerStrings = {"CARVE","CARPENTRY"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "WOODEN";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
	   +"ITEM_BASE_VALUE\tITEM_CLASS_ID\t"
	   +"LID_LOCK||STATUE||RIDE_BASIS||WEAPON_CLASS||CODED_WEAR_LOCATION||SMOKE_FLAG\t"
	   +"CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED||LIQUID_CAPACITY||LIGHT_DURATION\t"
	   +"BASE_ARMOR_AMOUNT||BASE_DAMAGE\tCONTAINER_TYPE||ATTACK_MODIFICATION\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	protected DoorKey key=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "carpentry.txt";}
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
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up carving "+building.name()+".");
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							building.setUsesRemaining(100);
						else
						if(activity==CraftingActivity.LEARNING)
						{
							deconstructRecipeInto( building, recipeHolder );
							building.destroy();
						}
						else
						if(activity == CraftingActivity.REFITTING)
						{
							building.basePhyStats().setHeight(0);
							building.recoverPhyStats();
						}
						else
						{
							dropAWinner(mob,building);
							if(key!=null)
							{
								dropAWinner(mob,key);
								if(building instanceof Container)
									key.setContainer((Container)building);
							}
						}
					}
				}
				building=null;
				key=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if(I instanceof MusicalInstrument)
			return false;
		if(I instanceof Rideable)
		{
			Rideable R=(Rideable)I;
			int rideType=R.rideBasis();
			switch(rideType)
			{
			case Rideable.RIDEABLE_LADDER:
			case Rideable.RIDEABLE_SLEEP:
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
				return true;
			default:
				return false;
			}
		}
		if(I instanceof Shield)
			return true;
		if(I instanceof Weapon)
		{
			Weapon W=(Weapon)I;
			if(((W.weaponClassification()!=Weapon.CLASS_BLUNT)&&(W.weaponClassification()!=Weapon.CLASS_STAFF))
			||(W.requiresAmmunition()))
				return false;
			return true;
		}
		if(I instanceof Light)
			return true;
		if(I instanceof Ammunition)
			return false;
		if(I instanceof Armor)
			return (isANativeItem(I.Name()));
		if(I instanceof Container)
		{
			Container C=(Container)I;
			if((C.containTypes()==Container.CONTAIN_CAGED)
			||(C.containTypes()==(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED)))
				return false;
			return true;
		}
		if((I instanceof Drink)&&(!(I instanceof Potion)))
			return true;
		if(I instanceof FalseLimb)
			return true;
		if(I.rawProperLocationBitmap()==Wearable.WORN_HELD)
			return true;
		return (isANativeItem(I.Name()));
	}

	public boolean supportsMending(Physical I){ return canMend(null,I,true);}
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if((!(E instanceof Item))
		||(!mayICraft((Item)E)))
		{
			if(!quiet)
				commonTell(mob,"That's not a carpentry item.");
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
			commonTell(mob,"Carve what? Enter \"carve list\" for a list, \"carve refit <item>\" to resize shoes or armor, \"carve learn <item>\", \"carve scan\", or \"carve mend <item>\".");
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
		String startStr=null;
		int duration=4;
		bundling=false;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("Item <S-NAME> <S-IS-ARE> skilled at carving:\n\r");
			int toggler=1;
			int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",15)+" Lvl "+CMStrings.padRight("Wood",4)+" ");
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
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,29)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRightPreserve(""+wood,4)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
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
			building=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob, building,false)) return false;
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		if(str.equalsIgnoreCase("refit"))
		{
			building=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			{
				commonTell(mob,"That's not made of wood.  That can't be refitted.");
				return false;
			}
			if(!(building instanceof Armor))
			{
				commonTell(mob,"You don't know how to refit that sort of thing.");
				return false;
			}
			if(building.phyStats().height()==0)
			{
				commonTell(mob,building.name()+" is already the right size.");
				return false;
			}
			activity = CraftingActivity.REFITTING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) refitting "+building.name()+".";
			displayText="You are refitting "+building.name();
			verb="refitting "+building.name();
		}
		else
		{
			building=null;
			activity = CraftingActivity.CRAFTING;
			aborted=false;
			key=null;
			messedUp=false;
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
					if((autoGenerate>0)||(level<=xlevel(mob)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to carve a '"+recipeName+"'.  Try \"carve list\" for a list.");
				return false;
			}
			
			final String woodRequiredStr = (String)foundRecipe.get(RCP_WOOD);
			final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
			if(componentsFoundList==null) return false;
			int woodRequired=CMath.s_int(woodRequiredStr);
			woodRequired=adjustWoodRequired(woodRequired,mob);
			
			if(amount>woodRequired) woodRequired=amount;
			String misctype=(String)foundRecipe.get(RCP_MISCTYPE);
			int[] pm={RawMaterial.MATERIAL_WOODEN};
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"wood",pm,
												0,null,null,
												bundling,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
				+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
			building=CMClass.getItem((String)foundRecipe.get(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(CMath.s_int((String)foundRecipe.get(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.get(RCP_LEVEL)),4);
			String itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) carving "+building.name()+".";
			displayText="You are carving "+building.name();
			playSound="sawing.wav";
			verb="carving "+building.name();
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.basePhyStats().setWeight((int)Math.round( (double)woodRequired * this.getItemWeightMultiplier( bundling )));
			building.setBaseValue(CMath.s_int((String)foundRecipe.get(RCP_VALUE)));
			building.setMaterial(data[0][FOUND_CODE]);
			int hardness=RawMaterial.CODES.HARDNESS(data[0][FOUND_CODE])-3;
			building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL))+(hardness));
			if(building.basePhyStats().level()<1) building.basePhyStats().setLevel(1);
			building.setSecretIdentity(getBrand(mob));
			int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
			long canContain=getContainerType((String)foundRecipe.get(RCP_CONTAINMASK));
			int armordmg=CMath.s_int((String)foundRecipe.get(RCP_ARMORDMG));
			if(bundling) building.setBaseValue(lostValue);
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.get(RCP_SPELL)).trim():"";
			addSpells(building,spell);
			key=null;
			if((building instanceof Container)
			&&(!(building instanceof Armor)))
			{
				if(capacity>0)
				{
					((Container)building).setCapacity(capacity+woodRequired);
					((Container)building).setContainTypes(canContain);
				}
				if(misctype.equalsIgnoreCase("LID"))
					((Container)building).setLidsNLocks(true,false,false,false);
				else
				if(misctype.equalsIgnoreCase("LOCK"))
				{
					((Container)building).setLidsNLocks(true,false,true,false);
					((Container)building).setKeyName(Double.toString(Math.random()));
					key=(DoorKey)CMClass.getItem("GenKey");
					((DoorKey)key).setKey(((Container)building).keyName());
					key.setName("a key");
					key.setDisplayText("a small key sits here");
					key.setDescription("looks like a key to "+building.name());
					key.recoverPhyStats();
					key.text();
				}
			}
			if(building instanceof Drink)
			{
				if(CMLib.flags().isGettable(building))
				{
					((Drink)building).setLiquidHeld(capacity*50);
					((Drink)building).setThirstQuenched(250);
					if((capacity*50)<250)
						((Drink)building).setThirstQuenched(capacity*50);
					((Drink)building).setLiquidRemaining(0);
				}
			}
			if(building instanceof Rideable)
			{
				setRideBasis((Rideable)building,misctype);
				if(capacity==0)
					((Rideable)building).setRiderCapacity(1);
				else
				if(capacity<5)
					((Rideable)building).setRiderCapacity(capacity);
			}
			if(building instanceof Weapon)
			{
				((Weapon)building).setWeaponClassification(Weapon.CLASS_BLUNT);
				setWeaponTypeClass((Weapon)building,misctype,Weapon.TYPE_SLASHING);
				building.basePhyStats().setAttackAdjustment((abilityCode()+(hardness*5)-1));
				building.basePhyStats().setDamage(armordmg+hardness);
				((Weapon)building).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				((Weapon)building).setRawLogicalAnd((capacity>1));
				if(!(building instanceof Container))
					building.basePhyStats().setAttackAdjustment(building.basePhyStats().attackAdjustment()+(int)canContain);
			}
			if(building instanceof Armor)
			{
				((Armor)building).basePhyStats().setArmor(0);
				if(armordmg!=0)
					((Armor)building).basePhyStats().setArmor(armordmg+(abilityCode()-1));
				setWearLocation(building,misctype,hardness);
			}
			if(building instanceof Light)
			{
				((Light)building).setDuration(capacity);
				if((building instanceof Container)
				&&(!misctype.equals("SMOKE")))
				{
					((Light)building).setDuration(200);
					((Container)building).setCapacity(0);
				}
			}
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
			if(key!=null) commands.add(key);
			commands.add(building);
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
