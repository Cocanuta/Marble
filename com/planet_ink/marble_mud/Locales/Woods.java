package com.planet_ink.marble_mud.Locales;
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
public class Woods extends StdRoom
{
	public String ID(){return "Woods";}
	public Woods()
	{
		super();
		name="the woods";
		basePhyStats.setWeight(3);
		recoverPhyStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_WOODS;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}


	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		&&(!msg.source().isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&(CMLib.dice().rollPercentage()==1)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
		{
			Ability A=CMClass.getAbility("Disease_PoisonIvy");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_WOOD),
		Integer.valueOf(RawMaterial.RESOURCE_PINE),
		Integer.valueOf(RawMaterial.RESOURCE_BALSA),
		Integer.valueOf(RawMaterial.RESOURCE_OAK),
		Integer.valueOf(RawMaterial.RESOURCE_MAPLE),
		Integer.valueOf(RawMaterial.RESOURCE_REDWOOD),
		Integer.valueOf(RawMaterial.RESOURCE_IRONWOOD),
		Integer.valueOf(RawMaterial.RESOURCE_SAP),
		Integer.valueOf(RawMaterial.RESOURCE_YEW),
		Integer.valueOf(RawMaterial.RESOURCE_HICKORY),
		Integer.valueOf(RawMaterial.RESOURCE_TEAK),
		Integer.valueOf(RawMaterial.RESOURCE_CEDAR),
		Integer.valueOf(RawMaterial.RESOURCE_ELM),
		Integer.valueOf(RawMaterial.RESOURCE_CHERRYWOOD),
		Integer.valueOf(RawMaterial.RESOURCE_BEECHWOOD),
		Integer.valueOf(RawMaterial.RESOURCE_WILLOW),
		Integer.valueOf(RawMaterial.RESOURCE_SYCAMORE),
		Integer.valueOf(RawMaterial.RESOURCE_SPRUCE),
		Integer.valueOf(RawMaterial.RESOURCE_FRUIT),
		Integer.valueOf(RawMaterial.RESOURCE_APPLES),
		Integer.valueOf(RawMaterial.RESOURCE_BERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_PEACHES),
		Integer.valueOf(RawMaterial.RESOURCE_CHERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_ORANGES),
		Integer.valueOf(RawMaterial.RESOURCE_LEMONS),
		Integer.valueOf(RawMaterial.RESOURCE_FUR),
		Integer.valueOf(RawMaterial.RESOURCE_NUTS),
		Integer.valueOf(RawMaterial.RESOURCE_HERBS),
		Integer.valueOf(RawMaterial.RESOURCE_HONEY),
		Integer.valueOf(RawMaterial.RESOURCE_VINE),
		Integer.valueOf(RawMaterial.RESOURCE_HIDE),
		Integer.valueOf(RawMaterial.RESOURCE_FEATHERS),
		Integer.valueOf(RawMaterial.RESOURCE_LEATHER)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public List<Integer> resourceChoices(){return Woods.roomResources;}
}
