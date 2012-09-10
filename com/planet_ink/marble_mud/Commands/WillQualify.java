package com.planet_ink.marble_mud.Commands;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
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

/**
 * <p>Title: False Realities Flavored marblemud</p>
 * <p>Description: The False Realities Version of marblemud</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

@SuppressWarnings("rawtypes")
public class WillQualify  extends Skills
{
	public WillQualify() {}
	private final String[] access={"WILLQUALIFY"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getQualifiedAbilities(MOB viewerM,
											  MOB ableM, 
											  String classID,
											  int maxLevel, 
											  String prefix,
											  HashSet<Object> types)
	{
		int highestLevel = maxLevel;
		StringBuffer msg = new StringBuffer("");
		int col = 0;
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,viewerM);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(19.0,viewerM);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(12.0,viewerM);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(13.0,viewerM);
		List<AbilityMapper.QualifyingID> DV=CMLib.ableMapper().getClassAllowsList(classID);
		for (int l = 0; l <= highestLevel; l++) 
		{
			StringBuffer thisLine = new StringBuffer("");
			for (Enumeration a = CMLib.ableMapper().getClassAbles(classID,true); a.hasMoreElements(); ) 
			{
				AbilityMapper.AbilityMapping cimable=(AbilityMapper.AbilityMapping)a.nextElement();
				if((cimable.qualLevel ==l)&&(!cimable.isSecret))
				{
					Ability A=CMClass.getAbility(cimable.abilityID);
					if((A!=null)
					&&((types.size()==0)
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES)))
						||(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_DOMAINS))))
					&&(CMLib.ableMapper().getCommonSkillLimit(ableM, A).specificSkillLimit > 0))
					{
						if ( (++col) > 2) 
						{
							thisLine.append("\n\r");
							col = 1;
						}
						thisLine.append("^N[^H" + CMStrings.padRight("" + l, COL_LEN1) + "^?] "
								+ CMStrings.padRight("^<HELP^>"+A.name()+"^</HELP^>", COL_LEN2) + " "
								+ CMStrings.padRight(A.requirements(viewerM)+(cimable.autoGain?" *":""), (col == 2) ? COL_LEN3 : COL_LEN4));
					}
				}
			}
			ExpertiseLibrary.ExpertiseDefinition E=null;
			Integer qualLevel=null;
			for(AbilityMapper.QualifyingID qID : DV)
			{
				qualLevel=Integer.valueOf(qID.qualifyingLevel);
				E=CMLib.expertises().getDefinition(qID.ID);
				if(E!=null)
				{
					int minLevel=E.getMinimumLevel();
					if(minLevel<qualLevel.intValue())
						minLevel=qualLevel.intValue();
					if(minLevel==l)
					{
						if((types.size()==0)
						||types.contains("EXPERTISE")
						||types.contains("EXPERTISES")
						||types.contains(E.ID.toUpperCase())
						||types.contains(E.name.toUpperCase()))
						{
							if ( (++col) > 2) 
							{
								thisLine.append("\n\r");
								col = 1;
							}
							thisLine.append("^N[^H" + CMStrings.padRight("" + l, 3) + "^?] "
									+ CMStrings.padRight("^<HELP^>"+E.name+"^</HELP^>", 19) + " "
									+ CMStrings.padRight(E.costDescription(), (col == 2) ? 12 : 13));
						}
					}
				}
			}
			if (thisLine.length() > 0) 
			{
				if (msg.length() == 0)
						msg.append("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
				msg.append(thisLine);
			}
		}
		if (msg.length() == 0)
				return msg;
		msg.insert(0, prefix);
		msg.append("\n\r* This skill is automatically granted.");
		return msg;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
					throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		String willQualErr = "Specify level, class, and or skill-type:  WILLQUALIFY ([LEVEL]) ([CLASS NAME]) ([SKILL TYPE]).";
		int level=CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL);
		CharClass C=mob.charStats().getCurrentClass();
		HashSet<Object> types=new HashSet<Object>();
		if(commands.size()>0) commands.removeElementAt(0);
		if((commands.size()>0)&&(CMath.isNumber((String)commands.firstElement())))
		{
			level=CMath.s_int((String)commands.firstElement());
			if(level<0)
			{
				mob.tell(willQualErr);
				return false;
			}
			commands.removeElementAt(0);
		}
		if(commands.size()>0)
		{
			CharClass C2=CMClass.findCharClass((String)commands.firstElement());
			if(C2!=null){ C=C2;commands.removeElementAt(0);}
		}
		while(commands.size()>0)
		{
			String str=((String)commands.firstElement()).toUpperCase().trim();
			int x=CMParms.indexOf(Ability.ACODE_DESCS,str);
			if(x<0) x=CMParms.indexOf(Ability.ACODE_DESCS,str.replace(' ','_'));
			if(x>=0)
				types.add(Integer.valueOf(x));
			else
			{
				x=CMParms.indexOf(Ability.DOMAIN_DESCS,str);
				if(x<0)
					x=CMParms.indexOf(Ability.DOMAIN_DESCS,str.replace(' ','_'));
				if(x<0)
				{
					if((CMLib.expertises().findDefinition(str,false)==null)
					&&!str.equalsIgnoreCase("EXPERTISE")
					&&!str.equalsIgnoreCase("EXPERTISES"))
					{
						mob.tell("'"+str+"' is not a valid skill type, domain, expertise, or character class.");
						mob.tell(willQualErr);
						return false;
					}
					types.add(str.toUpperCase().trim());
				}
				else
					types.add(Integer.valueOf(x<<5));
			}
			commands.removeElementAt(0);
		}
		
		msg.append("At level "+level+" of class '"+C.name()+"', you could qualify for:\n\r");
		msg.append(getQualifiedAbilities(mob,mob,C.ID(),level,"",types));
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
}
