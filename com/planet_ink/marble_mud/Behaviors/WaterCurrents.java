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
import com.planet_ink.marble_mud.Items.Basic.StdItem;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class WaterCurrents extends ActiveTicker
{
	public String ID(){return "WaterCurrents";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	protected String dirs="";

	public WaterCurrents()
	{
		super();
		minTicks=3;maxTicks=5;chance=75;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "water current moving";
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		Vector<String> V=CMParms.parse(newParms);
		dirs="";
		for(int v=0;v<V.size();v++)
		{
			int dir=Directions.getGoodDirectionCode((String)V.elementAt(v));
			if(dir>=0) dirs=dirs+Directions.getDirectionChar(dir);
		}
		if(dirs.length()==0)
			dirs="NE";
	}
	public void applyCurrents(Room R, Vector done)
	{
		Vector todo=new Vector();
		if((R!=null)&&(R.numInhabitants()>0))
		{
			MOB M=null;
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if((M!=null)
				&&(!M.isMonster())
				&&(M.riding()==null)
				&&(!CMLib.flags().isInFlight(M))
				&&((!(M instanceof Rideable))||(((Rideable)M).numRiders()==0))
				&&(!M.isInCombat())
				&&(!CMLib.flags().isMobile(M))
				&&(!done.contains(M)))
				{
					todo.addElement(M);
					done.addElement(M);
				}
			}
		}
		if((R!=null)&&(R.numItems()>0))
		{
			Item I=null;
			for(int i=0;i<R.numItems();i++)
			{
				I=R.getItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&((!(I instanceof Rideable))
						||(((Rideable)I).rideBasis()!=Rideable.RIDEABLE_WATER)
						||(((Rideable)I).numRiders()==0))
				&&(!CMLib.flags().isInFlight(I))
				&&(!CMLib.flags().isMobile(I))
				&&(!done.contains(I)))
				{
					todo.addElement(I);
					done.addElement(I);
				}
			}
		}
		if((todo.size()>0)&&(R!=null))
		{
			int dir=-1;
			Room R2=null;
			for(int dl=0;dl<dirs.length();dl++)
			{
				dir=Directions.getDirectionCode(""+dirs.charAt(dl));
				if(dir>=0)
				{
					R2=R.getRoomInDir(dir);
					if(R2!=null)
					{
						if((R.getExitInDir(dir)!=null)
						&&(R.getExitInDir(dir).isOpen())
						&&((R2.domainType()==R.domainType())
							||(R2.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)))
								break;
						R2=null;
					}
				}
			}
			if(R2!=null)
			{
				MOB M=null;
				Item I=null;
				for(int m=0;m<todo.size();m++)
				{
					if(todo.elementAt(m) instanceof MOB)
					{
						M=(MOB)todo.elementAt(m);
						CMMsg themsg=CMClass.getMsg(M,M,new AWaterCurrent(),CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept "+Directions.getDirectionName(dir).toLowerCase()+" by the current.");
						if(R.okMessage(M,themsg))
						{
							R.send(M,themsg);
							R2.bringMobHere(M,false);
							R2.showOthers(M,null,new AWaterCurrent(),CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+" by the current.");
							CMLib.commands().postLook(M,true);
						}
					}
					else
					if(todo.elementAt(m) instanceof Item)
					{
						I=(Item)todo.elementAt(m);
						R.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" is swept "+Directions.getDirectionName(dir).toLowerCase()+" by the current.");
						R2.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
						R2.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" is swept in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+" by the current.");
					}
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Vector sweeps=new Vector();
			if(ticking instanceof Room)
			{
				Room R=(Room)ticking;
				applyCurrents(R,sweeps);
				Room below=R.rawDoors()[Directions.DOWN];
				if((below!=null)
				&&(below.roomID().length()==0)
				&&(below instanceof GridLocale)
				&&((below.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(below.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
				{
					for(Iterator<Room> i=((GridLocale)below).getAllRooms().iterator(); i.hasNext();)
					{
						Room R2=(Room)i.next();
						applyCurrents(R2,sweeps);
					}
				}
			}
			else
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						applyCurrents(R,sweeps);
				}
			}
		}
		return true;
	}
	protected static final String[] empty={};
	protected static final String[] CODES={"CLASS","TEXT"};
	protected static class AWaterCurrent implements Ability, Cloneable
	{
		public AWaterCurrent()
		{
			super();
			//CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for mem & perf
		}
		public String ID() { return "AWaterCurrent"; }
		public String name(){ return "a water current";}
		public String Name(){return name();}
		public String description(){return "";}
		public String displayText(){return "";}
		protected boolean savable=true;
		protected String miscText="";
		protected Physical affected=null;
		protected int canAffectCode(){return 0;}
		protected int canTargetCode(){return 0;}
		public int getTicksBetweenCasts() { return 0;}
		public boolean canTarget(int can_code){return false;}
		public boolean canAffect(int can_code){return false;}
		public double castingTime(final MOB mob, final List<String> cmds){return 0.0;}
		public double combatCastingTime(final MOB mob, final List<String> cmds){return 0.0;}
		public double checkedCastingCost(final MOB mob, final List<String> cmds){return 0.0;}
		public void initializeClass(){}
		public int abilityCode(){return 0;}
		public void setAbilityCode(int newCode){}
		public int adjustedLevel(MOB mob, int asLevel){return -1;}
		public boolean bubbleAffect(){return false;}
		public ExpertiseLibrary.SkillCost getTrainingCost(MOB mob)
		{ return new ExpertiseLibrary.SkillCost(ExpertiseLibrary.CostType.TRAIN, Double.valueOf(1.0));}
		public long flags(){return Ability.FLAG_TRANSPORTING;}
		public long getTickStatus(){return Tickable.STATUS_NOT;}
		public int usageType(){return 0;}
		//protected void finalize(){ CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for mem & perf
		public long expirationDate(){return 0;}
		public void setExpirationDate(long time){}

		public void setName(String newName){}
		public void setDescription(String newDescription){}
		public void setDisplayText(String newDisplayText){}
		public String image(){return "";}
		public String rawImage(){return "";}
		public void setImage(String newImage){}
		public MOB invoker(){return null;}
		public void setInvoker(MOB mob){}
		public String[] triggerStrings(){return empty;}
		public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining){return true;}
		public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel){return false;}
		public boolean invoke(MOB mob, Physical target, boolean auto, int asLevel){return false;}
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
		public int castingQuality(MOB mob, Physical target){return Ability.QUALITY_INDIFFERENT;}

		public int classificationCode(){ return Ability.ACODE_PROPERTY;}
		public boolean isSavable(){ return savable;}
		public void setSavable(boolean truefalse)	{ savable=truefalse; }
		protected boolean amDestroyed=false;
		public void destroy(){amDestroyed=true;}
		public boolean amDestroyed(){return amDestroyed;}

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
			return new AWaterCurrent();
		}

		public int getSaveStatIndex(){return getStatCodes().length;}
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
			if(!(E instanceof AWaterCurrent)) return false;
			String[] codes=getStatCodes();
			for(int i=0;i<codes.length;i++)
				if(!E.getStat(codes[i]).equals(getStat(codes[i])))
					return false;
			return true;
		}
		protected void cloneFix(Ability E){}

		public CMObject copyOf()
		{
			try
			{
				AWaterCurrent E=(AWaterCurrent)this.clone();
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

		public void setMiscText(String newMiscText){ miscText=newMiscText;}
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
		protected static final int[] cost=new int[3];
		public int[] usageCost(MOB mob, boolean ignoreClassOverride){return cost;}


		public boolean isGeneric(){return false;}
	}
}
