package com.planet_ink.marble_mud.Abilities.Druid;
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
public class Chant_MagneticEarth extends Chant
{
	public String ID() { return "Chant_MagneticEarth"; }
	public String name(){ return "Magnetic Earth";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			Vector toGo=new Vector();
			boolean didSomething=false;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M!=invoker))
				{
					toGo.clear();
					for(int i=0;i<M.numItems();i++)
					{
						Item I=M.getItem(i);
						if((I!=null)
						&&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
						   ||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
						&&(I.container()==null)
						&&(I.amWearingAt(Wearable.IN_INVENTORY)
						   ||I.amWearingAt(Wearable.WORN_HELD)
						   ||I.amWearingAt(Wearable.WORN_WIELD)
						   ||I.amWearingAt(Wearable.WORN_EYES)
						   ||I.amWearingAt(Wearable.WORN_MOUTH)))
							toGo.addElement(I);
					}
					for(int i=0;i<toGo.size();i++)
					{
						Item I=(Item)toGo.elementAt(i);
						if(CMLib.commands().postDrop(M,I,true,true))
						{
							didSomething=true;
							R.show(M,I,CMMsg.MSG_OK_VISUAL,"<T-NAME> is pulled away from <S-NAME> to the magnetic ground!");
						}
					}
				}
			}
			if(didSomething)
			{
				R.recoverRoomStats();
				R.recoverRoomStats();
			}
		}
		return true;
	}

	protected boolean checked=false;
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((!checked)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(affected instanceof Room))
		{
			checked=true;
			if(!CMLib.threads().isTicking(this,-1))
				CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,1);
		}
		super.executeMsg(host,msg);
	}
	
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_CITY)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
		{
			mob.tell("This chant only works in caves, mountains, or rocky areas.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ground gains a powerful magnetic field!");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) the ground, but the magic fades.");
		// return whether it worked
		return success;
	}
}
