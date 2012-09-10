package com.planet_ink.marble_mud.Behaviors;
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
public class Beggar extends StdBehavior
{
	public String ID(){return "Beggar";}
	Vector mobsHitUp=new Vector();
	int tickTock=0;

	public String accountForYourself()
	{ 
		return "vagrant-like begging";
	}
	
	public void executeMsg(Environmental oking, CMMsg msg)
	{
		super.executeMsg(oking,msg);
		if((oking==null)||(!(oking instanceof MOB)))
			return;
		MOB mob=(MOB)oking;
		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_GIVE))
			msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you gov'ner!' to <T-NAME> ^?"));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.EMOTERS)) return true;
		tickTock++;
		if(tickTock<5) return true;
		tickTock=0;
		MOB mob=(MOB)ticking;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB mob2=mob.location().fetchInhabitant(i);
			if((mob2!=null)
			   &&(CMLib.flags().canBeSeenBy(mob2,mob))
			   &&(mob2!=mob)
			   &&(!mobsHitUp.contains(mob2))
			   &&(!mob2.isMonster()))
			{
				switch(CMLib.dice().roll(1,10,0))
				{
				case 1:
					CMLib.commands().postSay(mob,mob2,"A little something for a vet please?",false,false);
					break;
				case 2:
					CMLib.commands().postSay(mob,mob2,"Spare a gold piece "+((mob2.charStats().getStat(CharStats.STAT_GENDER)=='M')?"mister?":"madam?"),false,false);
					break;
				case 3:
					CMLib.commands().postSay(mob,mob2,"Spare some change?",false,false);
					break;
				case 4:
					CMLib.commands().postSay(mob,mob2,"Please "+((mob2.charStats().getStat(CharStats.STAT_GENDER)=='M')?"mister":"madam")+", a little something for an old "+((mob.charStats().getStat(CharStats.STAT_GENDER)=='M')?"man":"woman")+" down on "+mob.charStats().hisher()+" luck?",false,false);
					break;
				case 5:
					CMLib.commands().postSay(mob,mob2,"Hey, I lost my 'Will Work For Food' sign.  Can you spare me the money to buy one?",false,false);
					break;
				case 6:
					CMLib.commands().postSay(mob,mob2,"Spread a little joy to an old fogie?",false,false);
					break;
				case 7:
					CMLib.commands().postSay(mob,mob2,"Change?",false,false);
					break;
				case 8:
					CMLib.commands().postSay(mob,mob2,"Can you spare a little change?",false,false);
					break;
				case 9:
					CMLib.commands().postSay(mob,mob2,"Can you spare a little gold?",false,false);
					break;
				case 10:
					CMLib.commands().postSay(mob,mob2,"Gold piece for a poor fogie down on "+mob.charStats().hisher()+" luck?",false,false);
					break;
				}
				mobsHitUp.addElement(mob2);
				break;
			}
		}
		if(mobsHitUp.size()>0)
			mobsHitUp.removeElementAt(0);
		return true;
	}
}
