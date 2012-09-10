package com.planet_ink.marble_mud.Abilities.Properties;
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
import com.planet_ink.marble_mud.Libraries.interfaces.MaskingLibrary;
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
public class Prop_EnterAdjuster extends Property implements TriggeredAffect
{
	public String ID() { return "Prop_EnterAdjuster"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected MaskingLibrary.CompiledZapperMask mask=MaskingLibrary.CompiledZapperMask.EMPTY();
	protected String[] parameters=new String[]{"",""};

	public long flags(){return Ability.FLAG_ADJUSTER;}

	public int triggerMask()
	{ 
		return TriggeredAffect.TRIGGER_ENTER;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		parameters=CMLib.masking().separateMaskStrs(text());
		if(parameters[1].trim().length()==0)
			mask=MaskingLibrary.CompiledZapperMask.EMPTY();
		else
			mask=CMLib.masking().getPreCompiledMask(parameters[1]);
	}
	
	public String accountForYourself()
	{ 
		return Prop_HaveAdjuster.fixAccoutingsWithMask("Affects those who enter: "+parameters[0],parameters[1]);
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.targetMinor()==CMMsg.TYP_ENTER)&&((affected instanceof Room)||(affected instanceof Exit)))
		   ||((msg.targetMinor()==CMMsg.TYP_SIT)&&(affected==msg.target())&&(affected instanceof Rideable)))
		&&((mask==null)||(CMLib.masking().maskCheck(mask,msg.source(),true))))
		{
			MOB mob=msg.source();
			Vector theSpells=new Vector();
			int del=parameters[0].indexOf(';');
			while(del>=0)
			{
				String thisOne=parameters[0].substring(0,del);
				if((thisOne.length()>0)&&(!thisOne.equals(";")))
				{
					Ability A=CMClass.getAbility(thisOne);
					if((A!=null)&&(!CMLib.ableMapper().classOnly("Archon",A.ID())))
					{
						A=(Ability)A.copyOf();
						theSpells.addElement(A);
					}
				}
				parameters[0]=parameters[0].substring(del+1);
				del=parameters[0].indexOf(';');
			}
			if((parameters[0].length()>0)&&(!parameters[0].equals(";")))
			{
				Ability A=CMClass.getAbility(parameters[0]);
				if(A!=null)
				{
					A=(Ability)A.copyOf();
					theSpells.addElement(A);
				}
			}
			for(int i=0;i<theSpells.size();i++)
			{
				Ability thisOne=(Ability)((Ability)theSpells.elementAt(i)).copyOf();
				thisOne.invoke(mob,mob,true,0);
			}
			
			mob.basePhyStats().setAbility(mob.basePhyStats().ability()+CMParms.getParmPlus(parameters[0],"abi"));
			mob.basePhyStats().setArmor(mob.basePhyStats().armor()+CMParms.getParmPlus(parameters[0],"arm"));
			mob.basePhyStats().setAttackAdjustment(mob.basePhyStats().attackAdjustment()+CMParms.getParmPlus(parameters[0],"att"));
			mob.basePhyStats().setDamage(mob.basePhyStats().damage()+CMParms.getParmPlus(parameters[0],"dam"));
			mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|CMParms.getParmPlus(parameters[0],"dis"));
			mob.basePhyStats().setLevel(mob.basePhyStats().level()+CMParms.getParmPlus(parameters[0],"lev"));
			mob.basePhyStats().setRejuv(mob.basePhyStats().rejuv()+CMParms.getParmPlus(parameters[0],"rej"));
			mob.basePhyStats().setSensesMask(mob.basePhyStats().sensesMask()|CMParms.getParmPlus(parameters[0],"sen"));
			mob.basePhyStats().setSpeed(mob.basePhyStats().speed()+CMParms.getParmPlus(parameters[0],"spe"));
			mob.basePhyStats().setWeight(mob.basePhyStats().weight()+CMParms.getParmPlus(parameters[0],"wei"));
			mob.basePhyStats().setHeight(mob.basePhyStats().height()+CMParms.getParmPlus(parameters[0],"hei"));

			mob.baseCharStats().setStat(CharStats.STAT_CHARISMA,mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)+CMParms.getParmPlus(parameters[0],"cha"));
			mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)+CMParms.getParmPlus(parameters[0],"con"));
			mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)+CMParms.getParmPlus(parameters[0],"dex"));
			String val=CMParms.getParmStr(parameters[0],"gen","").toUpperCase();
			if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
				mob.baseCharStats().setStat(CharStats.STAT_GENDER,val.charAt(0));
			mob.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)+CMParms.getParmPlus(parameters[0],"int"));
			val=CMParms.getParmStr(parameters[0],"cla","").toUpperCase();
			if((val.length()>0)&&(CMClass.findCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
				mob.baseCharStats().setCurrentClass(CMClass.findCharClass(val));
			val=CMParms.getParmStr(parameters[0],"rac","").toUpperCase();
			if((val.length()>0)&&(CMClass.getRace(val)!=null))
			{
				int oldCat=mob.baseCharStats().ageCategory();
				mob.baseCharStats().setMyRace(CMClass.getRace(val));
				mob.baseCharStats().getMyRace().startRacing(mob,false);
				if(mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
					mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.baseCharStats().getMyRace().getAgingChart()[oldCat]);
			}
			mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)+CMParms.getParmPlus(parameters[0],"str"));
			mob.baseCharStats().setStat(CharStats.STAT_WISDOM,mob.baseCharStats().getStat(CharStats.STAT_WISDOM)+CMParms.getParmPlus(parameters[0],"wis"));
			if(CMParms.getParmPlus(parameters[0],"lev")!=0)
				mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+CMParms.getParmPlus(parameters[0],"lev"));

			mob.baseState().setHitPoints(mob.curState().getHitPoints()+CMParms.getParmPlus(parameters[0],"hit"));
			mob.curState().setHunger(mob.curState().getHunger()+CMParms.getParmPlus(parameters[0],"hun"));
			mob.curState().setMana(mob.curState().getMana()+CMParms.getParmPlus(parameters[0],"man"));
			mob.curState().setMovement(mob.curState().getMovement()+CMParms.getParmPlus(parameters[0],"mov"));
			mob.curState().setThirst(mob.curState().getThirst()+CMParms.getParmPlus(parameters[0],"thi"));

			mob.setPractices(mob.getPractices()+CMParms.getParmPlus(parameters[0],"prac"));
			mob.setTrains(mob.getTrains()+CMParms.getParmPlus(parameters[0],"trai"));
			mob.setQuestPoint(mob.getQuestPoint()+CMParms.getParmPlus(parameters[0],"ques"));
			int newMoney=CMParms.getParmPlus(parameters[0],"coin");
			if(newMoney!=0) CMLib.beanCounter().setMoney(mob,CMLib.beanCounter().getMoney(mob)+newMoney);
			int exp=CMParms.getParmPlus(parameters[0],"expe");
			if(exp>0) CMLib.leveler().postExperience(mob,null,null,exp,false);
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			CMLib.utensils().confirmWearability(mob);
		}
		return super.okMessage(myHost,msg);
	}
}
