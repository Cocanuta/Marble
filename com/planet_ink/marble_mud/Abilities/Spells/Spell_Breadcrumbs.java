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
public class Spell_Breadcrumbs extends Spell
{
	public String ID() { return "Spell_Breadcrumbs"; }
	public String name(){return "Breadcrumbs";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}
	public Vector trail=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your breadcrumbs fade away.");
		trail=null;
	}

	public String displayText(){
		StringBuffer str=new StringBuffer("(Breadcrumb Trail: ");
		if(trail!=null)
		synchronized(trail)
		{
			Room lastRoom=null;
			for(int v=trail.size()-1;v>=0;v--)
			{
				Room R=(Room)trail.elementAt(v);
				if(lastRoom!=null)
				{
					int dir=-1;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if(lastRoom.getRoomInDir(d)==R)
						{ dir=d; break;}
					}
					if(dir>=0)
						str.append(Directions.getDirectionName(dir)+" ");
					else
						str.append("Unknown ");
				}
				lastRoom=R;
			}
		}
		return str.toString()+")";
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(trail!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Room))
		{
			Room newRoom=(Room)msg.target();
			boolean kill=false;
			int t=0;
			while(t<trail.size())
			{
				if(kill) trail.removeElement(trail.elementAt(t));
				else
				{
					Room R=(Room)trail.elementAt(t);
					if(R==newRoom)
						kill=true;
					t++;
				}
			}
			if(kill) return;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Room adjacentRoom=newRoom.getRoomInDir(d);
				if((adjacentRoom!=null)
				   &&(newRoom.getExitInDir(d)!=null))
				{
					kill=false;
					t=0;
					while(t<trail.size())
					{
						if(kill) trail.removeElement(trail.elementAt(t));
						else
						{
							Room R=(Room)trail.elementAt(t);
							if(R==adjacentRoom)
								kill=true;
							t++;
						}
					}
				}
			}
			trail.addElement(newRoom);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if(target==null) return false;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already dropping breadcrumbs.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> attain(s) mysterious breadcrumbs.":"^S<S-NAME> invoke(s) the mystical breadcrumbs.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				trail=new Vector();
				trail.addElement(mob.location());
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke breadcrumbs, but fail(s).");

		// return whether it worked
		return success;
	}
}
