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
public class Dance_Cotillon extends Dance
{
	public String ID() { return "Dance_Cotillon"; }
	public String name(){ return "Cotillon";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}
	protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}
	protected MOB whichLast=null;

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((!mob.isInCombat())
			||(mob.getGroupMembers(new HashSet<MOB>()).size()<2))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected==invoker())&&((invoker()).isInCombat()))
		{
			if(whichLast==null)
				whichLast=invoker();
			else
			{
				MOB M=(MOB)affected;
				boolean pass=false;
				boolean found=false;
				for(int i=0;i<M.location().numInhabitants();i++)
				{
					MOB M2=M.location().fetchInhabitant(i);
					if(M2==whichLast)
						found=true;
					else
					if((M2!=whichLast)
					&&(found)
					&&(M2.fetchEffect(ID())!=null)
					&&(M2.isInCombat()))
					{
						whichLast=M2;
						break;
					}
					if(i==(M.location().numInhabitants()-1))
					{
						if(pass)
							return true;
						pass=true;
						i=-1;
					}
				}
				if((whichLast!=null)
				&&(M.isInCombat())
				&&(M.getVictim().getVictim()!=whichLast)
				&&(whichLast.location().show(whichLast,null,M.getVictim(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> dance(s) into <O-YOUPOSS> way.")))
					M.getVictim().setVictim(whichLast);
			}
		}
		return true;
	}

}
