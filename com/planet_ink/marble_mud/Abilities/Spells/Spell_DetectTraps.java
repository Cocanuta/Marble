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
public class Spell_DetectTraps extends Spell
{
	public String ID() { return "Spell_DetectTraps"; }
	public String name(){return "Detect Traps";}
	public String displayText(){return "(Detecting Traps)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	Room lastRoom=null;
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to traps.");
	}
	public String trapCheck(Physical P)
	{
		if(P!=null)
		if(CMLib.utensils().fetchMyTrap(P)!=null)
			return P.name()+" is trapped.\n\r";
		return "";
	}

	public String trapHere(MOB mob, Physical P)
	{
		StringBuffer msg=new StringBuffer("");
		if(P==null) return msg.toString();
		if((P instanceof Room)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(mob.location()));
		else
		if((P instanceof Container)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			Container C=(Container)P;
			List<Item> V=C.getContents();
			for(int v=0;v<V.size();v++)
				if(trapCheck((Item)V.get(v)).length()>0)
					msg.append(C.name()+" contains something trapped.");
		}
		else
		if((P instanceof Item)&&(CMLib.flags().canBeSeenBy(P,mob)))
			msg.append(trapCheck(P));
		else
		if((P instanceof Exit)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			Room room=mob.location();
			if(room!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(room.getExitInDir(d)==P)
				{
					Exit E2=room.getReverseExit(d);
					Room R2=room.getRoomInDir(d);
					msg.append(trapCheck(P));
					msg.append(trapCheck(E2));
					msg.append(trapCheck(R2));
					break;
				}
			}
		}
		else
		if((P instanceof MOB)&&(CMLib.flags().canBeSeenBy(P,mob)))
		{
			for(int i=0;i<((MOB)P).numItems();i++)
			{
				Item I=((MOB)P).getItem(i);
				if(trapCheck(I).length()>0)
					return P.name()+" is carrying something trapped.";
			}
			ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(P);
			if(SK!=null)
			{
				for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					Environmental E2=(Environmental)i.next();
					if(E2 instanceof Item)
						if(trapCheck((Item)E2).length()>0)
							return P.name()+" has something trapped in stock.";
				}
			}
		}
		return msg.toString();
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.target() instanceof Physical)
		&&(msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equals(ID())))
			{
				String str=trapHere((MOB)affected,(Physical)msg.target());
				if(str.length()>0)
					((MOB)affected).tell(str);
			}
			else
			if((trapHere((MOB)affected,(Physical)msg.target()).length()>0)
			&&(msg.source()!=msg.target()))
			{
				CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_LOOK,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(((MOB)target).isInCombat()||((MOB)target).isMonster())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting traps.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> gain(s) trap sensitivities!":"^S<S-NAME> incant(s) softly, and gain(s) sensitivity to traps!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes, but the spell fizzles.");

		return success;
	}
}
