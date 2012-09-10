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
public class GenInstrument extends GenItem implements MusicalInstrument
{
	public String ID(){	return "GenInstrument";}
	public GenInstrument()
	{
		super();
		setName("a generic musical instrument");
		basePhyStats.setWeight(12);
		setDisplayText("a generic musical instrument sits here.");
		setDescription("");
		baseGoldValue=15;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_OAK);
	}

	public void recoverPhyStats(){CMLib.flags().setReadable(this,false); super.recoverPhyStats();}
	public int instrumentType(){return CMath.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

	public boolean okMessage(Environmental E, CMMsg msg)
	{
		if(!super.okMessage(E,msg)) return false;
		if(amWearingAt(Wearable.WORN_WIELD)
		   &&(msg.source()==owner())
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(msg.source().location()!=null)
		   &&((msg.tool()==null)
			  ||(msg.tool()==this)
			  ||(!(msg.tool() instanceof Weapon))
			  ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			msg.source().location().show(msg.source(),null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> play(s) <O-NAME>.");
			return false;
		}

		return true;
	}
}
