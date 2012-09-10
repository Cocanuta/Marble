package com.planet_ink.marble_mud.Commands;
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
import com.planet_ink.marble_mud.Libraries.interfaces.AbilityMapper;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;


import java.util.*;
import java.io.IOException;

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
public class Create extends StdCommand
{
	public Create(){}

	private final String[] access={"CREATE"};
	public String[] getAccessWords(){return access;}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		Exit opExit=mob.location().getRawExit(direction);
		Room opRoom=mob.location().rawDoors()[direction];

		Exit reverseExit=null;
		if(opRoom!=null)
			reverseExit=opRoom.getRawExit(Directions.getOpDirectionCode(direction));
		if(reverseExit!=null)
		{
			if((thisExit.isGeneric())&&(reverseExit.isGeneric()))
			{
				thisExit=(Exit)reverseExit.copyOf();
				CMLib.genEd().modifyGenExit(mob,thisExit);
			}
		}


		mob.location().setRawExit(direction,thisExit);
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a portal opens up "+Directions.getInDirectionName(direction)+".\n\r");
		CMLib.database().DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.getRawExit(revDirCode)==reverseExit)
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.getRawExit(revDirCode)==null)&&(opRoom.rawDoors()[revDirCode]==mob.location()))
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(opRoom!=null) opRoom.getArea().fillInAreaRoom(opRoom);
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	public void polls(MOB mob, Vector commands)
	{
	}
	
	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=CMParms.combine(commands,2);
		Environmental dest=mob.location();
		Container setContainer=null;
		int x=itemID.indexOf('@');
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					Item I = mob.location().findItem(null, rest);
					if(I instanceof Container)
						setContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return;
					}
				}
				else
					dest=M;
			}
		}
		Item newItem=CMClass.getItem(itemID);
		if((newItem==null)&&(CMLib.english().numPossibleGold(null,itemID)>0))
		{
			long numCoins=CMLib.english().numPossibleGold(null,itemID);
			String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
			double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);
			if((numCoins>0)&&(denom>0.0))
				newItem=CMLib.beanCounter().makeCurrency(currency,denom,numCoins);
		}

		boolean doGenerica=true;
		if(newItem==null)
		{
			newItem=getNewCatalogItem(itemID);
			doGenerica=newItem==null;
		}
		
		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if((newItem instanceof ArchonOnly)
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell("NO!");
			return;
		}

		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		if(dest instanceof Room)
		{
			((Room)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
		}
		else
		if(dest instanceof MOB)
		{
			((MOB)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops into "+dest.name()+"'s arms.");
		}

		if((newItem.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+" created item "+newItem.ID()+".");
	}

	public void players(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
	
		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			mob.tell("There is already a player called '"+M.Name()+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!CMLib.login().isOkName(mobID))
		{
			mob.tell("'"+mobID+"' is not a valid name.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		mobID=CMStrings.capitalizeAndLower(mobID);
		CMLib.login().createCharacter(null,mobID,mob.session());
		mob.session().mob().setSession(null);
		mob.session().setMob(mob);
		M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.removeFromGame(false,true);
			CMLib.genEd().modifyPlayer(mob,M);
			Log.sysOut("Mobs",mob.Name()+" created player "+M.Name()+".");
		}
	}

	public void rooms(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String Locale=(String)commands.elementAt(3);
		thisRoom=CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+Locale+"'.\n\rThe format is CREATE ROOM [DIRECTION] ([ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		Room room=mob.location();
		thisRoom.setRoomID(room.getArea().getNewRoomID(room,direction));
		thisRoom.setArea(room.getArea());
		if(thisRoom.roomID().length()==0)
		{
			mob.tell("A room may not be created in that direction.  Are you sure you havn't reached the edge of a grid?");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.roomID());
		thisRoom.setDescription("");
		CMLib.database().DBCreateRoom(thisRoom);

		CMLib.map().createNewExit(mob.location(),thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.Name()+" created room "+thisRoom.roomID()+".");
	}
	
	public void accounts(MOB mob, Vector commands)
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ACCOUNT [NAME] [PASSWORD]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		PlayerAccount thisAcct=null;
		String AcctName=CMStrings.capitalizeAndLower((String)commands.elementAt(2));
		String password=CMStrings.capitalizeAndLower(CMParms.combine(commands,3));
		thisAcct=CMLib.players().getLoadAccount(AcctName);
		if(thisAcct!=null)
		{
			mob.tell("Account '"+AcctName+"' already exists!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!CMLib.login().isOkName(AcctName))
		{
			mob.tell("Name '"+AcctName+"' is not permitted.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		thisAcct=(PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		thisAcct.setAccountName(AcctName);
		thisAcct.setAccountExpiration(0);
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
			thisAcct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
		thisAcct.setLastDateTime(System.currentTimeMillis());
		thisAcct.setLastUpdated(System.currentTimeMillis());
		thisAcct.setPassword(password);
		CMLib.database().DBCreateAccount(thisAcct);

		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A new soul descends from the heavens and dissipates.\n\r");
		Log.sysOut("Create",mob.Name()+" created account "+thisAcct.accountName()+".");
	}
	
	public MOB getNewCatalogMob(String mobID)
	{
		MOB newMOB=CMLib.catalog().getCatalogMob(mobID);
		if(newMOB!=null)
		{
			newMOB=(MOB)newMOB.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newMOB,true);} catch(Exception t){}
			newMOB.text();
		}
		return newMOB;
	}
	
	public Item getNewCatalogItem(String itemID)
	{
		Item newItem=CMLib.catalog().getCatalogItem(itemID);
		if(newItem!=null)
		{
			newItem=(Item)newItem.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newItem,true);} catch(Exception t){}
			newItem.text();
		}
		return newItem;
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;
		if(newMOB==null)
		{
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}
		
		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		
		if(newMOB.Name().length()==0)
			newMOB.setName("A Standard MOB");
		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
		long rejuv=CMProps.getTicksPerMinute()+CMProps.getTicksPerMinute()+(CMProps.getTicksPerMinute()/2);
		if(rejuv>(CMProps.getTicksPerMinute()*20)) rejuv=(CMProps.getTicksPerMinute()*20);
		newMOB.phyStats().setRejuv((int)rejuv);
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.basePhyStats(),(char)newMOB.baseCharStats().getStat(CharStats.STAT_GENDER));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java Plane.");
		if((newMOB.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newMOB);
		Log.sysOut("Mobs",mob.Name()+" created mob "+newMOB.Name()+".");
	}

	public void races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if((R!=null)&&(R.isGeneric()))
		{
			mob.tell("This generic race already exists.. perhaps you might modify it?");
			return;
		}
		Race GR=null;
		if((R!=null)&&(!R.isGeneric()))
		{
			if((mob.session()==null)
			||(!mob.session().confirm("Currently, "+R.ID()+" is a standard race.  This will convert the " +
									  "race to a GenRace so that you can modify it.  Be warned that special " +
									  "functionality of the race may be lost by doing this.  You can undo this "+
									  "action by destroying the same race ID after creating it.  Do you wish to " +
									  "continue (y/N)?", "N")))
				return;
			GR=R.makeGenRace();
			raceID=GR.ID();
		}
		if(raceID.indexOf(' ')>=0)
		{
			mob.tell("'"+raceID+"' is an invalid race id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(GR==null)
		{
			GR=(Race)CMClass.getRace("GenRace").copyOf();
			GR.setRacialParms("<RACE><ID>"+CMStrings.capitalizeAndLower(raceID)+"</ID><NAME>"+CMStrings.capitalizeAndLower(raceID)+"</NAME></RACE>");
		}
		CMClass.addRace(GR);
		CMLib.genEd().modifyGenRace(mob,GR);
		CMLib.database().DBCreateRace(GR.ID(),GR.racialParms());
		if(R!=null)
			CMLib.utensils().swapRaces(GR,R);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just increased!");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE AREA [AREA NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaName=CMParms.combine(commands,2);
		Area A=CMLib.map().getArea(areaName);
		if(A!=null)
		{
			mob.tell("An area with the name '"+A.name()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaType="";
		int tries=0;
		while((areaType.length()==0)&&((++tries)<10))
		{
			areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
			if(CMClass.getAreaType(areaType)==null)
			{
				mob.session().println("Invalid area type! Valid ones are:");
				mob.session().println(CMLib.lister().reallyList(mob,CMClass.areaTypes(),-1,null).toString());
				areaType="";
			}
		}
		if(areaType.length()==0) areaType="StdArea";
		A=CMClass.getAreaType(areaType);
		A.setName(areaName);
		CMLib.map().addArea(A);
		CMLib.database().DBCreateArea(A);
		
		Room R=CMClass.getLocale("StdRoom");
		R.setRoomID(A.getNewRoomID(R,-1));
		R.setArea(A);
		R.setDisplayText(CMClass.classID(R)+"-"+R.roomID());
		R.setDescription("");
		CMLib.database().DBCreateRoom(R);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The size of the world just increased!");
		mob.tell("You are now at "+R.roomID()+".");
		R.bringMobHere(mob,true);
		CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
	}

	public void components(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rFormat: CREATE COMPONENT [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String skillID=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(skillID);
		if(A==null)
		{
			mob.tell("'"+skillID+"' is not a proper skill/spell ID.  Try LIST ABILITIES.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		skillID=A.ID();
		if(CMLib.ableMapper().getAbilityComponentMap().get(A.ID().toUpperCase())!=null)
		{
			mob.tell("'"+A.ID()+"' already exists, you'll need to destroy it first.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Vector<AbilityComponent> DV=new Vector<AbilityComponent>();
		CMLib.ableMapper().getAbilityComponentMap().put(skillID.toUpperCase().trim(),DV);
		DV.add(CMLib.ableMapper().createBlankAbilityComponent());
		CMLib.genEd().modifyComponents(mob,skillID);
		String parms=CMLib.ableMapper().getAbilityComponentCodedString(skillID);
		String error=CMLib.ableMapper().addAbilityComponent(parms,CMLib.ableMapper().getAbilityComponentMap());
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMLib.ableMapper().alterAbilityComponentFile(skillID,false);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The complication of skill usage just increased!");
	}
	
	public void expertises(MOB mob, Vector commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf('=')<0))
		{
			mob.tell("You have failed to specify the proper fields.\n\rFormat: CREATE EXPERTISE [EXPERTISE ID]=[PARAMETERS] as follows: \n\r");
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,true).text();
			StringBuffer inst=new StringBuffer("");
			List<String> V=new Vector();
			if(buf!=null) V=Resources.getFileLineVector(buf);
			for(int v=0;v<V.size();v++)
				if(((String)V.get(v)).startsWith("#"))
					inst.append(((String)V.get(v)).substring(1)+"\n\r");
				else
				if(((String)V.get(v)).length()>0) 
					break;
			if(mob.session()!=null) mob.session().wraplessPrintln(inst.toString());
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String parms=CMParms.combineWithQuotes(commands,2);
		String skillID=parms.substring(0,parms.indexOf('='));
		if(skillID.indexOf(' ')>=0)
		{
			mob.tell("Spaces are not allowed in expertise codes.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String WKID=CMStrings.replaceAll(skillID.toUpperCase(),"@X1","");
		WKID=CMStrings.replaceAll(WKID,"@X2","").trim();
		if(CMLib.expertises().getStages(WKID)>0)
		{
			mob.tell("'"+WKID+"' already exists, you'll need to destroy it first.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String error=CMLib.expertises().confirmExpertiseLine(parms,null,false);
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,true);
		F.saveText("\n"+parms,true);
		Resources.removeResource("skills/expertises.txt");
		CMLib.expertises().recompileExpertises();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The power of skill usage just increased!");
	}
	
	public void titles(MOB mob, Vector commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf('=')<0))
		{
			mob.tell("You have failed to specify the proper fields.\n\rFormat: CREATE TITLE [TITLE]=[ZAPPER MASK] as follows: \n\r");
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true).text();
			StringBuffer inst=new StringBuffer("");
			List<String> V=new Vector();
			if(buf!=null) V=Resources.getFileLineVector(buf);
			for(int v=0;v<V.size();v++)
				if(((String)V.get(v)).startsWith("#"))
					inst.append(((String)V.get(v)).substring(1)+"\n\r");
				else
				if(((String)V.get(v)).length()>0) 
					break;
			if(mob.session()!=null) mob.session().wraplessPrintln(inst.toString());
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String parms=CMParms.combineWithQuotes(commands,2);
		String skillID=parms.substring(0,parms.indexOf('='));
		if(CMLib.titles().isExistingAutoTitle(skillID))
		{
			mob.tell("'"+skillID+"' already exists, you'll need to destroy it first.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String error=CMLib.titles().evaluateAutoTitle(parms,false);
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
		F.saveText("\n"+parms,true);
		Resources.removeResource("titles.txt");
		CMLib.titles().reloadAutoTitles();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The prestige of the players just increased!");
	}
	
	public void abilities(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ABILITY [ABILITY ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String classD=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell("A generic ability with the ID '"+A.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell("'"+classD+"' is an invalid  id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Ability CR=(Ability)CMClass.getAbility("GenAbility").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenAbility(mob,CR);
		CMLib.database().DBCreateAbility(CR.ID(),"GenAbility",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just increased!");
	}

	public void languages(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE LANGUAGE [LANGUAGE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String classD=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell("A generic ability with the ID '"+A.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell("'"+classD+"' is an invalid  id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Language CR=(Language)CMClass.getAbility("GenLanguage").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenLanguage(mob,CR);
		CMLib.database().DBCreateAbility(CR.ID(),"GenLanguage",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just increased!");
	}

	public void craftSkills(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE CRAFTSKILL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String classD=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell("A generic ability with the ID '"+A.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell("'"+classD+"' is an invalid  id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Ability CR=(Ability)CMClass.getAbility("GenCraftSkill").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenCraftSkill(mob,CR);
		CMLib.database().DBCreateAbility(CR.ID(),"GenCraftSkill",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just increased!");
	}

	public void allQualify(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ALLQUALIFY EACH/ALL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String eachOrAll=(String)commands.get(2);
		if((!eachOrAll.equalsIgnoreCase("each"))&&(!eachOrAll.equalsIgnoreCase("all")))
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ALLQUALIFY EACH/ALL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}    
		String classD=CMParms.combine(commands,3);
		Ability A=CMClass.getAbility(classD);
		if(A==null)
		{
			mob.tell("Ability with the ID '"+classD+"' does not exist! Try LIST ABILITIES.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell("All-Qualify entry ("+eachOrAll+") ID '"+A.ID()+"' already exists.  Try DESTROY or MODIFY.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		AbilityMapper.AbilityMapping mapped = CMLib.genEd().modifyAllQualifyEntry(mob,eachOrAll.toUpperCase().trim(),A);
		map=CMLib.ableMapper().getAllQualifiesMap(null);
		subMap=map.get(eachOrAll.toUpperCase().trim());
		subMap.put(A.ID().toUpperCase().trim(), mapped);
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just increased!");
	}

	public void classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String classD=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classD);
		if((C!=null)&&(C.isGeneric()))
		{
			mob.tell("A generic class with the ID '"+C.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CharClass CR=null;
		if((C!=null)&&(!C.isGeneric()))
		{
			if((mob.session()==null)
			||(!mob.session().confirm("Currently, "+C.ID()+" is a standard character class.  This will convert the " +
									  "class to a GenCharClass so that you can modify it.  Be warned that special " +
									  "functionality of the class may be lost by doing this.  You can undo this "+
									  "action by destroying the same class ID after creating it.  Do you wish to " +
									  "continue (y/N)?", "N")))
				return;
			CR=C.makeGenCharClass();
			classD=CR.ID();
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell("'"+classD+"' is an invalid class id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(CR==null)
		{
			CR=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
			CR.setClassParms("<CCLASS><ID>"+CMStrings.capitalizeAndLower(classD)+"</ID><NAME>"+CMStrings.capitalizeAndLower(classD)+"</NAME></CCLASS>");
		}
		CMClass.addCharClass(CR);
		CMLib.genEd().modifyGenClass(mob,CR);
		CMLib.database().DBCreateClass(CR.ID(),CR.classParms());
		if(C!=null)
			CMLib.utensils().reloadCharClasses(C);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The employment of the world just increased!");
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;
		
		if(commands.size()<3)
		{
			mob.tell("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		String stuff=CMParms.combine(commands,2);
		if(CMLib.socials().fetchSocial(stuff,false)!=null)
		{
			mob.tell("The social '"+stuff+"' already exists.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		CMLib.socials().modifySocialInterface(mob,stuff);
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLASSES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			abilities(mob,commands);
		}
		else
		if(commandType.equals("LANGUAGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			languages(mob,commands);
		}
		else
		if(commandType.equals("CRAFTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			craftSkills(mob,commands);
		}
		else
		if(commandType.equals("ALLQUALIFY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			allQualify(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			components(mob,commands);
		}
		else
		if(commandType.equals("EXPERTISE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EXPERTISES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			expertises(mob,commands);
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TITLES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			titles(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDAREAS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			accounts(mob,commands);
		}
		else
		if(commandType.equals("DISABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)) 
				return errorOut(mob);
			String named=CMParms.combine(commands,2);
			if(CMSecurity.isDisabledSearch(named.toUpperCase()))
				mob.tell("'"+named+"' is already disabled");
			else
			{
				mob.tell("'"+named+"' is now disabled");
				CMSecurity.setDisableVar(named.toUpperCase().trim(), false);
			}
			return true;
		}
		else
		if(commandType.equals("DEBUGFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)) 
				return errorOut(mob);
			String named=CMParms.combine(commands,2);
			CMSecurity.DbgFlag flag = (CMSecurity.DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), named.toUpperCase().trim());
			if(flag==null)
			{
				mob.tell("'"+named+"' is not a valid flag.  Try: "+CMParms.toStringList(CMSecurity.DbgFlag.values()));
				return false;
			}
			if(CMSecurity.isDebugging(flag))
				mob.tell("'"+named+"' is already debugging");
			else
			{
				mob.tell("'"+named+"' is now debugging");
				
				CMSecurity.setDebugVar(flag, false);
			}
			return true;
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDSOCIALS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("HOLIDAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			String named=CMParms.combine(commands,2);
			if(named.trim().length()==0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
				mob.tell("Include a name!");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				return false;
			}
			String err=CMLib.quests().createHoliday(named,mob.location().getArea().name(),true);
			if(err.length()>0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
				mob.tell(err);
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				return false;
			}
			else
				mob.doCommand(new XVector("MODIFY","HOLIDAY",named),metaFlags);
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if((commands.size()<3)||(CMParms.combine(commands,2).indexOf('.')<0))
			{
				mob.tell("Create which faction?  You must give a filename with an extension.");
				return false;
			}
			else
			{
				String name=CMParms.combine(commands,2);
				if((name.indexOf(' ')>=0)||(name.length()==0))
				{
					mob.tell("That name is not allowed. No spaces!");
					return false;
				}
				Faction F=CMLib.factions().getFaction(name);
				if(F==null) F=CMLib.factions().getFactionByName(name);
				if(F!=null)
				{
					mob.tell("Faction '"+name+"' already exists.  Try another.");
					return false;
				}
				else
				if((!mob.isMonster())&&(mob.session().confirm("Create a new faction with ID/filename: 'resources/"+name+"' (N/y)? ","N")))
				{
					//name=Resources.buildResourcePath("")+name;
					StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,true).text();
					if((template==null)||(template.length()==0))
					{
						mob.tell("The file 'resources/examples/factiontemplate.ini' could not be located and is required for command line faction creation.");
						return false;
					}
					Resources.submitResource(name,template);
					Resources.saveFileResource("::"+name,null,template);
					F=(Faction)CMClass.getCommon("DefaultFaction");
					F.initializeFaction(template,name);
					CMLib.factions().modifyFaction(mob,F);
					Log.sysOut("CreateEdit",mob.Name()+" created Faction "+F.name()+" ("+F.factionID()+").");
				}
				else
					return false;
			}
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if(commandType.equals("NEWS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Item I=CMClass.getItem("StdJournal");
			I.setName("SYSTEM_NEWS");
			I.setDescription("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. ");
			CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,CMParms.combine(commands,2),CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null);
			if(mob.location().okMessage(mob,newMsg)&&I.okMessage(mob, newMsg))
			{
				mob.location().send(mob,newMsg);
				I.executeMsg(mob,newMsg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world is now more informed!^?");
			}
			else
				return false;
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		if(commandType.equals("POLL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.POLLS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Poll P=(Poll)CMClass.getCommon("DefaultPoll");
			while(CMLib.polls().getPoll(P.getName())!=null)
				P.setName(P.getName()+"!");
			P.setFlags(Poll.FLAG_ACTIVE);
			CMLib.polls().createPoll(P);
			CMLib.polls().modifyVote(P, mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world has grown more uncertain.^?");
			Log.sysOut("CreateEdit",mob.Name()+" created Poll "+P.getName()+".");
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
			{
				if((mob.session()!=null)&&(mob.session().confirm("Create a new Quest using the Quest Maker Wizard (y/N)? ","N")))
					CMLib.quests().questMaker(mob);
				else
				{
					mob.tell("You must specify a valid quest string.  Try AHELP QUESTS.");
					return false;
				}
			}
			else
			{
				String script=CMParms.combine(commands,2);
				Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setAuthor(mob.Name());
				Q.setScript(script);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
				{
					mob.tell("You must specify a VALID quest string.  This one contained errors.  Try AHELP QUESTS.");
					return false;
				}
				else
				if((CMLib.quests().fetchQuest(Q.name())!=null)
				&&((mob.isMonster())
					||(!mob.session().confirm("That quest is already loaded.  Load a duplicate (N/y)? ","N"))))
						return false;
				else
				{
					mob.tell("Quest '"+Q.name()+"' added.");
					CMLib.quests().addQuest(Q);
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
			{
				mob.tell("You must specify a valid clan name.  Try CLANLIST and AHELP CLANS.");
				return false;
			}
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=(Clan)CMClass.getCommon("DefaultClan");
				C.setName(name);
				if(C.name().trim().length()==0)
				{
					mob.tell("You must specify a VALID clan name.");
					return false;
				}
				else
				if(CMLib.clans().getClan(C.name())!=null)
				{
					mob.tell("That clan already exists, try CLANLIST.");
					return false;
				}
				else
				{
					mob.tell("Clan '"+C.name()+"' created.");
					C.setStatus(Clan.CLANSTATUS_ACTIVE);
					C.create();
					if(CMLib.clans().getClan(C.ID())==null)
						CMLib.clans().addClan(C);
				}
			}
		}
		else
		if(commandType.equals("GOVERNMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
			{
				mob.tell("You must specify a valid government name.  Try LIST GOVERNMENTS.");
				return false;
			}
			else
			{
				String name=CMParms.combine(commands,2);
				ClanGovernment G=CMLib.clans().createGovernment(name);
				if(G==null)
				{
					mob.tell("You must specify an unused government name.");
					return false;
				}
				else
				if(!mob.isMonster())
				{
					mob.tell("Government '"+G.getName()+"' created.");
					CMLib.genEd().modifyGovernment(mob, G);
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit",mob.Name()+" created Clan Government "+G.getName()+".");
				}
				else
					return false;
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			String firstWord=allWord;
			String lastWord=null;
			if(commands.size()>2)
			{
				firstWord=(String)commands.get(1);
				lastWord=(String)commands.lastElement();
			}
			Environmental E=null;
			E=CMClass.getItem(allWord);
			if(((E!=null)&&(E instanceof Item))
			||(CMLib.english().numPossibleGold(null,allWord)>0)
			||(CMLib.catalog().getCatalogItem(allWord)!=null))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands,metaFlags);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if(((E!=null)&&(E instanceof MOB))
				||(CMLib.catalog().getCatalogMob(allWord)!=null))
				{
					commands.insertElementAt("MOB",1);
					execute(mob,commands,metaFlags);
				}
				else
				if((lastWord!=null)&&(Directions.getGoodDirectionCode(lastWord)>=0))
				{
					commands.removeElementAt(commands.size()-1);
					allWord=CMParms.combine(commands,1);

					E=CMClass.getLocale(allWord);
					if(E==null)
						E=CMClass.getExit(allWord);
					if(E==null)
						E=CMClass.getAreaType(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("ROOM");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("EXIT");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Area))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("AREA");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					{
						mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, FACTION, COMPONENT, GOVERNMENT, HOLIDAY, CLAN, MOB, RACE, ABILITY, LANGUAGE, CRAFTSKILL, ALLQUALIFY, CLASS, POLL, DEBUGFLAG, DISABLEFLAG, NEWS, USER, or ROOM.");
						return false;
					}
				}
				else
				if(CMath.isInteger(firstWord)&&(lastWord!=null))
				{
					int num=CMath.s_int(firstWord);
					String theRest=CMParms.combine(commands,2);
					int matCode=RawMaterial.CODES.FIND_IgnoreCase(theRest);
					if(matCode<0)
						matCode=RawMaterial.CODES.FIND_StartsWith(theRest);
					if((theRest.length()>0)&&(matCode>=0))
					{
						for(int i=0;i<num;i++)
							mob.location().addItem(CMLib.materials().makeItemResource(matCode),ItemPossessor.Expire.Player_Drop);
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL, "Suddenly "+num+" "+RawMaterial.CODES.NAME(matCode)+" fall from the sky.");
					}
					else
					{
						Vector V=(Vector)commands.clone();
						V.remove(2);
						for(int i=0;i<num;i++)
							if(!execute(mob,V,metaFlags))
								return false;
					}
				}
				else
				{
					mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, FACTION, MOB, COMPONENT, GOVERNMENT, HOLIDAY, CLAN, RACE, ABILITY, LANGUAGE, CRAFTSKILL, ALLQUALIFY, CLASS, POLL, USER, DEBUGFLAG, NEWS, DISABLEFLAG, ROOM.");
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_CMD_GROUP);}

	
}
