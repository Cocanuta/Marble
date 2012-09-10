package com.planet_ink.marble_mud.Items.MiscMagic;
import com.planet_ink.marble_mud.Items.Basic.StdItem;
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

/**
 * <p>Title: False Realities Flavored marblemud</p>
 * <p>Description: The False Realities Version of marblemud</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class StdPowder extends StdItem implements MagicDust {
	public String ID(){	return "StdPowder";}

	public StdPowder()
	{
		super();

		setName("a pile of powder");
		basePhyStats.setWeight(1);
		setDisplayText("A small pile of powder sits here.");
		setDescription("A small pile of powder.");
		secretIdentity="This is a pile of inert materials.";
		baseGoldValue=0;
		material=RawMaterial.RESOURCE_ASH;
		recoverPhyStats();
	}
	
	public void spreadIfAble(MOB mob, Physical target)
	{
		List<Ability> spells = getSpells();
		if (spells.size() > 0)
			for (int i = 0; i < spells.size(); i++) 
			{
				Ability thisOne = (Ability) ( (Ability) spells.get(i)).copyOf();
				if(thisOne.canTarget(target))
				{
					if((malicious(this))||(!(target instanceof MOB)))
						thisOne.invoke(mob, target, true, phyStats().level());
					else
						thisOne.invoke((MOB)target,(MOB)target, true, phyStats().level());
				}
			}
		destroy();
	}


// That which makes Powders work.  They're an item that when successfully dusted on a target, are 'cast' on the target
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.sourceMinor()==CMMsg.TYP_THROW ) 
		{
			if((msg.tool()==this)&&(msg.target() instanceof Physical)) 
				spreadIfAble(msg.source(),(Physical)msg.target());
			else
				super.executeMsg(myHost,msg);
		}
		else
			super.executeMsg(myHost,msg);
	}

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}

	public boolean malicious(SpellHolder me) {
		List<Ability> spells=getSpells();
		for(Ability checking : spells) 
			if(checking.abstractQuality()==Ability.QUALITY_MALICIOUS)
				return true;
		return false;
	}
	public List<Ability> getSpells()
	{
		String names=getSpellList();

		Vector theSpells=new Vector();
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
				theSpells.addElement(A);
			}
		}
		recoverPhyStats();
		return theSpells;
	}

	public String secretIdentity()
	{
		return description()+"\n\r"+super.secretIdentity();
	}

}
