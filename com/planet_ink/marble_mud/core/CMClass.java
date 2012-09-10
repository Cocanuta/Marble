package com.planet_ink.marble_mud.core;
import com.planet_ink.marble_mud.WebMacros.interfaces.*;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.*;


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
/**
 * The core class loader, but more importantly, the core object template manager
 * for the whole mud.  Classes are grouped by their core interfaces, allowing them
 * to have short "ID" names as referents.  Classes are loaded and initialized from the
 * class loader and then kept as template objects, with newInstances created on demand (or
 * simply returned as the template, in cases where the objects are shared).
 * @author Bo Zimmerman
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CMClass extends ClassLoader
{
	protected static boolean debugging=false; 
	protected static volatile long lastUpdateTime=System.currentTimeMillis();
	protected static final Map<String,Class<?>> classes=new Hashtable<String,Class<?>>();

	private static CMClass[] clss=new CMClass[256];
	/**
	 * Creates a new instance of the class loader, updating the thread-group ref if necessary.
	 */
	public CMClass()
	{
		super();
		char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(clss==null) clss=new CMClass[256];
		if(clss[c]==null) clss[c]=this;
	}
	/**
	 * Returns the CMClass instance tied to this particular thread group, or null if not yet created.
	 * @return the CMClass instance tied to this particular thread group, or null if not yet created.
	 */
	private static CMClass c(){ return clss[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	/**
	 * Returns the CMClass instance tied to the given thread group, or null if not yet created.
	 * @param c the code for the thread group to return (0-255)
	 * @return the CMClass instance tied to the given thread group, or null if not yet created.
	 */
	public static CMClass c(byte c){return clss[c];}
	/**
	 * Returns the CMClass instance tied to this particular thread group, or null if not yet created.
	 * @return the CMClass instance tied to this particular thread group, or null if not yet created.
	 */
	public static CMClass instance(){return c();}

	private static boolean[] classLoaderSync={false};

	public static enum CMObjectType
	{
	/** stat constant for race type objects */
	RACE("com.planet_ink.marble_mud.Races.interfaces.Race"),
	/** stat constant for char class type objects */
	CHARCLASS("com.planet_ink.marble_mud.CharClasses.interfaces.CharClass"),
	/** stat constant for mob type objects */
	MOB("com.planet_ink.marble_mud.MOBS.interfaces.MOB"),
	/** stat constant for ability type objects */
	ABILITY("com.planet_ink.marble_mud.Abilities.interfaces.Ability"),
	/** stat constant for locale/room type objects */
	LOCALE("com.planet_ink.marble_mud.Locales.interfaces.Room"),
	/** stat constant for exit type objects */
	EXIT("com.planet_ink.marble_mud.Exits.interfaces.Exit"),
	/** stat constant for item type objects */
	ITEM("com.planet_ink.marble_mud.Items.interfaces.Item"),
	/** stat constant for behavior type objects */
	BEHAVIOR("com.planet_ink.marble_mud.Behaviors.interfaces.Behavior"),
	/** stat constant for clan type objects */
	CLAN("com.planet_ink.marble_mud.core.interfaces.Clan"),
	/** stat constant for weapon type objects */
	WEAPON("com.planet_ink.marble_mud.Items.interfaces.Weapon"),
	/** stat constant for armor type objects */
	ARMOR("com.planet_ink.marble_mud.Items.interfaces.Armor"),
	/** stat constant for misc magic type objects */
	MISCMAGIC("com.planet_ink.marble_mud.Items.interfaces.MiscMagic"),
	/** stat constant for area type objects */
	AREA("com.planet_ink.marble_mud.Areas.interfaces.Area"),
	/** stat constant for command type objects */
	COMMAND("com.planet_ink.marble_mud.Commands.interfaces.Command"),
	/** stat constant for clan items type objects */
	CLANITEM("com.planet_ink.marble_mud.Items.interfaces.ClanItem"),
	/** stat constant for misc tech type objects */
	MISCTECH("com.planet_ink.marble_mud.Items.interfaces.Electronics"),
	/** stat constant for webmacros type objects */
	WEBMACRO("com.planet_ink.marble_mud.WebMacros.interfaces.WebMacro"),
	/** stat constant for common type objects */
	COMMON("com.planet_ink.marble_mud.Common.interfaces.CMCommon"),
	/** stat constant for library type objects */
	LIBRARY("com.planet_ink.marble_mud.Libraries.interfaces.CMLibrary");
	
		public final String ancestorName; // in meters
		CMObjectType(String ancestorName) {
			this.ancestorName = ancestorName;
		}
	}

	/** collection of all object types that are classified as "items" of one sort or another */
	public static final CMObjectType[] OBJECTS_ITEMTYPES = new CMObjectType[]{
		CMObjectType.MISCMAGIC,
		CMObjectType.ITEM,
		CMObjectType.ARMOR,
		CMObjectType.CLANITEM,
		CMObjectType.MISCMAGIC,
		CMObjectType.MISCTECH,
		CMObjectType.WEAPON
	};

	/** static int for the web macro object with the longest name, used for web optimization */
	public static int longestWebMacro=-1;

	protected Hashtable<String,CMCommon> common=new Hashtable<String,CMCommon>();
	protected XVector<Race> 			 races=new XVector<Race>();
	protected XVector<CharClass>		 charClasses=new XVector<CharClass>();
	protected XVector<MOB>  			 MOBs=new XVector<MOB>();
	protected XVector<Ability>  		 abilities=new XVector<Ability>();
	protected XVector<Room> 			 locales=new XVector<Room>();
	protected XVector<Exit> 			 exits=new XVector<Exit>();
	protected XVector<Item> 			 items=new XVector<Item>();
	protected XVector<Behavior> 		 behaviors=new XVector<Behavior>();
	protected XVector<Weapon>   		 weapons=new XVector<Weapon>();
	protected XVector<Armor>			 armor=new XVector<Armor>();
	protected XVector<MiscMagic>		 miscMagic=new XVector<MiscMagic>();
	protected XVector<Electronics>  	 miscTech=new XVector<Electronics>();
	protected XVector<ClanItem> 		 clanItems=new XVector<ClanItem>();
	protected XVector<Area> 			 areaTypes=new XVector<Area>();
	protected XVector<Command>  		 commands=new XVector<Command>();
	protected XVector<CMLibrary>		 libraries=new XVector<CMLibrary>();
	protected Hashtable<String,WebMacro> webMacros=new Hashtable<String,WebMacro>();
	protected Hashtable<String,Command>  commandWords=new Hashtable<String,Command>();
   
	protected static final LinkedList<CMMsg> MSGS_CACHE=new LinkedList<CMMsg>();
	protected static final LinkedList<MOB>   MOB_CACHE=new LinkedList<MOB>();
	protected static final int  			 MAX_MSGS=10000+((Runtime.getRuntime().maxMemory()==Integer.MAX_VALUE)?10000:(int)(Runtime.getRuntime().maxMemory()/(long)10000));
	protected static final int  			 MAX_MOBS=50+(MAX_MSGS/200);

	/*
	 * removed to save memory and processing time -- but left for future use
	protected static final long[] OBJECT_CREATIONS=new long[OBJECT_TOTAL];
	protected static final long[] OBJECT_DESTRUCTIONS=new long[OBJECT_TOTAL];
	protected static final Map<CMObject,Object>[] OBJECT_CACHE=new WeakHashMap[OBJECT_TOTAL];
	protected static final boolean KEEP_OBJECT_CACHE=false;

	static
	{ 
		if(KEEP_OBJECT_CACHE) 
			for(int i=0;i<OBJECT_TOTAL;i++)
				OBJECT_CACHE[i]=new WeakHashMap<CMObject,Object>();
	}
	public final static void bumpCounter(final CMObject O, final int which)
	{
		if(KEEP_OBJECT_CACHE)
		{
			if(OBJECT_CACHE[which].containsKey(O))
			{
				Log.errOut("Duplicate!",new Exception("Duplicate Found!"));
				return;
			}
			OBJECT_CACHE[which].put(O,OBJECT_CACHE);
		}
		OBJECT_CREATIONS[which]++;
	}

	public final static void unbumpCounter(final CMObject O, final int which)
	{
		if(KEEP_OBJECT_CACHE)
		{
			if(OBJECT_CACHE[which].containsKey(O)) // yes, if its in there, its bad
			{
				OBJECT_CACHE[which].remove(O);
				Log.errOut("bumped!",O.getClass().getName());
				return;
			}
		}
		OBJECT_DESTRUCTIONS[which]++;
	}

	public static final String getCounterReport()
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<OBJECT_TOTAL;i++)
			if(OBJECT_CREATIONS[i]>0)
				str.append(CMStrings.padRight(OBJECT_DESCS[i],12)+": Created: "+OBJECT_CREATIONS[i]+", Destroyed: "+OBJECT_DESTRUCTIONS[i]+", Remaining: "+(OBJECT_CREATIONS[i]-OBJECT_DESTRUCTIONS[i])+"\n\r");
		return str.toString();
	}


	public static final long numRemainingObjectCounts(final int type)
	{
		return OBJECT_CREATIONS[type] - OBJECT_DESTRUCTIONS[type];
	}
	*/

	/**
	 * Returns whether the given class exists in the vm,
	 * not necessarily any given classloader.
	 * Requires a fully qualified java class name.
	 * @param className a fully qualified java class name.
	 * @return whether the given class exists in the vm
	 */
	public final static boolean exists(String className)
	{
		try 
		{
			Class.forName (className);
			return true;
		}
		catch (ClassNotFoundException exception) 
		{
			return false;
		}
	}

	/**
	 * Checks the given object against the given object type
	 * @see com.planet_ink.marble_mud.core.CMClass.CMObjectType
	 * @param O the object to inspect
	 * @param type the type to compare against
	 * @return true if theres a match, and false otherwise
	 */
	public final static boolean isType(final Object O, final CMObjectType type)
	{
		switch(type)
		{
			case RACE: return O instanceof Race;
			case CHARCLASS: return O instanceof CharClass;
			case MOB: return O instanceof MOB;
			case ABILITY: return O instanceof Ability;
			case LOCALE: return O instanceof Room;
			case EXIT: return O instanceof Exit;
			case ITEM: return O instanceof Item;
			case BEHAVIOR: return O instanceof Behavior;
			case CLAN: return O instanceof Clan;
			case WEAPON: return O instanceof Weapon;
			case ARMOR: return O instanceof Armor;
			case MISCMAGIC: return O instanceof MiscMagic;
			case AREA: return O instanceof Area;
			case COMMAND: return O instanceof Command;
			case CLANITEM: return O instanceof ClanItem;
			case MISCTECH: return O instanceof Electronics;
			case WEBMACRO: return O instanceof WebMacro;
			case COMMON: return O instanceof CMCommon;
			case LIBRARY: return O instanceof CMLibrary;
		}
		return false;
	}

	/**
	 * Returns a newInstance of an object of the given type and ID. NULL if not found.
	 * @see com.planet_ink.marble_mud.core.CMClass.CMObjectType
	 * @param ID the ID of the object to look for
	 * @param type the type of object to check
	 * @return a newInstance of an object of the given type and ID.
	 */
	public final static CMObject getByType(final String ID, final CMObjectType type)
	{
		switch(type)
		{
			case RACE: return CMClass.getRace(ID);
			case CHARCLASS: return CMClass.getCharClass(ID);
			case MOB: return CMClass.getMOB(ID);
			case ABILITY: return CMClass.getAbility(ID);
			case LOCALE: return CMClass.getLocale(ID);
			case EXIT: return CMClass.getExit(ID);
			case ITEM: return CMClass.getBasicItem(ID);
			case BEHAVIOR: return CMClass.getBehavior(ID);
			case CLAN: return CMClass.getCommon(ID);
			case WEAPON: return CMClass.getWeapon(ID);
			case ARMOR: return CMClass.getAreaType(ID);
			case MISCMAGIC: return CMClass.getMiscMagic(ID);
			case AREA: return CMClass.getAreaType(ID);
			case COMMAND: return CMClass.getCommand(ID);
			case CLANITEM: return CMClass.getClanItem(ID);
			case MISCTECH: return CMClass.getMiscMagic(ID);
			case WEBMACRO: return CMClass.getWebMacro(ID);
			case COMMON: return CMClass.getCommon(ID);
			case LIBRARY: return CMClass.getLibrary(ID);
		}
		return null;
	}

	/**
	 * Returns the object type of the given object
	 * @see com.planet_ink.marble_mud.core.CMClass.CMObjectType
	 * @param O the object to inspect
	 * @return the cmobjectype type
	 */
	public final static CMObjectType getType(final Object O)
	{
		if(O instanceof Race) return CMObjectType.RACE;
		if(O instanceof CharClass) return CMObjectType.CHARCLASS;
		if(O instanceof Ability) return CMObjectType.ABILITY;
		if(O instanceof Room) return CMObjectType.LOCALE;
		if(O instanceof MOB) return CMObjectType.MOB;
		if(O instanceof Exit) return CMObjectType.EXIT;
		if(O instanceof Behavior) return CMObjectType.BEHAVIOR;
		if(O instanceof WebMacro) return CMObjectType.WEBMACRO;
		if(O instanceof Area) return CMObjectType.AREA;
		if(O instanceof CMLibrary) return CMObjectType.LIBRARY;
		if(O instanceof CMCommon) return CMObjectType.COMMON;
		if(O instanceof Electronics) return CMObjectType.MISCTECH;
		if(O instanceof Command) return CMObjectType.COMMAND;
		if(O instanceof Clan) return CMObjectType.CLAN;
		if(O instanceof ClanItem) return CMObjectType.CLANITEM;
		if(O instanceof MiscMagic) return CMObjectType.MISCMAGIC;
		if(O instanceof Armor) return CMObjectType.ARMOR;
		if(O instanceof Weapon) return CMObjectType.WEAPON;
		if(O instanceof Item) return CMObjectType.ITEM;
		return null;
	}

	/**
	 * Given a string, Integer, or some other stringable object, this will return the
	 * cmobjecttype based on its name or ordinal relationship.
	 * @see com.planet_ink.marble_mud.core.CMClass.CMObjectType
	 * @param nameOrOrdinal the string, integer, or whatever object
	 * @return the cmobjecttype it refers to
	 */
	public static CMObjectType getTypeByNameOrOrdinal(final Object nameOrOrdinal)
	{
		if(nameOrOrdinal==null) return null;
		if(nameOrOrdinal instanceof Integer)
		{
			final int itemtypeord = ((Integer)nameOrOrdinal).intValue();
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
			  return CMClass.CMObjectType.values()[itemtypeord];
		}
		if(nameOrOrdinal instanceof Long)
		{
			final int itemtypeord = ((Long)nameOrOrdinal).intValue();
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
			  return CMClass.CMObjectType.values()[itemtypeord];
		}
		final String s=nameOrOrdinal.toString();
		if(s.length()==0) return null;
		if(CMath.isInteger(s))
		{
			final int itemtypeord=CMath.s_int(s);
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
				return CMClass.CMObjectType.values()[itemtypeord];
		}
		try
		{
			return CMClass.CMObjectType.valueOf(s);
		}
		catch(Exception e)
		{
			return (CMClass.CMObjectType)CMath.s_valueOf(CMClass.CMObjectType.values(), s.toUpperCase().trim());
		}
	}

	protected static final Object getClassSet(final String type) { return getClassSet(findObjectType(type));}
	protected static final Object getClassSet(final CMObjectType code)
	{
		switch(code)
		{
		case RACE: return c().races;
		case CHARCLASS: return c().charClasses;
		case MOB: return c().MOBs;
		case ABILITY: return c().abilities;
		case LOCALE: return c().locales;
		case EXIT: return c().exits;
		case ITEM: return c().items;
		case BEHAVIOR: return c().behaviors;
		case CLAN: return null;
		case WEAPON: return c().weapons;
		case ARMOR: return c().armor;
		case MISCMAGIC: return c().miscMagic;
		case AREA: return c().areaTypes;
		case COMMAND: return c().commands;
		case CLANITEM: return c().clanItems;
		case MISCTECH: return c().miscTech;
		case WEBMACRO: return c().webMacros;
		case COMMON: return c().common;
		case LIBRARY: return c().libraries;
		}
		return null;
	}

	/**
	 * Returns the total number of template/prototypes of the given type stored by 
	 * this CMClass instance.
	 * @see com.planet_ink.marble_mud.core.CMClass.CMObjectType
	 * @param type the type of object to count
	 * @return the number stored
	 */
	public static final int numPrototypes(final CMObjectType type)
	{
		final Object o = getClassSet(type);
		if(o instanceof Set) return ((Set)o).size();
		if(o instanceof List) return ((List)o).size();
		if(o instanceof Collection) return ((Collection)o).size();
		if(o instanceof HashSet) return ((HashSet)o).size();
		if(o instanceof Hashtable) return ((Hashtable)o).size();
		if(o instanceof Vector) return ((Vector)o).size();
		return 0;
	}

	/**
	 * An enumeration of all the stored races in this classloader for this thread
	 * @return an enumeration of all the stored races in this classloader for this thread
	 */
	public static final Enumeration<Race>   	races(){return c().races.elements();}
	/**
	 * An enumeration of all the stored common Objects in this classloader for this thread
	 * @return an enumeration of all the stored common Objects in this classloader for this thread
	 */
	public static final Enumeration<CMCommon>   commonObjects(){return c().common.elements();}
	/**
	 * An enumeration of all the stored char Classes in this classloader for this thread
	 * @return an enumeration of all the stored char Classes in this classloader for this thread
	 */
	public static final Enumeration<CharClass>  charClasses(){return c().charClasses.elements();}
	/**
	 * An enumeration of all the stored mob Types in this classloader for this thread
	 * @return an enumeration of all the stored mob Types in this classloader for this thread
	 */
	public static final Enumeration<MOB>		mobTypes(){return c().MOBs.elements();}
	/**
	 * An enumeration of all the stored races in this classloader for this thread
	 * @return an enumeration of all the stored races in this classloader for this thread
	 */
	public static final Enumeration<CMLibrary>  libraries(){return c().libraries.elements();}
	/**
	 * An enumeration of all the stored locales in this classloader for this thread
	 * @return an enumeration of all the stored locales in this classloader for this thread
	 */
	public static final Enumeration<Room>   	locales(){return c().locales.elements();}
	/**
	 * An enumeration of all the stored exits in this classloader for this thread
	 * @return an enumeration of all the stored exits in this classloader for this thread
	 */
	public static final Enumeration<Exit>   	exits(){return c().exits.elements();}
	/**
	 * An enumeration of all the stored behaviors in this classloader for this thread
	 * @return an enumeration of all the stored behaviors in this classloader for this thread
	 */
	public static final Enumeration<Behavior>   behaviors(){return c().behaviors.elements();}
	/**
	 * An enumeration of all the stored basic Items in this classloader for this thread
	 * @return an enumeration of all the stored basic Items in this classloader for this thread
	 */
	public static final Enumeration<Item>   	basicItems(){return c().items.elements();}
	/**
	 * An enumeration of all the stored weapons in this classloader for this thread
	 * @return an enumeration of all the stored weapons in this classloader for this thread
	 */
	public static final Enumeration<Weapon> 	weapons(){return c().weapons.elements();}
	/**
	 * An enumeration of all the stored armor in this classloader for this thread
	 * @return an enumeration of all the stored armor in this classloader for this thread
	 */
	public static final Enumeration<Armor>  	armor(){return c().armor.elements();}
	/**
	 * An enumeration of all the stored misc Magic in this classloader for this thread
	 * @return an enumeration of all the stored misc Magic in this classloader for this thread
	 */
	public static final Enumeration<MiscMagic>  miscMagic(){return c().miscMagic.elements();}
	/**
	 * An enumeration of all the stored misc Tech in this classloader for this thread
	 * @return an enumeration of all the stored misc Tech in this classloader for this thread
	 */
	public static final Enumeration<Electronics>miscTech(){return c().miscTech.elements();}
	/**
	 * An enumeration of all the stored clan Items in this classloader for this thread
	 * @return an enumeration of all the stored clan Items in this classloader for this thread
	 */
	public static final Enumeration<ClanItem>   clanItems(){return c().clanItems.elements();}
	/**
	 * An enumeration of all the stored area Types in this classloader for this thread
	 * @return an enumeration of all the stored area Types in this classloader for this thread
	 */
	public static final Enumeration<Area>   	areaTypes(){return c().areaTypes.elements();}
	/**
	 * An enumeration of all the stored commands in this classloader for this thread
	 * @return an enumeration of all the stored commands in this classloader for this thread
	 */
	public static final Enumeration<Command>	commands(){return c().commands.elements();}
	/**
	 * An enumeration of all the stored abilities in this classloader for this thread
	 * @return an enumeration of all the stored abilities in this classloader for this thread
	 */
	public static final Enumeration<Ability>	abilities(){return c().abilities.elements();}
	/**
	 * An enumeration of all the stored webmacros in this classloader for this thread
	 * @return an enumeration of all the stored webmacros in this classloader for this thread
	 */
	public static final Enumeration<WebMacro>   webmacros(){return c().webMacros.elements();}

	/**
	 * Returns a random available race prototype from your classloader
	 * @return a random available race prototype
	 */
	public static final Race		randomRace(){return (Race)c().races.elementAt((int)Math.round(Math.floor(Math.random()*((double)c().races.size()))));}
	/**
	 * Returns a random available char class prototype from your classloader
	 * @return a random available char class prototype
	 */
	public static final CharClass   randomCharClass(){return (CharClass)c().charClasses.elementAt((int)Math.round(Math.floor(Math.random()*((double)c().charClasses.size()))));}
	/**
	 * Returns a random available ability prototype from your classloader
	 * @return a random available ability prototype
	 */
	public static final Ability 	randomAbility(){ return (Ability)c().abilities.elementAt((int)Math.round(Math.floor(Math.random()*((double)c().abilities.size()))));}
	/**
	 * Returns a random available area prototype from your classloader
	 * @return a random available area prototype
	 */
	public static final Area		randomArea(){return (Area)c().areaTypes.elementAt((int)Math.round(Math.floor(Math.random()*((double)c().areaTypes.size()))));}
	/**
	 * Returns a new instance of a locale object of the given ID from your classloader
	 * @return a new instance of a locale object of the given ID
	 */
	public static final Room		getLocale(final String calledThis){ return (Room)getNewGlobal(c().locales,calledThis); }
	/**
	 * Returns a reference to the prototype for the library of the given ID from your classloader
	 * @return a reference to the prototype for the library of the given ID
	 */
	public static final CMLibrary   getLibrary(final String calledThis) { return (CMLibrary)getGlobal(c().libraries,calledThis); }
	/**
	 * Returns a new instance of a area object of the given ID from your classloader
	 * @return a new instance of a area object of the given ID
	 */
	public static final Area		getAreaType(final String calledThis) { return (Area)getNewGlobal(c().areaTypes,calledThis); }
	/**
	 * Returns a new instance of a exit object of the given ID from your classloader
	 * @return a new instance of a exit object of the given ID
	 */
	public static final Exit		getExit(final String calledThis) { return (Exit)getNewGlobal(c().exits,calledThis);}
	/**
	 * Returns a new instance of a MOB object of the given ID from your classloader
	 * @return a new instance of a MOB object of the given ID
	 */
	public static final MOB 		getMOB(final String calledThis) { return (MOB)getNewGlobal(c().MOBs,calledThis); }
	/**
	 * Returns a new instance of a weapon object of the given ID from your classloader
	 * @return a new instance of a weapon object of the given ID
	 */
	public static final Weapon  	getWeapon(final String calledThis) { return (Weapon)getNewGlobal(c().weapons,calledThis); }
	/**
	 * Returns a new instance of a clan item object of the given ID from your classloader
	 * @return a new instance of a clan item object of the given ID
	 */
	public static final ClanItem	getClanItem(final String calledThis) { return (ClanItem)getNewGlobal(c().clanItems,calledThis); }
	/**
	 * Returns a new instance of a misc magic object of the given ID from your classloader
	 * @return a new instance of a misc magic object of the given ID
	 */
	public static final Item		getMiscMagic(final String calledThis) { return (Item)getNewGlobal(c().miscMagic,calledThis); }
	/**
	 * Returns a new instance of a misc tech object of the given ID from your classloader
	 * @return a new instance of a misc tech object of the given ID
	 */
	public static final Item		getMiscTech(final String calledThis) { return (Item)getNewGlobal(c().miscTech,calledThis);}
	/**
	 * Returns a new instance of a armor object of the given ID from your classloader
	 * @return a new instance of a armor object of the given ID
	 */
	public static final Armor   	getArmor(final String calledThis) { return (Armor)getNewGlobal(c().armor,calledThis); }
	/**
	 * Returns a new instance of a basic item object of the given ID from your classloader
	 * @return a new instance of a basic item object of the given ID
	 */
	public static final Item		getBasicItem(final String calledThis) { return (Item)getNewGlobal(c().items,calledThis); }
	/**
	 * Returns a new instance of a behavior object of the given ID from your classloader
	 * @return a new instance of a behavior object of the given ID
	 */
	public static final Behavior	getBehavior(final String calledThis) { return (Behavior)getNewGlobal(c().behaviors,calledThis); }
	/**
	 * Returns a new instance of a ability object of the given ID from your classloader
	 * @return a new instance of a ability object of the given ID
	 */
	public static final Ability 	getAbility(final String calledThis) { return (Ability)getNewGlobal(c().abilities,calledThis); }
	/**
	 * Returns a reference to the prototype for the char class of the given ID from your classloader
	 * @return a reference to the prototype for the char class of the given ID
	 */
	public static final CharClass   getCharClass(final String calledThis){ return (CharClass)getGlobal(c().charClasses,calledThis);}
	/**
	 * Returns a new instance of a common object of the given ID from your classloader
	 * @return a new instance of a common object of the given ID
	 */
	public static final CMCommon	getCommon(final String calledThis){return (CMCommon)getNewGlobal(c().common,calledThis);}
	/**
	 * Returns a reference to the prototype for the command of the given ID from your classloader
	 * @return a reference to the prototype for the command of the given ID
	 */
	public static final Command 	getCommand(final String word){return (Command)getGlobal(c().commands,word);}
	/**
	 * Returns a reference to the prototype for the web macro of the given ID from your classloader
	 * @return a reference to the prototype for the web macro of the given ID
	 */
	public static final WebMacro	getWebMacro(final String macroName){return (WebMacro)c().webMacros.get(macroName);}
	/**
	 * Returns a reference to the prototype for the race of the given ID from your classloader
	 * @return a reference to the prototype for the race of the given ID
	 */
	public static final Race		getRace(final String calledThis){return (Race)getGlobal(c().races,calledThis);}

	/**
	 * Returns the number of prototypes in the classloader of the given set of types
	 * @param types the types to count
	 * @return the number of prototypes in the classloader of the given set of types
	 */
	public static final int numPrototypes(final CMObjectType[] types)
	{
		int total=0;
		for(int i=0;i<types.length;i++)
			total+=numPrototypes(types[i]);
		return total;
	}

	/**
	 * Fills the given list with the IDs of the various Item types, subject to the given filters 
	 * @param namesList the list to populate with IDs
	 * @param NonArchon true to not include Archon items
	 * @param NonGeneric true to not include Gen items
	 * @param NonStandard true to not include Standard items
	 */
	public static final void addAllItemClassNames(final List<String> namesList, final boolean NonArchon, 
												  final boolean NonGeneric, final boolean NonStandard)
	{
		namesList.addAll(getAllItemClassNames(basicItems(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(weapons(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(armor(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(miscMagic(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(miscTech(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(clanItems(),NonArchon,NonGeneric,NonStandard));
	}

	private static List<String> getAllItemClassNames(final Enumeration<? extends Item> i, 
													 final boolean NonArchon, final boolean NonGeneric, final boolean NonStandard)
	{
		final Vector<String> V=new Vector<String>();
		for(;i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonStandard)||(I.isGeneric()))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		return V;
	}

	/**
	 * Returns a new instance of an item object of the given ID from your classloader
	 * Will search basic, armor, weapons, misc magic, clan items, and misc tech respectively 
	 * @return a new instance of an item object of the given ID
	 */
	public static Item getItem(final String calledThis)
	{
		Item thisItem=(Item)getNewGlobal(c().items,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(c().armor,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(c().weapons,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(c().miscMagic,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(c().clanItems,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(c().miscTech,calledThis);
		return thisItem;
	}

	protected Item sampleItem=null;
	/**
	 * Returns the saved copy of the first basic item prototype
	 * @return the saved copy of the first basic item prototype
	 */
	public static final Item sampleItem()
	{
		final CMClass myC=c();
		if((myC.sampleItem==null)&&(myC.items.size()>0))
			myC.sampleItem= (Item)((Item)myC.items.firstElement()).copyOf();
		return myC.sampleItem;
	}
	
	/**
	 * Returns a reference to the prototype of an item object of the given ID from your classloader
	 * Will search basic, armor, weapons, misc magic, clan items, and misc tech respectively 
	 * @return a reference to the prototype of an item object of the given ID
	 */
	public static final Item getItemPrototype(final String itemID)
	{
		Item thisItem=(Item)getGlobal(c().items,itemID);
		if(thisItem==null) thisItem=(Item)getGlobal(c().armor,itemID);
		if(thisItem==null) thisItem=(Item)getGlobal(c().weapons,itemID);
		if(thisItem==null) thisItem=(Item)getGlobal(c().miscMagic,itemID);
		if(thisItem==null) thisItem=(Item)getGlobal(c().clanItems,itemID);
		if(thisItem==null) thisItem=(Item)getGlobal(c().miscTech,itemID);
		return thisItem;
	}

	/**
	 * Returns a reference to the prototype of a mob object of the given ID from your classloader
	 * @return a reference to the prototype of an mob object of the given ID
	 */
	public static final MOB getMOBPrototype(final String mobID)
	{ 
		return (MOB)CMClass.getGlobal(c().MOBs,mobID);
	}

	protected MOB sampleMOB=null;
	/**
	 * Returns the saved copy of the first mob prototype
	 * @return the saved copy of the first mob prototype
	 */
	public static final MOB sampleMOB()
	{
		final CMClass myC=c();
		if((myC.sampleMOB==null)&&(myC.MOBs.size()>0))
		{
			myC.sampleMOB=(MOB)((MOB)myC.MOBs.firstElement()).copyOf();
			myC.sampleMOB.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN);
			myC.sampleMOB.phyStats().setDisposition(PhyStats.IS_NOT_SEEN);
		}
		if(myC.sampleMOB.location()==null)
			myC.sampleMOB.setLocation(CMLib.map().getRandomRoom());
		return myC.sampleMOB;
	}

	/**
	 * Searches the command prototypes for a trigger word match and returns the command.
	 * @param word the command word to search for
	 * @param exactOnly true for a whole word match, false for a startsWith match
	 * @return the command prototypes for a trigger word match and returns the command.
	 */
	public static final Command findCommandByTrigger(final String word, final boolean exactOnly)
	{
		final CMClass myC=c();
		Command C=(Command)myC.commandWords.get(word.trim().toUpperCase());
		if((exactOnly)||(C!=null)) 
			return C;
		String upword=word.toUpperCase();
		String key;
		for(Enumeration<String> e=myC.commandWords.keys();e.hasMoreElements();)
		{
			key=(String)e.nextElement();
			if(key.toUpperCase().startsWith(upword))
				return (Command)myC.commandWords.get(key);
		}
		return null;
	}

	protected final int totalLocalClasses()
	{
		return races.size()+charClasses.size()+MOBs.size()+abilities.size()+locales.size()+exits.size()
			  +items.size()+behaviors.size()+weapons.size()+armor.size()+miscMagic.size()+clanItems.size()
			  +miscTech.size()+areaTypes.size()+common.size()+libraries.size()+commands.size()
			  +webMacros.size();
	}

	/**
	 * Returns the total number of prototypes of all classes in your classloader
	 * @return the total number of prototypes of all classes in your classloader
	 */
	public static final int totalClasses(){ return c().totalLocalClasses();}

	/**
	 * Deletes the class of the given object type from your classloader
	 * @param type the type of object that the given object belongs to
	 * @param O the specific prototype class to remove
	 * @return true
	 */
	public static final boolean delClass(final CMObjectType type, final CMObject O)
	{
		if(classes.containsKey(O.getClass().getName()))
			classes.remove(O.getClass().getName());
		final Object set=getClassSet(type);
		if(set==null) return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		if(set instanceof List)
		{
			((List)set).remove(O);
			if(set instanceof XVector)
				((XVector)set).sort();
		}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).remove(O.ID().trim());
		else
		if(set instanceof HashSet)
			((HashSet)set).remove(O);
		else
			return false;
		if(set==c().commands) 
			reloadCommandWords();
		//if(set==libraries) CMLib.registerLibraries(libraries.elements());
		return true;
	}

	/**
	 * Adds a new prototype of the given object type from your classloader
	 * @param type the type of object that the given object belongs to
	 * @param O the specific prototype class to add
	 * @return true
	 */
	public static final boolean addClass(final CMObjectType type, final CMObject O)
	{
		final Object set=getClassSet(type);
		if(set==null) return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		if(set instanceof List)
		{
			((List)set).add(O);
			if(set instanceof XVector)
				((XVector)set).sort();
		}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).put(O.ID().trim().toUpperCase(), O);
		else
		if(set instanceof HashSet)
			((HashSet)set).add(O);
		else
			return false;
		if(set==c().commands) 
			reloadCommandWords();
		if(set==c().libraries) 
			CMLib.registerLibraries(c().libraries.elements());
		return true;
	}

	/**
	 * Searches for a match to the given object type name, 
	 * preferring exact, but accepting prefixes.
	 * @param name the object type name to search for
	 * @return the matching object type or NULL
	 */
	public final static CMObjectType findObjectType(final String name)
	{
		for(CMObjectType o : CMObjectType.values())
		{
			if(o.toString().equalsIgnoreCase(name))
				return o;
		}
		final String upperName=name.toUpperCase(); 
		for(CMObjectType o : CMObjectType.values())
		{
			if(o.toString().toUpperCase().startsWith(upperName))
				return o;
		}
		for(CMObjectType o : CMObjectType.values())
		{
			if(upperName.startsWith(o.toString().toUpperCase()))
				return o;
		}
		return null;
	}

	/**
	 * Searches for a match to the given object type name, 
	 * preferring exact, but accepting prefixes. Returns 
	 * the ancestor java class type
	 * @param name the object type name to search for
	 * @return the matching object type interface/ancestor or NULL
	 */
	public final static String findTypeAncestor(final String code)
	{
		CMObjectType typ=findObjectType(code);
		if(typ!=null)
			return typ.ancestorName;
		return "";
	}

	/**
	 * Returns the internal object type to which the given object example
	 * belongs by checking its interface implementations/ancestry
	 * @param O the object to find the type of
	 * @return the type of object this is, or NULL
	 */
	public final static CMObjectType getObjectType(final Object O)
	{
		for(CMObjectType o : CMObjectType.values())
		{
			try{
				Class<?> ancestorCl = instance().loadClass(o.ancestorName);
				if(CMClass.checkAncestry(O.getClass(),ancestorCl))
					return o;
			}catch(Exception e){}
		}
		return null;
	}

	/**
	 * Loads the class with the given marblemud or java path to your classloader.
	 * @param classType the type of object to load
	 * @param path the file or java path of the class to load
	 * @param quiet true to not report errors to the log, false otherwise
	 * @return true if the prototype was loaded
	 */
	public static final boolean loadClass(final CMObjectType classType, final String path, final boolean quiet)
	{
		debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CLASSLOADER);
		final Object set=getClassSet(classType);
		if(set==null) return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();

		if(!loadListToObj(set,path,classType.ancestorName,quiet))
			return false;

		if(set instanceof List)
		{
			if(set instanceof XVector)
				((XVector)set).sort();
			if(set==c().commands) reloadCommandWords();
			if(set==c().libraries) CMLib.registerLibraries(c().libraries.elements());
		}
		return true;
	}

	protected static String makeDotClassPath(final String path)
	{
		String pathLess=path;
		final String upperPathLess=pathLess.toUpperCase();
		if(upperPathLess.endsWith(".CLASS"))
			pathLess=pathLess.substring(0,pathLess.length()-6);
		else
		if(upperPathLess.endsWith(".JAVA"))
			pathLess=pathLess.substring(0,pathLess.length()-5);
		else
		if(upperPathLess.endsWith(".JS"))
			pathLess=pathLess.substring(0,pathLess.length()-3);
		pathLess=pathLess.replace('/','.');
		pathLess=pathLess.replace('\\','.');
		return pathLess;
	}
	
	protected static String makeFilePath(final String path)
	{
		final String upperPath=path.toUpperCase();
		if((!upperPath.endsWith(".CLASS"))
		&&(!upperPath.endsWith(".JAVA"))
		&&(!upperPath.endsWith(".JS")))
			return path.replace('.','/')+".class";
		return path;
	}
	
	/**
	 * If the given class exists in the classloader, a new instance will be returned.
	 * If it does not, it will be loaded, and then a new instance of it will be returned.
	 * @param classType the type of class as a filter
	 * @param path the path of some sort to get a new instance of 
	 * @param quiet true to not post errors to the log, false otherwise
	 * @return a new instance of the given class 
	 */
	public static final Object getLoadNewClassInstance(final CMObjectType classType, final String path, final boolean quiet)
	{
		if((path==null)||(path.length()==0))
			return null;
		try{
			final String pathLess=makeDotClassPath(path);
			if(classes.containsKey(pathLess))
				return (classes.get(pathLess)).newInstance();
		}catch(Exception e){}
		final Vector<Object> V=new Vector<Object>(1);
		if(!loadListToObj(V,makeFilePath(path),classType.ancestorName,quiet))
			return null;
		if(V.size()==0) 
			return null;
		final Object o = (Object)V.firstElement();
		try
		{
			return o.getClass().newInstance();
		}
		catch(Exception e)
		{
			return o;
		}
	}

	/**
	 * Returns true if the given class has been loaded into the classloader, or if it is loadable
	 * through the cm class loading system.
	 * @param classType the type of class to check for (for ancestry confirmation)
	 * @param path the path of the class to check for
	 * @return true if it is loaded or loadable, false otherwise
	 */
	public final static boolean checkForCMClass(final CMObjectType classType, final String path)
	{
		if((path==null)||(path.length()==0))
			return false;
		try{
			final String pathLess=makeDotClassPath(path);
			if(classes.containsKey(pathLess))
				return true;
		}catch(Exception e){}
		final Vector<Object> V=new Vector<Object>(1);
		if(!loadListToObj(V,makeFilePath(path),classType.ancestorName,true))
			return false;
		if(V.size()==0) 
			return false;
		return true;
	}

	/**
	 * Returns the base prototype of the given type, by id
	 * @param type the cmobjecttype to return
	 * @param calledThis the ID of the cmobjecttype
	 * @return the base prototype of the given type, by id
	 */
	public static final CMObject getPrototypeByID(final CMObjectType type, final String calledThis)
	{
		Object set=getClassSet(type);
		if(set==null) return null;
		CMObject thisItem;
		if(set instanceof List)
			thisItem=getGlobal((List)set,calledThis);
		else
		if(set instanceof Map)
			thisItem=getGlobal((Map)set,calledThis);
		else
			return null;
		return thisItem;
	}

	/**
	 * Returns either a new instance of the class of the given full java name,
	 * or the marblemud prototype of the class with the given id.  Checks all
	 * cmobjecttypes.
	 * @param calledThis the ID or the given full java name.
	 * @return a new instance of the class, or the prototype
	 */
	public static final Object getObjectOrPrototype(final String calledThis)
	{
		String shortThis=calledThis;
		final int x=shortThis.lastIndexOf('.');
		if(x>0)
		{
			shortThis=shortThis.substring(x+1);
			try{	
				return classes.get(calledThis).newInstance();
			}catch(Exception e){}
		}
		for(CMObjectType o : CMObjectType.values())
		{
			final Object thisItem=getPrototypeByID(o,shortThis);
			if(thisItem!=null) return thisItem;
		}
		return null;
	}

	/**
	 * Returns a new instance of a Environmental of the given id, prefers items,
	 * but also checks mobs and abilities as well.
	 * @param calledThis the id of the cmobject
	 * @return a new instance of a Environmental
	 */
	public static final Environmental getUnknown(final String calledThis)
	{
		Environmental thisItem=(Environmental)getNewGlobal(c().items,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().armor,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().weapons,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().miscMagic,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().miscTech,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().MOBs,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().abilities,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(c().clanItems,calledThis);
		if((thisItem==null)&&(c().charClasses.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		return thisItem;
	}

	/**
	 * Does a search for a race of the given name, first checking
	 * for identical matches, then case insensitive name matches. 
	 * @param calledThis the name or id
	 * @return the race object
	 */
	public static final Race findRace(final String calledThis)
	{
		final Race thisItem=getRace(calledThis);
		if(thisItem!=null) return thisItem;
		Race R;
		final CMClass c=c();
		for(int i=0;i<c.races.size();i++)
		{
			R=(Race)c.races.elementAt(i);
			if(R.name().equalsIgnoreCase(calledThis))
				return R;
		}
		return null;
	}

	/**
	 * Does a search for a Char Class of the given name, first checking
	 * for identical matches, then case insensitive name matches. 
	 * @param calledThis the name or id
	 * @return the Char Class object
	 */
	public static final CharClass findCharClass(final String calledThis)
	{
		final CharClass thisItem=getCharClass(calledThis);
		if(thisItem!=null) return thisItem;
		CharClass C;
		final CMClass c=c();
		for(int i=0;i<c.charClasses.size();i++)
		{
			C=(CharClass)c.charClasses.elementAt(i);
			for(int n=0;n<C.nameSet().length;n++)
				if(C.nameSet()[n].equalsIgnoreCase(calledThis))
					return C;
		}
		return null;
	}

	/**
	 * Returns a new instance of the cmobject of the given id from the given list
	 * @param list the list to search, must be alphabetized
	 * @param ID the perfect cmobject ID of the object
	 * @return a new instance of the cmobject of the given id from the given list
	 */
	public static final CMObject getNewGlobal(final List<? extends CMObject> list, final String ID)
	{
		final CMObject O=(CMObject)getGlobal(list,ID);
		if(O!=null) return O.newInstance();
		return null;
	}

	/**
	 * Returns the prototype of the cmobject of the given id from the given list
	 * @param list the list to search, must be alphabetized
	 * @param ID the perfect cmobject ID of the object
	 * @return the prototype of the cmobject of the given id from the given list
	 */
	public static final CMObject getGlobal(final List<? extends CMObject> list, final String ID)
	{
		if(list.size()==0) return null;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=classID(list.get(mid)).compareToIgnoreCase(ID);
			if(comp==0)
				return list.get(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return null;
	}

	public static final Ability findAbility(final String calledThis)
	{
		return findAbility(calledThis,-1,-1,false);
	}

	public static final Ability findAbility(final String calledThis, final int ofClassDomain, final long ofFlags, final boolean exactOnly)
	{
		final Vector ableV;
		Ability A;
		if((ofClassDomain>=0)||(ofFlags>=0))
		{
			ableV = new Vector();
			for(Enumeration<Ability> e=c().abilities.elements();e.hasMoreElements();)
			{
				A=(Ability)e.nextElement();
				if((ofClassDomain<0)
				||((A.classificationCode() & Ability.ALL_ACODES)==ofClassDomain)
				||((A.classificationCode() & Ability.ALL_DOMAINS)==ofClassDomain))
				{
					if((ofFlags<0)
					||(CMath.bset(A.flags(),ofFlags)))
						ableV.addElement(A);
				}
			}
		} 
		else
			ableV = c().abilities;

		A=(Ability)getGlobal(ableV,calledThis);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,true);
		if((A==null)&&(!exactOnly)) A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,false);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}

	public static final Behavior findBehavior(final String calledThis)
	{
		Behavior B=(Behavior)getGlobal(c().behaviors,calledThis);
		if(B==null) B=getBehaviorByName(calledThis,true);
		if(B==null) B=getBehaviorByName(calledThis,false);
		if(B!=null) B=(Behavior)B.copyOf();
		return B;
	}

	public static final Behavior getBehaviorByName(final String calledThis, final boolean exact)
	{
		if(calledThis==null) return null;
		Behavior B=null;
		for(Enumeration<Behavior> e=behaviors();e.hasMoreElements();)
		{
			B=(Behavior)e.nextElement();
			if(B.name().equalsIgnoreCase(calledThis))
				return (Behavior)B.copyOf();
		}
		if(exact) return null;
		for(Enumeration<Behavior> e=behaviors();e.hasMoreElements();)
		{
			B=(Behavior)e.nextElement();
			if(CMLib.english().containsString(B.name(),calledThis))
				return (Behavior)B.copyOf();
		}
		return null;
	}

	public static final Ability getAbilityByName(final String calledThis, final boolean exact)
	{
		if(calledThis==null) return null;
		Ability A=null;
		for(Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=(Ability)e.nextElement();
			if(A.name().equalsIgnoreCase(calledThis))
				return A;
		}
		if(exact) return null;
		for(Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=(Ability)e.nextElement();
			if(CMLib.english().containsString(A.name(),calledThis))
				return A;
		}
		return null;
	}

	public static final Ability findAbility(final String calledThis, final CharStats charStats)
	{
		Ability A=null;
		final List<Ability> As=new LinkedList<Ability>();
		for(Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=(Ability)e.nextElement();
			for(int c=0;c<charStats.numClasses();c++)
			{
				CharClass C=charStats.getMyClass(c);
				if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>=0)
				{    As.add(A); break;}
			}
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
		if(A==null) A=(Ability)getGlobal(c().abilities,calledThis);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}

	public static final Ability findAbility(final String calledThis, final MOB mob)
	{
		final List<Ability> As=new LinkedList<Ability>();
		Ability A=null;
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A!=null) As.add(A);
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
		if(A==null)
			A=(Ability)getGlobal(c().abilities,calledThis);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}

	public static final CMObject getNewGlobal(final Map<String,? extends CMObject> list, final String ID)
	{
		final CMObject O=(CMObject)getGlobal(list,ID);
		if(O!=null) return O.newInstance();
		return null;
	}

	public static final CMObject getGlobal(final Map<String,? extends CMObject> fromThese, final String calledThis)
	{
		CMObject o=fromThese.get(calledThis);
		if(o==null)
		{
			for(String s : fromThese.keySet())
			{
				o=fromThese.get(s);
				if(classID(o).equalsIgnoreCase(calledThis))
					return o;
			}
			return null;
		}
		return o;
	}

	public static final void addRace(final Race GR)
	{
		Race R;
		for(int i=0;i<c().races.size();i++)
		{
			R=(Race)c().races.elementAt(i);
			if(R.ID().compareToIgnoreCase(GR.ID())>=0)
			{
				if(R.ID().compareToIgnoreCase(GR.ID())==0)
					c().races.setElementAt(GR,i);
				else
					c().races.insertElementAt(GR,i);
				return;
			}
		}
		c().races.addElement(GR);
	}

	public static final void addCharClass(final CharClass CR)
	{
		for(int i=0;i<c().charClasses.size();i++)
		{
			CharClass C=(CharClass)c().charClasses.elementAt(i);
			if(C.ID().compareToIgnoreCase(CR.ID())>=0)
			{
				if(C.ID().compareToIgnoreCase(CR.ID())==0)
					c().charClasses.setElementAt(CR,i);
				else
					c().charClasses.insertElementAt(CR,i);
				return;
			}
		}
		c().charClasses.addElement(CR);
	}

	public static final void delCharClass(final CharClass C)
	{
		c().charClasses.removeElement(C);
	}

	public static final void delRace(final Race R)
	{
		c().races.removeElement(R);
	}

	public static final void sortEnvironmentalsByID(final List<Environmental> V) 
	{
		final TreeMap<String,Environmental> hashed=new TreeMap<String,Environmental>();
		for(Environmental E : V)
			hashed.put(E.ID().toUpperCase(),E);
		V.clear();
		for(String key : hashed.keySet())
			V.add(hashed.get(key));
	}

	public static final void sortEnvironmentalsByName(final List<Environmental> V) 
	{
		final TreeMap<String,LinkedList<Environmental>> nameHash=new TreeMap<String,LinkedList<Environmental>>();
		String name;
		LinkedList<Environmental> list;
		for(Environmental E : V)
		{
			name=E.Name().toUpperCase();
			list = nameHash.get(name);
			if(list==null)
			{
				list=new LinkedList<Environmental>();
				nameHash.put(name,list);
			}
			list.add(E);
		}
		V.clear();
		for(LinkedList<Environmental> l : nameHash.values())
			V.addAll(l);
	}

	private final void initializeClassGroup(final List<? extends CMObject> V)
	{ 
		for(int v=0;v<V.size();v++) 
			((CMObject)V.get(v)).initializeClass();
	}

	private final void initializeClassGroup(final Map<String,? extends CMObject> H)
	{
		for(Object o : H.keySet())
			((CMObject)H.get(o)).initializeClass();
	}

	public final void intializeClasses()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true);
		for(CMObjectType o : CMObjectType.values())
			if((tCode==MudHost.MAIN_HOST)||(privacyV.contains(o.toString())))
			{
				Object set = CMClass.getClassSet(o); 
				if(set instanceof List)
					initializeClassGroup((List)set);
				else
				if(set instanceof Hashtable)
					initializeClassGroup((Map)set);
			}
	}

	public static Hashtable loadHashListToObj(final String filePath, String auxPath, final String ancester)
	{
		final Hashtable<String,Object> h=new Hashtable<String,Object>();
		int x=auxPath.indexOf(';');
		String path;
		while(x>=0)
		{
			path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			loadObjectListToObj(h,filePath,path,ancester);
			x=auxPath.indexOf(';');
		}
		loadObjectListToObj(h,filePath,auxPath,ancester);
		return h;
	}

	public static final XVector loadVectorListToObj(final String filePath, String auxPath, final String ancester)
	{
		final Vector v=new Vector();
		int x=auxPath.indexOf(';');
		String path;
		while(x>=0)
		{
			path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			loadObjectListToObj(v,filePath,path,ancester);
			x=auxPath.indexOf(';');
		}
		loadObjectListToObj(v,filePath,auxPath,ancester);
		return new XVector(new TreeSet(v));
	}

	public static final Vector<Object> loadClassList(final String filePath, String auxPath, final String subDir, final Class<?> ancestorC1, final boolean quiet)
	{
		final Vector v=new Vector();
		int x=auxPath.indexOf(';');
		while(x>=0)
		{
			String path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			if(path.equalsIgnoreCase("%default%"))
				loadListToObj(v,filePath, ancestorC1, quiet);
			else
				loadListToObj(v,path,ancestorC1, quiet);
			x=auxPath.indexOf(';');
		}
		if(auxPath.equalsIgnoreCase("%default%"))
			loadListToObj(v,filePath, ancestorC1, quiet);
		else
			loadListToObj(v,auxPath,ancestorC1, quiet);
		return v;
	}

	public static final boolean loadObjectListToObj(final Object o, final String filePath, final String path, final String ancester)
	{
		if(path.length()>0)
		{
			final boolean success;
			if(path.equalsIgnoreCase("%default%"))
				success=loadListToObj(o,filePath, ancester, false);
			else
				success=loadListToObj(o,path,ancester, false);
			return success;
		}
		return false;
	}

	public static final boolean loadListToObj(final Object toThis, final String filePath, final String ancestor, final boolean quiet)
	{
		final CMClass loader=new CMClass();
		Class<?> ancestorCl=null;
		if (ancestor != null && ancestor.length() != 0)
		{
			try
			{
				ancestorCl = loader.loadClass(ancestor);
			}
			catch (ClassNotFoundException e)
			{
				if(!quiet)
					Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
			}
		}
		return loadListToObj(toThis, filePath, ancestorCl, quiet);
	}

	public static final boolean loadListToObj(final Object toThis, final String filePath, final Class<?> ancestorCl, final boolean quiet)
	{
		final CMClass loader=new CMClass();
		final CMFile file=new CMFile(filePath,null,true);
		final Vector<String> fileList=new Vector<String>();
		if(file.canRead())
		{
			if(file.isDirectory())
			{
				final CMFile[] list=file.listFiles();
				for(int l=0;l<list.length;l++)
					if((list[l].getName().indexOf('$')<0)&&(list[l].getName().toUpperCase().endsWith(".CLASS")))
						fileList.addElement(list[l].getVFSPathAndName());
				for(int l=0;l<list.length;l++)
					if(list[l].getName().toUpperCase().endsWith(".JS"))
						fileList.addElement(list[l].getVFSPathAndName());
			}
			else
			{
				fileList.addElement(file.getVFSPathAndName());
			}
		}
		else
		{
			if(!quiet)
				Log.errOut("CMClass","Unable to access path "+file.getVFSPathAndName());
			return false;
		}
		String item;
		for(int l=0;l<fileList.size();l++)
		{
			item=(String)fileList.elementAt(l);
			if(item.startsWith("/")) item=item.substring(1);
			try
			{
				Object O=null;
				String packageName=item.replace('/','.');
				if(packageName.toUpperCase().endsWith(".CLASS"))
					packageName=packageName.substring(0,packageName.length()-6);
				final Class<?> C=loader.loadClass(packageName,true);
				if(C!=null)
				{
					if(!checkAncestry(C,ancestorCl))
					{
						if(!quiet)
							Log.sysOut("CMClass","WARNING: class failed ancestral check: "+packageName);
					}
					else
						O=C.newInstance();
				}
				if(O==null)
				{
					if(!quiet)
						Log.sysOut("CMClass","Unable to create class '"+packageName+"'");
				}
				else
				{
					String itemName=O.getClass().getName();
					final int x=itemName.lastIndexOf('.');
					if(x>=0) itemName=itemName.substring(x+1);
					if(toThis instanceof Hashtable)
					{
						final Hashtable H=(Hashtable)toThis;
						if(H.containsKey(itemName.trim().toUpperCase()))
							H.remove(itemName.trim().toUpperCase());
						H.put(itemName.trim().toUpperCase(),O);
					}
					else
					if(toThis instanceof List)
					{
						final List V=(List)toThis;
						boolean doNotAdd=false;
						for(int v=0;v<V.size();v++)
							if(rawClassName(V.get(v)).equals(itemName))
							{
								V.set(v,O);
								doNotAdd=true;
								break;
							}
						if(!doNotAdd)
							V.add(O);
					}
				}
			}
			catch(Exception e)
			{
				if(!quiet)
					Log.errOut("CMClass",e);
				return false;
			}
		}
		return true;
	}

	public static final String getObjInstanceStr(Environmental o)
	{
		if(o==null) return "NULL";
		int x=o.toString().indexOf('@');
		if(x<0) return o.Name()+o.toString();
		return o.Name()+o.toString().substring(x);
	}

	public static final String rawClassName(final Object O)
	{
		if(O==null) return "";
		return rawClassName(O.getClass());
	}

	public static final String rawClassName(final Class<?> C)
	{
		if(C==null) return "";
		final String name=C.getName();
		final int lastDot=name.lastIndexOf('.');
		if(lastDot>=0)
			return name.substring(lastDot+1);
		return name;
	}

	public static final CMFile getClassDir(final Class<?> C) 
	{
		final URL location = C.getProtectionDomain().getCodeSource().getLocation();
		String loc;
		if(location == null) {
			return null;
		}

		loc=location.getPath();
		loc=loc.replace('/',File.separatorChar);
		String floc=new java.io.File(".").getAbsolutePath();
		if(floc.endsWith(".")) floc=floc.substring(0,floc.length()-1);
		if(floc.endsWith(File.separator)) floc=floc.substring(0,floc.length()-File.separator.length());
		int x=floc.indexOf(File.separator);
		if(x>=0)floc=floc.substring(File.separator.length());
		x=loc.indexOf(floc);
		loc=loc.substring(x+floc.length());
		loc=loc.replace(File.separatorChar,'/');
		return new CMFile("/"+loc,null,false);
	}

	public static final boolean checkAncestry(final Class<?> cl, final Class<?> ancestorCl)
	{
		if (cl == null) return false;
		if (cl.isPrimitive() || cl.isInterface()) return false;
		if ( Modifier.isAbstract( cl.getModifiers()) || !Modifier.isPublic( cl.getModifiers()) ) return false;
		if (ancestorCl == null) return true;
		return (ancestorCl.isAssignableFrom(cl)) ;
	}

	public static final String classPtrStr(final Object e)
	{
		final String ptr=""+e;
		final int x=ptr.lastIndexOf('@');
		if(x>0)return ptr.substring(x+1);
		return ptr;
	}

	public static final String classID(final Object e)
	{
		if(e!=null)
		{
			if(e instanceof CMObject)
				return ((CMObject)e).ID();
			else
			if(e instanceof Command)
				return rawClassName(e);
			else
				return rawClassName(e);
		}
		return "";
	}

	/**
	 * This is a simple version for external clients since they
	 * will always want the class resolved before it is returned
	 * to them.
	 */
	public final Class<?> loadClass(final String className) throws ClassNotFoundException 
	{
		return (loadClass(className, true));
	}

	public final Class<?> finishDefineClass(String className, final byte[] classData, final String overPackage, final boolean resolveIt)
		throws ClassFormatError
	{
		Class<?> result=null;
		if(overPackage!=null)
		{
			int x=className.lastIndexOf('.');
			if(x>=0)
				className=overPackage+className.substring(x);
			else
				className=overPackage+"."+className;
		}
		try{result=defineClass(className, classData, 0, classData.length);}
		catch(NoClassDefFoundError e)
		{
			if(e.getMessage().toLowerCase().indexOf("(wrong name:")>=0)
			{
				int x=className.lastIndexOf('.');
				if(x>=0)
				{
					String notherName=className.substring(x+1);
					result=defineClass(notherName, classData, 0, classData.length);
				}
				else
					throw e;
			}
			else
				throw e;
		}
		if (result==null){throw new ClassFormatError();}
		if (resolveIt){resolveClass(result);}
		if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
		classes.put(className, result);
		return result;
	}

	/**
	 * This is the required version of loadClass<?> which is called
	 * both from loadClass<?> above and from the internal function
	 * FindClassFromClass.
	 */
	public synchronized final Class<?> loadClass(String className, final boolean resolveIt)
		throws ClassNotFoundException
	{
		String pathName=null;
		if(className.endsWith(".class")) className=className.substring(0,className.length()-6);
		if(className.toUpperCase().endsWith(".JS"))
		{
			pathName=className.substring(0,className.length()-3).replace('.','/')+className.substring(className.length()-3);
			className=className.substring(0,className.length()-3);
		}
		else
			pathName=className.replace('.','/')+".class";
		Class<?> result = (Class<?>)classes.get(className);
		if (result!=null)
		{
			if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
			return result;
		}
		if((super.findLoadedClass(className)!=null)
		||(className.indexOf("com.planet_ink.marble_mud.")<0)
		||(className.startsWith("com.planet_ink.marble_mud.core."))
		||(className.startsWith("com.planet_ink.marble_mud.application."))
		||(className.indexOf(".interfaces.")>=0))
		{
			try{
				result=super.findSystemClass(className);
				if(result!=null)
				{
					if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
					return result;
				}
			} catch(Exception t){}
		}
		/* Try to load it from our repository */
		final CMFile CF=new CMFile(pathName,null,false);
		final byte[] classData=CF.raw();
		if((classData==null)||(classData.length==0))
		{
			throw new ClassNotFoundException("File "+pathName+" not readable!");
		}
		if(CF.getName().toUpperCase().endsWith(".JS"))
		{
			final String name=CF.getName().substring(0,CF.getName().length()-3);
			final StringBuffer str=CF.textVersion(classData);
			if((str==null)||(str.length()==0))
				throw new ClassNotFoundException("JavaScript file "+pathName+" not readable!");
			final List<String> V=Resources.getFileLineVector(str);
			Class<?> extendsClass=null;
			final Vector implementsClasses=new Vector();
			String overPackage=null;
			for(int v=0;v<V.size();v++)
			{
				if((extendsClass==null)&&((String)V.get(v)).trim().toUpperCase().startsWith("//EXTENDS "))
				{
					String extendName=((String)V.get(v)).trim().substring(10).trim();
					try{extendsClass=loadClass(extendName);}
					catch(ClassNotFoundException e)
					{
						Log.errOut("CMClass","Could not load "+CF.getName()+" from "+className+" because "+extendName+" is an invalid extension.");
						throw e;
					}
				}
				if((overPackage==null)&&((String)V.get(v)).trim().toUpperCase().startsWith("//PACKAGE "))
					overPackage=((String)V.get(v)).trim().substring(10).trim();
				if(((String)V.get(v)).toUpperCase().startsWith("//IMPLEMENTS "))
				{
					String extendName=((String)V.get(v)).substring(13).trim();
					Class<?> C=null;
					try{C=loadClass(extendName);}catch(ClassNotFoundException e){continue;}
					implementsClasses.addElement(C);
				}
			}
			final Context X=Context.enter();
			final JScriptLib jlib=new JScriptLib();
			X.initStandardObjects(jlib);
			jlib.defineFunctionProperties(JScriptLib.functions, JScriptLib.class, ScriptableObject.DONTENUM);
			final CompilerEnvirons ce = new CompilerEnvirons();
			ce.initFromContext(X);
			final ClassCompiler cc = new ClassCompiler(ce);
			if(extendsClass==null)
				Log.errOut("CMClass","Warning: "+CF.getVFSPathAndName()+" does not extend any class!");
			else
				cc.setTargetExtends(extendsClass);
			Class<?> mainClass=null;
			if(implementsClasses.size()>0)
			{
				Class[] CS=new Class[implementsClasses.size()];
				for(int i=0;i<implementsClasses.size();i++) CS[i]=(Class)implementsClasses.elementAt(i);
				cc.setTargetImplements(CS);
			}
			final Object[] objs = cc.compileToClassFiles(str.toString(), "script", 1, name);
			for (int i=0;i<objs.length;i+=2)
			{
				Class<?> C=finishDefineClass((String)objs[i],(byte[])objs[i+1],overPackage,resolveIt);
				if(mainClass==null) mainClass=C;
			}
			Context.exit();
			if((debugging)&&(mainClass!=null)) 
				Log.debugOut("CMClass","Loaded: "+mainClass.getName());
			return mainClass;
		}
		result=finishDefineClass(className,classData,null,resolveIt);
		return result;
	}

	protected static final void reloadCommandWords()
	{
		c().commandWords.clear();
		Command C;
		String[] wordList;
		for(int c=0;c<c().commands.size();c++)
		{
			C=(Command)c().commands.elementAt(c);
			wordList=C.getAccessWords();
			if(wordList!=null)
				for(int w=0;w<wordList.length;w++)
					c().commandWords.put(wordList[w].trim().toUpperCase(),C);
		}
	}

	public static final boolean loadClasses(final CMProps page)
	{
		CMClass c=c();
		if(c==null) c=new CMClass();
		final CMClass baseC=clss[MudHost.MAIN_HOST];
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		// wait for baseC
		while((tCode!=MudHost.MAIN_HOST)&&(!classLoaderSync[0]))
		{try{Thread.sleep(500);}catch(Exception e){ break;}}

		final Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true);

		try
		{
			final String prefix="com/planet_ink/marble_mud/";
			debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CLASSLOADER);

			c.libraries=loadVectorListToObj(prefix+"Libraries/",page.getStr("LIBRARY"),CMObjectType.LIBRARY.ancestorName);
			if(c.libraries.size()==0) return false;
			CMLib.registerLibraries(c.libraries.elements());
			if(CMLib.unregistered().length()>0)
			{
				Log.errOut("CMClass","Fatal Error: libraries are unregistered: "+CMLib.unregistered().substring(0,CMLib.unregistered().length()-2));
				return false;
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("COMMON")))
				c.common=baseC.common;
			else
				c.common=loadHashListToObj(prefix+"Common/",page.getStr("COMMON"),CMObjectType.COMMON.ancestorName);
			if(c.common.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("WEBMACROS")))
				c.webMacros=baseC.webMacros;
			else
			{
				c.webMacros=CMClass.loadHashListToObj(prefix+"WebMacros/", "%DEFAULT%",CMObjectType.WEBMACRO.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"WebMacros loaded  : "+c.webMacros.size());
				for(Enumeration e=c.webMacros.keys();e.hasMoreElements();)
				{
					String key=(String)e.nextElement();
					if(key.length()>longestWebMacro)
						longestWebMacro=key.length();
				}
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("RACE")))
				c.races=baseC.races;
			else
			{
				c.races=loadVectorListToObj(prefix+"Races/",page.getStr("RACES"),CMObjectType.RACE.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+c.races.size());
			}
			if(c.races.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("CHARCLASS")))
				c.charClasses=baseC.charClasses;
			else
			{
				c.charClasses=loadVectorListToObj(prefix+"CharClasses/",page.getStr("CHARCLASSES"),CMObjectType.CHARCLASS.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Classes loaded    : "+c.charClasses.size());
			}
			if(c.charClasses.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("MOB")))
				c.MOBs=baseC.MOBs;
			else
			{
				c.MOBs=loadVectorListToObj(prefix+"MOBS/",page.getStr("MOBS"),CMObjectType.MOB.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"MOB Types loaded  : "+c.MOBs.size());
			}
			if(c.MOBs.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("EXIT")))
				c.exits=baseC.exits;
			else
			{
				c.exits=loadVectorListToObj(prefix+"Exits/",page.getStr("EXITS"),CMObjectType.EXIT.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Exit Types loaded : "+c.exits.size());
			}
			if(c.exits.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("AREA")))
				c.areaTypes=baseC.areaTypes;
			else
			{
				c.areaTypes=loadVectorListToObj(prefix+"Areas/",page.getStr("AREAS"),CMObjectType.AREA.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Area Types loaded : "+c.areaTypes.size());
			}
			if(c.areaTypes.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("LOCALE")))
				c.locales=baseC.locales;
			else
			{
				c.locales=loadVectorListToObj(prefix+"Locales/",page.getStr("LOCALES"),CMObjectType.LOCALE.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Locales loaded    : "+c.locales.size());
			}
			if(c.locales.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("ABILITY")))
				c.abilities=baseC.abilities;
			else
			{
				c.abilities=loadVectorListToObj(prefix+"Abilities/",page.getStr("ABILITIES"),CMObjectType.ABILITY.ancestorName);
				if(c.abilities.size()==0) return false;
				if((page.getStr("ABILITIES")!=null)
				&&(page.getStr("ABILITIES").toUpperCase().indexOf("%DEFAULT%")>=0))
				{
					Vector tempV;
					int size=0;
					tempV=loadVectorListToObj(prefix+"Abilities/Fighter/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Ranger/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Paladin/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					size+=tempV.size();
					if(size>0) Log.sysOut(Thread.currentThread().getName(),"Fighter Skills    : "+size);
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Druid/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Chants loaded     : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Languages/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Languages loaded  : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Properties/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Diseases/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Poisons/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Misc/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					Log.sysOut(Thread.currentThread().getName(),"Properties loaded : "+size);
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Prayers/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					Log.sysOut(Thread.currentThread().getName(),"Prayers loaded    : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Archon/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Skills/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Thief/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Common/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Specializations/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);
					if(size>0) Log.sysOut(Thread.currentThread().getName(),"Skills loaded     : "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Songs/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Songs loaded      : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Spells/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Spells loaded     : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/SuperPowers/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);
					if(size>0) Log.sysOut(Thread.currentThread().getName(),"Heroics loaded    : "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Traps/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Traps loaded      : "+tempV.size());
					c.abilities.addAll(tempV);

					c.abilities.sort();

					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading generic abilities");
					final List<DatabaseEngine.AckRecord> genAbilities=CMLib.database().DBReadAbilities();
					if(genAbilities.size()>0)
					{
						int loaded=0;
						for(DatabaseEngine.AckRecord rec : genAbilities)
						{
							String type=rec.typeClass;
							if((type==null)||(type.trim().length()==0))
								type="GenAbility";
							Ability A=(Ability)(CMClass.getAbility(type).copyOf());
							A.setStat("ALLXML",rec.data);
							if((!A.ID().equals("GenAbility"))&&(!A.ID().equals(type)))
							{
								c.abilities.addElement(A);
								loaded++;
							}
						}
						if(loaded>0)
						{
							Log.sysOut(Thread.currentThread().getName(),"GenAbles loaded   : "+loaded);
							c.abilities.sort();
						}
					}
				}
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("ITEM")))
				c.items=baseC.items;
			else
			{
				c.items=loadVectorListToObj(prefix+"Items/Basic/",page.getStr("ITEMS"),CMObjectType.ITEM.ancestorName);
				if(c.items.size()>0) Log.sysOut(Thread.currentThread().getName(),"Basic Items loaded: "+c.items.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("WEAPON")))
				c.weapons=baseC.weapons;
			else
			{
				c.weapons=loadVectorListToObj(prefix+"Items/Weapons/",page.getStr("WEAPONS"),CMObjectType.WEAPON.ancestorName);
				if(c.weapons.size()>0) Log.sysOut(Thread.currentThread().getName(),"Weapons loaded    : "+c.weapons.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("ARMOR")))
				c.armor=baseC.armor;
			else
			{
				c.armor=loadVectorListToObj(prefix+"Items/Armor/",page.getStr("ARMOR"),CMObjectType.ARMOR.ancestorName);
				if(c.armor.size()>0) Log.sysOut(Thread.currentThread().getName(),"Armor loaded      : "+c.armor.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("MISCMAGIC")))
				c.miscMagic=baseC.miscMagic;
			else
			{
				c.miscMagic=loadVectorListToObj(prefix+"Items/MiscMagic/",page.getStr("MISCMAGIC"),CMObjectType.MISCMAGIC.ancestorName);
				if(c.miscMagic.size()>0) Log.sysOut(Thread.currentThread().getName(),"Magic Items loaded: "+c.miscMagic.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("CLANITEMS")))
				c.clanItems=baseC.clanItems;
			else
			{
				c.clanItems=loadVectorListToObj(prefix+"Items/ClanItems/",page.getStr("CLANITEMS"),CMObjectType.CLANITEM.ancestorName);
				if(c.clanItems.size()>0) Log.sysOut(Thread.currentThread().getName(),"Clan Items loaded : "+c.clanItems.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("MISCTECH")))
				c.miscTech=baseC.miscTech;
			else
			{
				c.miscTech=loadVectorListToObj(prefix+"Items/MiscTech/",page.getStr("MISCTECH"),CMObjectType.MISCTECH.ancestorName);
				if(c.miscTech.size()>0) Log.sysOut(Thread.currentThread().getName(),"Electronics loaded: "+c.miscTech.size());
				Vector tempV=loadVectorListToObj(prefix+"Items/Software/",page.getStr("SOFTWARE"),"com.planet_ink.marble_mud.Items.interfaces.Software");
				if(tempV.size()>0) c.miscTech.addAll(tempV);

				c.miscTech.sort();
			}

			if((c.items.size()+c.weapons.size()+c.armor.size()+c.miscTech.size()+c.miscMagic.size()+c.clanItems.size())==0)
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("BEHAVIOR")))
				c.behaviors=baseC.behaviors;
			else
			{
				c.behaviors=loadVectorListToObj(prefix+"Behaviors/",page.getStr("BEHAVIORS"),CMObjectType.BEHAVIOR.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Behaviors loaded  : "+c.behaviors.size());
			}
			if(c.behaviors.size()==0) return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!privacyV.contains("COMMAND")))
			{
				c.commands=baseC.commands;
				c.commandWords=baseC.commandWords;
			}
			else
			{
				c.commands=loadVectorListToObj(prefix+"Commands/",page.getStr("COMMANDS"),CMObjectType.COMMAND.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Commands loaded   : "+c.commands.size());
			}
			if(c.commands.size()==0) return false;
		}
		catch(Exception t)
		{
			t.printStackTrace();
			return false;
		}

		reloadCommandWords();

		// misc startup stuff
		if((tCode==MudHost.MAIN_HOST)||(privacyV.contains("CHARCLASS")))
			for(int i=0;i<c.charClasses.size();i++)
			{
				CharClass C=(CharClass)c.charClasses.elementAt(i);
				C.copyOf();
			}
		if((tCode==MudHost.MAIN_HOST)||(privacyV.contains("RACE")))
		{
			for(int r=0;r<c.races.size();r++)
			{
				Race R=(Race)c.races.elementAt(r);
				R.copyOf();
			}
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading genRaces");
			final List<DatabaseEngine.AckRecord> genRaces=CMLib.database().DBReadRaces();
			if(genRaces.size()>0)
			{
				int loaded=0;
				for(int r=0;r<genRaces.size();r++)
				{
					Race GR=(Race)getRace("GenRace").copyOf();
					GR.setRacialParms(genRaces.get(r).data);
					if(!GR.ID().equals("GenRace"))
					{
						addRace(GR);
						loaded++;
					}
				}
				if(loaded>0)
					Log.sysOut(Thread.currentThread().getName(),"GenRaces loaded   : "+loaded);
			}
		}
		if((tCode==MudHost.MAIN_HOST)||(privacyV.contains("CHARCLASS")))
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading genClasses");
			final List<DatabaseEngine.AckRecord> genClasses=CMLib.database().DBReadClasses();
			if(genClasses.size()>0)
			{
				int loaded=0;
				for(int r=0;r<genClasses.size();r++)
				{
					CharClass CR=(CharClass)(CMClass.getCharClass("GenCharClass").copyOf());
					CR.setClassParms(genClasses.get(r).data);
					if(!CR.ID().equals("GenCharClass"))
					{
						addCharClass(CR);
						loaded++;
					}
				}
				if(loaded>0)
					Log.sysOut(Thread.currentThread().getName(),"GenClasses loaded : "+loaded);
			}
		}
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: initializing classes");
		c.intializeClasses();
		if((tCode==MudHost.MAIN_HOST)||(privacyV.contains("EXPERTISES")))
		{
			CMLib.expertises().recompileExpertises();
			Log.sysOut(Thread.currentThread().getName(),"Expertises defined: "+CMLib.expertises().numExpertises());
		}
		if(tCode==MudHost.MAIN_HOST)
			classLoaderSync[0]=true;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		return true;
	}

	public static long getLastClassUpdatedTime(){ return lastUpdateTime; }

	protected static final class JScriptLib extends ScriptableObject
	{
		public String getClassName(){ return "JScriptLib";}
		static final long serialVersionUID=47;
		public static String[] functions = {"toJavaString"};
		public String toJavaString(Object O){return Context.toString(O);}
	}
	
	public static final boolean returnMsg(final CMMsg msg)
	{
		if(MSGS_CACHE.size()<MAX_MSGS)
		{
			synchronized(CMClass.MSGS_CACHE)
			{
				MSGS_CACHE.addLast(msg);
				return true;
			}
		}
		return false;
	}

	public final static CMMsg MsgFactory()
	{
		try
		{
			synchronized(MSGS_CACHE)
			{
				return MSGS_CACHE.removeFirst();
			}
		}
		catch(Exception e)
		{
			return (CMMsg)getCommon("DefaultMessage");
		}
	}

	public static final CMMsg getMsg(final MOB source, final int newAllCode, final String allMessage)
	{ final CMMsg M=MsgFactory(); M.modify(source,newAllCode,allMessage); return M;}
	public static final CMMsg getMsg(final MOB source, final int newAllCode, final String allMessage, final int newValue)
	{ final CMMsg M=MsgFactory(); M.modify(source,newAllCode,allMessage,newValue); return M;}
	public static final CMMsg getMsg(final MOB source, final Environmental target, final int newAllCode, final String allMessage)
	{ final CMMsg M=MsgFactory(); M.modify(source,target,newAllCode,allMessage); return M;}
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newAllCode, final String allMessage)
	{ final CMMsg M=MsgFactory(); M.modify(source,target,tool,newAllCode,allMessage); return M;}
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newSourceCode, final int newTargetCode, final int newOthersCode, final String Message)
	{ final CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,newTargetCode,newOthersCode,Message); return M;}
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newSourceCode, final String sourceMessage, final String targetMessage, final String othersMessage)
	{ final CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,sourceMessage,newSourceCode,targetMessage,newSourceCode,othersMessage); return M;}
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newSourceCode, final String sourceMessage, final int newTargetCode, final String targetMessage, final int newOthersCode, final String othersMessage)
	{ final CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,sourceMessage,newTargetCode,targetMessage,newOthersCode,othersMessage); return M;}

	public static final boolean returnMob(final MOB mob)
	{
		if(MOB_CACHE.size()<MAX_MOBS)
		{
			synchronized(CMClass.MOB_CACHE)
			{
				MOB_CACHE.addLast(mob);
				return true;
			}
		}
		return false;
	}

	public final static MOB MobFactory()
	{
		try
		{
			synchronized(MOB_CACHE)
			{
				return MOB_CACHE.removeFirst();
			}
		}
		catch(Exception e)
		{
			return (MOB)getMOB("StdFactoryMOB");
		}
	}

	public static final MOB getFactoryMOB(){ final MOB M=MobFactory(); return M;}
	
	public static final void shutdown() 
	{
		for(int c=0;c<clss.length;c++)
			if(clss[c]!=null)
				clss[c].unload();
	}

	public final void unload()
	{
		common.clear();
		races.clear();
		charClasses.clear();
		MOBs.clear();
		abilities.clear();
		locales.clear();
		exits.clear();
		items.clear();
		behaviors.clear();
		weapons.clear();
		armor.clear();
		miscMagic.clear();
		miscTech.clear();
		areaTypes.clear();
		clanItems.clear();
		commands.clear();
		webMacros.clear();
		commandWords.clear();
	}
}
