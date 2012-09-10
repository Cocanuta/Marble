package com.planet_ink.marble_mud.Abilities.Misc;
import com.planet_ink.marble_mud.Abilities.StdAbility;
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
public class Dueler extends StdAbility
{
	public String ID() { return "Dueler"; }
	public String name(){ return "Dueler";}
	protected Dueler otherDueler = null;
	protected MOB otherDuelPartner=null;
	protected long lastTimeISawYou=System.currentTimeMillis();
	protected boolean oldPVPStatus = false;
	protected CharState oldCurState = null;
	protected List<Ability> oldEffects = new LinkedList<Ability>();
	protected Hashtable<Item,Item> oldEq = new Hashtable<Item,Item>();
	protected int autoWimp=0;
	
	public String displayText()
	{ 
		if(otherDueler != null)
			return "(Dueling "+otherDueler.affecting().name()+")";
		return "";
	}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public void unInvoke()
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((mob != null)&&(oldCurState!=null))
			{
				Dueler oDA=otherDueler;
				if(oDA!=null)
				{
					otherDueler=null;
					oDA.otherDueler=null;
					oDA.unInvoke();
				}
				if((canBeUninvoked())
				&&(!mob.amDead())
				&&(CMLib.flags().isInTheGame(mob,true)))
					mob.tell("Your duel has ended.");
				if(!oldPVPStatus) 
					mob.setBitmap(CMath.unsetb(mob.getBitmap(), MOB.ATT_PLAYERKILL));
				oldCurState.copyInto(mob.curState());
				LinkedList<Ability> cleanOut=new LinkedList<Ability>();
				for(Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(!oldEffects.contains(A))
						cleanOut.add(A);
				}
				for(Ability A : cleanOut)
				{
					if(!(A instanceof Dueler)) 
						A.unInvoke();
					mob.delEffect(A);
					A.destroy();
				}
				for(Item I : oldEq.keySet())
				{
					Item copyI=oldEq.get(I);
					if(I.amDestroyed())
						mob.addItem(copyI);
					else
					if(I.usesRemaining() < copyI.usesRemaining())
						I.setUsesRemaining(copyI.usesRemaining());
				}
				mob.setWimpHitPoint(autoWimp);
				mob.recoverCharStats();
				mob.recoverMaxState();
				mob.recoverPhyStats();
				mob.makePeace();
				Ability A=CMClass.getAbility("Immunities");
				if(A!=null)
					A.invoke(mob, new XVector("LEGAL","TICKS=1"), mob, true, 0);
			}
		}
		oldEffects.clear();
		oldEq.clear();
		oldCurState=null;
		otherDuelPartner=null;
		super.unInvoke();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.source()==affecting()))
		{
			MOB target=msg.source();
			Room deathRoom=target.location();
			String msp=CMProps.msp("death"+CMLib.dice().roll(1,7,0)+".wav",50);
			CMMsg msg2=CMClass.getMsg(target,null,otherDuelPartner,
					CMMsg.MSG_OK_VISUAL,"^f^*^<FIGHT^>!!!!!!!!!!!!!!YOU ARE DEFEATED!!!!!!!!!!!!!!^</FIGHT^>^?^.\n\r"+msp,
					CMMsg.MSG_OK_VISUAL,null,
					CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^><S-NAME> is DEFEATED!!!^</FIGHT^>^?\n\r"+msp);
			deathRoom.send(target, msg2);
			CMLib.combat().doDeathPostProcessing(msg);
			target.makePeace();
			//source.makePeace();
			unInvoke();
			return false;
		}
		else
		if((msg.target()==affecting())
		&&(msg.targetMinor()==CMMsg.TYP_LEGALWARRANT)
		&&(msg.tool()==otherDuelPartner))
			return false;
		return true;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((ticking instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)ticking;
			mob.setWimpHitPoint(0);
			final Dueler tDuel=otherDueler;
			if((tDuel==null)
			||(tDuel.amDestroyed)
			||(tDuel.unInvoked)
			||(!(tDuel.affecting() instanceof MOB))
			||(mob.amDead())
			||(((MOB)tDuel.affecting()).amDead())
			||(!(CMLib.flags().isInTheGame(mob, false)))
			||(!(CMLib.flags().isInTheGame((MOB)tDuel.affecting(), false)))
			||(mob.getVictim()==null)
			||((mob.getVictim()!=tDuel.affecting())&&(mob.getVictim().amUltimatelyFollowing()!=tDuel.affecting()))
			)
				unInvoke();
			else
			{
				MOB tMOB=(MOB)tDuel.affecting();
				if(mob.location()==tMOB.location())
					lastTimeISawYou=System.currentTimeMillis();
				if((System.currentTimeMillis()-lastTimeISawYou)>30000)
					unInvoke();
			}
		}
		return true;
	}
	
	public void init(MOB mob)
	{
		oldPVPStatus=CMath.bset(mob.getBitmap(), MOB.ATT_PLAYERKILL);
		mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
		oldCurState=(CharState)mob.curState().copyOf();
		oldEffects.clear();
		for(Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
			oldEffects.add(a.nextElement());
		autoWimp=mob.getWimpHitPoint();
		mob.setWimpHitPoint(0);
		for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
		{
			Item I=i.nextElement();
			if(((I instanceof Weapon)||(I instanceof Armor))
			&&(!I.amWearingAt(Item.IN_INVENTORY)))
				oldEq.put(I,(Item)I.copyOf());
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel)
	{
		if(target==null) target=mob;
		if(!(target instanceof MOB)) return false;
		if(((MOB)target).location()==null) return false;
		if(((MOB)target).location().show(mob,(MOB)target,this,CMMsg.MSG_OK_VISUAL,"^R<S-NAME> and <T-NAME> start(s) dueling!^?"))
		{
			MOB tmob = (MOB)target;
			Dueler A;
			Dueler tA;
			A=(Dueler)mob.fetchEffect(ID());
			if(A!=null){ A.unInvoke(); mob.delEffect(A); }
			A=(Dueler)tmob.fetchEffect(ID());
			if(A!=null){ A.unInvoke(); tmob.delEffect(A); }
			A=(Dueler)newInstance();
			tA=(Dueler)newInstance();
			A.otherDueler=tA;
			A.otherDuelPartner=tmob;
			tA.otherDueler=A;
			tA.otherDuelPartner=mob;
			A.init(mob);
			tA.init(tmob);
			mob.setVictim(tmob);
			tmob.setVictim(mob);
			A.startTickDown(mob, mob, Ability.TICKS_FOREVER);
			tA.startTickDown(tmob, tmob, Ability.TICKS_FOREVER);
		}
		return true;
	}
}
