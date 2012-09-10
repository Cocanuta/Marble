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
public class GenAmmunition extends StdItem implements Ammunition
{
	public String ID(){	return "GenAmmunition";}
	protected String	readableText="";
	public GenAmmunition()
	{
		super();

		setName("a batch of arrows");
		setDisplayText("a generic batch of arrows sits here.");
		setUsesRemaining(100);
		setAmmunitionType("arrows");
		setDescription("");
		recoverPhyStats();
	}

	public boolean isGeneric(){return true;}

	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text)
	{
		if(isReadable()) CMLib.flags().setReadable(this,false);
		readableText=text;
	}
	public String ammunitionType(){return readableText;}
	public void setAmmunitionType(String text){readableText=text;}

	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return super.okMessage(myHost,msg);
		else
		if(msg.targetMinor()==CMMsg.NO_EFFECT)
			return super.okMessage(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			mob.tell("You can't hold "+name()+".");
			return false;
		case CMMsg.TYP_WEAR:
			mob.tell("You can't wear "+name()+".");
			return false;
		case CMMsg.TYP_WIELD:
			mob.tell("You can't wield "+name()+" as a weapon.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
