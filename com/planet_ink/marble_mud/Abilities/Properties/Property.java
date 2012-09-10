package com.planet_ink.marble_mud.Abilities.Properties;
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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;


import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

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
public class Property implements Ability
{
	public String ID() { return "Property"; }
	public String name(){ return "a Property";}
	public String Name(){return name();}
	public String description(){return "";}
	public String displayText(){return "";}
	protected boolean savable=true;
	protected String miscText="";
	protected Physical affected=null;
	
	/**
	 * Designates whether, when used as a property/effect, what sort of objects this 
	 * ability can affect. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.marble_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can affect
	 */
	protected int canAffectCode(){return 0;}
	/**
	 * Designates whether, when invoked as a skill, what sort of objects this 
	 * ability can effectively target. Uses the Ability.CAN_* constants.
	 * @see com.planet_ink.marble_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can target
	 */
	protected int canTargetCode(){return 0;}
	public boolean canTarget(int can_code){return CMath.bset(canTargetCode(),can_code);}
	public boolean canAffect(int can_code){return CMath.bset(canAffectCode(),can_code);}
	public double castingTime(final MOB mob, final List<String> cmds){return 0.0;}
	public double combatCastingTime(final MOB mob, final List<String> cmds){return 0.0;}
	public double checkedCastingCost(final MOB mob, final List<String> cmds){return 0.0;}
	public int abilityCode(){return 0;}
	public void setAbilityCode(int newCode){}
	public int adjustedLevel(MOB mob, int asLevel){return -1;}
	public int getTicksBetweenCasts() { return 0;}
	public boolean bubbleAffect(){return false;}
	public long flags(){return 0;}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public int usageType(){return 0;}
	public void initializeClass(){}
	public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob)
	{ return new ExpertiseLibrary.SkillCost(ExpertiseLibrary.CostType.TRAIN, Double.valueOf(1.0));}

	public void setName(String newName){}
	public void setDescription(String newDescription){}
	public void setDisplayText(String newDisplayText){}
	public String image(){return "";}
	public String rawImage(){return "";}
	public void setImage(String newImage){}
	public MOB invoker(){return null;}
	public void setInvoker(MOB mob){}
	public static final String[] empty={};
	public String[] triggerStrings(){return empty;}
	public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel){return false;}
	public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel){return false;}
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining){return true;}
	public boolean autoInvocation(MOB mob){return false;}
	public void unInvoke(){}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return true;}
	public boolean isNowAnAutoEffect(){return true;}
	public List<String> externalFiles(){return null;}

	public boolean canBeTaughtBy(MOB teacher, MOB student){return false;}
	public boolean canBePracticedBy(MOB teacher, MOB student){return false;}
	public boolean canBeLearnedBy(MOB teacher, MOB student){return false;}
	public void teach(MOB teacher, MOB student){}
	public void practice(MOB teacher, MOB student){}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public long expirationDate(){return 0;}
	public void setExpirationDate(long time){}
	public void startTickDown(MOB invokerMOB, Physical affected, int tickTime)
	{
		if(affected.fetchEffect(ID())==null)
			affected.addEffect(this);
	}

	public int proficiency(){return 0;}
	public void setProficiency(int newProficiency){}
	public boolean proficiencyCheck(MOB mob, int adjustment, boolean auto){return false;}
	public void helpProficiency(MOB mob, int adjustment){}

	public Physical affecting(){return affected;}
	public void setAffectedOne(Physical P){affected=P;}

	public boolean putInCommandlist(){return false;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int castingQuality(MOB invoker, Physical target){return Ability.QUALITY_INDIFFERENT;}

	public int classificationCode(){ return Ability.ACODE_PROPERTY;}
	public boolean isSavable(){ return savable;	}
	public void setSavable(boolean truefalse)	{ savable=truefalse; }
	protected boolean amDestroyed=false;
	public void destroy(){amDestroyed=true; affected=null; miscText=null; }
	public boolean amDestroyed(){return amDestroyed;}

	//protected void finalize(){ CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for mem & perf

	public CMObject newInstance()
	{
		try
		{
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new Property();
	}

	public Property()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for mem & perf
	}
	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setMiscText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof Property)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
	private void cloneFix(Ability E){}

	public CMObject copyOf()
	{
		try
		{
			Property E=(Property)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for mem & perf
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void setMiscText(String newMiscText)
	{ miscText=newMiscText;}
	public String text(){ return miscText;}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public boolean appropriateToMyFactions(MOB mob){return true;}
	public String accountForYourself(){return "";}
	public String requirements(MOB mob){return "";}

	public boolean canAffect(Physical P)
	{
		if((P==null)&&(canAffectCode()==0)) return true;
		if(P==null) return false;
		if((P instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0)) return true;
		if((P instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0)) return true;
		if((P instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0)) return true;
		if((P instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0)) return true;
		if((P instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0)) return true;
		return false;
	}

	public boolean canTarget(Physical P)
	{ return false;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		return;
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}
	public boolean tick(Tickable ticking, int tickID)
	{ return true;	}
	public void makeLongLasting(){}
	public void makeNonUninvokable(){}
	private static final int[] cost=new int[3];
	public int[] usageCost(MOB mob,boolean ignoreCostOverride){return cost;}
	public boolean isGeneric(){return false;}
}
