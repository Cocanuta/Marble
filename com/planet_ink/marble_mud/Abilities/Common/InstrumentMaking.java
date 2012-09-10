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
public class InstrumentMaking extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "InstrumentMaking"; }
	public String name(){ return "Instrument Making";}
	private static final String[] triggerStrings = {"INSTRUMENTMAKING","INSTRUMENTMAKE"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "WOODEN";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tRIDE_CAPACITY||CODED_WEAR_LOCATION\tMETAL_OR_WOOD\tOPTIONAL_RACE_ID\tINSTRUMENT_TYPE";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_MATERIAL=7;
	protected static final int RCP_RACES=8;
	protected static final int RCP_TYPE=9;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "instruments.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public boolean supportsDeconstruction() { return true; }

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I)) 
			return false;
		if(isANativeItem(I.Name()))
			return true;
		if(I instanceof MusicalInstrument)
			return true;
		return false;
	}
	
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
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up making "+building.name()+".");
						building.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( building, recipeHolder );
						building.destroy();
					}
					else
						dropAWinner(mob,building);
				}
				building=null;
			}
		}
		super.unInvoke();
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
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what Instrument? Enter \"instrumentmake list\" for a list, \"instrumentmake learn <item>\" to gain recipes.");
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
		boolean archon=CMSecurity.isASysOp(mob)||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS);
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",16)+" Lvl "+CMStrings.padRight("Type",10)+" Material required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					String type=(String)V.get(RCP_MATERIAL);
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5) type="";
					String race=((String)V.get(RCP_RACES)).trim();
					String itype=CMStrings.capitalizeAndLower(((String)V.get(RCP_TYPE)).toLowerCase()).trim();
					if((level<xlevel(mob))
					&&((race.length()==0)||archon||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,16)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(itype,10)+" "+wood+" "+type+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		building=null;
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
				String race=((String)V.get(RCP_RACES)).trim();
				int level=CMath.s_int((String)V.get(RCP_LEVEL));
				if(((autoGenerate>0)||(level<=xlevel(mob)))
				&&((race.length()==0)||archon||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"instrumentmake list\" for a list.");
			return false;
		}
		
		final String woodRequiredStr = (String)foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);
		
		if(amount>woodRequired) woodRequired=amount;
		String materialRequired=(String)foundRecipe.get(RCP_MATERIAL);
		String misctype=(String)foundRecipe.get(RCP_MISCTYPE);
		int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
		if(!materialRequired.toUpperCase().startsWith("METAL"))
		{
			pm[0]=RawMaterial.MATERIAL_WOODEN;
			pm[1]=RawMaterial.MATERIAL_WOODEN;
		}
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"material",pm,
											0,null,null,
											bundling,
											autoGenerate,
											null);
		if(data==null) return false;
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
		startStr="<S-NAME> start(s) making "+building.name()+".";
		displayText="You are making "+building.name();
		verb="making "+building.name();
		playSound="sanding.wav";
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.basePhyStats().setWeight((int)Math.round( (double)woodRequired * this.getItemWeightMultiplier( bundling )));
		building.setBaseValue(CMath.s_int((String)foundRecipe.get(RCP_VALUE)));
		building.setMaterial(data[0][FOUND_CODE]);
		building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL)));
		if(building.basePhyStats().level()<1) building.basePhyStats().setLevel(1);
		String type=(String)foundRecipe.get(RCP_TYPE);
		for(int i=0;i<MusicalInstrument.TYPE_DESC.length;i++)
			if(type.equalsIgnoreCase(MusicalInstrument.TYPE_DESC[i]))
				((MusicalInstrument)building).setInstrumentType(i);
		building.setSecretIdentity(getBrand(mob));
		if(building instanceof Rideable)
		{
			((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
			((Rideable)building).setRiderCapacity(CMath.s_int(misctype));
			if(((Rideable)building).riderCapacity()<=0)
				((Rideable)building).setRiderCapacity(1);
		}
		else
		{
			setWearLocation(building,misctype,0);
		}
		if(bundling) building.setBaseValue(lostValue);
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();


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
