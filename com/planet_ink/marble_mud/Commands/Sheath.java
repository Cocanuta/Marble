package com.planet_ink.marble_mud.Commands;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Sheath extends StdCommand
{
	public Sheath(){}

	private final String[] access={"SHEATH"};
	public String[] getAccessWords(){return access;}

	public static Vector getSheaths(MOB mob)
	{
		Vector sheaths=new Vector();
		if(mob!=null)
		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem(i);
			if((I!=null)
			&&(!I.amWearingAt(Wearable.IN_INVENTORY))
			&&(I instanceof Container)
			&&(!(I instanceof Drink))
			&&(((Container)I).capacity()>0)
			&&(((Container)I).containTypes()!=Container.CONTAIN_ANYTHING))
				sheaths.addElement(I);
		}
		return sheaths;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean quiet=false;
		boolean noerrors=false;
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("IFPOSSIBLE")))
		{
			commands.removeElementAt(commands.size()-1);
			noerrors=true;
		}

		Item item1=null;
		Item item2=null;
		if(commands.size()>0)
			commands.removeElementAt(0);
		if(commands.size()==0)
		{
			for(int i=0;i<mob.numItems();i++)
			{
				Item I=mob.getItem(i);
				if((I!=null)
				&&(I instanceof Weapon)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
				{
					if(I.amWearingAt(Wearable.WORN_WIELD))
						item1=I;
					else
					if(I.amWearingAt(Wearable.WORN_HELD))
						item2=I;
				}
			}
			if((noerrors)&&(item1==null)&&(item2==null))
				return false;
		}
		Vector sheaths=getSheaths(mob);
		Vector items=new Vector();
		Vector containers=new Vector();
		Item sheathable=null;
		if(commands.size()==0)
		{
			if(item2==item1) item2=null;
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((item1!=null)
				&&(!items.contains(item1))
				&&(sheath.canContain(item1)))
				{
					items.addElement(item1);
					containers.addElement(sheath);
				}
				else
				if((item2!=null)
				&&(!items.contains(item2))
				&&(sheath.canContain(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item2!=null)
			for(int i=0;i<sheaths.size();i++)
			{
				Container sheath=(Container)sheaths.elementAt(i);
				if((sheath.canContain(item2))
				&&(!items.contains(item2)))
				{
					items.addElement(item2);
					containers.addElement(sheath);
				}
			}
			if(item1!=null)	sheathable=item1;
			else
			if(item2!=null)	sheathable=item2;
		}
		else
		{
			commands.insertElementAt("all",0);
			Container container=(Container)CMLib.english().possibleContainer(mob,commands,false,Wearable.FILTER_WORNONLY);
			String thingToPut=CMParms.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
			if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
			if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
			boolean doBugFix = true;
			while(doBugFix || allFlag)
			{
				doBugFix=false;
				Item putThis=mob.fetchWornItem(thingToPut+addendumStr);
				if(putThis==null) break;
				if(((putThis.amWearingAt(Wearable.WORN_WIELD))
				   ||(putThis.amWearingAt(Wearable.WORN_HELD)))
				   &&(putThis instanceof Weapon))
				{
					if(CMLib.flags().canBeSeenBy(putThis,mob)&&(!items.contains(putThis)))
					{
						sheathable=putThis;
						items.addElement(putThis);
						if((container!=null)&&(container.canContain(putThis)))
							containers.addElement(container);
						else
						{
							Container tempContainer=null;
							for(int i=0;i<sheaths.size();i++)
							{
								Container sheath=(Container)sheaths.elementAt(i);
								if(sheath.canContain(putThis))
								{tempContainer=sheath; break;}
							}
							if(tempContainer==null)
								items.remove(putThis);
							else
								containers.addElement(tempContainer);
						}
					}
				}
				addendumStr="."+(++addendum);
			}
		}

		if(items.size()==0)
		{
			if(!noerrors)
				if(sheaths.size()==0)
					mob.tell("You are not wearing an appropriate sheath.");
				else
				if(sheathable!=null)
					mob.tell("You aren't wearing anything you can sheath "+sheathable.name()+" in.");
				else
				if(commands.size()==0)
					mob.tell("You don't seem to be wielding anything you can sheath.");
				else
					mob.tell("You don't seem to be wielding that.");
		}
		else
		for(int i=0;i<items.size();i++)
		{
			Item putThis=(Item)items.elementAt(i);
			Container container=(Container)containers.elementAt(i);
			if(CMLib.commands().postRemove(mob,putThis,true))
			{
				CMMsg putMsg=CMClass.getMsg(mob,container,putThis,CMMsg.MSG_PUT,((quiet?null:"<S-NAME> sheath(s) <O-NAME> in <T-NAME>.")));
				if(mob.location().okMessage(mob,putMsg))
					mob.location().send(mob,putMsg);
			}
		}
		return false;
	}
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),200.0));
	}
	public double combatActionsCost(MOB mob, List<String> cmds)
	{
		return CMProps.getCombatActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),200.0));
	}
	public boolean canBeOrdered(){return true;}

	
}
