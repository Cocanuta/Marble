package com.planet_ink.marble_mud.Commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Go extends StdCommand
{
	public Go(){}

	private final String[] access={"GO","WALK"};
	public String[] getAccessWords(){return access;}
	
	protected Command stander=null;
	protected Vector ifneccvec=null;
	public void standIfNecessary(MOB mob, int metaFlags)
		throws java.io.IOException
	{
		if((ifneccvec==null)||(ifneccvec.size()!=2))
		{
			ifneccvec=new Vector();
			ifneccvec.addElement("STAND");
			ifneccvec.addElement("IFNECESSARY");
		}
		if(stander==null) stander=CMClass.getCommand("Stand");
		if((stander!=null)&&(ifneccvec!=null))
			stander.execute(mob,ifneccvec,metaFlags);
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		standIfNecessary(mob,metaFlags);
		if((commands.size()>3)
		&&(commands.firstElement() instanceof Integer))
		{
			return CMLib.tracking().walk(mob,
						((Integer)commands.elementAt(0)).intValue(),
						((Boolean)commands.elementAt(1)).booleanValue(),
						((Boolean)commands.elementAt(2)).booleanValue(),
						((Boolean)commands.elementAt(3)).booleanValue(),false);

		}
		String whereStr=CMParms.combine(commands,1);
		Room R=mob.location();
		int direction=-1;
		if(whereStr.equalsIgnoreCase("OUT"))
		{
			if(!CMath.bset(R.domainType(),Room.INDOORS))
			{
				mob.tell("You aren't indoors.");
				return false;
			}
			
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if((R.getExitInDir(d)!=null)
				&&(R.getRoomInDir(d)!=null)
				&&(!CMath.bset(R.getRoomInDir(d).domainType(),Room.INDOORS)))
				{
					if(direction>=0)
					{
						mob.tell("Which way out?  Try North, South, East, etc..");
						return false;
					}
					direction=d;
				}
			if(direction<0)
			{
				mob.tell("There is no direct way out of this place.  Try a direction.");
				return false;
			}
		}
		if(direction<0)
			direction=Directions.getGoodDirectionCode(whereStr);
		if(direction<0)
		{
			Environmental E=null;
			if(R!=null)
				E=R.fetchFromRoomFavorItems(null,whereStr);
			if(E instanceof Rideable)
			{
				Command C=CMClass.getCommand("Enter");
				return C.execute(mob,commands,metaFlags);
			}
			if((E instanceof Exit)&&(R!=null))
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(R.getExitInDir(d)==E)
					{ direction=d; break;}
			}
		}
		String doing=(String)commands.elementAt(0);
		if(direction>=0)
			CMLib.tracking().walk(mob,direction,false,false,false,false);
		else
		{
			boolean doneAnything=false;
			if(commands.size()>2)
				for(int v=1;v<commands.size();v++)
				{
					int num=1;
					String s=(String)commands.elementAt(v);
					if(CMath.s_int(s)>0)
					{
						num=CMath.s_int(s);
						v++;
						if(v<commands.size())
							s=(String)commands.elementAt(v);
					}
					else
					if(("NSEWUDnsewud".indexOf(s.charAt(s.length()-1))>=0)
					&&(CMath.s_int(s.substring(0,s.length()-1))>0))
					{
						num=CMath.s_int(s.substring(0,s.length()-1));
						s=s.substring(s.length()-1);
					}

					direction=Directions.getGoodDirectionCode(s);
					if(direction>=0)
					{
						doneAnything=true;
						for(int i=0;i<num;i++)
						{
							if(mob.isMonster())
							{
								if(!CMLib.tracking().walk(mob,direction,false,false,false,false))
									return false;
							}
							else
							{
								Vector V=new Vector();
								V.addElement(doing);
								V.addElement(Directions.getDirectionName(direction));
								mob.enqueCommand(V,metaFlags,0);
							}
						}
					}
					else
						break;
				}
			if(!doneAnything)
				mob.tell(CMStrings.capitalizeAndLower(doing)+" which direction?\n\rTry north, south, east, west, up, or down.");
		}
		return false;
	}
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		double cost=CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);
		if((mob!=null)&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTORUN)))
			cost /= 4.0;
		return CMProps.getActionCost(ID(), cost);
	}
	public boolean canBeOrdered(){return true;}
}
