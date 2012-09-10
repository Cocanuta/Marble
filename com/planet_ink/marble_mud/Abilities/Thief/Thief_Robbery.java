package com.planet_ink.marble_mud.Abilities.Thief;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class Thief_Robbery extends ThiefSkill
{
	public String ID() { return "Thief_Robbery"; }
	public String name(){ return "Robbery";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	private static final String[] triggerStrings = {"ROBBERY","ROB"};
	public String[] triggerStrings(){return triggerStrings;}
	public Vector mobs=new Vector();
	private DVector lastOnes=new DVector(2);
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	
	protected int timesPicked(MOB target)
	{
		int times=0;
		for(int x=0;x<lastOnes.size();x++)
		{
			MOB M=(MOB)lastOnes.elementAt(x,1);
			Integer I=(Integer)lastOnes.elementAt(x,2);
			if(M==target)
			{
				times=I.intValue();
				lastOnes.removeElement(M);
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeElementAt(0);
		lastOnes.addElement(target,Integer.valueOf(times+1));
		return times+1;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(affected))
		   &&(mobs.contains(msg.source())))
		{
			if((msg.targetMinor()==CMMsg.TYP_BUY)
			   ||(msg.targetMinor()==CMMsg.TYP_BID)
			   ||(msg.targetMinor()==CMMsg.TYP_SELL)
			   ||(msg.targetMinor()==CMMsg.TYP_LIST)
			   ||(msg.targetMinor()==CMMsg.TYP_VALUE)
			   ||(msg.targetMinor()==CMMsg.TYP_VIEW))
			{
				msg.source().tell(affected.name()+" looks unwilling to do business with you.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if((target==null)||(((MOB)target).amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
				return Ability.QUALITY_INDIFFERENT;
			if((!((MOB)target).mayIFight(mob))||(CMLib.coffeeShops().getShopKeeper(target)==null))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Rob what from whom?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		String itemToSteal=(String)commands.elementAt(0);

		MOB target=null;
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,1));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
			return false;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)*2));

		if((!target.mayIFight(mob))||(CMLib.coffeeShops().getShopKeeper(target)==null))
		{
			mob.tell("You cannot rob from "+target.charStats().himher()+".");
			return false;
		}
		if(target==mob)
		{
			mob.tell("You cannot rob yourself.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		ShopKeeper shop=CMLib.coffeeShops().getShopKeeper(target);
		Environmental stock=shop.getShop().getStock(itemToSteal,mob);
		Physical stolen=(stock instanceof Physical)?(Physical)stock:null;
		if(stolen!=null)
		{
			ShopKeeper.ShopPrice price=CMLib.coffeeShops().sellingPrice(target,mob,stolen,shop,false);
			if((stolen instanceof Ability)
			||(stolen instanceof MOB)
			||(stolen instanceof Room)
			||(stolen instanceof LandTitle)
			||((price.experiencePrice>0)||(price.questPointPrice>0)))
			{
				mob.tell(mob,target,stolen,"You cannot rob '<O-NAME>' from <T-NAME>.");
				return false;
			}
			if(!shop.getShop().doIHaveThisInStock(stolen.Name(),mob))
				stolen=null;
		}

		int discoverChance=(mob.charStats().getStat(CharStats.STAT_CHARISMA)-(target.charStats().getStat(CharStats.STAT_WISDOM))*5)
						+(getX1Level(mob)*5);
		int times=timesPicked(target);
		if(times>5) discoverChance-=(20*(times-5));
		if(!CMLib.flags().canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;
		boolean success=proficiencyCheck(mob,-(levelDiff),auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to rob <T-NAMESELF>; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to rob you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to rob <T-NAME> and fails!");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)
				{
					mobs.clear();
					mobs.addElement(mob);
					beneficialAffect(mob,target,asLevel,0);
				}
				else
					A.mobs.addElement(mob);
			}
			else
				mob.tell(mob,target,null,auto?"":"You fumble the attempt to rob <T-NAME>.");
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if(stolen!=null)
					str="<S-NAME> rob(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
				{
					str="<S-NAME> attempt(s) to rob <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
					code=CMMsg.MSG_QUIETMOVEMENT;
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(CMLib.dice().rollPercentage()<discoverChance)
				hisStr=null;
			else
			{
				str+=" <T-NAME> spots you!";
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);
				if((target.isMonster())&&(mob.getVictim()==null)) mob.setVictim(target);
			}

			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)	beneficialAffect(mob,target,asLevel,0);
				A=(Thief_Robbery)target.fetchEffect(ID());
				if(A!=null)
					A.mobs.addElement(mob);
				
				if((!target.isMonster())&&(mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
				}
				else
				if(((hisStr==null)||mob.isMonster())
				&&(!alreadyFighting)
				&&((stolen==null)||(CMLib.dice().rollPercentage()>stolen.phyStats().level())))
				{
					if(target.getVictim()==mob)
						target.makePeace();
				}
				if(stolen!=null)
				{
					List<Environmental> products=shop.getShop().removeSellableProduct(stolen.Name(),mob);
					if(products.get(0) instanceof Item)
					{
						stolen=(Item)products.get(0);
						mob.location().addItem((Item)stolen,ItemPossessor.Expire.Player_Drop);
						msg=CMClass.getMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
					}
				}
			}
		}
		return success;
	}

}
