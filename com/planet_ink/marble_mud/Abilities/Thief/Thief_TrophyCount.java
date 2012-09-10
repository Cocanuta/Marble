package com.planet_ink.marble_mud.Abilities.Thief;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Thief_TrophyCount extends ThiefSkill
{
	public String ID() { return "Thief_TrophyCount"; }
	public String name(){ return "Trophy Count";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private static final String[] triggerStrings = {"TROPHYCOUNT"};
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;}
	Hashtable theList=new Hashtable();

	public String text()
	{
		StringBuffer str=new StringBuffer("<MOBS>");
		for(Enumeration e=theList.elements();e.hasMoreElements();)
		{
			String[] one=(String[])e.nextElement();
			str.append("<MOB>");
			str.append(CMLib.xml().convertXMLtoTag("RACE",one[0]));
			str.append(CMLib.xml().convertXMLtoTag("KILLS",one[1]));
			str.append("</MOB>");
		}
		str.append("</MOBS>");
		return str.toString();
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.tool()!=null)
		&&(msg.tool()==affected))
		{
			Race R=msg.source().charStats().getMyRace();
			if(!R.ID().equalsIgnoreCase("StdRace"))
			{
				String[] set=(String[])theList.get(R.name());
				if(set==null)
				{
					set=new String[4];
					set[0]=R.name();
					set[1]="0";
					theList.put(R.name(),set);
				}
				set[1]=Integer.toString(CMath.s_int(set[1])+1);
				if((affected!=null)&&(affected instanceof MOB))
				{
					Ability A=((MOB)affected).fetchAbility(ID());
					if(A!=null)	A.setMiscText(text());
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	public void setMiscText(String str)
	{
		theList.clear();
		if((str.trim().length()>0)&&(str.trim().startsWith("<MOBS>")))
		{
			List<XMLLibrary.XMLpiece> buf=CMLib.xml().parseAllXML(str);
			List<XMLLibrary.XMLpiece> V=CMLib.xml().getContentsFromPieces(buf,"MOBS");
			if(V!=null)
			for(int i=0;i<V.size();i++)
			{
				XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.get(i);
				if(ablk.tag.equalsIgnoreCase("MOB"))
				{
					String[] one=new String[4];
					one[0]=CMLib.xml().getValFromPieces(ablk.contents,"RACE");
					one[1]=CMLib.xml().getValFromPieces(ablk.contents,"KILLS");
					theList.put(one[0],one);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			StringBuffer str=new StringBuffer("");
			str.append(CMStrings.padRight("Name",20)+"Kills\n\r");
			for(Enumeration e=theList.elements();e.hasMoreElements();)
			{
				String[] one=(String[])e.nextElement();
				int kills=CMath.s_int(one[1]);
				str.append(CMStrings.padRight(one[0],20)+kills+"\n\r");
			}
			if(mob.session()!=null)
				mob.session().rawPrintln(str.toString());
			return true;
		}
		mob.tell("You failed to recall your count.");
		return false;
	}
}
