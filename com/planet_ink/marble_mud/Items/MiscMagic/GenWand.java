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
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;

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
public class GenWand extends StdWand
{
	public String ID(){	return "GenWand";}
	protected String readableText="";
	
	public GenWand()
	{
		super();

		setName("a wand");
		setDisplayText("a simple wand is here.");
		setDescription("A wand made out of wood.");
		secretIdentity=null;
		setUsesRemaining(0);
		baseGoldValue=20000;
		basePhyStats().setLevel(12);
		CMLib.flags().setReadable(this,false);
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	public boolean isGeneric(){return true;}

	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}
	public Ability getSpell()
	{
		return CMClass.getAbility(readableText);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){ readableText=text;secretWord=StdWand.getWandWord(readableText);}

	protected int maxUses=Integer.MAX_VALUE;
	public int maxUses(){return maxUses;}
	public void setMaxUses(int newMaxUses){maxUses=newMaxUses;}
	
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
	}
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code,val);
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(GenericBuilder.GENITEMCODES,this);
		return codes; 
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWand)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
