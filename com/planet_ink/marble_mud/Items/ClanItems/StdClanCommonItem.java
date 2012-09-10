package com.planet_ink.marble_mud.Items.ClanItems;
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
public class StdClanCommonItem extends StdClanItem
{
	public String ID(){	return "StdClanCommonItem";}
	protected int workDown=0;
	private static final Hashtable needChart=new Hashtable();
	protected boolean glows=false;
	
	public StdClanCommonItem()
	{
		super();

		setName("a clan workers item");
		basePhyStats.setWeight(1);
		setDisplayText("an workers item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_GATHERITEM);
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	public boolean fireHere(Room R)
	{
		for(int i=0;i<R.numItems();i++)
		{
			Item I2=R.getItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
				return true;
		}
		return false;
	}
	
	public Vector resourceHere(Room R, int material)
	{
		Vector here=new Vector();
		for(int i=0;i<R.numItems();i++)
		{
			Item I2=R.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(I2 instanceof RawMaterial)
			&&(((I2.material()&RawMaterial.RESOURCE_MASK)==material)
				||(((I2.material())&RawMaterial.MATERIAL_MASK)==material))
			&&(!CMLib.flags().enchanted(I2)))
				here.addElement(I2);
		}
		return here;
	}
	
	public Vector resourceHere(MOB M, int material)
	{
		Vector here=new Vector();
		for(int i=0;i<M.numItems();i++)
		{
			Item I2=M.getItem(i);
			if((I2!=null)
			&&(I2.container()==null)
			&&(I2 instanceof RawMaterial)
			&&(((I2.material()&RawMaterial.RESOURCE_MASK)==material)
				||(((I2.material())&RawMaterial.MATERIAL_MASK)==material))
			&&(!CMLib.flags().enchanted(I2)))
				here.addElement(I2);
		}
		return here;
	}
	
	public List resourceHere(Room R, List materials)
	{
		List allMat=new Vector();
		List V=null;
		for(int m=0;m<materials.size();m++)
		{
			V=resourceHere(R,((Integer)materials.get(m)).intValue());
			for(int v=0;v<V.size();v++)
				allMat.add(V.get(v));
			V.clear();
		}
		return allMat;
	}
	public List resourceHere(MOB M, List materials)
	{
		List allMat=new Vector();
		List V=null;
		for(int m=0;m<materials.size();m++)
		{
			V=resourceHere(M,((Integer)materials.get(m)).intValue());
			for(int v=0;v<V.size();v++)
				allMat.add(V.get(v));
			V.clear();
		}
		return allMat;
	}
	
	public List enCode(MOB M, String req)
	{
		req=req.toUpperCase();
		List V=new Vector();
		for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
		{
			int x=req.indexOf(RawMaterial.MATERIAL_DESCS[i]);
			if(x<0) continue;
			if((x>0)&&Character.isLetter(req.charAt(x-1)))
				continue;
			if(((x+RawMaterial.MATERIAL_DESCS[i].length())<req.length())
			&&Character.isLetter(req.charAt((x+RawMaterial.MATERIAL_DESCS[i].length()))))
				continue;
			V.add(Integer.valueOf(i<<8));
		}
		RawMaterial.CODES codes = RawMaterial.CODES.instance();
		for(int s=0;s<codes.total();s++)
		{
			String S=codes.name(s);
			int x=req.indexOf(S);
			if(x<0) continue;
			if((x>0)&&Character.isLetter(req.charAt(x-1)))
				continue;
			if(((x+S.length())<req.length())
			&&Character.isLetter(req.charAt((x+S.length()))))
				continue;
			V.add(Integer.valueOf(codes.get(s)));
		}
		if((M.location()!=null)
		&&(V.contains(Integer.valueOf(RawMaterial.MATERIAL_METAL)))
		&&(resourceHere(M.location(),RawMaterial.MATERIAL_WOODEN).size()==0))
			V.add(Integer.valueOf(RawMaterial.MATERIAL_WOODEN));
		return V;
	}

	public void affectPhyStats(Physical affected, PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		if((glows)
		&&(affected instanceof MOB)
		&&(!super.amWearingAt(Wearable.IN_INVENTORY))
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location().domainType()==Room.DOMAIN_INDOORS_CAVE)
		&&(((MOB)affected).getStartRoom()!=null)
		&&(((MOB)affected).getStartRoom().getArea()==((MOB)affected).location().getArea()))
			stats.setSensesMask(stats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}
	
	
	public void setReadableText(String newText)
	{
		super.setReadableText(newText);
		glows=(newText.equalsIgnoreCase("Mining"));
	}
	
	public boolean trackTo(MOB M, MOB M2)
	{
		Ability A=CMClass.getAbility("Skill_Track");
		if(A!=null)
		{
			Room R=M2.location();
			if((R!=null)&&(CMLib.flags().isInTheGame(M2,true)))
			{
				A.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\""),R,true,0);
				return true;
			}
		}
		return false;
	}
	public boolean trackTo(MOB M, Room R)
	{
		Ability A=CMClass.getAbility("Skill_Track");
		if((A!=null)&&(R!=null))
		{
			A.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(R)+"\""),R,true,0);
			return true;
		}
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
		&&(((MOB)owner()).getClanID().equals(clanID()))
		&&((--workDown)<=0)
		&&(!CMLib.flags().isATrackingMonster((MOB)owner()))
		&&(CMLib.flags().isInTheGame((MOB)owner,true))
		&&(!CMLib.flags().isAnimalIntelligence((MOB)owner())))
		{
			workDown=CMLib.dice().roll(1,7,0);
			MOB M=(MOB)owner();
			if(M.fetchEffect(readableText())==null)
			{
				Ability A=CMClass.getAbility(readableText());
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				{
					A.setProficiency(100);
					boolean success=false;
					if(((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_CRAFTINGSKILL)
					&&(CMLib.flags().isMobile(M)))
					{
						DVector DV=(DVector)needChart.get(M.location().getArea());
						if(DV!=null)
						{
							List needs=null;
							MOB M2=null;
							boolean getToWork=false;
							if(A.ID().equalsIgnoreCase("FireBuilding"))
							{
								MOB possibleMOBToGoTo=null;
								for(int i=0;i<DV.size();i++)
								{
									try{
										int rand=i;
										needs=(List)DV.elementAt(rand,2);
										M2=(MOB)DV.elementAt(rand,1);
									}catch(Exception e){continue;}
									if((needs!=null)&&(M2!=null)
									&&(needs.contains(Integer.valueOf(RawMaterial.MATERIAL_METAL)))
									&&(!fireHere(M2.location()))
									&&(resourceHere(M2.location(),RawMaterial.MATERIAL_WOODEN).size()>0))
									{
										if(M.location()==M2.location())
										{
											getToWork=true;
											break;
										}
										else
										if((possibleMOBToGoTo==null)||(CMLib.dice().roll(1,2,0)==1))
											possibleMOBToGoTo=M2;
									}
								}
								if((!getToWork)
								&&(possibleMOBToGoTo!=null)
								&&(trackTo(M,possibleMOBToGoTo)))
								{
									return true;
								}
							}
							List rsc=null;
							// if I have the stuff on hand.
							if(!getToWork)
							for(int i=DV.size()-1;i>=0;i--)
							{
								try{
									int rand=i;
									needs=(List)DV.elementAt(rand,2);
									M2=(MOB)DV.elementAt(rand,1);
									if(!CMLib.flags().isInTheGame(M2,true))
									{
										DV.removeElementAt(i);
										continue;
									}
								}catch(Exception e){continue;}
								if((needs!=null)&&(M2!=null)
								&&(M.location()==M2.location()))
								{
									rsc=resourceHere(M,needs);
									if(rsc.size()>0)
									{
										for(int r=0;r<rsc.size();r++)
											CMLib.commands().postDrop(M,(Environmental)rsc.get(r),false,true);
										return true;
									}
								}
							}
							if(!getToWork)
							for(int i=0;i<DV.size();i++)
							{
								try{
									int rand=CMLib.dice().roll(1,DV.size(),-1);
									needs=(List)DV.elementAt(rand,2);
									M2=(MOB)DV.elementAt(rand,1);
								}catch(Exception e){continue;}
								if((needs!=null)&&(M2!=null)
								&&(M.location()!=M2.location()))
								{
									rsc=resourceHere(M,needs);
									if((rsc.size()>0)
									&&(trackTo(M,M2)))
										return true;
								}
							}
							if(!getToWork)
							for(int i=0;i<DV.size();i++)
							{
								try{
									int rand=CMLib.dice().roll(1,DV.size(),-1);
									needs=(List)DV.elementAt(rand,2);
									M2=(MOB)DV.elementAt(rand,1);
								}catch(Exception e){continue;}
								if((needs!=null)&&(M2!=null)
								&&(M.location()!=M2.location()))
								{
									rsc=resourceHere(M.location(),needs);
									if(rsc.size()>0)
									{
										for(int r=0;r<rsc.size();r++)
											CMLib.commands().postGet(M,null,(Item)rsc.get(r),false);
										if(trackTo(M,M2))
											return true;
									}
								}
							}
						}
					}
					
					if((M.location()!=null)
					&&(CMLib.flags().aliveAwakeMobileUnbound(M,true))
					&&(!CMLib.flags().canBeSeenBy(M.location(),M)))
						switch(CMLib.dice().roll(1,7,0))
						{
						case 1: CMLib.commands().postSay(M,null,"I can't see a thing."); break;
						case 2: CMLib.commands().postSay(M,null,"It's too dark to work."); break;
						case 3: CMLib.commands().postSay(M,null,"How am I supposed to work in these conditions?"); break;
						case 4: CMLib.commands().postSay(M,null,"Too dadgum dark."); break;
						case 5: CMLib.commands().postSay(M,null,"Is anyone there?  I can't see!"); break;
						case 6: CMLib.commands().postSay(M,null,"Someone turn on the lights to I can work!"); break;
						case 7: CMLib.commands().postSay(M,null,"I could use some light, if you expect me to work."); break;
						}
					
					if((M.numItems()>1)&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL))
					{
						Item I=null;
						int tries=0;
						while((I==null)&&((++tries)<20))
						{
							I=M.getRandomItem();
							if((I==null)
							||(I==this)
							||(I instanceof RawMaterial)
							||(!I.amWearingAt(Wearable.IN_INVENTORY)))
								I=null;
						}
						Vector V=new Vector();
						if(I!=null)	V.addElement(I.name());
						success=A.invoke(M,V,null,false,phyStats().level());
					}
					else
						success=A.invoke(M,new Vector(),null,false,phyStats().level());
					if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
					{
						DVector DV=(DVector)needChart.get(M.location().getArea());
						if(!success)
						{
							if(DV==null)
							{
								DV=new DVector(2);
								needChart.put(M.location().getArea(),DV);
							}
							DV.removeElement(M);
							String req=A.accountForYourself();
							int reqIndex=req.indexOf(':');
							if(reqIndex>0)
								DV.addElement(M,enCode(M,req.substring(reqIndex+1)));
							else
								DV.addElement(M,enCode(M,req));
						}
						else
						if(DV!=null)
							DV.removeElement(M);
					}
				}

			}
		}
		return true;
	}
}
