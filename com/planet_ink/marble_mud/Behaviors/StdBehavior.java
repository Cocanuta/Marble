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
public class StdBehavior implements Behavior
{
	public String ID(){return "StdBehavior";}
	public String name(){return ID();}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return 0;}
	public boolean grantsAggressivenessTo(MOB M){return false;}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public void initializeClass(){}
	public String accountForYourself(){ return "";}
	protected boolean isSavableBehavior=true;

	public StdBehavior()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.BEHAVIOR);//removed for perf
	}

	protected String parms="";

	/** return a new instance of the object*/
	public CMObject newInstance()
	{
		try
		{
			return (Behavior)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBehavior();
	}
	public CMObject copyOf()
	{
		try
		{
			Behavior B=(Behavior)this.clone();
			//CMClass.bumpCounter(B,CMClass.CMObjectType.BEHAVIOR);//removed for perf
			B.setParms(getParms());
			return B;
		}
		catch(CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}
	public void registerDefaultQuest(String questName){}
	public void startBehavior(PhysicalAgent forMe){}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.BEHAVIOR);}//removed for perf
	public void setSavable(boolean truefalse){isSavableBehavior=truefalse;}
	public boolean isSavable(){return isSavableBehavior;}
	public boolean amDestroyed(){ return false;}
	public void destroy() {parms="";}
	protected MOB getBehaversMOB(Tickable ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof MOB)
			return (MOB)ticking;
		else
		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof MOB)
					return (MOB)((Item)ticking).owner();

		return null;
	}

	protected Room getBehaversRoom(Tickable ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof Room)
			return (Room)ticking;

		MOB mob=getBehaversMOB(ticking);
		if(mob!=null)
			return mob.location();

		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof Room)
					return (Room)((Item)ticking).owner();

		return null;
	}

	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters;}
	public String parmsFormat(){return CMParms.FORMAT_UNDEFINED;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public List<String> externalFiles(){return null;}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		return;
	}

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		return true;
	}

	public boolean canImprove(int can_code){return CMath.bset(canImproveCode(),can_code);}
	public boolean canImprove(PhysicalAgent E)
	{
		if((E==null)&&(canImproveCode()==0)) return true;
		if(E==null) return false;
		if((E instanceof MOB)&&((canImproveCode()&Ability.CAN_MOBS)>0)) return true;
		if((E instanceof Item)&&((canImproveCode()&Ability.CAN_ITEMS)>0)) return true;
		if((E instanceof Exit)&&((canImproveCode()&Ability.CAN_EXITS)>0)) return true;
		if((E instanceof Room)&&((canImproveCode()&Ability.CAN_ROOMS)>0)) return true;
		if((E instanceof Area)&&((canImproveCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}
	public static boolean canActAtAll(Tickable affecting)
	{ 
		return CMLib.flags().canActAtAll(affecting);
	}

	public static boolean canFreelyBehaveNormal(Tickable affecting)
	{ 
		return CMLib.flags().canFreelyBehaveNormal(affecting);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof Environmental) && (((Environmental)ticking).amDestroyed()))
			return false;
		return true;
	}

	protected static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return getParms();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setParms(val); break;
		}
	}
	public int getSaveStatIndex(){return getStatCodes().length;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	public boolean sameAs(Behavior E)
	{
		if(!(E instanceof StdBehavior)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
