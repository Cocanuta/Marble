package com.planet_ink.marble_mud.WebMacros;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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
public class StatRejuvCharts extends StdWebMacro
{
	public String name()	{return "StatRejuvCharts";}

	protected String getReq(ExternalHTTPRequests httpReq, String tag)
	{
		String s=httpReq.getRequestParameter(tag);
		if(s==null) s="";
		return s;
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		StringBuffer buf=new StringBuffer("");
		String which=httpReq.getRequestParameter("WHICH");
		MOB mob=CMClass.getMOB("StdMOB");
		mob.baseState().setMana(100);
		mob.baseState().setMovement(100);
		mob.baseState().setHitPoints(100);
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.curState().setHunger(1000);
		mob.curState().setThirst(1000);
		if((which!=null)&&(which.equals("HP")))
			buf.append("<BR>Chart: Hit Points<BR>");
		else
		if((which!=null)&&(which.equals("MN")))
			buf.append("<BR>Chart: Mana<BR>");
		else
		if((which!=null)&&(which.equals("MV")))
			buf.append("<BR>Chart: Movement<BR>");
		else
			buf.append("<BR>Chart: Hit Points<BR>");
		buf.append("Flags: ");
		int disposition=0;

		if((getReq(httpReq,"SITTING").length()>0))
		{ disposition=PhyStats.IS_SITTING; buf.append("Sitting ");}
		if((getReq(httpReq,"SLEEPING").length()>0))
		{ disposition=PhyStats.IS_SLEEPING; buf.append("Sleeping ");}
		if((getReq(httpReq,"FLYING").length()>0))
		{ disposition=PhyStats.IS_FLYING; buf.append("Flying ");}
		if((getReq(httpReq,"SWIMMING").length()>0))
		{ disposition=PhyStats.IS_SWIMMING; buf.append("Swimming ");}
		if((getReq(httpReq,"RIDING").length()>0))
		{ mob.setRiding((Rideable)CMClass.getMOB("GenRideable")); buf.append("Riding ");}
		boolean hungry=(httpReq.getRequestParameter("HUNGRY")!=null)&&(httpReq.getRequestParameter("HUNGRY").length()>0);
		if(hungry){ buf.append("Hungry ");		mob.curState().setHunger(0);}
		boolean thirsty=(httpReq.getRequestParameter("THIRSTY")!=null)&&(httpReq.getRequestParameter("THIRSTY").length()>0);
		if(thirsty){ buf.append("Thirsty ");		mob.curState().setThirst(0);}
		mob.basePhyStats().setDisposition(disposition);
		mob.recoverPhyStats();

		buf.append("<P><TABLE WIDTH=100% BORDER=1>");
		buf.append("<TR><TD><B>STATS:</B></TD>");
		for(int stats=4;stats<=25;stats++)
			buf.append("<TD><B>"+stats+"</B></TD>");
		buf.append("</TR>");
		for(int level=1;level<=30;level++)
		{
			buf.append("<TR>");
			buf.append("<TD><B>LVL "+level+"</B></TD>");
			for(int stats=4;stats<=25;stats++)
			{
				for(int c: CharStats.CODES.BASE())
					mob.baseCharStats().setStat(c,stats);
				mob.recoverCharStats();
				mob.basePhyStats().setLevel(level);
				mob.recoverPhyStats();
				mob.curState().setMana(0);
				mob.curState().setMovement(0);
				mob.curState().setHitPoints(0);

				double con=(double)mob.charStats().getStat(CharStats.STAT_CONSTITUTION);
				double man=(double)(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)+mob.charStats().getStat(CharStats.STAT_WISDOM));
				double str=(double)mob.charStats().getStat(CharStats.STAT_STRENGTH);
				if(mob.curState().getHunger()<1)
				{
					con=con*0.85;
					man=man*0.75;
					str=str*0.85;
				}
				if(mob.curState().getThirst()<1)
				{
					con=con*0.85;
					man=man*0.75;
					str=str*0.85;
				}
				if(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)
					man=man*.5;

				double lvl=(double)mob.phyStats().level();
				double lvlby1p5=CMath.div(lvl,1.5);
				//double lvlby2=CMath.div(lvl,2.0);
				//double lvlby3=CMath.div(lvl,3.0);

				double hpGain=(con>1.0)?((con/40.0)*lvlby1p5)+(con/4.5)+2.0:1.0;
				double manaGain=(man>2.0)?((man/80.0)*lvl)+(man/4.5)+2.0:1.0;
				double moveGain=(str>1.0)?((str/40.0)*lvl)+(str/3.0)+5.0:1.0;

				if(CMLib.flags().isSleeping(mob))
				{
					hpGain+=(hpGain/2.0);
					manaGain+=(manaGain/2.0);
					moveGain+=(moveGain/2.0);
					if((mob.riding()!=null)&&(mob.riding() instanceof Item))
					{
						hpGain+=(hpGain/8.0);
						manaGain+=(manaGain/8.0);
						moveGain+=(moveGain/8.0);
					}
				}
				else
				if((CMLib.flags().isSitting(mob))||(mob.riding()!=null))
				{
					hpGain+=(hpGain/4.0);
					manaGain+=(manaGain/4.0);
					moveGain+=(moveGain/4.0);
					if((mob.riding()!=null)&&(mob.riding() instanceof Item))
					{
						hpGain+=(hpGain/8.0);
						manaGain+=(manaGain/8.0);
						moveGain+=(moveGain/8.0);
					}
				}
				else
				{
					if(CMLib.flags().isFlying(mob))
						moveGain+=(moveGain/8.0);
					else
					if(CMLib.flags().isSwimming(mob))
					{
						hpGain-=(hpGain/2.0);
						manaGain-=(manaGain/4.0);
						moveGain-=(moveGain/2.0);
					}
				}

				if((!mob.isInCombat())
				&&(!CMLib.flags().isClimbing(mob)))
				{
					if((hpGain>0)&&(!CMLib.flags().isGolem(mob)))
						mob.curState().adjHitPoints((int)Math.round(hpGain),mob.maxState());
					if(manaGain>0)
						mob.curState().adjMana((int)Math.round(manaGain),mob.maxState());
					if(moveGain>0)
						mob.curState().adjMovement((int)Math.round(moveGain),mob.maxState());
				}

				if((which!=null)&&(which.equals("HP")))
					buf.append("<TD>"+mob.curState().getHitPoints()+"</TD>");
				else
				if((which!=null)&&(which.equals("MN")))
					buf.append("<TD>"+mob.curState().getMana()+"</TD>");
				else
				if((which!=null)&&(which.equals("MV")))
					buf.append("<TD>"+mob.curState().getMovement()+"</TD>");
				else
					buf.append("<TD>"+mob.curState().getHitPoints()+"</TD>");
			}
			buf.append("</TR>");
		}
		mob.destroy();
		buf.append("</TABLE>");
		return clearWebMacros(buf);
	}

}
