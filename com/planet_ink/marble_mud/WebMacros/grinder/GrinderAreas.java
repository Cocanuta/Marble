package com.planet_ink.marble_mud.WebMacros.grinder;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class GrinderAreas
{
	public static String getAreaList(Area pickedA, MOB mob, boolean noInstances)
	{
		StringBuffer AreaList=new StringBuffer("");
		boolean anywhere=(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDAREAS));
		boolean everywhere=(CMSecurity.isASysOp(mob)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDAREAS));
		for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if((everywhere||(A.amISubOp(mob.Name())&&anywhere))
			&&((!noInstances)||(!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))))
				if((pickedA!=null)&&(pickedA==A))
					AreaList.append("<OPTION SELECTED VALUE=\""+A.Name()+"\">"+A.name());
				else
					AreaList.append("<OPTION VALUE=\""+A.Name()+"\">"+A.name());
		}
		return AreaList.toString();
	}

	public static String doBehavs(PhysicalAgent E, ExternalHTTPRequests httpReq, java.util.Map<String,String> parms)
	{
		E.delAllBehaviors();
		if(httpReq.isRequestParameter("BEHAV1"))
		{
			int num=1;
			String behav=httpReq.getRequestParameter("BEHAV"+num);
			String theparm=httpReq.getRequestParameter("BDATA"+num);
			while((behav!=null)&&(theparm!=null))
			{
				if(behav.length()>0)
				{
					Behavior B=CMClass.getBehavior(behav);
					if(B==null) return "Unknown behavior '"+behav+"'.";
					B.setParms(theparm);
					E.addBehavior(B);
					B.startBehavior(E);
				}
				num++;
				behav=httpReq.getRequestParameter("BEHAV"+num);
				theparm=httpReq.getRequestParameter("BDATA"+num);
			}
		}
		return "";
	}
	
	public static String doAffects(Physical P, ExternalHTTPRequests httpReq, java.util.Map<String,String> parms)
	{
		P.delAllEffects(false);
		if(httpReq.isRequestParameter("AFFECT1"))
		{
			int num=1;
			String aff=httpReq.getRequestParameter("AFFECT"+num);
			String theparm=httpReq.getRequestParameter("ADATA"+num);
			while((aff!=null)&&(theparm!=null))
			{
				if(aff.length()>0)
				{
					Ability B=CMClass.getAbility(aff);
					if(B==null) return "Unknown Effect '"+aff+"'.";
					B.setMiscText(theparm);
					P.addNonUninvokableEffect(B);
				}
				num++;
				aff=httpReq.getRequestParameter("AFFECT"+num);
				theparm=httpReq.getRequestParameter("ADATA"+num);
			}
		}
		return "";
	}

	public static String modifyArea(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms)
	{
		Vector areasNeedingUpdates=new Vector();
		String last=httpReq.getRequestParameter("AREA");
		if((last==null)||(last.length()==0)) return "Old area name not defined!";
		Area A=CMLib.map().getArea(last);
		if(A==null) return "Old Area not defined!";
		areasNeedingUpdates.addElement(A);

		boolean redoAllMyDamnRooms=false;
		Vector allMyDamnRooms=null;
		String oldName=null;

		// class!
		String className=httpReq.getRequestParameter("CLASSES");
		if((className==null)||(className.length()==0))
			return "Please select a class type for this area.";
		if(!className.equalsIgnoreCase(CMClass.classID(A)))
		{
			allMyDamnRooms=new Vector();
			for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			Area oldA=A;
			A=CMClass.getAreaType(className);
			if(A==null)
				return "The class you chose does not exist.  Choose another.";
			CMLib.map().delArea(oldA);
			CMLib.map().addArea(A);
			A.setName(oldA.Name());
			redoAllMyDamnRooms=true;
			areasNeedingUpdates.remove(oldA);
			areasNeedingUpdates.addElement(A);
		}

		// name
		String name=httpReq.getRequestParameter("NAME");
		if((name==null)||(name.length()==0))
			return "Please enter a name for this area.";
		name=name.trim();
		if(!name.equals(A.Name().trim()))
		{
			if(CMLib.map().getArea(name)!=null)
				return "The name you chose is already in use.  Please enter another.";
			allMyDamnRooms=new Vector();
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
				allMyDamnRooms.addElement(r.nextElement());
			CMLib.map().delArea(A);
			oldName=A.Name();
			CMLib.database().DBDeleteArea(A);
			A=CMClass.getAreaType(A.ID());
			A.setName(name);
			CMLib.map().addArea(A);
			CMLib.database().DBCreateArea(A);
			redoAllMyDamnRooms=true;
			httpReq.addRequestParameters("AREA",A.Name());
		}

		// climate
		if(httpReq.isRequestParameter("CLIMATE"))
		{
			int climate=CMath.s_int(httpReq.getRequestParameter("CLIMATE"));
			for(int i=1;;i++)
				if(httpReq.isRequestParameter("CLIMATE"+(Integer.toString(i))))
					climate=climate|CMath.s_int(httpReq.getRequestParameter("CLIMATE"+(Integer.toString(i))));
				else
					break;
			A.setClimateType(climate);
		}
		else
			A.setClimateType(0);

		// tech level
		if(httpReq.isRequestParameter("TECHLEVEL"))
			A.setTechLevel(CMath.s_int(httpReq.getRequestParameter("TECHLEVEL")));

		// modify subop list
		for(Enumeration<String> s=A.subOps();s.hasMoreElements();)
			A.delSubOp(s.nextElement());
		for(int i=1;;i++)
			if(httpReq.isRequestParameter("SUBOP"+(Integer.toString(i))))
				A.addSubOp(httpReq.getRequestParameter("SUBOP"+(Integer.toString(i))));
			else
				break;

		int num=1;
		if(httpReq.isRequestParameter("BLURBFLAG1"))
		{
			Vector prics=new Vector();
			String DOUBLE=httpReq.getRequestParameter("BLURBFLAG"+num);
			String MASK=httpReq.getRequestParameter("BLURB"+num);
			while((DOUBLE!=null)&&(MASK!=null))
			{
				if(DOUBLE.trim().length()>0)
					prics.addElement((DOUBLE.toUpperCase().trim()+" "+MASK).trim());
				num++;
				DOUBLE=httpReq.getRequestParameter("BLURBFLAG"+num);
				MASK=httpReq.getRequestParameter("BLURB"+num);
			}
			for(Enumeration<String> f=A.areaBlurbFlags();f.hasMoreElements();)
				A.delBlurbFlag(f.nextElement());
			for(int v=0;v<prics.size();v++)
				A.addBlurbFlag((String)prics.elementAt(v));
		}
		// description
		String desc=httpReq.getRequestParameter("DESCRIPTION");
		if(desc==null)desc="";
		A.setDescription(CMLib.coffeeFilter().safetyFilter(desc));

		// image
		String img=httpReq.getRequestParameter("IMAGE");
		if(img==null)img="";
		A.setImage(CMLib.coffeeFilter().safetyFilter(img));

		// gridy
		String gridy=httpReq.getRequestParameter("GRIDY");
		if((gridy!=null)&&(A instanceof GridZones))
			((GridZones)A).setYGridSize(CMath.s_int(gridy));
		// gridx
		String gridx=httpReq.getRequestParameter("GRIDX");
		if((gridx!=null)&&(A instanceof GridZones))
			((GridZones)A).setXGridSize(CMath.s_int(gridx));

		// author
		String author=httpReq.getRequestParameter("AUTHOR");
		if(author==null)author="";
		A.setAuthorID(CMLib.coffeeFilter().safetyFilter(author));

		// currency
		String currency=httpReq.getRequestParameter("CURRENCY");
		if(currency==null)currency="";
		A.setCurrency(CMLib.coffeeFilter().safetyFilter(currency));

		// SHOPPREJ
		String SHOPPREJ=httpReq.getRequestParameter("SHOPPREJ");
		if(SHOPPREJ==null)SHOPPREJ="";
		A.setPrejudiceFactors(CMLib.coffeeFilter().safetyFilter(SHOPPREJ));

		// BUDGET
		String BUDGET=httpReq.getRequestParameter("BUDGET");
		if(BUDGET==null)BUDGET="";
		A.setBudget(CMLib.coffeeFilter().safetyFilter(BUDGET));

		// DEVALRATE
		String DEVALRATE=httpReq.getRequestParameter("DEVALRATE");
		if(DEVALRATE==null)DEVALRATE="";
		A.setDevalueRate(CMLib.coffeeFilter().safetyFilter(DEVALRATE));

		// INVRESETRATE
		String INVRESETRATE=httpReq.getRequestParameter("INVRESETRATE");
		if(INVRESETRATE==null)INVRESETRATE="0";
		A.setInvResetRate(CMath.s_int(CMLib.coffeeFilter().safetyFilter(INVRESETRATE)));

		// IGNOREMASK
		String IGNOREMASK=httpReq.getRequestParameter("IGNOREMASK");
		if(IGNOREMASK==null)IGNOREMASK="";
		A.setIgnoreMask(CMLib.coffeeFilter().safetyFilter(IGNOREMASK));

		
		if(A instanceof AutoGenArea)
		{
			String AGXMLPATH=httpReq.getRequestParameter("AGXMLPATH");
			if(AGXMLPATH==null)AGXMLPATH="";
			((AutoGenArea) A).setGeneratorXmlPath(CMLib.coffeeFilter().safetyFilter(AGXMLPATH));
			
			String AGAUTOVAR=httpReq.getRequestParameter("AGAUTOVAR");
			if(AGAUTOVAR==null)AGAUTOVAR="";
			((AutoGenArea) A).setAutoGenVariables(CMLib.coffeeFilter().safetyFilter(AGAUTOVAR));
		}
		
		// PRICEFACTORS
		num=1;
		if((A instanceof Economics)
		&&(httpReq.isRequestParameter("IPRIC1")))
		{
			Vector prics=new Vector();
			String DOUBLE=httpReq.getRequestParameter("IPRIC"+num);
			String MASK=httpReq.getRequestParameter("IPRICM"+num);
			while((DOUBLE!=null)&&(MASK!=null))
			{

				if(CMath.isNumber(DOUBLE))
					prics.addElement((DOUBLE+" "+MASK).trim());
				num++;
				DOUBLE=httpReq.getRequestParameter("IPRIC"+num);
				MASK=httpReq.getRequestParameter("IPRICM"+num);
			}
			((Economics)A).setItemPricingAdjustments(CMParms.toStringArray(prics));
		}

		// modify Parent Area list
		while(A.getParents().hasMoreElements())
			A.removeParent(A.getParents().nextElement());
		for(int i=1;;i++)
			if(httpReq.isRequestParameter("PARENT"+(Integer.toString(i))))
			{
				Area parent=CMLib.map().getArea(httpReq.getRequestParameter("PARENT"+(Integer.toString(i))));
				if(parent!=null)
				{
					if(A.canParent(parent))
					{
						A.addParent(parent);
						parent.addChild(A);
						areasNeedingUpdates.addElement(parent);
					}
					else
						return "The area, '"+parent.Name()+"', cannot be added as a parent, as this would create a circular reference.";
				}
			}
			else
				break;

		// modify Child Area list
		while(A.getChildren().hasMoreElements())
			A.removeChild(A.getChildren().nextElement());
		for(int i=1;;i++)
		  if(httpReq.isRequestParameter("CHILDREN"+(Integer.toString(i))))
		  {
			  Area child=CMLib.map().getArea(httpReq.getRequestParameter("CHILDREN"+(Integer.toString(i))));
			  if(child!=null)
			  {
				  if(A.canChild(child))
				  {
					  A.addChild(child);
					  child.addParent(A);
					  areasNeedingUpdates.addElement(child);
				  }
				  else
					  return "The area, '"+child.Name()+"', cannot be added as a child, as this would create a circular reference.";
			  }
			  else
				  break;
		}

		// archive file
		String file=httpReq.getRequestParameter("ARCHP");
		if(file==null)file="";
		A.setArchivePath(file);

		String error=GrinderAreas.doAffects(A,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderAreas.doBehavs(A,httpReq,parms);
		if(error.length()>0) return error;

		if((redoAllMyDamnRooms)&&(allMyDamnRooms!=null))
			CMLib.map().renameRooms(A,oldName,allMyDamnRooms);

		for(int i=0;i<areasNeedingUpdates.size();i++) // will always include A
		{
			Area A2=(Area)areasNeedingUpdates.elementAt(i);
			CMLib.database().DBUpdateArea(A2.Name(),A2);
			CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A2);
		}
		return "";
	}
}
