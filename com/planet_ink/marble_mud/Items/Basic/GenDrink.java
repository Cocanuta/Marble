package com.planet_ink.marble_mud.Items.Basic;
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
public class GenDrink extends StdDrink
{
	public String ID(){    return "GenDrink";}
	protected String	readableText="";
	public GenDrink()
	{
		super();
		setName("a generic drinking container");
		basePhyStats.setWeight(1);
		setDisplayText("a generic drinking container sits here.");
		setDescription("");
		baseGoldValue=1;
		capacity=5;
		amountOfThirstQuenched=100;
		amountOfLiquidHeld=700;
		amountOfLiquidRemaining=700;
		liquidType=RawMaterial.RESOURCE_FRESHWATER;
		setMaterial(RawMaterial.RESOURCE_LEATHER);
		recoverPhyStats();
	}


	public boolean isGeneric(){return true;}

	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}
	public int liquidType(){
		if((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			return material();
		if(CMath.s_int(readableText)==0) return RawMaterial.RESOURCE_FRESHWATER;
		return CMath.s_int(readableText);
	}
	public void setLiquidType(int newLiquidType){readableText=""+newLiquidType;}

	public String keyName()
	{
		return readableText;
	}
	public void setKeyName(String newKeyName)
	{
		readableText=newKeyName;
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}
	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES",
							  "QUENCHED","LIQUIDHELD","LIQUIDTYPE"};
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasALid();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+thirstQuenched();
		case 5: return ""+liquidHeld();
		case 6: return ""+liquidType();
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
		case 0: setLidsNLocks(hasALid(),isOpen(),CMath.s_bool(val),false); break;
		case 1: setLidsNLocks(CMath.s_bool(val),isOpen(),hasALock(),false); break;
		case 2: setCapacity(CMath.s_parseIntExpression(val)); break;
		case 3: setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS,val)); break;
		case 4: setThirstQuenched(CMath.s_parseIntExpression(val)); break;
		case 5: setLiquidHeld(CMath.s_parseIntExpression(val)); break;
		case 6: {
			int x=CMath.s_parseListIntExpression(RawMaterial.CODES.NAMES(), val);
			x=((x>=0)&&(x<RawMaterial.RESOURCE_MASK))?RawMaterial.CODES.GET(x):x;
			setLiquidType(x); break;
		}
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
		String[] MYCODES=CMProps.getStatCodesList(GenDrink.MYCODES,this);
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
		if(!(E instanceof GenDrink)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
