package com.planet_ink.marble_mud.Abilities.Thief;
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
public class Thief_Con extends ThiefSkill
{
	public String ID() { return "Thief_Con"; }
	public String name(){ return "Con";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"CON"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE; }
	protected MOB lastChecked=null;
	public double castingTime(final MOB mob, final List<String> cmds){return CMProps.getActionSkillCost(ID(),CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFABLETIME),20.0));}
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		if(commands!=null) commands=new XVector<String>(commands);
		if(!conCheck(mob,commands,givenTarget,auto,asLevel))
			return false;
		Vector V=new Vector();
		V.addElement(commands.get(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;
		commands.remove(0);
		if(secondsElapsed>0)
		{
			if((secondsElapsed%4)==0)
				return mob.location().show(mob,target,CMMsg.MSG_SPEAK,"^T<S-NAME> continue(s) conning <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
			return true;
		}
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to con <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		else
			return false;
		return true;
	}

	public boolean conCheck(MOB mob, List<String> commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands!=null) commands= new XVector<String>(commands);
		if(commands.size()<1)
		{
			mob.tell("Con whom into doing what?");
			return false;
		}
		Vector V=new Vector();
		V.addElement(commands.get(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;
		
		commands.remove(0);

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
		{
			mob.tell("You can't con "+target.name()+".");
			return false;
		}

		if(target.isInCombat())
		{
			mob.tell(target.name()+" is too busy fighting right now.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("You are too busy fighting right now.");
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("Con "+target.charStats().himher()+" into doing what?");
			return false;
		}


		if(((String)commands.get(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't con someone into following you.");
			return false;
		}
		
		CMObject O=CMLib.english().findCommand(target,commands);
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob))||(((Command)O).ID().equals("Sleep")))
			{
				mob.tell("You can't con someone into doing that.");
				return false;
			}
		}
		else
		{
			if(O instanceof Ability)
				O=CMLib.english().getToEvoke(target,commands);
			if(O instanceof Ability)
			{
				if(CMath.bset(((Ability)O).flags(),Ability.FLAG_NOORDERING))
				{
					mob.tell("You can't con "+target.name()+" to do that.");
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands!=null) commands=new XVector(commands);
		if(!conCheck(mob,commands,givenTarget,auto,asLevel))
			return false;
		Vector V=new Vector();
		V.addElement(commands.elementAt(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;
		commands.removeElementAt(0);

		int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=((mob.phyStats().level()+(2*super.getXLEVELLevel(mob)))-target.phyStats().level())*10;
		if(levelDiff>0) levelDiff=0;
		boolean success=proficiencyCheck(mob,(mob.charStats().getStat(CharStats.STAT_CHARISMA)*2)+levelDiff,auto);

		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> tr(ys) to con <T-NAMESELF> to '"+CMParms.combine(commands,0)+"', but <S-IS-ARE> unsuccessful.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> con(s) <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
			mob.recoverPhyStats();
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().show(mob,target,CMMsg.MSG_ORDER,null)))
			{
				mob.location().send(mob,msg);
				target.enqueCommand(commands,Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
			}
			target.recoverPhyStats();
		}
		if(target==lastChecked)
			setProficiency(oldProficiency);
		lastChecked=target;
		return success;
	}

}
