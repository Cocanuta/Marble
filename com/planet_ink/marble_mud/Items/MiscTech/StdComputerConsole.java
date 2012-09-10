package com.planet_ink.marble_mud.Items.MiscTech;
import com.planet_ink.marble_mud.Items.Basic.StdRideable;
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
public class StdComputerConsole extends StdRideable 
	implements Electronics, ShipComponent, Electronics.ElecPanel
{
	public String ID(){	return "StdShipConsole";}
	public StdComputerConsole()
	{
		super();
		setName("a computer console");
		basePhyStats.setWeight(20);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		rideBasis=Rideable.RIDEABLE_TABLE;
		riderCapacity=1;
		basePhyStats.setSensesMask(basePhyStats.sensesMask()|PhyStats.SENSE_ITEMREADABLE);
		setLidsNLocks(true,true,false,false);
		capacity=500;
		material=RawMaterial.RESOURCE_STEEL;
		recoverPhyStats();
	}

	public int fuelType(){return RawMaterial.RESOURCE_ENERGY;}
	public void setFuelType(int resource){}
	public long powerCapacity(){return 1;}
	public void setPowerCapacity(long capacity){}
	public long powerRemaining(){return 1;}
	public void setPowerRemaining(long remaining){}
	protected boolean activated=false;
	public boolean activated(){return activated;}
	public void activate(boolean truefalse){activated=truefalse;}
	
	protected ElecPanelType panelType=Electronics.ElecPanel.ElecPanelType.COMPUTER;
	public ElecPanelType panelType(){return panelType;}
	public void setPanelType(ElecPanelType type){panelType=type;}
	
	protected long lastSoftwareCheck=0;
	protected List<Software> software=null;
	
	protected String currentMenu="";
	
	public boolean canContain(Environmental E)
	{
		return E instanceof Software;
	}

	public String readableText()
	{
		final StringBuilder str=new StringBuilder(super.readableText());
		if(str.length()>0) str.append("\n\r");
		if(!activated())
			str.append("The screen is blank.  Try ACTIVATEing it first.");
		else
		{
			if((software==null)||(System.currentTimeMillis()-lastSoftwareCheck)>(60*1000))
			{
				final List<Item> list=getContents();
				final LinkedList<Software> softwareList=new LinkedList<Software>();
				for(Item I : list)
					if(I instanceof Software)
						softwareList.add((Software)I);
				lastSoftwareCheck=System.currentTimeMillis();
				software=softwareList;
			}
			for(final Software S : software)
				if(S.getInternalName().equals(currentMenu))
					str.append(S.readableText());
				else
				if(S.getParentMenu().equals(currentMenu))
					str.append(S.getActivationString()).append(": ").append(S.getActivationDescription()).append("\n\r");
		}
		
		return str.toString();
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
				return true;
			case CMMsg.TYP_ACTIVATE:
				if((msg.targetMessage()==null)&&(activated()))
				{
					msg.source().tell(name()+" is already booted up.");
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.targetMessage()==null)&&(!activated()))
				{
					msg.source().tell(name()+" is already shut down.");
					return false;
				}
				break;
			}
		}
		return super.okMessage(host,msg);
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
				lastSoftwareCheck=0;
				break;
			case CMMsg.TYP_ACTIVATE:
				if(!activated())
				{
					activate(true);
					msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> boot(s) up <T-NAME>.");
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(activated())
				{
					activate(false);
					msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shut(s) up <T-NAME>.");
				}
				break;
			}
		}
		super.executeMsg(host,msg);
	}
}
