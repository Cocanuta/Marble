package com.planet_ink.marble_mud.Commands;
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
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
@SuppressWarnings("rawtypes")
public class Affect extends StdCommand
{
	private final String[] access={"AFFECTS","AFFECT","AFF","AF"};
	public String[] getAccessWords(){return access;}

	public String getMOBState(final MOB mob)
	{
		final StringBuffer msg=new StringBuffer("");
		if((mob.playerStats()!=null)&&(mob.soulMate()==null)&&(mob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
		{
			if(CMSecurity.isASysOp(mob)) 
				mob.playerStats().setHygiene(0);
			else
			{
				int x=(int)(mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
				if(x<=1) msg.append("^!You could use a bath.^?\n\r"); 
				else
				if(x<=3) msg.append("^!You could really use a bath.^?\n\r"); 
				else
				if(x<=7) msg.append("^!You need to bathe, soon.^?\n\r");
				else
				if(x<15) msg.append("^!You desperately need to bathe.^?\n\r");
				else msg.append("^!Your stench is horrendous! Bathe dammit!.^?\n\r");
			}
		}

		if(CMLib.flags().isBound(mob))
			msg.append("^!You are bound.^?\n\r");

		// dont do falling -- the flag doubles for drowning/treading water anyway.
		//if(CMLib.flags().isFalling(mob))
		//    msg.append("^!You are falling!!!^?\n\r");
		//else
		if(CMLib.flags().isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(CMLib.flags().isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(CMLib.flags().isSwimmingInWater(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(CMLib.flags().isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(CMLib.flags().isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");

		if(mob.riding()!=null)
			msg.append("^!You are "+mob.riding().stateString(mob)+" "+mob.riding().name()+".^?\n\r");
		
		if(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
			msg.append("^!Your playerkill flag is on.^?\n\r");

		if(CMLib.flags().isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(CMLib.flags().isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");// ("+CMLib.flags().getHideScore(mob)+").^?\n\r");
		if(CMLib.flags().isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");
		if(CMath.bset(mob.getBitmap(),MOB.ATT_QUIET))
			msg.append("^!You are in QUIET mode.^?\n\r");
		
		if(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)
			msg.append("^!You are fatigued.^?\n\r");
		if(mob.curState().getHunger()<1)
			msg.append("^!You are hungry.^?\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("^!You are thirsty.^?\n\r");
		return msg.toString();
	}
	
	public String getAffects(Session S, Physical P, boolean xtra, boolean autosAlso)
	{
		final StringBuffer msg=new StringBuffer("");
		final int NUM_COLS=2;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(36.0,S);
		int colnum=NUM_COLS;
		MOB mob=(S!=null)?S.mob():null;
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			String disp=A.displayText();
			if(autosAlso && disp.length()==0)
				disp=A.ID()+"+"+A.proficiency();
			if((A!=null)&&(disp.length()>0))
			{
				if(disp.startsWith("(")&&disp.endsWith(")"))
				{
					long tr=A.expirationDate();
					if(A.invoker()!=null) tr=tr-(System.currentTimeMillis()-A.invoker().lastTickedDateTime());
					if(tr<Ability.TICKS_ALMOST_FOREVER)
						disp+=" ^.^N"+CMLib.time().date2EllapsedTime(tr, TimeUnit.SECONDS, true);
				}
				if(xtra)
					disp+=", BY="+((A.invoker()==null)?"N/A":A.invoker().Name());
				String[] disps={disp};
				if(CMStrings.lengthMinusColors(disp)>(COL_LEN*NUM_COLS))
				{
					String s=CMLib.coffeeFilter().fullOutFilter(S,mob,null,null,null,disp,true);
					s=CMStrings.replaceAll(s,"\r","");
					Vector<String> V=CMParms.parseAny(s,'\n',true);
					disps=new String[V.size()];
					for(int d=0;d<V.size();d++)
						disps[d]=(String)V.elementAt(d);
					colnum=NUM_COLS;
				}
				for(int d=0;d<disps.length;d++) 
				{
					disp=disps[d];
					if(((++colnum)>=NUM_COLS)||(CMStrings.lengthMinusColors(disp)>COL_LEN))
					{ 
						msg.append("\n\r"); 
						colnum=0;
					}
					msg.append("^S"+CMStrings.padRightPreserve("^<HELPNAME NAME='"+CMStrings.removeColors(A.name())+"'^>"+disp+"^</HELPNAME^>",COL_LEN));
					if(CMStrings.lengthMinusColors(disp)>COL_LEN) colnum=99;
				}
			}
		}
		msg.append("^N\n\r");
		return msg.toString();
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Session S=mob.session();
		if(S!=null)
		{
			if(CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDMOBS))
			{
				String name=CMParms.combine(commands,1);
				if(name.length()>0)
				{
					Physical P=null;
					if((name.equalsIgnoreCase("here")||(name.equalsIgnoreCase("room"))))
						P=CMLib.map().roomLocation(mob);
					else
					if((name.equalsIgnoreCase("area")||(name.equalsIgnoreCase("zone"))))
						P=CMLib.map().areaLocation(mob);
					else
						P=mob.location().fetchFromMOBRoomFavorsItems(mob,null,name,Wearable.FILTER_ANY);
					if(P==null)
						S.colorOnlyPrint("You don't see "+name+" here.\n\r^N");
					else
					{
						if(S==mob.session())
							S.colorOnlyPrint(" \n\r^!"+P.name()+" is affected by: ^?");
						String msg=getAffects(S,P,true,CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
						if(msg.length()<5)
							S.colorOnlyPrintln("Nothing!\n\r^N");
						else
							S.colorOnlyPrintln(msg);
					}
					return false;
				}
				
			}
			if(S==mob.session())
				S.colorOnlyPrintln("\n\r"+getMOBState(mob)+"\n\r");
			if(S==mob.session())
				S.colorOnlyPrint("^!You are affected by: ^?");
			String msg=getAffects(S,mob,CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS),CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
			if(msg.length()<5)
				S.colorOnlyPrintln("Nothing!\n\r^N");
			else
				S.colorOnlyPrintln(msg);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		Physical target=mob;
		Session S=(mob!=null)?mob.session():null;
		for(Object o : args)
		{
			if(o instanceof Physical)
			{
				if(o instanceof MOB)
					S=((MOB)o).session();
				target=(Physical)o;
			}
		}
		return getAffects(S,target,false,false);
	}
}
