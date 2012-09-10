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
public class GenPiano extends GenRideable implements MusicalInstrument
{
	public String ID(){	return "GenPiano";}
	public GenPiano()
	{
		super();
		setName("a generic piano");
		setDisplayText("a generic piano sits here.");
		setDescription("");
		baseGoldValue=1015;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		basePhyStats().setWeight(2000);
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=2;
		setMaterial(RawMaterial.RESOURCE_OAK);
	}

	public void recoverPhyStats(){CMLib.flags().setReadable(this,false); super.recoverPhyStats();}
	public int instrumentType(){return CMath.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

}
