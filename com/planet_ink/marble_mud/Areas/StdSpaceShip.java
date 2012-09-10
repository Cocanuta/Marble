package com.planet_ink.marble_mud.Areas;
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
public class StdSpaceShip implements Area, SpaceObject, SpaceShip
{
	protected static Climate climateObj=null;
	
	public long[]   		coordinates 	=new long[3];
	public double[] 		direction   	=new double[2];
	public long 			velocity		=0;
	public long 			accelleration   =0;
	protected String[]  	xtraValues  	=null;
	protected String		imageName   	="";
	protected RoomnumberSet properRoomIDSet =null;
	protected TimeClock 	localClock  	=(TimeClock)CMClass.getCommon("DefaultTimeClock");
	protected String		currency		="";
	private long			expirationDate  =0;
	protected SpaceObject   spaceTarget 	=null;
	protected SpaceObject   spaceSource 	=null;
	protected SpaceObject   orbiting		=null;
	protected boolean   	amDestroyed 	=false;
	protected String		name			="a space ship";
	protected Room  		savedDock   	=null;
	protected String		description 	="";
	protected String		miscText		="";
	protected SVector<Room> myRooms 		=new SVector();
	protected State   		flag			=State.ACTIVE;
	protected long  		tickStatus  	=Tickable.STATUS_NOT;
	protected String		author  		=""; // will be used for owner, I guess.
	protected PhyStats  	phyStats		=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats  	basePhyStats	=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected boolean   	initializedArea =false;
	protected final Area 	me			 	=this;
	
	protected SVector<Ability>  		affects=new SVector<Ability>(1);
	protected SVector<Behavior> 		behaviors=new SVector<Behavior>(1);
	protected SVector<ScriptingEngine>  scripts=new SVector<ScriptingEngine>(1);
	protected SLinkedList<Area> 		parents=new SLinkedList<Area>();
	protected List<String>  			parentsToLoad=new SLinkedList<String>();
	protected STreeMap<String,String>   blurbFlags=new STreeMap<String,String>();

