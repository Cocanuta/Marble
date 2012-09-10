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
public class Taxidermy extends CraftingSkill
{
	public String ID() { return "Taxidermy"; }
	public String name(){ return "Taxidermy";}
	private static final String[] triggerStrings = {"STUFF","TAXIDERMY"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "BODIES";}
	public String parametersFormat(){ return "POSE_NAME\nPOSE_DESCRIPTION\n...\n";}

	protected String foundShortName="";

	public Taxidermy()
	{
		super();
		displayText="You are stuffing...";
		verb="stuffing";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"You've messed up stuffing "+foundShortName+"!");
					else
						dropAWinner(mob,building);
				}
			}
		}
		super.unInvoke();
	}

	protected List<List<String>> loadRecipes()
	{
		String filename="taxidermy.txt";
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			V=new Vector();
			StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,true).text();
			List<String> strV=Resources.getFileLineVector(str);
			List<String> V2=null;
			boolean header=true;
			for(int v=0;v<strV.size();v++)
			{
				String s=(String)strV.get(v);
				if(header)
				{
					if((V2!=null)&&(V2.size()>0))
						V.add(V2);
					V2=new Vector();
				}
				if(s.length()==0)
					header=true;
				else
				if(V2 != null)
				{
					V2.add(s);
					header=false;
				}
			}
			if((V2!=null)&&(V2.size()>0))
				V.add(V2);
			if(V.size()==0)
				Log.errOut("Taxidermy","Poses not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}


	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		List<List<String>> POSES=loadRecipes();
		String pose=null;
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			StringBuffer str=new StringBuffer("^xTaxidermy Poses^?^.\n");
			for(int p=0;p<POSES.size();p++)
			{
				List<String> PP=POSES.get(p);
				if(PP.size()>1) 
					str.append(((String)PP.get(0))+"\n");
			}
			mob.tell(str.toString());
			return true;
		}
		else
		if(commands.size()>0)
		{
			for(int p=0;p<POSES.size();p++)
			{
				List<String> PP=POSES.get(p);
				if((PP.size()>1)&&(((String)PP.get(0)).equalsIgnoreCase((String)commands.firstElement())))
				{
					commands.removeElementAt(0);
					pose=(String)PP.get(CMLib.dice().roll(1,PP.size()-1,0));
					break;
				}
			}
		}

		verb="stuffing";
		String str=CMParms.combine(commands,0);
		Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,"You don't see anything called '"+str+"' here.");
			return false;
		}
		foundShortName=I.Name();
		if((!(I instanceof DeadBody))||(((DeadBody)I).playerCorpse())||(((DeadBody)I).mobName().length()==0))
		{
			commonTell(mob,"You don't know how to stuff "+I.name()+".");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().getItem(i);
			if(I2.container()==I)
			{
				commonTell(mob,"You need to remove the contents of "+I2.name()+" first.");
				return false;
			}
		}
		int woodRequired=I.basePhyStats().weight()/5;
		int[] pm={RawMaterial.MATERIAL_CLOTH};
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"cloth stuffing",pm,
											0,null,null,
											false,
											0,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		building=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
		messedUp=!proficiencyCheck(mob,0,auto);
		if(building!=null)    foundShortName=I.Name();
		int duration=15+(woodRequired/3);
		if(duration>65) duration=65;
		duration=getDuration(duration,mob,1,10);
		building=CMClass.getItem("GenItem");
		building.basePhyStats().setWeight(woodRequired);
		String name=((DeadBody)I).mobName();
		String desc=((DeadBody)I).mobDescription();
		I.setMaterial(data[0][FOUND_CODE]);
		building.setName("the stuffed body of "+name);
		CharStats C=(I instanceof DeadBody)?((DeadBody)I).charStats():null;
		if((pose==null)||(C==null))
			building.setDisplayText("the stuffed body of "+name+" stands here");
		else
		{
			pose=CMStrings.replaceAll(pose,"<S-NAME>",building.name());
			pose=CMStrings.replaceAll(pose,"<S-HIS-HER>",C.hisher());
			pose=CMStrings.replaceAll(pose,"<S-HIM-HER>",C.himher());
			pose=CMStrings.replaceAll(pose,"<S-HIM-HERSELF>",C.himher()+"self");
			building.setDisplayText(pose);
		}
		building.setDescription(desc);
		building.setSecretIdentity(getBrand(mob));
		building.recoverPhyStats();
		displayText="You are stuffing "+I.name();
		verb="stuffing "+I.name();
		playSound="scissor.wav";
		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) stuffing "+I.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		I.destroy();
		return true;
	}
}
