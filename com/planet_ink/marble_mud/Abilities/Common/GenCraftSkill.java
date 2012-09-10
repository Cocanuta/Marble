package com.planet_ink.marble_mud.Abilities.Common;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.CMClass.CMObjectType;
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
import com.planet_ink.marble_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;


import java.util.*;
import java.util.regex.Pattern;


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
public class GenCraftSkill extends EnhancedCraftingSkill implements ItemCraftor
{
	public String ID = "GenCraftSkill";
	public String ID() { return ID;}
	public String Name(){return name();}
	public String name(){ return (String)V(ID,V_NAME);}

	private static final Hashtable<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int V_NAME=0;//S
	private static final int V_TRIG=1;//S[]
	private static final int V_HELP=2;//S
	private static final int V_FNAM=3;//S
	private static final int V_RSCS=4;//S
	private static final int V_VERB=5;//S
	private static final int V_CNMN=6;//B
	private static final int V_CNRF=7;//B
	private static final int V_CNBN=8;//B
	private static final int V_SOND=9;//S
	private static final int NUM_VS=10;//S

	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\t"
	   +"ITEM_BASE_VALUE\tITEM_CLASS_ID\t"
	   +"LID_LOCK||STATUE||RIDE_BASIS||WEAPON_CLASS||CODED_WEAR_LOCATION||SMOKE_FLAG\t"
	   +"CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED||LIQUID_CAPACITY||LIGHT_DURATION\t"
	   +"BASE_ARMOR_AMOUNT||BASE_DAMAGE\tCONTAINER_TYPE||ATTACK_MODIFICATION\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_AMOUNTMATS=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	protected DoorKey key=null;

	public boolean supportsDeconstruction() { return false; }

	private static final Object[] makeEmpty()
	{
		Object[] O=new Object[NUM_VS];
		O[V_NAME]="Crafting Skill";
		O[V_TRIG]=new String[]{"CRAFT"};
		O[V_HELP]="<ABILITY>This skill is not yet documented.";
		O[V_FNAM]="";
		O[V_RSCS]="WOODEN";
		O[V_VERB]="crafting";
		O[V_CNMN]=Boolean.valueOf(true);
		O[V_CNRF]=Boolean.valueOf(true);
		O[V_CNBN]=Boolean.valueOf(true);
		O[V_SOND]="sawing.wav";
		return O;
	}

	public String[] triggerStrings(){return (String[])V(ID,V_TRIG);}

	public String parametersFile(){ return (String)V(ID,V_FNAM);}

	public String supportedResourceString(){return (String)V(ID,V_RSCS);}

	private static final Object V(String ID, int varNum)
	{
		if(vars.containsKey(ID)) return vars.get(ID)[varNum];
		Object[] O=makeEmpty();
		vars.put(ID,O);
		return O[varNum];
	}

	private static final void SV(String ID,int varNum,Object O)
	{
		if(vars.containsKey(ID))
			vars.get(ID)[varNum]=O;
		else
		{
			Object[] O2=makeEmpty();
			vars.put(ID,O2);
			O2[varNum]=O;
		}
	}

	public GenCraftSkill()
	{
		super();
	}

	public CMObject newInstance()
	{
		try
		{
			GenCraftSkill A=(GenCraftSkill)this.getClass().newInstance();
			A.ID=ID;
			return A;
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenCraftSkill();
	}

	protected void cloneFix(Ability E)
	{
	}

	public boolean isGeneric(){return true;}

	// lots of work to be done here
	public int getSaveStatIndex(){return getStatCodes().length;}

	private static final String[] CODES={"CLASS",//0
										 "TEXT",//1
										 "NAME",//2S
										 "HELP",//27I
										 "TRIGSTR",//4S[]
										 "FILENAME",//2S
										 "MATLIST",//2S
										 "VERB",//2S
										 "CANMEND",//2S
										 "CANREFIT",//2S
										 "CANBUNDLE",//2S
										 "SOUND",//2S
										};

	public String[] getStatCodes(){return CODES;}

	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}

