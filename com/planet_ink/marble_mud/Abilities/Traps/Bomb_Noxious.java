package com.planet_ink.marble_mud.Abilities.Traps;
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
public class Bomb_Noxious extends StdBomb
{
	public String ID() { return "Bomb_Noxious"; }
	public String name(){ return "stink bomb";}
	protected int trapLevel(){return 12;}
	public String requiresToSet(){return "an egg";}

	public List<Item> getTrapComponents() {
		Vector V=new Vector();
		V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_EGGS));
		return V;
	}
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P)) return false;
		if((!(P instanceof Item))
		||(((Item)P).material()!=RawMaterial.RESOURCE_EGGS))
		{
			if(mob!=null)
				mob.tell("You an egg to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the stink bomb!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,affected.name()+" explodes stink into <T-YOUPOSS> eyes!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility("Spell_StinkingCloud");
				if(A!=null){
					A.invoke(target,target,true,invoker().phyStats().level()+abilityCode());
					A=target.fetchEffect(A.ID());
					if(A!=null)A.setInvoker(invoker());
				}
			}
		}
	}

}
