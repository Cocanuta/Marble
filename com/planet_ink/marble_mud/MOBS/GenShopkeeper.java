package com.planet_ink.marble_mud.MOBS;
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
public class GenShopkeeper extends StdShopKeeper
{
	public String ID(){return "GenShopkeeper";}
	protected String prejudiceFactors="";
	private String ignoreMask="";

	public GenShopkeeper()
	{
		super();
		username="a generic shopkeeper";
		setDescription("He looks like he wants to sell something to you.");
		setDisplayText("A generic shopkeeper stands here.");
		basePhyStats().setAbility(11); // his only off-default
	}

	public boolean isGeneric(){return true;}

	public String prejudiceFactors(){return prejudiceFactors;}
	public void setPrejudiceFactors(String factors){prejudiceFactors=factors;}
	public String ignoreMask(){return ignoreMask;}
	public void setIgnoreMask(String factors){ignoreMask=factors;}
	public String text()
	{
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
			miscText=CMLib.encoder().compressString(CMLib.coffeeMaker().getPropertiesStr(this,false));
		else
			miscText=CMLib.coffeeMaker().getPropertiesStr(this,false);
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CMLib.coffeeMaker().resetGenMOB(this,newText);
	}
	private final static String[] MYCODES={"WHATISELL","PREJUDICE","BUDGET","DEVALRATE","INVRESETRATE","IGNOREMASK","PRICEMASKS"};
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenMobStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+getWhatIsSoldMask();
		case 1: return prejudiceFactors();
		case 2: return budget();
		case 3: return devalueRate();
		case 4: return ""+invResetRate();
		case 5: return ignoreMask();
		case 6: return CMParms.toStringList(itemPricingAdjustments());
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:{
			if((val.length()==0)||(CMath.isLong(val)))
				setWhatIsSoldMask(CMath.s_long(val));
			else
			if(CMParms.containsIgnoreCase(ShopKeeper.DEAL_DESCS,val))
				setWhatIsSoldMask(CMParms.indexOfIgnoreCase(ShopKeeper.DEAL_DESCS,val));
			break;
		}
		case 1: setPrejudiceFactors(val); break;
		case 2: setBudget(val); break;
		case 3: setDevalueRate(val); break;
		case 4: setInvResetRate(CMath.s_parseIntExpression(val)); break;
		case 5: setIgnoreMask(val); break;
		case 6: setItemPricingAdjustments((val.trim().length()==0)?new String[0]:CMParms.toStringArray(CMParms.parseCommas(val,true))); break;
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
		String[] MYCODES=CMProps.getStatCodesList(GenShopkeeper.MYCODES,this);
		String[] superCodes=GenericBuilder.GENMOBCODES;
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
		if(!(E instanceof GenShopkeeper)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
