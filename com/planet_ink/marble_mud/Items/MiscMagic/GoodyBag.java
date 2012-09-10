package com.planet_ink.marble_mud.Items.MiscMagic;
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
import com.planet_ink.marble_mud.Libraries.interfaces.MoneyLibrary;
import com.planet_ink.marble_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
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
public class GoodyBag extends BagOfEndlessness implements ArchonOnly
{
	public String ID(){	return "GoodyBag";}
	boolean alreadyFilled=false;
	public GoodyBag()
	{
		super();
		setName("a goody bag");
		setDisplayText("a small bag is sitting here.");
		setDescription("A nice little bag to put your things in.");
		secretIdentity="The Archon's Goody Bag";
		recoverPhyStats();
	}

	private void putInBag(Item I)
	{
		I.setContainer(this);
		if(owner() instanceof Room)
			((Room)owner()).addItem(I);
		else
		if(owner() instanceof MOB)
			((MOB)owner()).addItem(I);
		I.recoverPhyStats();
	}

	public void addMoney(double value)
	{
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(getContents().size()==0)
			{
				List<String> V=CMLib.beanCounter().getAllCurrencies();
				for(int v=0;v<V.size();v++)
				{
					String currency=(String)V.get(v);
					MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(currency);
					for(int v2=0;v2<DV.length;v2++)
					{
						Coins C=CMLib.beanCounter().makeBestCurrency(currency,DV[v2].value,owner(),this);
						if(C!=null)	C.setNumberOfCoins(100);
					}
				}
				Item I=CMClass.getItem("GenSuperPill");
				I.setName("a training pill");
				I.setDisplayText("A small round pill has been left here.");
				((Pill)I).setSpellList("train+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a practice pill");
				I.setDisplayText("A tiny little pill has been left here.");
				((Pill)I).setSpellList("prac+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a quest point pill");
				I.setDisplayText("A questy little pill has been left here.");
				((Pill)I).setSpellList("ques+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 100 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+100");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 500 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+500");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 1000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+1000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 2000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+2000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a 5000 exp pill");
				I.setDisplayText("An important little pill has been left here.");
				((Pill)I).setSpellList("expe+5000");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a strength pill");
				I.setDisplayText("An strong little pill has been left here.");
				((Pill)I).setSpellList("str+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("an intelligence pill");
				I.setDisplayText("An smart little pill has been left here.");
				((Pill)I).setSpellList("int+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a wisdom pill");
				I.setDisplayText("A wise little pill has been left here.");
				((Pill)I).setSpellList("wis+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a dexterity pill");
				I.setDisplayText("A quick little pill has been left here.");
				((Pill)I).setSpellList("dex+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a constitution pill");
				I.setDisplayText("A nutricious little pill has been left here.");
				((Pill)I).setSpellList("con+1");
				putInBag(I);
				I=CMClass.getItem("GenSuperPill");
				I.setName("a charisma pill");
				I.setDisplayText("A pretty little pill has been left here.");
				((Pill)I).setSpellList("cha+1");
				putInBag(I);
			}
		}
		super.executeMsg(myHost,msg);
	}
}
