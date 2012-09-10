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
public class DiseaseCure extends StdAbility
{
	public String ID() { return "DiseaseCure"; }
	public String name(){ return "A Cure";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	protected boolean processing=false;

	public List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)
			&&(A instanceof DiseaseAffect)
			&&((text().length()==0)||(A.name().toUpperCase().indexOf(text().toUpperCase())>=0)||(A.ID().toUpperCase().indexOf(text().toUpperCase())>=0)))
				offenders.addElement(A);
		}
		return offenders;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null) return;
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				if(msg.amITarget(myItem))
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_DRINK:
						if(myItem instanceof Drink)
						{
							invoke(msg.source(),null,msg.source(),true,0);
							myItem.destroy();
						}
						break;
					case CMMsg.TYP_EAT:
						if(myItem instanceof Food)
						{
							invoke(msg.source(),null,msg.source(),true,0);
							myItem.destroy();
						}
						break;
					case CMMsg.TYP_WEAR:
						if(myItem.rawProperLocationBitmap()!=Wearable.WORN_HELD)
						{
							invoke(msg.source(),null,msg.source(),true,0);
							myItem.destroy();
						}
						break;
					}
			}
			processing=false;
		}
		super.executeMsg(myHost,msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		List<Ability> offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.get(a)).unInvoke();
				if((!CMLib.flags().stillAffectedBy(target,offensiveAffects,false))&&(target.location()!=null))
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) much better.");
			}
		}

		// return whether it worked
		return success;
	}
}
