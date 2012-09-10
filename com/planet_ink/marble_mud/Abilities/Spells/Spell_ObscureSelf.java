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
@SuppressWarnings("rawtypes")
public class Spell_ObscureSelf extends Spell
{
	public String ID() { return "Spell_ObscureSelf"; }
	public String name(){return "Obscure Self";}
	public String displayText(){return "(Obscure Self)";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
	private final static String[][] stuff={
		{"<S-NAME>","<T-NAME>","someone"},
		{"<S-HIS-HER>","<T-HIS-HER>","his or her"},
		{"<S-HIM-HER>","<T-HIM-HER>","him or her"},
		{"<S-NAMESELF>","<T-NAMESELF>","someone"},
		{"<S-HE-SHE>","<T-HE-SHE>","he or she"},
		{"<S-YOUPOSS>","<T-YOUPOSS>","someone's"},
		{"<S-HIM-HERSELF>","<T-HIM-HERSELF>","him or herself"},
		{"<S-HIS-HERSELF>","<T-HIS-HERSELF>","his or herself"}
	};


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		String othersMessage=msg.othersMessage();
		String sourceMessage=msg.sourceMessage();
		String targetMessage=msg.targetMessage();
		boolean somethingsChanged=false;
		int x=0;
		if((msg.amITarget(mob))&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE))
		{
			if((!msg.amISource(mob))&&((msg.targetMinor()==CMMsg.TYP_LOOK)
										||(msg.targetMinor()==CMMsg.TYP_EXAMINE)
										||(msg.targetMinor()==CMMsg.TYP_READ)))
			{
				msg.source().tell("He or she is too vague to make out any details.");
				return false;
			}

			if(othersMessage!=null)
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=othersMessage.indexOf(stuff[i][1]);
					while(x>=0)
					{
						somethingsChanged=true;
						othersMessage=othersMessage.substring(0,x)+stuff[i][2]+othersMessage.substring(x+(stuff[i][1]).length());
						x=othersMessage.indexOf(stuff[i][1]);
					}
				}
			}
			if((!msg.amISource(mob))&&(sourceMessage!=null))
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=sourceMessage.indexOf(stuff[i][1]);
					while(x>=0)
					{
						somethingsChanged=true;
						sourceMessage=sourceMessage.substring(0,x)+stuff[i][2]+sourceMessage.substring(x+(stuff[i][1]).length());
						x=sourceMessage.indexOf(stuff[i][1]);
					}
				}
			}
		}
		if(msg.amISource(mob))
		{
			if(othersMessage!=null)
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=othersMessage.indexOf(stuff[i][0]);
					while(x>=0)
					{
						somethingsChanged=true;
						othersMessage=othersMessage.substring(0,x)+stuff[i][2]+othersMessage.substring(x+(stuff[i][0]).length());
						x=othersMessage.indexOf(stuff[i][0]);
					}
				}
			}
			if((!msg.amITarget(mob))&&(targetMessage!=null))
			{
				for(int i=0;i<stuff.length;i++)
				{
					x=targetMessage.indexOf(stuff[i][0]);
					while(x>=0)
					{
						somethingsChanged=true;
						targetMessage=targetMessage.substring(0,x)+stuff[i][2]+targetMessage.substring(x+(stuff[i][0]).length());
						x=targetMessage.indexOf(stuff[i][0]);
					}
				}
			}
		}
		if(somethingsChanged)
			msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),sourceMessage,msg.targetCode(),targetMessage,msg.othersCode(),othersMessage);
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) a bit less obscure.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already obscure.");
			return false;
		}

		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
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
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"^S<T-NAME> become(s) obscure!":"^S<S-NAME> whisper(s) to <S-HIM-HERSELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s) to <S-HIM-HERSELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
