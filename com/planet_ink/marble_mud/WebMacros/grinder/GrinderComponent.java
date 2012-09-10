package com.planet_ink.marble_mud.WebMacros.grinder;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.Clan.MemberRecord;
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
public class GrinderComponent
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=httpReq.getRequestParameter("COMPONENT");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			String fixedCompID=last.replace(' ','_').toUpperCase();
			List<AbilityComponent> set=new Vector<AbilityComponent>();
			int posDex=1;
			while(httpReq.isRequestParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex) && httpReq.getRequestParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex).trim().length()>0)
			{
				String mask=httpReq.getRequestParameter(fixedCompID+"_PIECE_MASK_"+posDex);
				String str=httpReq.getRequestParameter(fixedCompID+"_PIECE_STRING_"+posDex);
				String amt=httpReq.getRequestParameter(fixedCompID+"_PIECE_AMOUNT_"+posDex);
				String conn=httpReq.getRequestParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex);
				String loc=httpReq.getRequestParameter(fixedCompID+"_PIECE_LOCATION_"+posDex);
				String type=httpReq.getRequestParameter(fixedCompID+"_PIECE_TYPE_"+posDex);
				String consumed=httpReq.getRequestParameter(fixedCompID+"_PIECE_CONSUMED_"+posDex);
				if(!conn.equalsIgnoreCase("DELETE"))
				{
					AbilityComponent able=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
					able.setAmount(CMath.s_int(amt));
					if(posDex==1)
						able.setConnector(AbilityComponent.CompConnector.AND);
					else
						able.setConnector(AbilityComponent.CompConnector.valueOf(conn));
					able.setConsumed((consumed!=null)&&(consumed.equalsIgnoreCase("on")||consumed.equalsIgnoreCase("checked")));
					able.setLocation(AbilityComponent.CompLocation.valueOf(loc));
					able.setMask(mask);
					able.setType(AbilityComponent.CompType.valueOf(type), str);
					set.add(able);
				}
				posDex++;
			}
			
			if(CMLib.ableMapper().getAbilityComponentMap().containsKey(last.toUpperCase().trim()))
			{
				List<AbilityComponent> xset=CMLib.ableMapper().getAbilityComponentMap().get(last.toUpperCase().trim());
				xset.clear();
				xset.addAll(set);
			}
			else
				CMLib.ableMapper().getAbilityComponentMap().put(last.toUpperCase().trim(),set);
			CMLib.ableMapper().alterAbilityComponentFile(last.toUpperCase().trim(),false);
		}
		return "";
	}
}