	public String getStat(String code)
	{
		/*
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1)))) numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		*/
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return text();
		case 2: return (String)V(ID,V_NAME);
		case 3: return (String)V(ID,V_HELP);
		case 4: return CMParms.toStringList((String[])V(ID,V_TRIG));
		case 5: return (String)V(ID,V_FNAM);
		case 6: return ((String)V(ID,V_RSCS)).replace('|',',');
		case 7: return (String)V(ID,V_VERB);
		case 8: return Boolean.toString(((Boolean)V(ID,V_CNMN)).booleanValue());
		case 9: return Boolean.toString(((Boolean)V(ID,V_CNRF)).booleanValue());
		case 10: return Boolean.toString(((Boolean)V(ID,V_CNBN)).booleanValue());
		case 11: return (String)V(ID,V_SOND);
		default:
			if(code.equalsIgnoreCase("allxml")) return getAllXML();
			break;
		}
		return "";
	}

	public void setStat(String code, String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1)))) numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
		if(val.trim().length()>0)
		{
			V(ID,V_NAME); // force creation, if necc
			Object[] O=(Object[])vars.get(ID);
			vars.remove(ID);
			vars.put(val,O);
			if(num!=9)
				CMClass.delClass(CMObjectType.ABILITY,this);
			ID=val;
			if(num!=9)
				CMClass.addClass(CMObjectType.ABILITY,this);
		}
		break;
		case 1: setMiscText(val); break;
		case 2: SV(ID,V_NAME,val);
				if(ID.equalsIgnoreCase("GenCraftSkill"))
					break;
				break;
		case 3: SV(ID,V_HELP,val); break;
		case 4: SV(ID,V_TRIG,CMParms.parseCommas(val,true).toArray(new String[0])); break;
		case 5: SV(ID,V_FNAM,val); break;
		case 6: SV(ID,V_RSCS,val.toUpperCase().replace(',','|')); break;
		case 7: SV(ID,V_VERB,val); break;
		case 8: SV(ID,V_CNMN,Boolean.valueOf(CMath.s_bool(val))); break;
		case 9: SV(ID,V_CNRF,Boolean.valueOf(CMath.s_bool(val))); break;
		case 10: SV(ID,V_CNBN,Boolean.valueOf(CMath.s_bool(val))); break;
		case 11: SV(ID,V_SOND,val); break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenCraftSkill")) parseAllXML(val);
			break;
		}
	}

	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenCraftSkill)) return false;
		if(!((GenCraftSkill)E).ID().equals(ID)) return false;
		if(!((GenCraftSkill)E).text().equals(text())) return false;
		return true;
	}

	private void parseAllXML(String xml)
	{
		List<XMLLibrary.XMLpiece> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0)) return;
		for(int c=0;c<getStatCodes().length;c++)
			if(getStatCodes()[c].equals("CLASS"))
				ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
			else
			if(!getStatCodes()[c].equals("TEXT"))
				setStat(getStatCodes()[c],CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c])));
	}

	private String getAllXML()
	{
		StringBuffer str=new StringBuffer("");
		for(int c=0;c<getStatCodes().length;c++)
			if(!getStatCodes()[c].equals("TEXT"))
				str.append("<"+getStatCodes()[c]+">"
						+CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
						+"</"+getStatCodes()[c]+">");
		return str.toString();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected List<List<String>> loadRecipes()
	{
		if(parametersFile().length()==0)
			return new Vector<List<String>>();
		return super.loadRecipes(parametersFile());
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
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.REFITTING)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up "+((String)V(ID,V_VERB))+" "+building.name()+".");
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
							building.setUsesRemaining(100);
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
		return mayICraft(null,I);
	}

	public boolean mayICraft(final MOB mob, final Item I)
	{
		if(I==null) return false;
		if(!super.isMadeOfSupportedResource(I))
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		for(final List<String> recipe : recipes)
		{
			String finalName=recipe.get(RCP_FINALNAME);
			finalName=CMStrings.replaceAll(finalName, "%", ".*").toLowerCase();
			if(Pattern.matches(finalName, I.Name().toLowerCase()))
				return true;
		}
		return false;
	}

	public boolean supportsMending(Physical I){ return canMend(null,I,true); }

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		Boolean canMendB=(Boolean)V(ID,V_CNMN);
		if(!canMendB.booleanValue()) 
			return false;
		if(!super.canMend(mob,E,quiet)) 
			return false;
		Item IE=(Item)E;
		if(mayICraft(mob, IE))
			return true;
		if(!super.isMadeOfSupportedResource(IE))
		{
			if(!quiet)
			{
				commonTell(mob,"That can't be mended with this skill.");
			}
			return false;
		}
		return true;
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_AMOUNTMATS );
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
		final String noun=CMStrings.capitalizeAndLower(triggerStrings()[0]);
		final String verbing=V(ID,V_VERB).toString();
		Boolean canMendB=(Boolean)V(ID,V_CNMN);
		Boolean canRefitB=(Boolean)V(ID,V_CNRF);
		Boolean canBundleB=(Boolean)V(ID,V_CNBN);
		if(commands.size()==0)
		{
			StringBuilder features=new StringBuilder(noun+" what? Enter \""+noun.toLowerCase()+" list\" for a list");
			if(canMendB.booleanValue()) features.append(", \""+noun.toLowerCase()+" mend <item>\" to mend broken items, \""+noun.toLowerCase()+" scan\" to scan for mendable items");
			if(canRefitB.booleanValue()) features.append(", \""+noun.toLowerCase()+" refit <item>\" to resize wearables");
			if(canBundleB.booleanValue()) features.append(", \""+noun.toLowerCase()+" bundle\" to make bundles");
			features.append(".");
			commonTell(mob,features.toString());
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle"))
		&&(canBundleB.booleanValue()))
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
			StringBuffer buf=new StringBuffer("Item <S-NAME> <S-IS-ARE> skilled at "+verbing+":\n\r");
			int toggler=1;
			int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",15)+" Lvl "+CMStrings.padRight("Mats",4)+" ");
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					String mats=getComponentDescription(mob,V,RCP_AMOUNTMATS);
					if(mats.length()>5)
					{
						if(toggler>1) buf.append("\n\r");
						toggler=toggleTop;
					}
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append(CMStrings.padRight(item,29)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRightPreserve(""+mats,4)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		if(str.equalsIgnoreCase("scan") && canMendB.booleanValue())
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend") && canMendB.booleanValue())
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
		if(str.equalsIgnoreCase("refit") && canRefitB.booleanValue())
		{
			building=null;
			activity = CraftingActivity.CRAFTING;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(building==null) return false;
			if((!this.mayICraft(mob, building))&&(!super.isMadeOfSupportedResource(building)))
			{
				commonTell(mob,"That's can't be refitted with this skill.");
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
				commonTell(mob,"You don't know how to "+noun.toLowerCase()+" a '"+recipeName+"'.  Try \""+noun.toLowerCase()+" list\" for a list.");
				return false;
			}
			
			final String requiredMats = (String)foundRecipe.get(RCP_AMOUNTMATS);
			final List<Object> componentsFoundList=getAbilityComponents(mob, requiredMats, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
			if(componentsFoundList==null) return false;
			int numRequired=CMath.isInteger(requiredMats)?CMath.s_int(requiredMats):0;
			numRequired=adjustWoodRequired(numRequired,mob);
			
			if(amount>numRequired) numRequired=amount;
			String misctype=(String)foundRecipe.get(RCP_MISCTYPE);
			Integer[] ipm=super.supportedResourcesMap();
			int[] pm=new int[ipm.length];
			for(int i=0;i<ipm.length;i++) pm[i]=ipm[i].intValue();
			bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												numRequired,"material",pm,
												0,null,null,
												bundling,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			fixDataForComponents(data,componentsFoundList);
			numRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
				CMLib.materials().destroyResources(mob.location(),numRequired,data[0][FOUND_CODE],0,null)
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
				itemName="a "+numRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) "+verbing+" "+building.name()+".";
			displayText="You are "+verbing+" "+building.name();
			playSound=(String)V(ID,V_SOND);
			verb=verbing+" "+building.name();
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.basePhyStats().setWeight((int)Math.round( (double)numRequired * this.getItemWeightMultiplier( bundling )));
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
					((Container)building).setCapacity(capacity+numRequired);
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
