package com.planet_ink.marble_mud.Abilities.Poisons;
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
public class Poison_Alcohol extends Poison
{
	public String ID() { return "Poison_Alcohol"; }
	public String name(){ return "Alcohol";}
	private static final String[] triggerStrings = {"POISONALCOHOL"};
	public String displayText(){ return (drunkness<=3)?"(Tipsy)":((drunkness<10)?"(Drunk)":"(Smashed)");}
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return super.flags()|Ability.FLAG_INTOXICATING;}

	protected int POISON_TICKS(){return 65;}
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "You feel sober again.";}
	protected String POISON_START(){return "^G<S-NAME> burp(s)!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> inebriate(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	protected boolean disableHappiness=false;

	protected int alchoholContribution(){return 1;}
	protected int level(){return 1;}
	protected int drunkness=5;

	public Poison_Alcohol()
	{
		super();
		drunkness=5;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(drunkness+((MOB)affected).phyStats().level()));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_DEXTERITY,(affectableStats.getStat(CharStats.STAT_DEXTERITY)-drunkness));
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((CMLib.dice().rollPercentage()==1)&&(!((MOB)affected).isMonster()))
			{
				Ability A=CMClass.getAbility("Disease_Migraines");
				if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					A.invoke(mob,mob,true,0);
			}
			CMLib.commands().postStand(mob,true);
		}
		super.unInvoke();
	}

	protected boolean catchIt(MOB mob, Physical target)
	{
		boolean caughtIt=super.catchIt(mob,target);
		if(!(affected instanceof Drink)) 
			return caughtIt;
		if(CMLib.dice().roll(1,1000,0)>(alchoholContribution()*alchoholContribution()*alchoholContribution()))
			return caughtIt;
		if((target!=null)&&(target instanceof MOB)&&(target.fetchEffect(ID())==null))
		{
			MOB targetMOB=(MOB)target;
			Ability A=targetMOB.fetchEffect("Addictions");
			if(A==null)
			{
				A=CMClass.getAbility("Addictions");
				if(A!=null) A.invoke(targetMOB,affected,true,0);
			}
		}
		return caughtIt;
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if(disableHappiness){disableHappiness=false; return true;}

		MOB mob=(MOB)affected;
		if(mob==null) return true;

		Room room=mob.location();
		if((CMLib.dice().rollPercentage()<(4*drunkness))&&(CMLib.flags().aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(CMLib.flags().isEvil(mob))
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_NOISE,"<S-NAME> belch(es) grotesquely.");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> spin(s) <S-HIS-HER> head around.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_NOISE,"<S-NAME> can't stop snarling.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> just fell over!");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely sh** faced!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at the ground.");
				break;
			}
			else
			if(!CMLib.flags().isGood(mob))
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> burp(s) noncommitally.");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> almost fell over.");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and almost smile(s).");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es)!");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely drunk!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly ahead.");
				break;
			}
			else
			switch(CMLib.dice().roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and smile(s).");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> bob(s) <S-HIS-HER> head back and forth.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't stop smiling.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> lean(s) slightly to one side.");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely a bit tipsy!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at <S-HIS-HER> eyelids.");
				break;
			}

		}
		return true;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			if(msg.source()!=affected)
				return true;
			if(msg.source().location()==null)
				return true;

			if((msg.amISource((MOB)affected))
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&(drunkness>=5)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))))
			{
				Ability A=CMClass.getAbility("Drunken");
				if(A!=null)
				{
					A.setProficiency(100);
					A.invoke(msg.source(),null,true,0);
					A.setAffectedOne(msg.source());
					if(!A.okMessage(myHost,msg))
						return false;
				}
			}
			else
			if((!msg.targetMajor(CMMsg.MASK_ALWAYS))
			&&(CMLib.dice().rollPercentage()<(drunkness*20))
			&&(msg.targetMajor()>0))
			{
				if((msg.target() !=null)
					&&(msg.target() instanceof MOB))
						msg.modify(msg.source(),msg.source().location().fetchInhabitant(CMLib.dice().roll(1,msg.source().location().numInhabitants(),0)-1),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
			}
		}
		else
		{

		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int largest=alchoholContribution();
		if((givenTarget instanceof MOB)&&(auto))
		{
			Vector found=new Vector();
			Vector remove=new Vector();
			largest=0;
			for(final Enumeration<Ability> a=givenTarget.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Poison_Alcohol)
				{
					largest+=((Poison_Alcohol)A).drunkness;
					if(((Poison_Alcohol)A).level()>=level())
						found.addElement(A);
					else
						remove.addElement(A);
				}
			}
			largest+=alchoholContribution();
			if(found.size()>0)
			{
				CMMsg msg=CMClass.getMsg(mob,givenTarget,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_POISON|CMMsg.MASK_ALWAYS,POISON_CAST());
				Room R=(((MOB)givenTarget).location()!=null)?((MOB)givenTarget).location():mob.location();
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					if(msg.value()<=0)
					{
						R.show((MOB)givenTarget,null,CMMsg.MSG_OK_VISUAL,POISON_START());
						for(int a=0;a<found.size();a++)
						{
							((Poison_Alcohol)found.elementAt(a)).drunkness=largest;
							((Poison_Alcohol)found.elementAt(a)).tickDown=POISON_TICKS();
						}
						R.recoverRoomStats();
						return true;
					}
				}
				return false;
			}

			for(int i=0;i<remove.size();i++)
				givenTarget.delEffect((Ability)remove.elementAt(i));
		}
		boolean success=super.invoke(mob,commands,givenTarget,auto,asLevel);
		if(success&&(givenTarget instanceof MOB)&&(auto))
		{
			Ability A=givenTarget.fetchEffect(ID());
			if(A!=null)
			{
				((Poison_Alcohol)A).drunkness=largest;
				((Poison_Alcohol)A).tickDown=POISON_TICKS();
			}
			Room R=(((MOB)givenTarget).location()!=null)?((MOB)givenTarget).location():mob.location();
			R.recoverRoomStats();
		}
		return success;
	}
}
