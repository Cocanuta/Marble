package com.planet_ink.marble_mud.Items.MiscTech;
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
public class GenElecItem extends StdElecItem
{
	public String ID(){	return "GenElecItem";}
	protected String readableText="";
	public GenElecItem()
	{
		super();
		setName("a generic electric item");
		basePhyStats.setWeight(2);
		setDisplayText("a generic electric item sits here.");
		setDescription("");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}

	public boolean isGeneric(){return true;}

	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"FUELTYPE","POWERCAP","ACTIVATED","POWERREM"};
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+fuelType();
		case 1: return ""+powerCapacity();
		case 2: return ""+activated();
		case 3: return ""+powerRemaining();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:{
				int x=CMath.s_parseListIntExpression(RawMaterial.CODES.NAMES(), val);
				x=((x>=0)&&(x<RawMaterial.RESOURCE_MASK))?RawMaterial.CODES.GET(x):x;
				setFuelType(x); 
				break;
			   } 
		case 1: setPowerCapacity(CMath.s_parseLongExpression(val)); break;
		case 2: activate(CMath.s_bool(val)); break;
		case 3: setPowerRemaining(CMath.s_parseLongExpression(val)); break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] MYCODES=CMProps.getStatCodesList(GenElecItem.MYCODES,this);
		String[] superCodes=GenericBuilder.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenElecItem)) return false;
		String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		return true;
	}
}
