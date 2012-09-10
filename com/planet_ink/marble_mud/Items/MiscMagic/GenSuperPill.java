package com.planet_ink.marble_mud.Items.MiscMagic;
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
public class GenSuperPill extends GenPill implements ArchonOnly
{
	public String ID(){	return "GenSuperPill";}
	public GenSuperPill()
	{
		super();

		setName("a pill");
		basePhyStats.setWeight(1);
		setDisplayText("A strange pill lies here.");
		setDescription("Large and round, with strange markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_CORN;
	}


	public boolean isGeneric(){return true;}

	public String secretIdentity()
	{
		String tx=StdScroll.makeSecretIdentity("super pill",super.secretIdentity(),"",getSpells());
		String id=readableText;
		int x=id.toUpperCase().indexOf("ARM");
		for(StringBuffer ID=new StringBuffer(id);((x>0)&&(x<id.length()));x++)
			if(id.charAt(x)=='-')
			{
				ID.setCharAt(x,'+');
				id=ID.toString();
				break;
			}
			else
			if(id.charAt(x)=='+')
			{
				ID.setCharAt(x,'-');
				id=ID.toString();
				break;
			}
			else
			if(Character.isDigit(id.charAt(x)))
				break;
		x=id.toUpperCase().indexOf("DIS");
		if(x>=0)
		{
			long val=CMParms.getParmPlus(id,"dis");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.IS_VERBS.length;num++)
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.IS_VERBS[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
		x=id.toUpperCase().indexOf("SEN");
		if(x>=0)
		{
			long val=CMParms.getParmPlus(id,"sen");
			int y=id.indexOf(""+val,x);
			if((val!=0)&&(y>x))
			{
				StringBuffer middle=new StringBuffer("");
				for(int num=0;num<PhyStats.CAN_SEE_VERBS.length;num++)
					if(CMath.bset(val,CMath.pow(2,num)))
						middle.append(PhyStats.CAN_SEE_VERBS[num]+" ");
				id=id.substring(0,x)+middle.toString().trim()+id.substring(y+((""+val).length()));
			}
		}
		return tx+"\n("+id+")\n";
	}

	public void EATME(MOB mob)
	{
		boolean redress=false;
		if(getSpells().size()>0)
			eatIfAble(mob);
		if((CMParms.getParmPlus(readableText,"beacon")>0)
		&&(mob.location()!=null))
			mob.setStartRoom(mob.location());
		mob.basePhyStats().setAbility(mob.basePhyStats().ability()+CMParms.getParmPlus(readableText,"abi"));
		mob.basePhyStats().setArmor(mob.basePhyStats().armor()+CMParms.getParmPlus(readableText,"arm"));
		mob.basePhyStats().setAttackAdjustment(mob.basePhyStats().attackAdjustment()+CMParms.getParmPlus(readableText,"att"));
		mob.basePhyStats().setDamage(mob.basePhyStats().damage()+CMParms.getParmPlus(readableText,"dam"));
		mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|CMParms.getParmPlus(readableText,"dis"));
		mob.basePhyStats().setLevel(mob.basePhyStats().level()+CMParms.getParmPlus(readableText,"lev"));
		mob.basePhyStats().setRejuv(mob.basePhyStats().rejuv()+CMParms.getParmPlus(readableText,"rej"));
		mob.basePhyStats().setSensesMask(mob.basePhyStats().sensesMask()|CMParms.getParmPlus(readableText,"sen"));
		mob.basePhyStats().setSpeed(mob.basePhyStats().speed()+CMParms.getParmPlus(readableText,"spe"));
		mob.basePhyStats().setWeight(mob.basePhyStats().weight()+CMParms.getParmPlus(readableText,"wei"));
		if(CMParms.getParmPlus(readableText,"wei")!=0) redress=true;
		mob.basePhyStats().setHeight(mob.basePhyStats().height()+CMParms.getParmPlus(readableText,"hei"));
		if(CMParms.getParmPlus(readableText,"hei")!=0) redress=true;

		String val=CMParms.getParmStr(readableText,"gen","").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			mob.baseCharStats().setStat(CharStats.STAT_GENDER,val.charAt(0));
		val=CMParms.getParmStr(readableText,"cla","").toUpperCase();
		if((val.length()>0)&&(CMClass.findCharClass(val)!=null)&&(!val.equalsIgnoreCase("Archon")))
		{
			mob.baseCharStats().setCurrentClass(CMClass.findCharClass(val));
			if((!mob.isMonster())&&(mob.soulMate()==null))
				CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_CLASSCHANGE);
		}
		if(CMParms.getParmPlus(readableText,"lev")!=0)
			mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+CMParms.getParmPlus(readableText,"lev"));
		val=CMParms.getParmStr(readableText,"rac","").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			redress=true;
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		for(int i : CharStats.CODES.BASE())
		{
			mob.baseCharStats().setStat(i,mob.baseCharStats().getStat(i)+CMParms.getParmPlus(readableText,CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3)));
			int max = CharStats.CODES.toMAXBASE(i);
			mob.baseCharStats().setStat(max,mob.baseCharStats().getStat(max)+CMParms.getParmPlus(readableText,"max"+CMStrings.limit(CharStats.CODES.NAME(i).toLowerCase(),3)));
		}

		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+CMParms.getParmPlus(readableText,"hit"));
		mob.curState().setHunger(mob.curState().getHunger()+CMParms.getParmPlus(readableText,"hun"));
		mob.baseState().setMana(mob.baseState().getMana()+CMParms.getParmPlus(readableText,"man"));
		mob.baseState().setMovement(mob.baseState().getMovement()+CMParms.getParmPlus(readableText,"mov"));
		mob.curState().setThirst(mob.curState().getThirst()+CMParms.getParmPlus(readableText,"thi"));

		mob.setPractices(mob.getPractices()+CMParms.getParmPlus(readableText,"prac"));
		mob.setTrains(mob.getTrains()+CMParms.getParmPlus(readableText,"trai"));
		mob.setQuestPoint(mob.getQuestPoint()+CMParms.getParmPlus(readableText,"ques"));
		int newMoney=CMParms.getParmPlus(readableText,"coin");
		if(newMoney!=0) CMLib.beanCounter().setMoney(mob,CMLib.beanCounter().getMoney(mob)+newMoney);
		int exp=CMParms.getParmPlus(readableText,"expe");
		if(exp!=0) CMLib.leveler().postExperience(mob,null,null,exp,false);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		if(redress)	CMLib.utensils().confirmWearability(mob);
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					EATME(mob);
					super.executeMsg(myHost,msg);
				}
				else
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}
}
