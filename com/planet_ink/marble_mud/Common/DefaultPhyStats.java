package com.planet_ink.marble_mud.Common;
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
import java.util.Vector;

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
public class DefaultPhyStats implements PhyStats
{
	public String ID(){return "DefaultPhyStats";}
	protected int[] DEFAULT_STATS={0,0,100,0,0,0,0,0,0,0};
	protected int[] stats=DEFAULT_STATS.clone();
	protected double Speed=1.0;			// should be positive
	protected String replacementName=null;
	protected String[] ambiances=null;
	private final static String[] empty=new String[0];

	public DefaultPhyStats(){}
	public void setAllValues(int def)
	{
		for(int i=0;i<NUM_STATS;i++)
			stats[i]=def;
		Speed=(double)def;
	}

	public void reset()
	{
		for(int i=0;i<DEFAULT_STATS.length;i++)
			stats[i]=DEFAULT_STATS[i];
		Speed=1.0;
		replacementName=null;
		ambiances=null;
	}
	
	public int sensesMask(){return stats[STAT_SENSES];}
	public int disposition(){return stats[STAT_DISPOSITION];}
	public int level(){return stats[STAT_LEVEL];}
	public int ability(){return stats[STAT_ABILITY];}
	public int rejuv(){return stats[STAT_REJUV];}
	public int weight(){return stats[STAT_WEIGHT];}
	public int height(){return stats[STAT_HEIGHT];}
	public int armor(){return stats[STAT_ARMOR];}
	public int damage(){return stats[STAT_DAMAGE];}
	public double speed(){return Speed;}
	public int attackAdjustment(){return stats[STAT_ATTACK];}
	public String newName(){ return replacementName;}
	public String[] ambiances(){ return (ambiances==null)?empty:ambiances;}

	public void setRejuv(int newRejuv){stats[STAT_REJUV]=newRejuv;}
	public void setLevel(int newLevel){stats[STAT_LEVEL]=newLevel;}
	public void setArmor(int newArmor){stats[STAT_ARMOR]=newArmor;}
	public void setDamage(int newDamage){stats[STAT_DAMAGE]=newDamage;}
	public void setWeight(int newWeight){stats[STAT_WEIGHT]=newWeight;}
	public void setSpeed(double newSpeed){Speed=newSpeed;}
	public void setAttackAdjustment(int newAdjustment){stats[STAT_ATTACK]=newAdjustment;}
	public void setAbility(int newAdjustment){stats[STAT_ABILITY]=newAdjustment;}
	public void setDisposition(int newDisposition){stats[STAT_DISPOSITION]=newDisposition;}
	public void setSensesMask(int newMask){stats[STAT_SENSES]=newMask;}
	public void setHeight(int newHeight){stats[STAT_HEIGHT]=newHeight;}
	public void setName(String newName){ replacementName=newName;}
	public String getCombatStats(){return "L"+stats[STAT_LEVEL]+":A"+stats[STAT_ARMOR]+":K"+stats[STAT_ATTACK]+":D"+stats[STAT_DAMAGE];}
	public void addAmbiance(String ambiance)
	{
		ambiance=ambiance.trim();
		String[] ambis=ambiances();
		for(int i=0;i<ambis.length;i++)
			if(ambis[i].equalsIgnoreCase(ambiance))
				return;
		ambiances=CMParms.toStringArray(CMParms.parseCommas(CMParms.toStringList(ambis)+","+ambiance,true));
	}
	public void delAmbiance(String ambiance)
	{
		ambiance=ambiance.trim();
		int i=0;
		String[] ambis=ambiances();
		Vector<String> V=null;
		for(i=0;i<ambis.length;i++)
			if(ambis[i].equalsIgnoreCase(ambiance))
			{
				if(ambis.length==1)
					ambiances=null;
				else
				{
					V=CMParms.parseCommas(CMParms.toStringList(ambis),true);
					if(V.size()==ambis.length)
						V.removeElementAt(i);
					ambiances=CMParms.toStringArray(V);
				}
				return;
			}
	}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPhyStats();}}
	public void initializeClass(){}
	public void copyInto(PhyStats intoStats)
	{
		if(intoStats instanceof DefaultPhyStats)
		{
			for(int i=0;i<NUM_STATS;i++)
				((DefaultPhyStats)intoStats).stats[i]=stats[i];
			((DefaultPhyStats)intoStats).Speed=Speed;
			((DefaultPhyStats)intoStats).ambiances=ambiances;
			((DefaultPhyStats)intoStats).replacementName=replacementName;
		}
		else
		for(int i=0;i<getStatCodes().length;i++)
			intoStats.setStat(getStatCodes()[i],getStat(getStatCodes()[i]));
	}
	public CMObject copyOf()
	{
		try
		{
			DefaultPhyStats E=(DefaultPhyStats)this.clone();
			E.stats=(int[])E.stats.clone();
			return E;
		}
		catch(java.lang.CloneNotSupportedException e)
		{
			return new DefaultPhyStats();
		}
	}
	private final static String[] CODES={
		"SENSES","DISPOSITION","LEVEL",
		"ABILITY","REJUV","WEIGHT","HEIGHT",
		"ARMOR","DAMAGE","ATTACK", "AMBIANCES"};
	public int getSaveStatIndex(){return getStatCodes().length;}
	public String[] getStatCodes(){return CODES;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(PhyStats E){
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
			   return false;
		return true;
	}

	public void setStat(String code, String val)
	{
		switch(getCodeNum(code)){
		case 0: setSensesMask(CMath.s_parseIntExpression(val)); break;
		case 1: setDisposition(CMath.s_parseIntExpression(val)); break;
		case 2: setLevel(CMath.s_parseIntExpression(val)); break;
		case 3: setAbility(CMath.s_parseIntExpression(val)); break;
		case 4: setRejuv(CMath.s_parseIntExpression(val)); break;
		case 5: setWeight(CMath.s_parseIntExpression(val)); break;
		case 6: setHeight(CMath.s_parseIntExpression(val)); break;
		case 7: setArmor(CMath.s_parseIntExpression(val)); break;
		case 8: setDamage(CMath.s_parseIntExpression(val)); break;
		case 9: setAttackAdjustment(CMath.s_parseIntExpression(val)); break;
		case 10: ambiances=(val.trim().length()==0)?null:CMParms.toStringArray(CMParms.parseCommas(val,true)); break;
		}
	}
	public String getStat(String code)
	{
		switch(getCodeNum(code)){
		case 0: return ""+sensesMask();
		case 1: return ""+disposition();
		case 2: return ""+level();
		case 3: return ""+ability();
		case 4: return ""+rejuv();
		case 5: return ""+weight();
		case 6: return ""+height();
		case 7: return ""+armor();
		case 8: return ""+damage();
		case 9: return ""+attackAdjustment();
		case 10: return CMParms.toStringList(ambiances());
		default: return "";
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
