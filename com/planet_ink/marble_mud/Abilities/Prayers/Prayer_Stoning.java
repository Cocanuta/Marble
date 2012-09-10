package com.planet_ink.marble_mud.Abilities.Prayers;
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
public class Prayer_Stoning extends Prayer
{
	public String ID() { return "Prayer_Stoning"; }
	public String name(){ return "Stoning";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected Vector cits=new Vector();

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		cits=new Vector();
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		MOB mob=(MOB)affected;
		Room R=mob.location();
		if(R!=null)
		{
			for(int i=0;i<cits.size();i++)
			{
				MOB M=(MOB)cits.elementAt(i);
				if((M.location()!=mob.location())||(mob.amDead()))
				{
					CMLib.tracking().wanderAway(M,true,false);
					M.destroy();
					M.setLocation(null);
				}
				else
				{
					if(CMLib.dice().rollPercentage()>=50)
					{
						int dmg=mob.maxState().getHitPoints()/20;
						if(dmg<1) dmg=1;
						Item W=M.fetchWieldedItem();
						if(W!=null)
						{
							W.basePhyStats().setDamage(dmg);
							W.phyStats().setDamage(dmg);
						}
						CMLib.combat().postDamage(M,mob,W,dmg,CMMsg.MSG_WEAPONATTACK|CMMsg.MASK_ALWAYS,Weapon.TYPE_BASHING,"<S-NAME> stone(s) <T-NAMESELF>!");
					}
					else
						R.show(M,mob,null,CMMsg.MSG_NOISE,"<S-NAME> shout(s) obscenities at <T-NAMESELF>.");
				}
			}
			while(cits.size()<10)
			{
				MOB M=CMClass.getMOB("AngryCitizen");
				if(M==null)
				{
					unInvoke();
					break;
				}
				Room R2=CMClass.getLocale("StdRoom");
				cits.addElement(M);
				M.bringToLife(R2,true);
				CMLib.tracking().wanderIn(M,R);
				M.setStartRoom(null);
				R2.destroy();
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		LegalBehavior B=null;
		if(mob.location()!=null) B=CMLib.law().getLegalBehavior(mob.location());
		List<LegalWarrant> warrants=new Vector();
		if(B!=null)
			warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
		if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
		{
			mob.tell("You are not allowed to stone "+target.Name()+" at this time.");
			return false;
		}
		
		if((!auto)&&(!CMLib.flags().isBoundOrHeld(target))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(target.name()+" must be bound first.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> call(s) for the stoning of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE);
					for(int i=0;i<warrants.size();i++)
					{
						LegalWarrant W=(LegalWarrant)warrants.get(i);
						W.setCrime("pardoned");
						W.setOffenses(0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> call(s) for the stoning of <T-NAMESELF>.");


		// return whether it worked
		return success;
	}
}
