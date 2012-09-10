package com.planet_ink.marble_mud.Abilities.Spells;
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
public class Spell_MarkerPortal extends Spell
{
	public String ID() { return "Spell_MarkerPortal"; }
	public String name(){return "Marker Portal";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	Room newRoom=null;
	Room oldRoom=null;

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(newRoom!=null)
			{
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,"The swirling portal closes.");
				newRoom.rawDoors()[Directions.GATE]=null;
				newRoom.setRawExit(Directions.GATE,null);
			}
			if(oldRoom!=null)
			{
				oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,"The swirling portal closes.");
				oldRoom.rawDoors()[Directions.GATE]=null;
				oldRoom.setRawExit(Directions.GATE,null);
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		newRoom=null;
		oldRoom=null;

		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)
						&&(A.ID().equals("Spell_SummonMarker"))
						&&(A.invoker()==mob))
						{
							newRoom=R;
							break;
						}
					}
				if(newRoom!=null) break;
			}
		}catch(NoSuchElementException nse){}
		if(newRoom==null)
		{
			mob.tell("You can't seem to focus on your marker.  Are you sure you've already summoned it?");
			return false;
		}
		oldRoom=mob.location();
		if(oldRoom==newRoom)
		{
			mob.tell("But your marker is HERE!");
			return false;
		}

		if((oldRoom.getRoomInDir(Directions.GATE)!=null)
		||(oldRoom.getExitInDir(Directions.GATE)!=null))
		{
			mob.tell("A portal cannot be created here.");
			return false;
		}

		int profNeg=0;
		for(int i=0;i<newRoom.numInhabitants();i++)
		{
			MOB t=newRoom.fetchInhabitant(i);
			if(t!=null)
			{
				int adjustment=t.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				if(t.isMonster()) adjustment=adjustment*3;
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems()*20;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,-profNeg,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			CMMsg msg=CMClass.getMsg(mob,oldRoom,this,verbalCastCode(mob,oldRoom,auto),"^S<S-NAME> conjur(s) a blinding, swirling portal here.^?");
			CMMsg msg2=CMClass.getMsg(mob,newRoom,this,verbalCastCode(mob,newRoom,auto),"A blinding, swirling portal appears here.");
			if((oldRoom.okMessage(mob,msg))&&(newRoom.okMessage(mob,msg2)))
			{
				oldRoom.send(mob,msg);
				newRoom.send(mob,msg2);
				Exit e=CMClass.getExit("GenExit");
				e.setDescription("A swirling portal to somewhere");
				e.setDisplayText("A swirling portal to somewhere");
				e.setDoorsNLocks(false,true,false,false,false,false);
				e.setExitParams("portal","close","open","closed.");
				e.setName("a swirling portal");
				Ability A1=CMClass.getAbility("Prop_RoomView");
				if(A1!=null){
					A1.setMiscText(CMLib.map().getExtendedRoomID(newRoom));
					e.addNonUninvokableEffect(A1);
				}
				Exit e2=(Exit)e.copyOf();
				Ability A2=CMClass.getAbility("Prop_RoomView");
				if(A2!=null){
					A2.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
					e2.addNonUninvokableEffect(A2);
				}
				oldRoom.rawDoors()[Directions.GATE]=newRoom;
				newRoom.rawDoors()[Directions.GATE]=oldRoom;
				oldRoom.setRawExit(Directions.GATE,e);
				newRoom.setRawExit(Directions.GATE,e2);
				beneficialAffect(mob,e,asLevel,5);
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to conjur a portal, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
