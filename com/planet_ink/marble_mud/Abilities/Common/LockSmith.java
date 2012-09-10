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

@SuppressWarnings("rawtypes")
public class LockSmith extends CraftingSkill
{
	public String ID() { return "LockSmith"; }
	public String name(){ return "Locksmithing";}
	private static final String[] triggerStrings = {"LOCKSMITH","LOCKSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "METAL|MITHRIL";}

	private String keyCode="";
	protected Physical workingOn=null;
	protected boolean boltlock=false;
	private boolean delock=false;

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
						commonTell(mob,"You've ruined "+building.name()+"!");
					else
					if(!delock)
						dropAWinner(mob,building);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public Item getBuilding(Environmental target)
	{
		Item newbuilding=CMClass.getItem("GenKey");
		if((workingOn instanceof Exit)
		&&((Exit)workingOn).hasALock()
		&&(((Exit)workingOn).keyName().length()>0))
			keyCode=((Exit)workingOn).keyName();
		if((workingOn instanceof Container)
		&&(((Container)workingOn).hasALock())
		&&(((Container)workingOn).keyName().length()>0))
			keyCode=((Container)workingOn).keyName();
		((DoorKey)newbuilding).setKey(keyCode);
		return newbuilding;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((workingOn instanceof Container) && (mob.location()!=CMLib.map().roomLocation(workingOn)))
			{
				commonTell(mob,"You've stopped "+verb+".");
				building=null;
				unInvoke();
				return super.tick(ticking, tickID);
			}
			if(tickDown==6)
			{
				if(building==null) building=getBuilding(workingOn);
				if((workingOn!=null)&&(mob.location()!=null)&&(!aborted))
				{
					if(workingOn instanceof Exit)
					{
						if((delock)||(!((Exit)workingOn).hasALock()))
						{
							int dir=-1;
							for(int d=0;d<Directions.DIRECTIONS_BASE().length;d++)
								if(mob.location().getExitInDir(Directions.DIRECTIONS_BASE()[d])==workingOn)
								{dir=Directions.DIRECTIONS_BASE()[d]; break;}
							if((messedUp)||(dir<0))
							{
								if(delock)
									commonTell(mob,"You've failed to remove the lock.");
								else
									commonTell(mob,"You've ruined the lock.");
								building=null;
								unInvoke();
							}
							else
							{
								Exit exit2=mob.location().getPairedExit(dir);
								Room room2=mob.location().getRoomInDir(dir);
								((Exit)workingOn).basePhyStats().setLevel(xlevel(mob));
								((Exit)workingOn).recoverPhyStats();
								((Exit)workingOn).setDoorsNLocks(true,false,true,!delock,!delock,!delock);
								if(building instanceof DoorKey)
								{
									if(((DoorKey)building).getKey().length()==0)
										((DoorKey)building).setKey(keyCode);
									((Exit)workingOn).setKeyName(((DoorKey)building).getKey());
								}
								CMLib.database().DBUpdateExits(mob.location());
								if((exit2!=null)
								&&(!boltlock)
								&&(exit2.hasADoor())
								&&(exit2.isGeneric())
								&&(room2!=null))
								{
									exit2.basePhyStats().setLevel(xlevel(mob));
									exit2.setDoorsNLocks(true,false,true,!delock,!delock,!delock);
									if(building instanceof DoorKey)
										exit2.setKeyName(((DoorKey)building).getKey());
									CMLib.database().DBUpdateExits(room2);
								}
							}
						}
					}
					else
					if(workingOn instanceof Container)
					{
						if(delock||(!((Container)workingOn).hasALock()))
						{
							if(messedUp)
							{
								if(delock)
									commonTell(mob,"You've failed to remove the lock.");
								else
									commonTell(mob,"You've ruined the lock.");
								building=null;
								unInvoke();
							}
							else
							{
								((Container)workingOn).setLidsNLocks(true,false,!delock,!delock);
								if(building instanceof DoorKey)
								{
									if(((DoorKey)building).getKey().length()==0)
										((DoorKey)building).setKey(keyCode);
									((Container)workingOn).setKeyName(((DoorKey)building).getKey());
								}
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()==0)
		||(CMParms.combine(commands,0).equalsIgnoreCase("list")))
		{
			commonTell(mob,"Locksmith what or where? Enter the name of a container or door direction. Put the word \"boltlock\" in front of the door direction to make a one-way lock.  Put the word \"delock\" in front of the door direction to remove the locks.");
			return false;
		}
		keyCode=""+Math.random();
		String startStr=null;
		int duration=8;
		building=null;
		boolean keyFlag=false;
		workingOn=null;
		messedUp=false;
		int woodRequired=1;
		boolean lboltlock=false;
		if((commands.size()>0)&&("BOLTLOCK".startsWith(((String)commands.firstElement()).toUpperCase())))
		{
			lboltlock=true;
			commands.removeElementAt(0);
		}
		boolean ldelock=false;
		if((commands.size()>0)&&("DELOCK".startsWith(((String)commands.firstElement()).toUpperCase())))
		{
			ldelock=true;
			commands.removeElementAt(0);
		}
		String recipeName=CMParms.combine(commands,0);
		int dir=Directions.getGoodDirectionCode(recipeName);
		if(dir<0)
			workingOn=mob.location().fetchFromMOBRoomFavorsItems(mob,null,recipeName,Wearable.FILTER_UNWORNONLY);
		else
			workingOn=mob.location().getExitInDir(dir);

		if((workingOn==null)||(!CMLib.flags().canBeSeenBy(workingOn,mob)))
		{
			commonTell(mob,"You don't see a '"+recipeName+"' here.");
			return false;
		}
		if(workingOn instanceof Exit)
		{
			if(!((Exit)workingOn).hasADoor())
			{
				commonTell(mob,"There is no door in that direction.");
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,"That door isn't built right -- it can't be modified.");
				return false;
			}
			if(!ldelock)
			{
				if(((Exit)workingOn).hasALock())
					keyFlag=true;
				else
					woodRequired=5;
			}

			Room otherRoom=(dir>=0)?mob.location().getRoomInDir(dir):null;
			if((!CMLib.law().doesOwnThisProperty(mob,mob.location()))
			&&((otherRoom==null)
				||(!CMLib.law().doesOwnThisProperty(mob,otherRoom))))
			{
				commonTell(mob,"You'll need the permission of the owner to do that.");
				return false;
			}
		}
		else
		if(workingOn instanceof Container)
		{
			if(!((Container)workingOn).hasALid())
			{
				commonTell(mob,"That doesn't have a lid.");
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,"That just isn't built right -- it can't be modified.");
				return false;
			}
			if(!ldelock)
			{
				if(((Container)workingOn).hasALock())
					keyFlag=true;
				else
					woodRequired=3;
			}
			if((((Container)workingOn).owner() instanceof Room)
			&&(!CMLib.flags().isGettable((Container)workingOn))
			&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
			{
				commonTell(mob,"You'll need the permission of the owner of this place to do that.");
				return false;
			}
		}
		else
		{
			commonTell(mob,"You can't put a lock on that.");
			return false;
		}

		String itemName=null;
		int makeResource=-1;
		if(ldelock)
		{
			itemName="a broken lock";
			keyFlag=false;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		{
			int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												0,null,null,
												false,
												0,
												null);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			itemName=(RawMaterial.CODES.NAME(data[0][FOUND_CODE])+" key").toLowerCase();
			itemName=CMLib.english().startWithAorAn(itemName);
			makeResource=data[0][FOUND_CODE];
		}
		building=getBuilding(workingOn);
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a GenKey!!!");
			return false;
		}
		if((makeResource>=0)&&(building!=null)) building.setMaterial(makeResource);
		duration=getDuration(25,mob,workingOn.phyStats().level(),8);
		if(keyFlag) duration=duration/2;
		building.setName(itemName);
		startStr="<S-NAME> start(s) working on "+(keyFlag?"a key for ":"")+workingOn.name()+".";
		displayText="You are working on "+(keyFlag?"a key for ":"")+workingOn.name();
		verb="working on "+(keyFlag?"a key for ":"")+workingOn.name();
		playSound="drill.wav";
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.basePhyStats().setWeight(woodRequired);
		building.setBaseValue(1);
		building.basePhyStats().setLevel(1);
		building.setSecretIdentity(getBrand(mob));
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();

		int proficiencyAddition=0;
		if(workingOn.phyStats().level()>xlevel(mob))
			proficiencyAddition=workingOn.phyStats().level()-xlevel(mob);
		messedUp=!proficiencyCheck(mob,proficiencyAddition*5,auto);

		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			boltlock=lboltlock;
			delock=ldelock;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
