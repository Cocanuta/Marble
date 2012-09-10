package com.planet_ink.marble_mud.Items.Weapons;
import java.util.List;

import com.planet_ink.marble_mud.Items.MiscMagic.StdWand;
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
public class GenStaff extends GenWeapon implements Wand
{
	public String ID(){    return "GenStaff";}
	protected String secretWord=CMProps.getAnyListFileValue(CMProps.SYSTEMLF_MAGIC_WORDS);

	public GenStaff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("");
		secretIdentity="";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(4);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(4);
		baseGoldValue=1;
		recoverPhyStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_OAK;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		recoverPhyStats();
	}

	protected int maxUses=Integer.MAX_VALUE;
	public int maxUses(){return maxUses;}
	public void setMaxUses(int newMaxUses){maxUses=newMaxUses;}
	
	public boolean isGeneric(){return true;}

	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}
	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){ readableText=text;secretWord=StdWand.getWandWord(readableText);}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(readableText());
	}

	public String magicWord()
	{
		return secretWord;
	}
	public void waveIfAble(MOB mob,
						   Physical afftarget,
						   String message)
	{
		StdWand.waveIfAble(mob,afftarget,message,this);
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this)&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
				StdWand.waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage(),this);
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)&&(!amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers =msg.trailerMsgs();
				if(trailers!=null)
					for(CMMsg msg2 : trailers)
						if(msg2.targetMinor()==CMMsg.TYP_WAND_USE)
							alreadyWanding=true;
				if(!alreadyWanding)
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,CMStrings.getSayFromMessage(msg.sourceMessage()),CMMsg.NO_EFFECT,null));
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}
	// wand stats handled by genweapon, filled by readableText
}
