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
public class Prop_AreaForSale extends Property implements LandTitle
{
	public String ID() { return "Prop_AreaForSale"; }
	public String name(){ return "Putting an area up for sale";}
	protected int canAffectCode(){return Ability.CAN_AREAS;}
	protected Hashtable lastItemNums=new Hashtable();
	public String accountForYourself()
	{ return "For Sale";	}
	protected long lastCall=0;
	protected long lastMobSave=0;
	protected int lastDayDone=-1;

	public boolean allowsExpansionConstruction(){ return false;}

	public int landPrice()
	{
		if(text().length()==0)
			return 100000;
		String s=text();
		int index=s.length();
		while((--index)>=0)
		{
			if((!Character.isDigit(s.charAt(index)))
			&&(!Character.isWhitespace(s.charAt(index))))
				break;
		}
		int price=CMath.s_int(s.substring(index+1).trim());
				
		if(price<=0) price=100000;
		return price;
	}
	
	public String getUniqueLotID(){ return "AREA_PROPERTY_"+landPropertyID();}
	
	public void setLandPrice(int price)
	{   
		setMiscText(landOwner()+"/"
			+(rentalProperty()?"RENTAL ":"")
			+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
			+price);
	}
	
	public String landOwner()
	{
		if(text().indexOf('/')<0) return "";
		return text().substring(0,text().indexOf('/'));
	}

	public String getTitleID()
	{
		if(affected != null)
    		return "LAND_TITLE_FOR#"+affected.Name();
		return "";
	}

	public CMObject landOwnerObject()
	{
		String owner=landOwner();
		if(owner.length()==0) return null;
		Clan C=CMLib.clans().getClan(owner);
		if(C!=null) return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	public void setLandOwner(String owner)
	{   
		setMiscText(owner+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+landPrice());
	}

	public int backTaxes()
	{
		if(text().indexOf('/')<0) return 0;
		int x=text().indexOf("TAX",text().indexOf('/'));
		if(x<0) return 0;
		String s=(String)CMParms.parse(text().substring(x+3)).firstElement();
		return CMath.s_int(s.substring(0,s.length()-1));
	}
	public void setBackTaxes(int tax)
	{	
		setMiscText(landOwner()+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((tax!=0)?"TAX"+tax+"X ":"")
				+landPrice());
	}
	
	public boolean rentalProperty()
	{
		if(text().indexOf('/')<0) return text().indexOf("RENTAL")>=0;
		return text().indexOf("RENTAL",text().indexOf('/'))>0;
	}
	public void setRentalProperty(boolean truefalse)
	{	
		setMiscText(landOwner()+"/"
				+(truefalse?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+landPrice());
	}
	
	// update title, since it may affect clusters, worries about ALL involved
	public void updateTitle()
	{
		if(affected instanceof Area)
			CMLib.database().DBUpdateArea(((Area)affected).name(),(Area)affected);
		else
		{
			Area A=CMLib.map().getArea(landPropertyID());
			if(A!=null) CMLib.database().DBUpdateArea(A.Name(),A);
		}
	}

	public String landPropertyID(){
		if((affected!=null)&&(affected instanceof Area))
			((Area)affected).Name();
		return "";
	}

	public void setLandPropertyID(String landID){}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		Prop_RoomForSale.robberyCheck(this,msg);
		return true;
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target() instanceof Room))
				||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Area)
		&&((System.currentTimeMillis()-lastMobSave)>360000))
		{
			lastMobSave=System.currentTimeMillis();
			List<Room> V=getAllTitledRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.get(v);
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					lastMobSave=System.currentTimeMillis();
					Vector mobs=new Vector();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)
						&&(M.isSavable())
						&&(M.getStartRoom()==R)
						&&((M.basePhyStats().rejuv()==0)||(M.basePhyStats().rejuv()==PhyStats.NO_REJUV)))
							mobs.addElement(M);
					}
					if(!CMSecurity.isSaveFlag("NOPROPERTYMOBS"))
						CMLib.database().DBUpdateTheseMOBs(R,mobs);
				}
			}
			lastMobSave=System.currentTimeMillis();
		}
	}

	public List<Room> getAllTitledRooms()
	{
		Vector V=new Vector();
		Area A=null;
		if(affected instanceof Area)
			A=(Area)affected;
		else
			A=CMLib.map().getArea(landPropertyID());
		for(Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}
	public List<Room> getConnectedPropertyRooms() { return getAllTitledRooms();}

	// update lot, since its called by the savethread, ONLY worries about itself
	public void updateLot(List optPlayerList)
	{
		if(((System.currentTimeMillis()-lastCall)>360000)
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)))
		{
			List<Room> V=getAllTitledRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.get(v);
				lastCall=System.currentTimeMillis();
				Integer lastItemNum=(Integer)lastItemNums.get(R);
				lastItemNums.put(R,Integer.valueOf(Prop_RoomForSale.updateLotWithThisData(R,this,false,false,optPlayerList,(lastItemNum==null)?-1:lastItemNum.intValue())));
			}
			lastCall=System.currentTimeMillis();
			Area A=null;
			if(affected instanceof Area)
				A=(Area)affected;
			else
				A=CMLib.map().getArea(landPropertyID());
			if(lastDayDone!=A.getTimeObj().getDayOfMonth())
			{
				lastDayDone=A.getTimeObj().getDayOfMonth();
				if((landOwner().length()>0)&&rentalProperty())
					if(Prop_RoomForSale.doRentalProperty(A,A.Name(),landOwner(),landPrice()))
					{
						setLandOwner("");
						CMLib.database().DBUpdateArea(A.Name(),A);
					}
			}
		}
	}
}
