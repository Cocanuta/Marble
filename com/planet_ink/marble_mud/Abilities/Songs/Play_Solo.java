package com.planet_ink.marble_mud.Abilities.Songs;
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
public class Play_Solo extends Play
{
	public String ID() { return "Play_Solo"; }
	public String name(){ return "Solo";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected boolean persistantSong(){return false;}
	protected boolean skipStandardSongTick(){return true;}
	protected String songOf(){return "a "+name();}
	protected boolean skipStandardSongInvoke(){return true;}

	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB myChar=(MOB)affected;
			if(!msg.amISource(myChar)
			&&(msg.tool()!=null)
			&&(!msg.tool().ID().equals(ID()))
			&&(msg.tool() instanceof Ability)
			&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
			{
				MOB otherBard=msg.source();
				if(((otherBard.phyStats().level()+CMLib.dice().roll(1,30,0)+getXLEVELLevel(otherBard))>(myChar.phyStats().level()+CMLib.dice().roll(1,20,0)+getXLEVELLevel(myChar)))
				&&(otherBard.location()!=null))
				{
					if((otherBard.location().show(otherBard,myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!"))
					&&((otherBard.location()==originRoom)
							||(originRoom==null)
							||originRoom.showOthers(otherBard, myChar, null, CMMsg.MSG_OK_ACTION,"<S-NAME> upstage(s) <T-NAMESELF>, stopping <T-HIS-HER> solo!")))
								unplayMe(myChar,null);
				}
				else
				if(otherBard.location()!=null)
				{
					otherBard.tell("You can't seem to upstage "+myChar.name()+"'s solo.");
					if(!invoker().curState().adjMana(-10,invoker().maxState()))
						unplayMe(myChar,null);
					return false;
				}
			}
		}
		return true;
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((!mob.isInCombat())||(CMLib.flags().domainAffects(mob.getVictim(), Ability.ACODE_SONG).size()==0))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		unplayAll(mob,mob);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(originRoom==R)
						R.send(mob,msg);
					else
						R.sendOthers(mob,msg);
					invoker=mob;
					Play newOne=(Play)this.copyOf();
	
					Vector songsToCancel=new Vector();
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if(M!=null)
						for(int a=0;a<M.numEffects();a++) // personal affects
						{
							Ability A=M.fetchEffect(a);
							if((A!=null)
							&&(A.invoker()!=mob)
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
								songsToCancel.addElement(A);
						}
					}
					int reqMana=songsToCancel.size()*10;
					if(mob.curState().getMana()<reqMana)
					{
						mob.tell("You needed "+reqMana+" mana to play this solo!");
						return false;
					}
					mob.curState().adjMana(-reqMana,mob.maxState());
					for(int i=0;i<songsToCancel.size();i++)
					{
						Ability A=(Ability)songsToCancel.elementAt(i);
						A.unInvoke();
					}
					mob.addEffect(newOne);
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
