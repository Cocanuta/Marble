package com.planet_ink.marble_mud.Items.MiscMagic;
import com.planet_ink.marble_mud.Items.Basic.GenDrink;
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
public class GenMultiPotion extends GenDrink implements Potion
{
	public String ID(){	return "GenMultiPotion";}

	public GenMultiPotion()
	{
		super();

		material=RawMaterial.RESOURCE_GLASS;
		setName("a flask");
		basePhyStats.setWeight(1);
		setDisplayText("A flask sits here.");
		setDescription("A strange flask with stranger markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverPhyStats();
	}


	public boolean isGeneric(){return true;}
	public int liquidType(){return RawMaterial.RESOURCE_DRINKABLE;}

	public boolean isDrunk(){return (readableText.toUpperCase().indexOf(";DRUNK")>=0);}
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

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("potion",super.secretIdentity(),"",getSpells());
	}

	public int value()
	{
		if(isDrunk())
			return 0;
		return super.value();
	}

	public String getSpellList()
	{ return readableText;}
	public void setSpellList(String list){readableText=list;}
	public List<Ability> getSpells()
	{
		return StdPotion.getSpells(this);
	}
	public void setReadableText(String text){
		readableText=text;
		setSpellList(readableText);
	}
	

	public void drinkIfAble(MOB mob)
	{
		List<Ability> spells=getSpells();
		if(mob.isMine(this))
		{
			if((!isDrunk())&&(spells.size()>0))
			{
				for(int i=0;i<spells.size();i++)
				{
					Ability thisOne=(Ability)((Ability)spells.get(i)).copyOf();
					thisOne.invoke(mob,mob,true,phyStats().level());
				}
			}

			if((liquidRemaining()<=thirstQuenched())&&(!isDrunk()))
				setDrunk(true);
		}

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
					if(isDrunk())
					{
						mob.tell(name()+" vanishes!");
						destroy();
						mob.recoverPhyStats();
					}
				}
				else
				{
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
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
	// stats handled by gendrink, spells by readabletext
}
