package com.planet_ink.marble_mud.Abilities.Common;
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
public class GatheringSkill extends CommonSkill
{
	public String ID() { return "GatheringSkill"; }
	public String name(){ return "GatheringSkill";}
	private static final String[] triggerStrings = {"FLETCH","FLETCHING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "";}
	protected static final Map<String,List<Integer>> supportedResources=new Hashtable();
	
	public GatheringSkill(){super();}
	

	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_WORK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	public List<Integer> myResources()
	{
		if(supportedResources.containsKey(ID()))
			return supportedResources.get(ID());
		String mask=supportedResourceString();
		List<Integer> maskV=new Vector();
		String str=mask;
		while(mask.length()>0)
		{
			str=mask;
			int x=mask.indexOf('|');
			if(x>=0)
			{
				str=mask.substring(0,x);
				mask=mask.substring(x+1);
			}
			else
				mask="";
			if(str.length()>0)
			{
				boolean found=false;
				if(str.startsWith("_"))
				{
					int rsc=RawMaterial.CODES.FIND_IgnoreCase(str.substring(1));
					if(rsc>=0)
					{
						maskV.add(Integer.valueOf(rsc));
    					found=true;
					}
				}
				if(!found)
				{
					List<Integer> notResources=new ArrayList<Integer>();
					int y=str.indexOf('-');
					if(y>0)
					{
    					List<String> restV=CMParms.parseAny(str.substring(y+1),"-",true);
    					str=str.substring(0,y);
    					for(String sv : restV)
    					{
    						 int code=RawMaterial.CODES.FIND_CaseSensitive(sv);
    						 if(code >= 0)
        						 notResources.add(Integer.valueOf(code));
    					}
					}
					int matIndex=CMParms.indexOfIgnoreCase(RawMaterial.MATERIAL_DESCS, str);
					if(matIndex>=0)
					{
						List<Integer> rscs=new XVector<Integer>(RawMaterial.CODES.COMPOSE_RESOURCES(matIndex));
						maskV.addAll(rscs);
						maskV.removeAll(notResources);
						found=rscs.size()>0;
					}
				}
				if(!found)
				{
					int rsc=RawMaterial.CODES.FIND_IgnoreCase(str);
					if(rsc>=0)
						maskV.add(Integer.valueOf(rsc));
				}
			}
		}
		supportedResources.put(ID(),maskV);
		return maskV;
	}
	
	public boolean bundle(MOB mob, Vector what)
	{
		if((what.size()<3)
		||((!CMath.isNumber((String)what.elementAt(1)))&&(!((String)what.elementAt(1)).equalsIgnoreCase("ALL"))))
		{
			commonTell(mob,"You must specify an amount to bundle, followed by what resource to bundle.");
			return false;
		}
		int amount=CMath.s_int((String)what.elementAt(1));
		if(((String)what.elementAt(1)).equalsIgnoreCase("ALL")) amount=Integer.MAX_VALUE;
		if(amount<=0)
		{
			commonTell(mob,amount+" is not an appropriate amount.");
			return false;
		}
		int numHere=0;
		Room R=mob.location();
		if(R==null) return false;
		String name=CMParms.combine(what,2);
		int foundResource=-1;
		Item foundAnyway=null;
		List<Integer> maskV=myResources();
		Hashtable foundAblesH=new Hashtable();
		Ability A=null;
		long lowestNonZeroFoodNumber=Long.MAX_VALUE;
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.getItem(i);
			if(CMLib.english().containsString(I.Name(),name))
			{
				if(foundAnyway==null) foundAnyway=I;
				if((I instanceof RawMaterial)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().enchanted(I))
				&&(I.container()==null)
				&&((I.material()==foundResource)
					||((foundResource<0)&&maskV.contains(Integer.valueOf(I.material())))))
				{
					if((I instanceof Decayable)
					&&(((Decayable)I).decayTime()>0)
					&&(((Decayable)I).decayTime()<lowestNonZeroFoodNumber))
						lowestNonZeroFoodNumber=((Decayable)I).decayTime();
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						A=a.nextElement();
						if((A!=null)
						&&(!A.canBeUninvoked())
						&&(!foundAblesH.containsKey(A.ID())))
							foundAblesH.put(A.ID(),A);
					}
					foundResource=I.material();
					numHere+=I.phyStats().weight();
				}
			}
		}
		if((numHere==0)||(foundResource<0))
		{
			if(foundAnyway!=null)
				commonTell(mob,"You can't bundle "+foundAnyway.name()+" with this skill.");
			else
				commonTell(mob,"You don't see any "+name+" on the ground here.");
			return false;
		}
		if(amount==Integer.MAX_VALUE) amount=numHere;
		if(numHere<amount)
		{
			commonTell(mob,"You only see "+numHere+" pounds of "+name+" on the ground here.");
			return false;
		}
		if(lowestNonZeroFoodNumber==Long.MAX_VALUE)
			lowestNonZeroFoodNumber=0;
		Item I=(Item)CMLib.materials().makeResource(foundResource,Integer.toString(mob.location().domainType()),true,null);
		if(I==null)
		{
			commonTell(mob,"You could not bundle "+name+" due to "+foundResource+" being an invalid resource code.  Bug it!");
			return false;
		}
		I.setName("a "+amount+"# "+RawMaterial.CODES.NAME(foundResource).toLowerCase()+" bundle");
		I.setDisplayText(I.name()+" is here.");
		I.basePhyStats().setWeight(amount);
		if(R.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> create(s) <O-NAME>."))
		{
			int lostValue=CMLib.materials().destroyResources(R,amount,foundResource,-1,I);
			I.setBaseValue(lostValue);
			if(I instanceof Food)
				((Food)I).setNourishment(((Food)I).nourishment()*amount);
			if(I instanceof Drink)
				((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()*amount);
			if((!I.amDestroyed())&&(!R.isContent(I)))
				R.addItem(I,ItemPossessor.Expire.Player_Drop);
		}
		if(I instanceof Decayable)
			((Decayable)I).setDecayTime(lowestNonZeroFoodNumber);
		for(Enumeration e=foundAblesH.keys();e.hasMoreElements();)
			I.addNonUninvokableEffect((Ability)((Environmental)foundAblesH.get(e.nextElement())).copyOf());
		R.recoverRoomStats();
		return true;
	}
	
	
}
