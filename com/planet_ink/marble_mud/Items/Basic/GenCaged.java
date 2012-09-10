package com.planet_ink.marble_mud.Items.Basic;
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

import com.planet_ink.marble_mud.Libraries.interfaces.*;

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
public class GenCaged extends GenItem implements CagedAnimal
{
	public String ID(){	return "GenCaged";}
	public GenCaged()
	{
		super();
		setName("a caged creature");
		basePhyStats.setWeight(150);
		setDisplayText("a caged creature sits here.");
		setDescription("");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		setMaterial(RawMaterial.RESOURCE_MEAT);
		recoverPhyStats();
	}
	protected byte[]	readableText=null;
	public String readableText(){return readableText==null?"":CMLib.encoder().decompressString(readableText);}
	public void setReadableText(String text){readableText=(text.trim().length()==0)?null:CMLib.encoder().compressString(text);}
	public boolean cageMe(MOB M)
	{
		if(M==null) return false;
		if(!M.isMonster()) return false;
		name=M.Name();
		displayText=M.displayText();
		setDescription(M.description());
		basePhyStats().setLevel(M.basePhyStats().level());
		basePhyStats().setWeight(M.basePhyStats().weight());
		basePhyStats().setHeight(M.basePhyStats().height());
		StringBuffer itemstr=new StringBuffer("");
		itemstr.append("<MOBITEM>");
		itemstr.append(CMLib.xml().convertXMLtoTag("MICLASS",CMClass.classID(M)));
		itemstr.append(CMLib.xml().convertXMLtoTag("MISTART",CMLib.map().getExtendedRoomID(M.getStartRoom())));
		itemstr.append(CMLib.xml().convertXMLtoTag("MIDATA",CMLib.coffeeMaker().getPropertiesStr(M,true)));
		itemstr.append("</MOBITEM>");
		setCageText(itemstr.toString());
		recoverPhyStats();
		return true;
	}
	
	public void destroy()
	{
		if((CMSecurity.isDebugging(CMSecurity.DbgFlag.MISSINGKIDS))&&(fetchEffect("Age")!=null)&&CMath.isInteger(fetchEffect("Age").text())&&(CMath.s_int(fetchEffect("Age").text())>Short.MAX_VALUE))
			Log.debugOut("MISSKIDS",new Exception(Name()+" went missing form "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(this))));
		super.destroy();
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)
			||((msg.tool()==this)&&(msg.target()==container())&&(container()!=null)))
		&&((basePhyStats().ability()&ABILITY_MOBPROGRAMMATICALLY)==0)
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_DROP)))
		{
			MOB M=unCageMe();
			if((M!=null)&&(msg.source().location()!=null))
				M.bringToLife(msg.source().location(),true);
			destroy();
			return;
		}
		super.executeMsg(myHost,msg);
	}
	public MOB unCageMe()
	{
		MOB M=null;
		if(cageText().length()==0) return M;
		List<XMLLibrary.XMLpiece> buf=CMLib.xml().parseAllXML(cageText());
		if(buf==null)
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		XMLLibrary.XMLpiece iblk=CMLib.xml().getPieceFromPieces(buf,"MOBITEM");
		if((iblk==null)||(iblk.contents==null))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		String itemi=CMLib.xml().getValFromPieces(iblk.contents,"MICLASS");
		String startr=CMLib.xml().getValFromPieces(iblk.contents,"MISTART");
		Environmental newOne=CMClass.getMOB(itemi);
		List<XMLLibrary.XMLpiece> idat=CMLib.xml().getContentsFromPieces(iblk.contents,"MIDATA");
		if((idat==null)||(newOne==null)||(!(newOne instanceof MOB)))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM' data.");
			return M;
		}
		CMLib.coffeeMaker().setPropertiesStr(newOne,idat,true);
		M=(MOB)newOne;
		M.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		M.setStartRoom(null);
		if(M.isGeneric())
			CMLib.coffeeMaker().resetGenMOB(M,M.text());
		if((startr.length()>0)&&(!startr.equalsIgnoreCase("null")))
		{
			Room R=CMLib.map().getRoom(startr);
			if(R!=null)
				M.setStartRoom(R);
		}
		return M;
	}
	public String cageText(){ return CMLib.xml().restoreAngleBrackets(readableText());}
	public void setCageText(String text)
	{
		setReadableText(CMLib.xml().parseOutAngleBrackets(text));
		CMLib.flags().setReadable(this,false);
	}
}
