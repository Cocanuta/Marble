package com.planet_ink.marble_mud.Items.MiscMagic;
import com.planet_ink.marble_mud.Items.Basic.StdDrink;
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
public class StdPotion extends StdDrink implements Potion
{
	public String ID(){	return "StdPotion";}
	public StdPotion()
	{
		super();

		setName("a potion");
		basePhyStats.setWeight(1);
		setDisplayText("An empty potion sits here.");
		setDescription("An empty potion with strange residue.");
		secretIdentity="What was once a powerful potion.";
		capacity=1;
		containType=Container.CONTAIN_LIQUID;
		liquidType=RawMaterial.RESOURCE_DRINKABLE;
		baseGoldValue=200;
		material=RawMaterial.RESOURCE_GLASS;
		recoverPhyStats();
	}

	public int liquidType(){return RawMaterial.RESOURCE_DRINKABLE;}
	public boolean isDrunk(){return (getSpellList().toUpperCase().indexOf(";DRUNK")>=0);}
	public int value()
	{
		if(isDrunk())
			return 0;
		return super.value();
	}
	
	public void setDrunk(boolean isTrue)
	{
		if(isTrue&&isDrunk()) return;
		if((!isTrue)&&(!isDrunk())) return;
		if(isTrue)
			setSpellList(getSpellList()+";DRUNK");
		else
		{
			String list="";
			List<Ability> theSpells=getSpells();
			for(int v=0;v<theSpells.size();v++)
				list+=((Ability)theSpells.get(v)).ID()+";";
			setSpellList(list);
		}
	}

	public void drinkIfAble(MOB mob)
	{
		List<Ability> spells=getSpells();
		if(mob.isMine(this))
			if((!isDrunk())&&(spells.size()>0))
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.get(i)).copyOf();
					thisOne.invoke(mob,mob,true,phyStats().level());
					setDrunk(true);
					setLiquidRemaining(0);
				}
	}

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}
	
	public static List<Ability> getSpells(SpellHolder me)
	{
		int baseValue=200;
		Vector<Ability> theSpells=new Vector<Ability>();
		String names=me.getSpellList();
		int del=names.indexOf(';');
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=CMClass.getAbility(thisOne);
				if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
				{
					A=(Ability)A.copyOf();
					baseValue+=(100*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(';');
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				baseValue+=(100*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				theSpells.addElement(A);
			}
		}
		me.setBaseValue(baseValue);
		me.recoverPhyStats();
		return theSpells;
	}
	
	public List<Ability> getSpells(){ return getSpells(this);}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells(this));
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this))
		   &&(msg.targetMinor()==CMMsg.TYP_DRINK)
		   &&(msg.othersMessage()==null)
		   &&(msg.sourceMessage()==null))
				return true;
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					drinkIfAble(mob);
					mob.tell(name()+" vanishes!");
					destroy();
					mob.recoverPhyStats();
				}
				else
				{
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
					super.executeMsg(myHost,msg);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

}
