package com.planet_ink.marble_mud.Abilities.Fighter;
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
public class Fighter_PointBlank extends FighterSkill
{
	public String ID() { return "Fighter_PointBlank"; }
	public String name(){ return "Point Blank Shot";}
	public String displayText(){ return "";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public int checkDown=4;

	protected List<Weapon> qualifiedWeapons=new Vector<Weapon>();

	protected void cloneFix(Ability E)
	{
		super.cloneFix(E);
		qualifiedWeapons=new XVector<Weapon>(((Fighter_PointBlank)E).qualifiedWeapons);
	}
	
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		qualifiedWeapons=new Vector();
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(msg.target() instanceof Weapon))
		{
			Weapon W=(Weapon)msg.target();
			if((W.weaponClassification()==Weapon.CLASS_RANGED)
			&&(W.ammunitionType().length()>0))
			{
				if(((msg.targetMinor()==CMMsg.TYP_WEAR)
				   ||(msg.targetMinor()==CMMsg.TYP_WIELD)
				   ||(msg.targetMinor()==CMMsg.TYP_HOLD))
				&&(!qualifiedWeapons.contains(W))
				&&((msg.source().fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
				{
					qualifiedWeapons.add(W);
					Ability A=(Ability)this.copyOf();
					A.setInvoker(invoker());
					A.setSavable(false);
					W.addEffect(A);
					W.recoverPhyStats();
				}
				else
				if(((msg.targetMinor()==CMMsg.TYP_REMOVE)
					||(msg.targetMinor()==CMMsg.TYP_DROP))
				&&(qualifiedWeapons.contains(msg.target())))
				{
					qualifiedWeapons.remove(msg.target());
					W.delEffect(W.fetchEffect(ID()));
					W.recoverPhyStats();
				}
			}
		}
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Item)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ITEMNOMINRANGE);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(--checkDown<=0)
		{
			checkDown=5;
			Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof Weapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(((Weapon)w).ammunitionType().length()>0)
			&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
			{
				if((CMLib.dice().rollPercentage()<10)&&(mob.isInCombat())&&(mob.rangeToTarget() > 0))
					helpProficiency(mob, 0);
				if(w.fetchEffect(ID())==null)
				{
					if(!qualifiedWeapons.contains(w))
						qualifiedWeapons.add((Weapon)w);
					Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					A.setInvoker(invoker());
					w.addEffect(A);
					w.recoverPhyStats();
				}
			}
			for(int i=qualifiedWeapons.size()-1;i>=0;i--)
			{
				Item I=(Item)qualifiedWeapons.get(i);
				if((I.amWearingAt(Wearable.IN_INVENTORY))
				||(I.owner()!=affected))
				{
					qualifiedWeapons.remove(I);
					I.delEffect(I.fetchEffect(ID()));
					I.recoverPhyStats();
				}
			}
		}
		return true;
	}
}
