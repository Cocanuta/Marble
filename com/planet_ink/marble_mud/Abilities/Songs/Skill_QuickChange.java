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
@SuppressWarnings({"unchecked","rawtypes"})
public class Skill_QuickChange extends BardSkill
{
	public String ID() { return "Skill_QuickChange"; }
	public String name(){ return "QuickChange";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"QUICKCHANGE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;}
	static final String locationsDelim="<ITEMLOCATIONS>";
	static final String containerDelim="<ITEMCONTAINERS>";
	
	private class PackedItem
	{
		public Item I;
		public long wornLoc;
		public Item containerI;
		public PackedItem(Item I, Item containerI, long wornLocation)
		{
			this.I=I;
			this.wornLoc=wornLocation;
			this.containerI=containerI;
		}
	}

	public List<PackedItem> getAllWornItems(MOB mob)
	{
		List<PackedItem> items=new LinkedList<PackedItem>();
		for(Enumeration<Item> i= mob.items(); i.hasMoreElements();)
		{
			Item I=i.nextElement();
			if((!I.amWearingAt(Item.IN_INVENTORY))
			||(!I.ultimateContainer(null).amWearingAt(Item.IN_INVENTORY)))
				items.add(new PackedItem(I,I.container(),I.rawWornCode()));
		}
		return items;
	}
	 
	public List<PackedItem> getAllPackedItems(Session S)
	{
		List<PackedItem> items=new LinkedList<PackedItem>();
		if(super.miscText.trim().length()>0)
		{
			int locStart=super.miscText.lastIndexOf(locationsDelim);
			int contStart=super.miscText.lastIndexOf(containerDelim);
			if((locStart>0)&&(contStart>locStart))
			{
				List<Item> itemList=new Vector<Item>();
				CMLib.coffeeMaker().addItemsFromXML(super.miscText.substring(0,locStart), itemList, S);
				List<String> itemLocList=CMParms.parseAny(super.miscText.substring(locStart+locationsDelim.length(),contStart), ';', true);
				List<String> itemConList=CMParms.parseAny(super.miscText.substring(contStart+containerDelim.length()), ';', true);
				if((itemLocList.size()==itemList.size())&&(itemLocList.size()==itemConList.size()))
				{
					for(int i=0;i<itemList.size();i++)
					{
						long wornLoc=CMath.s_long(itemLocList.get(i));
						int containerDex=CMath.s_int(itemConList.get(i));
						Item containerI=null; 
						if(containerDex>=0)
							containerI=itemList.get(containerDex);
						items.add(new PackedItem((Item)itemList.get(i),containerI,wornLoc));
					}
				}
			}
		}
		return items;
	}
	
	public void wearThese(MOB mob, List<PackedItem> items)
	{
		for(PackedItem I : items)
		{
			if(I.containerI instanceof Container)
				I.I.setContainer((Container)I.containerI);
			mob.addItem(I.I);
			I.I.wearAt(I.wornLoc);
		}
	}
	
	public void packThese(List<PackedItem> items)
	{
		List<Item> itemList=new Vector<Item>();
		for(PackedItem I : items)
			itemList.add(I.I);
		StringBuilder str=new StringBuilder("<ITEMS>");
		str.append(CMLib.coffeeMaker().getItemsXML(itemList, new Hashtable(), new HashSet(), 0));
		str.append("</ITEMS>");
		str.append(locationsDelim);
		for(PackedItem I : items)
			str.append(I.wornLoc).append(";");
		str.append(containerDelim);
		for(PackedItem I : items)
			str.append(itemList.indexOf(I.containerI)).append(";");
		super.miscText=str.toString();
		for(PackedItem I : items)
			I.I.destroy();
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),"<S-NAME> perform(s) a quick costume change.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				List<PackedItem> myCurrentGear=getAllWornItems(mob);
				List<PackedItem> mySavedGear=getAllPackedItems(mob.session());
				packThese(myCurrentGear);
				if(mySavedGear.size()==0)
					mob.tell("That outfit is now tucked away for a quick change later on.");
				else
					wearThese(mob,mySavedGear);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to change clothes, but forget(s) how.");

		return success;
	}

}
