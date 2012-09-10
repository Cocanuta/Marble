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
public class Spell_Boomerang extends Spell
{
	public String ID() { return "Spell_Boomerang"; }
	public String name(){return "Returning";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	protected MOB owner=null;

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SELL))
		{
			unInvoke();
			if(affected!=null)	affected.delEffect(this);
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(affected))
		&&(text().length()==0))
		{
			setMiscText(msg.source().Name());
			msg.source().tell(affected.name()+" will now return back to you.");
			makeNonUninvokable();
		}
		if((affected instanceof Item)&&(text().length()>0))
		{
			Item I=(Item)affected;
			if(owner==null)
			{
				if((I.owner()!=null)
				&&(I.owner() instanceof MOB)
				&&(I.owner().Name().equals(text())))
					owner=(MOB)I.owner();
				else
					owner=CMLib.players().getPlayer(text());
			}
			if((owner!=null)&&(I.owner()!=null)&&(I.owner()!=owner))
			{
				if((msg.sourceMinor()==CMMsg.TYP_DROP)||(msg.target()==I)||(msg.tool()==I))
				{
					msg.addTrailerMsg(CMClass.getMsg(owner,null,CMMsg.NO_EFFECT,null));
				}
				else
				if(!owner.isMine(I))
				{
					owner.tell(I.name()+" returns to your inventory!");
					I.unWear();
					I.setContainer(null);
					owner.moveItemTo(I);
				}
				else
				{
					I.unWear();
					I.setContainer(null);
					owner.moveItemTo(I);
					I.setOwner(owner);
				}
			}
		}
	}
	
	// this fixes a damn PUT bug
	public void affectPhyStats(Physical affectedEnv, PhyStats stats)
	{
		super.affectPhyStats(affectedEnv, stats);
		if(affectedEnv instanceof Item)
		{
			final Item item = (Item)affectedEnv;
			if(item.container()!=null)
			{
				final Item container = item.ultimateContainer(null);
				if((container.amDestroyed())||(container.owner() != item.owner()))
					item.setContainer(null);
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows slightly!");
				mob.tell(target.name()+" will now await someone to GET it before acknowleding its new master.");
				setMiscText("");
				beneficialAffect(mob,target,asLevel,0);
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}