	public void initializeClass(){}
	public long[] coordinates(){return coordinates;}
	public double[] direction(){return direction;}
	protected Room getDock(){ return CMLib.map().getRoom(savedDock);}
	public void setClimateObj(Climate obj){climateObj=obj;}
	public Climate getClimateObj()
	{
		if(climateObj==null)
		{
			climateObj=(Climate)CMClass.getCommon("DefaultClimate");
			climateObj.setCurrentWeatherType(Climate.WEATHER_CLEAR);
			climateObj.setNextWeatherType(Climate.WEATHER_CLEAR);
		}
		return climateObj;
	}
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}
	public TimeClock getTimeObj(){return localClock;}
	public void setTimeObj(TimeClock obj){localClock=obj;}
	public void setCurrency(String newCurrency){currency=newCurrency;}
	public String getCurrency(){return currency;}
	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	public long flags(){return 0;}
	
	public SpaceObject knownTarget(){return spaceTarget;}
	public void setKnownTarget(SpaceObject O){spaceTarget=O;}
	public SpaceObject knownSource(){return spaceSource;}
	public void setKnownSource(SpaceObject O){spaceSource=O;}
	public SpaceObject orbiting(){return orbiting;}
	public void setOrbiting(SpaceObject O){orbiting=O;}
	public void setCoords(long[] coords){coordinates=coords;}
	public void setDirection(double[] dir){direction=dir;}
	public long velocity(){return velocity;}
	public void setVelocity(long v){velocity=v;}
	public long accelleration(){return accelleration;}
	public void setAccelleration(long v){accelleration=v;}
	
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(this,null,this);
		phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		coordinates=null;
		direction=null;
		spaceSource=null;
		spaceTarget=null;
		orbiting=null;
		basePhyStats=phyStats;
		miscText=null;
		imageName=null;
		affects=null;
		behaviors=null;
		scripts=null;
		author=null;
		currency=null;
		parents=null;
		initializedArea=true;
		parentsToLoad=null;
		climateObj=null;
		amDestroyed=true;
	}
	public boolean amDestroyed(){return amDestroyed;}
	public boolean isSavable()
	{
		return ((!amDestroyed) 
				&& (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				&& (CMLib.flags().isSavable(this)));
	}
	public void setSavable(boolean truefalse){}
	public String ID(){    return "StdSpaceShip";}
	public int climateType(){return Area.CLIMASK_NORMAL;}
	public void setClimateType(int newClimateType){}

	public StdSpaceShip()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.AREA);}//removed for mem & perf
	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return name;
	}
	public void setName(String newName){
		name=newName;
		localClock.setLoadName(newName);
	}
	public String Name(){return name;}
	public PhyStats phyStats()
	{
		return phyStats;
	}
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			if(A!=null) A.affectPhyStats(me,phyStats);
        }});
	}
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}
	public void setNextWeatherType(int weatherCode){}
	public void setCurrentWeatherType(int weatherCode){}
	public int getTechLevel(){return Area.THEME_TECHNOLOGY;}
	public void setTechLevel(int level){}

	public String image(){return imageName;}
	public String rawImage(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}
	
	public String getArchivePath(){return "";}
	public void setArchivePath(String pathFile){}
	
	public void setAreaState(State newState)
	{
		if((newState==State.ACTIVE)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		flag=newState;
	}
	public State getAreaState(){return flag;}
	public boolean amISubOp(String username){return false;}
	public String getSubOpList(){return "";}
	public void setSubOpList(String list){}
	public void addSubOp(String username){}
	public void delSubOp(String username){}
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
		return new StdSpaceShip();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(StdSpaceShip ship)
	{
		basePhyStats=(PhyStats)ship.basePhyStats().copyOf();
		phyStats=(PhyStats)ship.phyStats().copyOf();

		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		if(ship.parents==null)
			parents=null;
		else
			parents=new SLinkedList(ship.parents);
		initializedArea=ship.initializedArea;
		for(Enumeration<Behavior> e=ship.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
			if(B!=null)
				behaviors.addElement(B);
		}
		for(final Enumeration<Ability> a=ship.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
		ScriptingEngine SE=null;
		for(Enumeration<ScriptingEngine> e=ship.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
		setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
	}
	public CMObject copyOf()
	{
		try
		{
			StdSpaceShip E=(StdSpaceShip)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.AREA);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		MsgListener N=null;
		for(int b=0;b<numBehaviors();b++)
		{
			N=fetchBehavior(b);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int s=0;s<numScripts();s++)
		{
			N=fetchScript(s);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			N=a.nextElement();
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		
		if((flag==State.FROZEN)
		||(flag==State.STOPPED)
		||(!CMLib.flags().allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC)))
		{
			Room room=null;
			if((msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).location()!=null))
				room=((MOB)msg.target()).location();
			else
			if((msg.source()!=null)
			&&(msg.source().location()!=null))
				room=msg.source().location();
			if(room!=null)
			{
				if(room.getArea()==this)
					room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
				else
					room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work there.");
			}

			return false;
		}
		return true;
	}

	protected Enumeration<String> allBlurbFlags()
	{
		MultiEnumeration<String> multiEnum = new MultiEnumeration<String>(areaBlurbFlags());
		for(Iterator<Area> i=getParentsIterator();i.hasNext();)
			multiEnum.addEnumeration(i.next().areaBlurbFlags());
		return multiEnum;
	}

	public String getBlurbFlag(String flag)
	{
		if((flag==null)||(flag.trim().length()==0))
			return null;
		return blurbFlags.get(flag.toUpperCase().trim());
	}
	public int numBlurbFlags(){return blurbFlags.size();}
	public int numAllBlurbFlags()
	{
		int num=numBlurbFlags();
		for(Iterator<Area> i=getParentsIterator();i.hasNext();)
			num += i.next().numAllBlurbFlags();
		return num;
	}
	public Enumeration<String> areaBlurbFlags()
	{
		return new IteratorEnumeration<String>(blurbFlags.keySet().iterator());
	}
	
	public void addBlurbFlag(String flagPlusDesc)
	{
		if(flagPlusDesc==null) return;
		flagPlusDesc=flagPlusDesc.trim();
		if(flagPlusDesc.length()==0) return;
		int x=flagPlusDesc.indexOf(' ');
		String flag=null;
		if(x>=0)
		{
			flag=flagPlusDesc.substring(0,x).toUpperCase();
			flagPlusDesc=flagPlusDesc.substring(x).trim();
		}
		else
		{
			flag=flagPlusDesc.toUpperCase().trim();
			flagPlusDesc="";
		}
		if(getBlurbFlag(flag)==null)
			blurbFlags.put(flag,flagPlusDesc);
	}
	public void delBlurbFlag(String flagOnly)
	{
		if(flagOnly==null) return;
		flagOnly=flagOnly.toUpperCase().trim();
		if(flagOnly.length()==0) return;
		blurbFlags.remove(flagOnly);
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		eachBehavior(new EachApplicable<Behavior>(){ public final void apply(final Behavior B){
			B.executeMsg(me, msg);
		} });
		eachScript(new EachApplicable<ScriptingEngine>(){ public final void apply(final ScriptingEngine S){
			S.executeMsg(me, msg);
		} });
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			A.executeMsg(me,msg);
        }});
	}

	public Enumeration<Room> getCompleteMap(){return getProperMap();}
	public List<Room> getMetroCollection(){return new ReadOnlyList(myRooms);}
	
	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	public long getTickStatus(){ return tickStatus;}
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(flag==State.STOPPED) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			getTimeObj().tick(this,tickID);
			tickStatus=Tickable.STATUS_BEHAVIOR;
			eachBehavior(new EachApplicable<Behavior>(){ public final void apply(final Behavior B){
				B.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>(){ public final void apply(final ScriptingEngine S){
				S.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_AFFECT;
			eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
				if(!A.tick(ticking,tickID))
					A.unInvoke();
	        }});
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public String getWeatherDescription(){return "There is no weather here.";}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(phyStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|phyStats().sensesMask());
		int disposition=phyStats().disposition()
			&((~(PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if(affects==null) return;
		try{
    		for(int a=0;a<affects.size();a++)
    		{
    			final Ability A=affects.get(a);
    			if(A!=null) applier.apply(A);
    		}
		} catch(ArrayIndexOutOfBoundsException e){}
	}
	public void delAllEffects(boolean unInvoke)
	{
		for(int a=numEffects()-1;a>=0;a--)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke) A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}
	public int numEffects()
	{
		return affects.size();
	}
	
	public Enumeration<Ability> effects(){return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();}
	
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}

	public void fillInAreaRooms() { }

	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		return false;
	}
	public void fillInAreaRoom(Room R){}
	public void dockHere(Room R)
	{
		if(R==null) return;
		if(getDock()!=null) unDock(false);
		Room airLockRoom=null;
		int airLockDir=-1;
		Room backupRoom=null;
		int backupDir=-1;
		for(Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			Room R2=(Room)e.nextElement();
			if(R2!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((R2.getRawExit(d)!=null)
				&&((R2.rawDoors()[d]==null)||(R2.rawDoors()[d].getArea()!=this))
				&&(R2.getRawExit(d).ID().endsWith("AirLock")))
				{ 
					airLockRoom=R2; 
					R2.rawDoors()[d]=null; 
					airLockDir=d; 
					break;
				}
				
				if((d<4)&&(R2.rawDoors()==null))
				{
					backupRoom=R2;
					backupDir=d;
				}
			}
			if(airLockDir>=0) break;
		}
		if(airLockRoom==null)
		{
			airLockRoom=backupRoom;
			airLockDir=backupDir;
		}
		
		if(airLockRoom!=null)
		{
			if(airLockRoom.rawDoors()[airLockDir]==null)
				airLockRoom.rawDoors()[airLockDir]=R;
			if(airLockRoom.getRawExit(airLockDir)==null)
				airLockRoom.setRawExit(airLockDir,CMClass.getExit("GenAirLock"));
			Item portal=CMClass.getMiscTech("GenSSPortal");
			portal.setName(Name());
			portal.setDisplayText(Name());
			portal.setDescription(description());
			portal.setReadableText(CMLib.map().getExtendedRoomID(R));
			CMLib.flags().setGettable(portal,false);
			R.addItem(portal);
			portal.setExpirationDate(0);
			savedDock=R;
			CMLib.map().delObjectInSpace(this);
			R.recoverRoomStats();
		}
	}
	public void unDock(boolean toSpace)
	{
		if(getDock()==null) return;
		Room dock=getDock();
		for(int i=0;i<dock.numItems();i++)
		{
			Item I=dock.getItem(i);
			if(I.Name().equals(Name()))
				I.destroy();
		}
		for(Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R!=null)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(R.rawDoors()[d]==dock)
					R.rawDoors()[d]=null;
			}
		}
		dock=null;
		if(toSpace)
		{
			CMLib.map().addObjectToSpace(this);
		}
	}

	public RoomnumberSet getCachedRoomnumbers()
	{
		RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(myRooms)
		{
			Room R=null;
			for(int p=myRooms.size()-1;p>=0;p--)
			{
				R=(Room)myRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}
	public RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}
	
	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		Hashtable allNums=new Hashtable();
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.getArea().Name().equals(Name()))
				&&(R.roomID().startsWith(Name()+"#")))
				{
					int newnum=CMath.s_int(R.roomID().substring(Name().length()+1));
					if(newnum>=highest)    highest=newnum;
					if(newnum<=lowest) lowest=newnum;
					allNums.put(Integer.valueOf(newnum),R);
				}
			}
		}catch(NoSuchElementException e){}
		if((highest<0)&&(CMLib.map().getRoom(Name()+"#0"))==null)
			return Name()+"#0";
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!allNums.containsKey(Integer.valueOf(i)))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+Math.random();
	}
	
	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
  public void delAllBehaviors()
  {
	  boolean didSomething=(behaviors!=null)&&(behaviors.size()>0);
	  if(didSomething) behaviors.clear();
	  behaviors=null;
	  if(didSomething && ((scripts==null)||(scripts.size()==0)))
		CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
  }
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Enumeration<Behavior> behaviors() { return behaviors.elements();}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public int[] getAreaIStats(){return new int[Area.Stats.values().length];}
	public StringBuffer getAreaStats(){    return new StringBuffer("This is a space ship");}

	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if(behaviors!=null)
		try{
    		for(int a=0;a<behaviors.size();a++)
    		{
    			final Behavior B=behaviors.get(a);
    			if(B!=null) applier.apply(B);
    		}
    	} catch(ArrayIndexOutOfBoundsException e){}
	}

	/** Manipulation of the scripts list */
	public void addScript(ScriptingEngine S)
	{
		if(S==null) return;
		if(!scripts.contains(S)) 
		{
			for(ScriptingEngine S2 : scripts)
				if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
					return;
			scripts.addElement(S);
		}
	}
	public void delScript(ScriptingEngine S)
	{
		scripts.removeElement(S);
	}
	public void delAllScripts()
	{
		boolean didSomething=(scripts!=null)&&(scripts.size()>0);
		if(didSomething) scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.size()==0)))
		  CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
	}
	public int numScripts(){return (scripts==null)?0:scripts.size();}
	public Enumeration<ScriptingEngine> scripts() { return (scripts==null)?EmptyEnumeration.INSTANCE:scripts.elements();}
	public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts=this.scripts;
		if(scripts!=null)
		try{
    		for(int a=0;a<scripts.size();a++)
    		{
    			final ScriptingEngine S=scripts.get(a);
    			if(S!=null) applier.apply(S);
    		}
    	} catch(ArrayIndexOutOfBoundsException e){}
	}
	
	public void addProperRoom(Room R)
	{
		if(R==null) return;
		if(R.getArea()!=this)
		{
			R.setArea(this);
			return;
		}
		synchronized(myRooms)
		{
			if(!myRooms.contains(R))
			{
				addProperRoomnumber(R.roomID());
				Room R2=null;
				for(int i=0;i<myRooms.size();i++)
				{
					R2=(Room)myRooms.elementAt(i);
					if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
					{
						if(R2.ID().compareToIgnoreCase(R.roomID())==0)
							myRooms.setElementAt(R,i);
						else
							myRooms.insertElementAt(R,i);
						return;
					}
				}
				myRooms.addElement(R);
			}
		}
	}
	
	public void delProperRoom(Room R)
	{
		if(R==null) return;
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		synchronized(myRooms)
		{
			if(myRooms.removeElement(R))
				delProperRoomnumber(R.roomID());
		}
	}
	
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().add(roomID);
	}
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().remove(roomID);
	}
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		return myRooms.contains(R);
	}
	
	public Room getRoom(String roomID)
	{
		if(myRooms.size()==0) return null;
		synchronized(myRooms)
		{
			int start=0;
			int end=myRooms.size()-1;
			while(start<=end)
			{
				int mid=(end+start)/2;
				int comp=((Room)myRooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
				if(comp==0)
					return (Room)myRooms.elementAt(mid);
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
	
			}
		}
		return null;
	}

	public int metroSize(){return properSize();}
	public int properSize()
	{
		synchronized(myRooms)
		{
			return myRooms.size();
		}
	}
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xGridSize()*((GridLocale)R).yGridSize();
				else
					num++;
		}
		return num;
	}
	public Room getRandomMetroRoom(){return getRandomProperRoom();}
	public Room getRandomProperRoom()
	{
		synchronized(myRooms)
		{
			if(properSize()==0) return null;
			Room R=(Room)myRooms.elementAt(CMLib.dice().roll(1,properSize(),-1));
			if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
			return R;
		}
	}
	public boolean isProperlyEmpty(){ return getProperRoomnumbers().isEmpty(); }
	public void setProperRoomnumbers(RoomnumberSet set){ properRoomIDSet=set;}
	public RoomnumberSet getMetroRoomnumbers(){return getProperRoomnumbers();}
	public Enumeration<Room> getMetroMap(){return getProperMap();}
	public void addMetroRoomnumber(String roomID){}
	public void delMetroRoomnumber(String roomID){}
	public void addMetroRoom(Room R){}
	public void delMetroRoom(Room R){}
	public Enumeration<Room> getProperMap()
	{
		synchronized(myRooms)
		{
			return myRooms.elements();
		}
	}
	public Enumeration<Room> getFilledProperMap() { return getProperMap();}
	public Enumeration<String> subOps(){ return EmptyEnumeration.INSTANCE;}

	public void addChildToLoad(String str){}
	public void addParentToLoad(String str) { parentsToLoad.add(str);}

	// Children
	public Enumeration<Area> getChildren() {return EmptyEnumeration.INSTANCE; }
	public String getChildrenList() { return "";}
	public Area getChild(String named) { return null;}
	public boolean isChild(Area named) { return false;}
	public boolean isChild(String named) { return false;}
	public void addChild(Area area) {}
	public void removeChild(Area area) {}
	public boolean canChild(Area area) { return false;}
	
	public SLinkedList<Area> loadAreas(Collection<String> loadableSet) 
	{
		final SLinkedList<Area> finalSet = new SLinkedList<Area>();
		for (final String areaName : loadableSet) 
		{
			Area A = CMLib.map().getArea(areaName);
			if (A == null)
				continue;
			finalSet.add(A);
		}
		return finalSet;
	}
	
	public synchronized void initializeAreaLink() 
	{
		if(initializedArea)
			return;
		SLinkedList<Area> futureParents=loadAreas(parentsToLoad);
		parents=new SLinkedList<Area>();
		for(Area parentA : futureParents)
			if(canParent(parentA))
				parents.add(parentA);
			else
				Log.errOut("StdSpaceShip","Can not make '"+parentA.name()+"' parent of '"+name+"'");
		initializedArea=true;
	}
		
	
	protected final Iterator<Area> getParentsIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return parents.iterator();
	}
	
	protected final Iterator<Area> getParentsReverseIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return parents.descendingIterator();
	}
	
	public Enumeration<Area> getParents() { return new IteratorEnumeration<Area>(parents.iterator()); }
	
	public List<Area> getParentsRecurse()
	{
		final LinkedList<Area> V=new LinkedList<Area>();
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=(Area)a.next();
			V.add(A);
			V.addAll(A.getParentsRecurse());
		}
		return V;
	}

	public String getParentsList() 
	{
		StringBuffer str=new StringBuffer("");
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=(Area)a.next();
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

	public Area getParent(String named) 
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=(Area)a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
			   return A;
		}
		return null;
	}
	
	public boolean isParent(Area area) 
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=(Area)a.next();
			if(A == area)
			   return true;
		}
		return false;
	}
	
	public boolean isParent(String named) 
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=(Area)a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return true;
		}
		return false;
	}
	
	public void addParent(Area area) 
	{
		if(!canParent(area))
			return;
		for(final Iterator<Area> i=getParentsIterator(); i.hasNext();) 
		{
			final Area A=i.next();
			if(A.Name().equalsIgnoreCase(area.Name()))
			{
				parents.remove(A);
				break;
			}
		}
		parents.add(area);
	}
	
	public void removeParent(Area area) 
	{ 
		if(isParent(area))
			parents.remove(area);
	}
	
	public boolean canParent(Area area) 
	{
		return true;
	}

	public String prejudiceFactors(){return "";}
	public void setPrejudiceFactors(String factors){}
	public final static String[] empty=new String[0];
	public String[] itemPricingAdjustments(){return empty;}
	public void setItemPricingAdjustments(String[] factors){}
	public String ignoreMask(){return "";}
	public void setIgnoreMask(String factors){}
	public String budget(){return "";}
	public void setBudget(String factors){}
	public String devalueRate(){return "";}
	public void setDevalueRate(String factors){}
	public int invResetRate(){return 0;}
	public void setInvResetRate(int ticks){}
	public int finalInvResetRate(){ return 0;}
	public String finalPrejudiceFactors(){ return "";}
	public String finalIgnoreMask(){ return "";}
	public String[] finalItemPricingAdjustments(){ return empty;}
	public String finalBudget(){ return "";}
	public String finalDevalueRate(){ return "";}
   
	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","TECHLEVEL","BLURBS"};
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
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTechLevel();
		case 5: return ""+CMLib.xml().getXMLList(blurbFlags.toStringVector(" "));
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(CMath.s_parseBitIntExpression(Area.CLIMATE_DESCS,val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(CMath.s_parseBitIntExpression(Area.THEME_DESCS,val)); break;
		case 5:
		{
			if(val.startsWith("+"))
				addBlurbFlag(val.substring(1));
			else
			if(val.startsWith("-"))
				delBlurbFlag(val.substring(1));
			else
			{
				blurbFlags=new STreeMap<String,String>();
				List<String> V=CMLib.xml().parseXMLList(val);
				for(String s : V)
				{
					int x=s.indexOf(' ');
					if(x<0)
						blurbFlags.put(s,"");
					else
						blurbFlags.put(s.substring(0,x),s.substring(x+1));
				}
			}
			break;
		}
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdSpaceShip)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
