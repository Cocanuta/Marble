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
public class GenFatWallpaper extends GenWallpaper
{
	public String ID(){	return "GenFatWallpaper";}
	protected String	displayText="";
	public String displayText(){ return displayText;}
	public void setDisplayText(String newText){displayText=newText;}
	protected long expirationDate=0;
	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)
		&&((msg.targetMinor()==CMMsg.TYP_EXPIRE)||(msg.targetMinor()==CMMsg.TYP_DEATH)))
		{
			return true;
		}
		if(!super.okMessage(myHost,msg))
			return false;
		return true;
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)
		&&((msg.targetMinor()==CMMsg.TYP_EXPIRE)||(msg.targetMinor()==CMMsg.TYP_DEATH)))
			destroy();
		super.executeMsg(myHost,msg);
	}
	private static final String[] CODES={"DISPLAY"};
	public String[] getStatCodes(){
		String[] THINCODES=super.getStatCodes();
		String[] codes=new String[THINCODES.length+1];
		for(int c=0;c<THINCODES.length;c++)
			codes[c]=THINCODES[c];
		codes[THINCODES.length]="DISPLAY";
		return codes;
	}
	protected int getMyCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		if(getMyCodeNum(code)<0) return super.getStat(code);
		switch(getMyCodeNum(code))
		{
		case 0: 
			return displayText();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(getMyCodeNum(code)<0)
			super.setStat(code,val);
		else
		switch(getMyCodeNum(code))
		{
		case 0: setDisplayText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenFatWallpaper)) return false;
		if(!super.sameAs(E)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
			{
				return false;
			}
		return true;
	}
}
