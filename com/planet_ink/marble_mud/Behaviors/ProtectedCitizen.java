package com.planet_ink.marble_mud.Behaviors;
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
import com.planet_ink.marble_mud.Libraries.interfaces.TrackingLibrary;
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
public class ProtectedCitizen extends ActiveTicker
{
	public String ID(){return "ProtectedCitizen";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	protected static String zapper=null;
	protected static String defcityguard="cityguard";
	protected static String[] defclaims={"Help! I'm being attacked!","Help me!!"};
	protected String cityguard=null;
	protected String[] claims=null;
	protected int radius=7;
	protected int maxAssistance=1;
	protected boolean wander=false;

	public ProtectedCitizen()
	{
		super();
		minTicks=1; maxTicks=3; chance=99; radius=7; maxAssistance=1;
		tickReset();
	}

	public String accountForYourself()
	{ 
		return "whiney citizen";
	}
	
	public void setParms(String parms)
	{
		super.setParms(parms);
		cityguard=null;
		zapper=null;
		wander=parms.toUpperCase().indexOf("WANDER")>=0;
		radius=CMParms.getParmInt(parms,"radius",radius);
		maxAssistance=CMParms.getParmInt(parms,"maxassists",maxAssistance);
		claims=null;
	}

	public String getCityguardName()
	{
		if(cityguard!=null) return "-NAME \"+"+cityguard+"\"";
		if(zapper!=null) return zapper;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		char c=';';
		int x=s.indexOf(c);
		if(x<0){ c='/'; x=s.indexOf(c);}
		if(x<0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		cityguard=s.substring(0,x).trim();
		if(cityguard.length()==0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		if((cityguard.indexOf('+')>0)
		||(cityguard.indexOf('-')>0)
		||(cityguard.indexOf('>')>0)
		||(cityguard.indexOf('<')>0)
		||(cityguard.indexOf('=')>0))
		{
			zapper=cityguard;
			cityguard=null;
			return zapper;
		}
		return cityguard;
	}

	public String[] getClaims()
	{
		if(claims!=null) return claims;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ claims=defclaims; return claims;}

		char c=';';
		int x=s.indexOf(c);
		if(x<0){ c='/'; x=s.indexOf(c);}
		if(x<0)
		{ claims=defclaims; return claims;}
		s=s.substring(x+1).trim();
		if(s.length()==0)
		{ claims=defclaims; return claims;}
		Vector V=new Vector();
		x=s.indexOf(c);
		while(x>=0)
		{
			String str=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if(str.length()>0)V.addElement(str);
			x=s.indexOf(c);
		}
		if(s.length()>0)V.addElement(s);
		claims=new String[V.size()];
		for(int i=0;i<V.size();i++)
			claims[i]=(String)V.elementAt(i);
		return claims;
	}

	public boolean assistMOB(MOB mob)
	{
		int assistance=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(M.getVictim()==mob.getVictim()))
			   assistance++;
		}
		if(assistance>=maxAssistance)
			return false;

		String claim=getClaims()[CMLib.dice().roll(1,getClaims().length,-1)].trim();
		if(claim.startsWith(","))
			mob.doCommand(CMParms.parse("EMOTE \""+claim.substring(1).trim()+"\""),Command.METAFLAG_FORCED);
		else
			mob.doCommand(CMParms.parse("YELL \""+claim+"\""),Command.METAFLAG_FORCED);

		Room thisRoom=mob.location();
		Vector V=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY);
		if(!wander) flags.plus(TrackingLibrary.TrackingFlag.AREAONLY);
		CMLib.tracking().getRadiantRooms(thisRoom,V,flags,null,radius,null);
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			MOB M=null;
			if(R.getArea().Name().equals(mob.location().getArea().Name()))
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M2=R.fetchInhabitant(i);
					if((M2!=null)
					&&(M2.mayIFight(mob.getVictim()))
					&&(M2!=mob.getVictim())
					&&(CMLib.flags().aliveAwakeMobileUnbound(M2,true)
					&&(!M2.isInCombat())
					&&(CMLib.flags().isMobile(M2))
					&&(CMLib.masking().maskCheck(getCityguardName(),M2,false))
					&&(!BrotherHelper.isBrother(mob.getVictim(),M2,false))
					&&(canFreelyBehaveNormal(M2))
					&&(!CMLib.flags().isATrackingMonster(M2))
					&&(CMLib.flags().canHear(M2))))
					{
						M=M2; break;
					}
				}
			if(M!=null)
			{
				if(R==mob.location())
					CMLib.combat().postAttack(M,mob.getVictim(),M.fetchWieldedItem());
				else
				{
					int dir=CMLib.tracking().radiatesFromDir(R,V);
					if(dir>=0)
						CMLib.tracking().walk(M,dir,false,false);
				}
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			if((ticking instanceof MOB)&&(((MOB)ticking).isInCombat()))
				assistMOB((MOB)ticking);
		}
		return true;
	}
}
