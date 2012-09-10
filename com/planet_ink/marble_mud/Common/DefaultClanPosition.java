package com.planet_ink.marble_mud.Common;
import java.util.*;

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

public class DefaultClanPosition implements ClanPosition
{
	public String ID(){return "DefaultClanPosition";}
	
	/** the named ID of the position */
	protected String 	ID;
	/** the named ID of the position */
	protected int 		roleID;
	/** the ordered rank of the position */
	protected int 		rank;
	/** the name of the position within this government */
	protected String	name;
	/** the plural name of the position within this government */
	protected String	pluralName;
	/** the maximum number of members that can hold this position */
	protected int		max;
	/** the internal zapper mask for internal requirements to this position */
	protected String	innerMaskStr;
	/** the internal zapper mask for internal requirements to this position */
	protected boolean 	isPublic;
	/** a chart of whether this position can perform the indexed function in this government */
	protected Clan.Authority[] functionChart;
	
	/** return a new instance of the object*/
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClanPosition();}}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			return (ClanPosition)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultClanPosition();
		}
	}
	
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public int getRoleID() {
		return roleID;
	}
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPluralName() {
		return pluralName;
	}
	public void setPluralName(String pluralName) {
		this.pluralName = pluralName;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public String getInnerMaskStr() {
		return innerMaskStr;
	}
	public void setInnerMaskStr(String innerMaskStr) {
		this.innerMaskStr = innerMaskStr;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public Clan.Authority[] getFunctionChart() {
		return functionChart;
	}
	public void setFunctionChart(Clan.Authority[] functionChart) {
		this.functionChart = functionChart;
	}
	
	private static enum POS_STAT_CODES {
		ID,RANK,NAME,PLURALNAME,MAX,INNERMASK,ISPUBLIC,FUNCTIONS
	}
	public String[] getStatCodes() { return CMParms.toStringArray(POS_STAT_CODES.values());}
	public int getSaveStatIndex() { return POS_STAT_CODES.values().length;}
	private POS_STAT_CODES getStatIndex(String code) { return (POS_STAT_CODES)CMath.s_valueOf(POS_STAT_CODES.values(),code); }
	public String getStat(String code) 
	{
		final POS_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return "";}
		switch(stat)
		{
		case NAME: return name;
		case ID: return ID;
		case RANK: return Integer.toString(rank);
		case MAX: return Integer.toString(max);
		case PLURALNAME: return pluralName;
		case INNERMASK: return innerMaskStr;
		case ISPUBLIC: return Boolean.toString(isPublic);
		case FUNCTIONS:{
			final StringBuilder str=new StringBuilder("");
			for(int a=0;a<Clan.Function.values().length;a++)
				if(functionChart[a]==Clan.Authority.CAN_DO)
				{
					if(str.length()>0) str.append(",");
					str.append(Clan.Function.values()[a]);
				}
			return str.toString();
		}
		default: Log.errOut("Clan","getStat:Unhandled:"+stat.toString()); break;
		}
		return "";
	}
	public boolean isStat(String code) { return getStatIndex(code)!=null;}
	public void setStat(String code, String val) 
	{
		final POS_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return;}
		switch(stat)
		{
		case NAME: name=val; break;
		case ISPUBLIC: isPublic=CMath.s_bool(val); break;
		case ID: ID=val.toUpperCase().trim(); break;
		case RANK: rank=CMath.s_int(val); break;
		case MAX: max=CMath.s_int(val); break;
		case PLURALNAME: pluralName=val; break;
		case INNERMASK: innerMaskStr=val; break;
		case FUNCTIONS:{
			final Vector<String> funcs=CMParms.parseCommas(val.toUpperCase().trim(), true);
			for(int a=0;a<Clan.Function.values().length;a++)
				if(functionChart[a]!=Clan.Authority.MUST_VOTE_ON)
					functionChart[a]=Clan.Authority.CAN_NOT_DO;
			for(final String funcName : funcs)
			{
				Clan.Function func=(Clan.Function)CMath.s_valueOf(Clan.Function.values(), funcName);
				if(func!=null) functionChart[func.ordinal()] = Clan.Authority.CAN_DO;
			}
			break;
		}
		default: Log.errOut("Clan","setStat:Unhandled:"+stat.toString()); break;
		}
	}
	public String toString() { return ID;}
}
