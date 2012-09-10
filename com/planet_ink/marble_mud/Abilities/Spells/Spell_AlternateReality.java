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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_AlternateReality extends Spell
{
	public String ID() { return "Spell_AlternateReality"; }
	public String name(){return "Alternate Reality";}
	public String displayText(){return "(Alternate Reality)";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your reality returns to normal.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return false;
		MOB mob=(MOB)affected;
		if(!mob.isInCombat())
		{
			unInvoke();
			return false;
		}
		return true;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amISource((MOB)affected)))
		&&(msg.target()!=null)
		&&(invoker()!=null))
		{
			Set<MOB> H=invoker().getGroupMembers(new HashSet<MOB>());
			if(H.contains(msg.target()))
			{
				msg.source().tell("But you are on "+invoker().name()+"'s side!");
				if(invoker().getVictim()!=affected)
					((MOB)affected).setVictim(invoker().getVictim());
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.getVictim()!=mob)
		{
			mob.tell("But "+target.charStats().heshe()+" isn't fighting you!");
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) to <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0));
					if(success)
					{
						Room R=target.location();
						R.show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> change(s) sides!");
						target.makePeace();
						if(mob.getVictim()==target)
							mob.setVictim(null);
						Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
						if(!H.contains(mob))H.add(mob);
						Vector badGuys=new Vector();
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)&&(M!=mob)&&(M!=target))
							{
								if(!H.contains(M))
								{
									if(M.getVictim()==mob)
									{
										badGuys.clear();
										badGuys.addElement(M);
										break;
									}
									badGuys.addElement(M);
								}
								else
								if(M.getVictim()==target)
									M.setVictim(null);
							}
						}
						if(badGuys.size()>0)
						{
							target.setVictim((MOB)badGuys.elementAt(CMLib.dice().roll(1,badGuys.size(),-1)));
							if(mob.getVictim()==null)
								mob.setVictim((MOB)badGuys.elementAt(CMLib.dice().roll(1,badGuys.size(),-1)));
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) to <T-NAME>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
