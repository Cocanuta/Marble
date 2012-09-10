package com.planet_ink.marble_mud.Libraries;
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


import java.io.IOException;
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
public class CoffeeUtensils extends StdLibrary implements CMMiscUtils
{
	public String ID(){return "CoffeeUtensils";}
	
	public String niceCommaList(List<?> V, boolean andTOrF)
	{
		String id="";
		for(int v=0;v<V.size();v++)
		{
			String s=null;
			if(V.get(v) instanceof Environmental)
				s=((Environmental)V.get(v)).name();
			else
			if(V.get(v) instanceof String)
				s=(String)V.get(v);
			else
				continue;
			if(V.size()==1)
				id+=s;
			else
			if(v==(V.size()-1))
				id+=((andTOrF)?"and ":"or ")+s;
			else
				id+=s+", ";
		}
		return id;
	}
	
	public String getFormattedDate(Environmental E)
	{
		String date=CMStrings.padRight("Unknown",11);
		if(E!=null)
		{
			TimeClock C=(E instanceof Area)?((Area)E).getTimeObj():((CMLib.map().roomLocation(E)!=null)?CMLib.map().roomLocation(E).getArea().getTimeObj():null);
			if(C!=null)
				date=CMStrings.padRight(C.getDayOfMonth()+"-"+C.getMonth()+"-"+C.getYear(),11);
		}
		return date;
	}

	public void outfit(MOB mob, List<Item> items)
	{
		if((mob==null)||(items==null)||(items.size()==0))
			return;
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.get(i);
			if(mob.findItem("$"+I.name()+"$")==null)
			{
				I=(Item)I.copyOf();
				I.text();
				I.recoverPhyStats();
				mob.addItem(I);
				if(I.whereCantWear(mob)<=0)
					I.wearIfPossible(mob);
				if(((I instanceof Armor)||(I instanceof Weapon))
				&&(I.amWearingAt(Wearable.IN_INVENTORY)))
					I.destroy();
			}
		}
	}

	public Trap makeADeprecatedTrap(Physical unlockThis)
	{
		Trap theTrap=null;
		int roll=(int)Math.round(Math.random()*100.0);
		if(unlockThis instanceof Exit)
		{
			if(((Exit)unlockThis).hasADoor())
			{
				if(((Exit)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Enter");
		}
		else
		if(unlockThis instanceof Container)
		{
			if(((Container)unlockThis).hasALid())
			{
				if(((Container)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Get");
		}
		else
		if(unlockThis instanceof Item)
			theTrap=(Trap)CMClass.getAbility("Trap_Get");
		return theTrap;
	}


	public void setTrapped(Physical myThang, boolean isTrapped)
	{
		Trap t=makeADeprecatedTrap(myThang);
		t.setReset(50);
		setTrapped(myThang,t,isTrapped);
	}
	public void setTrapped(Physical myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numEffects();a++)
		{
			Ability A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof Trap))
				A.unInvoke();
		}

		if((isTrapped)&&(myThang.fetchEffect(theTrap.ID())==null))
			myThang.addEffect(theTrap);
	}

	public Trap fetchMyTrap(Physical myThang)
	{
		if(myThang==null) return null;
		for(final Enumeration<Ability> a=myThang.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof  Trap))
				return (Trap)A;
		}
		return null;
	}
	public boolean reachableItem(MOB mob, Environmental E)
	{
		if((E==null)||(!(E instanceof Item)))
			return true;
		Item I=(Item)E;
		Rideable R=mob.riding();
		if((mob.isMine(I))
		||((mob.riding()!=null)&&((I==mob.riding())
								  ||(I.owner()==R)
								  ||(I.ultimateContainer(R)==R)))
		||(I.owner()==null)
		||((I.owner() instanceof Room)&&(!((Room)I.owner()).isContent(I))))
		   return true;
		return false;
	}

	public double memoryUse ( Environmental E, int number )
	{
		double s=-1.0;
		try
		{
			int n = number;
			Object[] objs = new Object[n] ;
			Environmental cl = E;
			Runtime rt = Runtime.getRuntime() ;
			long m0 =rt.totalMemory() - rt.freeMemory() ;
			System.gc() ;
			Thread.sleep( 500 ) ;
			for (int i = 0 ; i < n ; ++i) objs[i] =
					E=(Environmental)cl.copyOf();
			System.gc() ;
			Thread.sleep( 1000 ) ;
			long m1 =rt.totalMemory() - rt.freeMemory() ;
			long dm = m1 - m0 ;
			s = (double)dm / (double)n ;
			if(s<0.0) return memoryUse(E,number);
		}
		catch(Exception e){return -1;}
		return s;
	}

	public void extinguish(MOB source, Physical target, boolean mundane)
	{
		if(target instanceof Room)
		{
			Room R=(Room)target;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null) extinguish(source,M,mundane);
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.getItem(i);
				if(I!=null) extinguish(source,I,mundane);
			}
			return;
		}
		for(int a=target.numEffects()-1;a>=0;a--) // personal effects
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)&&((!mundane)||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)))
			{
				if((CMath.bset(A.flags(),Ability.FLAG_HEATING)&&(!mundane))
				||(CMath.bset(A.flags(),Ability.FLAG_FIREBASED))
				||((A.ID().equalsIgnoreCase("Spell_SummonElemental")&&A.text().toUpperCase().indexOf("FIRE")>=0)))
					A.unInvoke();
			}
		}
		if((target instanceof MOB)&&(!mundane))
		{
			MOB tmob=(MOB)target;
			if(tmob.charStats().getMyRace().ID().equals("FireElemental"))
				CMLib.combat().postDeath(source,(MOB)target,null);
			for(int i=0;i<tmob.numItems();i++)
			{
				Item I=tmob.getItem(i);
				if(I!=null) extinguish(tmob,I,mundane);
			}
		}
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,Tickable.TICKID_LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}

	public void roomAffectFully(CMMsg msg, Room room, int dirCode)
	{
		room.send(msg.source(),msg);
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return;
		if(dirCode<0)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(room.getExitInDir(d)==msg.target()){ dirCode=d; break;}
		}
		if(dirCode<0) return;
		Exit pair=room.getPairedExit(dirCode);
		if(pair!=null)
		{
			CMMsg altMsg=null;
			if((msg.targetMinor()==CMMsg.TYP_OPEN)&&(pair.isLocked()))
			{
				altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null);
				pair.executeMsg(msg.source(),altMsg);
			}
			altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
			pair.executeMsg(msg.source(),altMsg);
		}
	}
	
	public int disenchantItem(Item target)
	{
		int level=target.basePhyStats().level();
		boolean doneSomething=false;
		if(target instanceof Wand)
		{
			Ability A=((Wand)target).getSpell();
			if(A!=null)
				level=level-CMLib.ableMapper().lowestQualifyingLevel(A.ID())+2;
			((Wand)target).setSpell(null);
			((Wand)target).setUsesRemaining(0);
			doneSomething=true;
		}
		else
		if(target instanceof SpellHolder)
		{
			((SpellHolder)target).setSpellList("");
			doneSomething=true;
		}
		else
		if((target.phyStats().ability()>0)
		&&(!(target instanceof Coins)))
		{
			level=level-(target.basePhyStats().ability()*3);
			target.basePhyStats().setAbility(0);
			doneSomething=true;
		}

		LinkedList<Ability> affects=new LinkedList<Ability>();
		for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				affects.add(A);
		}
		for(Ability A : affects)
		{
			A.unInvoke();
			level=level-1;
			target.delEffect(A);
			doneSomething=true;
		}
		if(target.amDestroyed())
			return 0;
		if(doneSomething) return level;
		return -999;
	}

	public boolean disInvokeEffects(Environmental E)
	{
		if(E==null) return false;
		if(E instanceof Affectable)
		{
			Affectable aE=(Affectable)E;
			LinkedList<Ability> affects=new LinkedList<Ability>();
			for(int a=aE.numEffects()-1;a>=0;a--) // personal affects
			{
				Ability A=aE.fetchEffect(a);
				if(A!=null)
					affects.add(A);
			}
			for(Ability A : affects)
			{
				if(A.canBeUninvoked() && (!A.isAutoInvoked()))
					A.unInvoke();
			}
		}
		return !E.amDestroyed();
	}

	public int processVariableEquipment(MOB mob)
	{
		int newLastTickedDateTime=0;
		if(mob!=null)
		{
			Room R=mob.location();
			if(R!=null)
			{
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(!M.isMonster())&&(CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.CMDMOBS)))
					{ newLastTickedDateTime=-1; break;}
				}
				if(newLastTickedDateTime==0)
				{
					List<List<Item>> rivals=new Vector<List<Item>>();
					for(int i=0;i<mob.numItems();i++)
					{
						Item I=mob.getItem(i);
						if((I!=null)&&(I.basePhyStats().rejuv()>0)&&(I.basePhyStats().rejuv()!=PhyStats.NO_REJUV))
						{
							List<Item> V=null;
							for(int r=0;r<rivals.size();r++)
							{
								List<Item> V2=(List<Item>)rivals.get(r);
								Item I2=(Item)V2.get(0);
								if(I2.rawWornCode()==I.rawWornCode())
								{ V=V2; break;}
							}
							if(V==null){ V=new Vector<Item>(); rivals.add(V);}
							V.add(I);
						}
					}
					for(int i=0;i<rivals.size();i++)
					{
						List<Item> V=rivals.get(i);
						if((V.size()==1)||(((Item)V.get(0)).rawWornCode()==0))
						{
							for(int r=0;r<V.size();r++)
							{
								Item I=(Item)V.get(r);
								if(CMLib.dice().rollPercentage()<I.basePhyStats().rejuv())
									mob.delItem(I);
								else
								{
									I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									I.phyStats().setRejuv(PhyStats.NO_REJUV);
								}
							}
						}
						else
						{
							int totalChance=0;
							for(int r=0;r<V.size();r++)
							{
								Item I=(Item)V.get(r);
								totalChance+=I.basePhyStats().rejuv();
							}
							int chosenChance=CMLib.dice().roll(1,totalChance,0);
							totalChance=0;
							Item chosenI=null;
							for(int r=0;r<V.size();r++)
							{
								Item I=(Item)V.get(r);
								if(chosenChance<=(totalChance+I.basePhyStats().rejuv()))
								{
									chosenI=I;
									break;
								}
								totalChance+=I.basePhyStats().rejuv();
							}
							for(int r=0;r<V.size();r++)
							{
								Item I=(Item)V.get(r);
								if(chosenI!=I)
									mob.delItem(I);
								else
								{
									I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									I.phyStats().setRejuv(PhyStats.NO_REJUV);
								}
							}
						}
					}
					if(mob instanceof ShopKeeper)
					{
						List<Item> V=new Vector<Item>();
						CoffeeShop shop = ((ShopKeeper)mob).getShop();
						for(Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
						{
							Environmental E=(Environmental)i.next();
							if((E instanceof Item)
							&&(((Item)E).basePhyStats().rejuv()>0)
							&&(((Item)E).basePhyStats().rejuv()!=PhyStats.NO_REJUV))
								V.add((Item)E);
						}
						for(Item I : V)
						{
							if(CMLib.dice().rollPercentage()>I.basePhyStats().rejuv())
								shop.delAllStoreInventory(I);
							else
							{
								I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
								I.phyStats().setRejuv(PhyStats.NO_REJUV);
							}
						}
					}
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
				}
			}
		}
		return newLastTickedDateTime;
	}

	public void recursiveDropMOB(MOB mob,
								 Room room,
								 Item thisContainer,
								 boolean bodyFlag)
	{
		// caller is responsible for recovering any env
		// stat changes!
		
		if(CMLib.flags().isHidden(thisContainer))
			thisContainer.basePhyStats().setDisposition(thisContainer.basePhyStats().disposition()&((int)PhyStats.ALLMASK-PhyStats.IS_HIDDEN));
		mob.delItem(thisContainer);
		thisContainer.unWear();
		if(!bodyFlag) bodyFlag=(thisContainer instanceof DeadBody);
		if(bodyFlag)
		{
			room.addItem(thisContainer);
			thisContainer.setExpirationDate(0);
		}
		else
			room.addItem(thisContainer,ItemPossessor.Expire.Player_Drop);
		thisContainer.recoverPhyStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.numItems();i++)
			{
				Item thisItem=mob.getItem(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
				{
					recursiveDropMOB(mob,room,thisItem,bodyFlag);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}
	
	
	public MOB getMobPossessingAnother(MOB mob)
	{
		if(mob==null) return null;
		MOB M=null;
		for(Session S : CMLib.sessions().localOnlineIterable())
		{
			M=S.mob();
			if((M!=null)&&(M.soulMate()==mob))
				return M;
		}
		return null;
	}
	
	
	public boolean armorCheck(MOB mob, Item I, int allowedArmorLevel)
	{
		if((((I instanceof Armor)||(I instanceof Shield)))
		&&(I.rawProperLocationBitmap()&CharClass.ARMOR_WEARMASK)>0)
		{
			boolean ok=true;
			switch(I.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LEATHER:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_NONMETAL))
					ok=false;
				break;
			case RawMaterial.MATERIAL_ENERGY:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN))
				   return false;
				break;
			case RawMaterial.MATERIAL_CLOTH:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||((allowedArmorLevel==CharClass.ARMOR_VEGAN)
				   &&((I.material()==RawMaterial.RESOURCE_HIDE)
					  ||(I.material()==RawMaterial.RESOURCE_FUR)
					  ||(I.material()==RawMaterial.RESOURCE_FEATHERS)
					  ||(I.material()==RawMaterial.RESOURCE_WOOL))))
					ok=false;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
			case RawMaterial.MATERIAL_WOODEN:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_GLASS:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_FLESH:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
				||(allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
					ok=false;
				break;
			default:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
					ok=false;
				break;
			}
			return ok;
		}
		return true;
	}
	
	public boolean armorCheck(MOB mob, int allowedArmorLevel)
	{
		if(allowedArmorLevel==CharClass.ARMOR_ANY) return true;

		for(int i=0;i<mob.numItems();i++)
		{
			Item I=mob.getItem(i);
			if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean ok=armorCheck(mob,I,allowedArmorLevel);
				if((!ok)&&((I.rawWornCode()&CharClass.ARMOR_WEARMASK)>0))
					return false;
			}
		}
		return true;
	}
	
	
	public List<DeadBody> getDeadBodies(Environmental E)
	{
		if(E instanceof DeadBody)
			return new XVector<DeadBody>((DeadBody)E);
		if(E instanceof Container)
		{
			Vector<DeadBody> Bs=new Vector<DeadBody>();
			List<Item> V=((Container)E).getContents();
			for(int v=0;v<V.size();v++)
				Bs.addAll(getDeadBodies((Environmental)V.get(v)));
			return Bs;
		}
		return new Vector<DeadBody>();
	}
	
	
	public DVector parseLootPolicyFor(MOB mob)
	{
		if(mob==null) return new DVector(3);
		Vector<String> lootPolicy=(!mob.isMonster())?new Vector<String>():CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_ITEMLOOTPOLICY),true);
		DVector policies=new DVector(3);
		for(int p=0;p<lootPolicy.size();p++)
		{
			String s=((String)lootPolicy.elementAt(p)).toUpperCase().trim();
			if(s.length()==0) continue;
			MaskingLibrary.CompiledZapperMask compiledMask=null;
			int maskDex=s.indexOf("MASK=");
			if(maskDex>=0)
			{
				s=s.substring(0,maskDex).trim();
				compiledMask=CMLib.masking().maskCompile(((String)lootPolicy.elementAt(p)).substring(maskDex+5).trim());
			}
			else
				compiledMask=MaskingLibrary.CompiledZapperMask.EMPTY();
			Vector<String> parsed=CMParms.parse(s);
			int pct=100;
			for(int x=0;x<parsed.size();x++)
				if(CMath.isInteger((String)parsed.elementAt(x)))
					pct=CMath.s_int((String)parsed.elementAt(x));
				else
				if(CMath.isPct((String)parsed.elementAt(x)))
					pct=(int)Math.round(CMath.s_pct((String)parsed.elementAt(x))*100.0);
			int flags=0;
			if(parsed.contains("RUIN")) flags|=CMMiscUtils.LOOTFLAG_RUIN;
			else
			if(parsed.contains("LOSS")) flags|=CMMiscUtils.LOOTFLAG_LOSS;
			if(flags==0) flags|=CMMiscUtils.LOOTFLAG_LOSS;
			if(parsed.contains("WORN")) flags|=CMMiscUtils.LOOTFLAG_WORN;
			else
			if(parsed.contains("UNWORN")) flags|=CMMiscUtils.LOOTFLAG_UNWORN;
			policies.addElement(Integer.valueOf(pct),Integer.valueOf(flags),compiledMask);
		}
		return policies;
	}
	
	public void confirmWearability(MOB mob)
	{
		if(mob==null) return;
		Race R=mob.charStats().getMyRace();
		DVector reWearSet=new DVector(2);
		Item item=null;
		for(int i=0;i<mob.numItems();i++)
		{
			item=mob.getItem(i);
			if((item!=null)&&(!item.amWearingAt(Wearable.IN_INVENTORY)))
			{
				Long oldCode=Long.valueOf(item.rawWornCode());
				item.unWear();
				if(reWearSet.size()==0)
					reWearSet.addElement(item,oldCode);
				else
				{
					short layer=(item instanceof Armor)?((Armor)item).getClothingLayer():0;
					int d=0;
					for(;d<reWearSet.size();d++)
						if(reWearSet.elementAt(d,1) instanceof Armor)
						{
							if(((Armor)reWearSet.elementAt(d,1)).getClothingLayer()>layer)
								break;
						}
						else
						if(0>layer)
							break;
					if(d>=reWearSet.size())
						reWearSet.addElement(item,oldCode);
					else
						reWearSet.insertElementAt(d,item,oldCode);
				}

			}
		}
		for(int r=0;r<reWearSet.size();r++)
		{
			item=(Item)reWearSet.elementAt(r,1);
			long oldCode=((Long)reWearSet.elementAt(r,2)).longValue();
			int msgCode=CMMsg.MSG_WEAR;
			if((oldCode&Wearable.WORN_WIELD)>0)
				msgCode=CMMsg.MSG_WIELD;
			else
			if((oldCode&Wearable.WORN_HELD)>0)
				msgCode=CMMsg.MSG_HOLD;
			CMMsg msg=CMClass.getMsg(mob,item,null,CMMsg.NO_EFFECT,null,msgCode,null,CMMsg.NO_EFFECT,null);
			if((R.okMessage(mob,msg))
			&&(item.okMessage(item,msg))
			&&((mob.charStats().getWearableRestrictionsBitmap()&oldCode)==0)
			&&(item.canWear(mob,oldCode)))
			   item.wearAt(oldCode);
		}
		// why wasn't that here before?
		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public Item isRuinedLoot(DVector policies, Item I)
	{
		if(I==null) return null;
		if((CMath.bset(I.phyStats().disposition(),PhyStats.IS_UNSAVABLE))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNORUIN))
		||(I instanceof Coins))
			return I;
		if(I.name().toLowerCase().indexOf("ruined ")>=0)
			return I;
		for(int d=0;d<policies.size();d++)
		{
			if((((MaskingLibrary.CompiledZapperMask)policies.elementAt(d,3)).entries.length>0)
			&&(!CMLib.masking().maskCheck((MaskingLibrary.CompiledZapperMask)policies.elementAt(d,3),I,true)))
				continue;
			if(CMLib.dice().rollPercentage()>((Integer)policies.elementAt(d,1)).intValue())
				continue;
			int flags=((Integer)policies.elementAt(d,2)).intValue();
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_WORN)&&I.amWearingAt(Wearable.IN_INVENTORY))
				continue;
			else
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_UNWORN)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
				continue;
			if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_LOSS))
				return null;
			Item I2=CMClass.getItem("GenItem");
			I2.basePhyStats().setWeight(I.basePhyStats().weight());
			I2.setName(I.Name());
			I2.setDisplayText(I.displayText());
			I2.setDescription(I2.description());
			I2.recoverPhyStats();
			I2.setMaterial(I.material());
			String ruinDescAdder=null;
			switch(I2.material()&RawMaterial.MATERIAL_MASK)
			{
				case RawMaterial.MATERIAL_LEATHER:
				case RawMaterial.MATERIAL_CLOTH:
				case RawMaterial.MATERIAL_VEGETATION:
				case RawMaterial.MATERIAL_FLESH:
				case RawMaterial.MATERIAL_PAPER:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is torn and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_WOODEN:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is battered and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_GLASS:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is shattered and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_PRECIOUS:
				case RawMaterial.MATERIAL_SYNTHETIC:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is cracked and ruined beyond repair."; 
					break;
				case RawMaterial.MATERIAL_UNKNOWN:
				case RawMaterial.MATERIAL_ENERGY:
				case RawMaterial.MATERIAL_LIQUID:
				default:
					ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is ruined beyond repair."; 
					break;
			}
			I2.setDescription(CMStrings.endWithAPeriod(I2.description())+" "+ruinDescAdder);
			String oldName=I2.Name();
			I2.setName(CMLib.english().insertUnColoredAdjective(I2.Name(),"ruined"));
			int x=I2.displayText().toUpperCase().indexOf(oldName.toUpperCase());
			I2.setBaseValue(0);
			if(x>=0)
				I2.setDisplayText(I2.displayText().substring(0,x)+I2.Name()+I2.displayText().substring(x+oldName.length()));
			return I2;
		}
		return I;
	}
	
	public void reloadCharClasses(CharClass oldC)
	{
		for(Enumeration<Room> e=CMLib.map().rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if(M==null) continue;
				for(int c=0;c<M.baseCharStats().numClasses();c++)
					if(M.baseCharStats().getMyClass(c)==oldC)
					{
						M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
						break;
					}
				for(int c=0;c<M.charStats().numClasses();c++)
					if(M.charStats().getMyClass(c)==oldC)
					{
						M.charStats().setMyClasses(M.charStats().getMyClassesStr());
						break;
					}
			}
			for(Enumeration<MOB> e2=CMLib.players().players();e2.hasMoreElements();)
			{
				MOB M=(MOB)e2.nextElement();
				for(int c=0;c<M.baseCharStats().numClasses();c++)
					if(M.baseCharStats().getMyClass(c)==oldC)
					{
						M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
						break;
					}
				for(int c=0;c<M.charStats().numClasses();c++)
					if(M.charStats().getMyClass(c)==oldC)
					{
						M.charStats().setMyClasses(M.charStats().getMyClassesStr());
						break;
					}
			}
		}
	}
	
	public void swapRaces(Race newR, Race oldR)
	{
		for(Enumeration<Room> e=CMLib.map().rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if(M==null) continue;
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
					M.charStats().setMyRace(newR);
			}
			for(Enumeration<MOB> e2=CMLib.players().players();e2.hasMoreElements();)
			{
				MOB M=(MOB)e2.nextElement();
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
					M.charStats().setMyRace(newR);
			}
		}
	}
	
	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel)
	{
		MOB rejuvedMOB=CMLib.players().getPlayer(((DeadBody)body).mobName());
		
		if(rejuvedMOB!=null) // doing this here is helpful -- it can trigger a socket error.
			rejuvedMOB.tell("You are being resurrected.");
		
		if((rejuvedMOB!=null)&&(rejuvedMOB.session()!=null)&&(!rejuvedMOB.session().isStopped()))
		{
			if(rejuvedMOB.location()!=corpseRoom)
			{
				rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappears!");
				corpseRoom.bringMobHere(rejuvedMOB,false);
			}
			Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
			if(A!=null) rejuvedMOB.delAbility(A);
			A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
			if(A!=null) rejuvedMOB.delEffect(A);

			int it=0;
			while(it<rejuvedMOB.location().numItems())
			{
				Item item=rejuvedMOB.location().getItem(it);
				if((item!=null)&&(item.container()==body))
				{
					CMMsg msg2=CMClass.getMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg2);
					CMMsg msg3=CMClass.getMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg3);
					it=0;
				}
				else
					it++;
			}
			body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
			body.destroy();
			rejuvedMOB.basePhyStats().setDisposition(CMath.unsetb(rejuvedMOB.basePhyStats().disposition(),PhyStats.IS_SITTING));
			rejuvedMOB.phyStats().setDisposition(CMath.unsetb(rejuvedMOB.basePhyStats().disposition(),PhyStats.IS_SITTING));
			rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
			corpseRoom.recoverRoomStats();
			Vector<String> whatsToDo=CMParms.parse(CMProps.getVar(CMProps.SYSTEM_PLAYERDEATH));
			for(int w=0;w<whatsToDo.size();w++)
			{
				String whatToDo=(String)whatsToDo.elementAt(w);
				if(whatToDo.startsWith("UNL"))
					CMLib.leveler().level(rejuvedMOB);
				else
				if(whatToDo.startsWith("ASTR"))
				{}
				else
				if(whatToDo.startsWith("PUR"))
				{}
				else
				if((whatToDo.trim().equals("0"))||(CMath.s_int(whatToDo)>0))
				{
					if(XPLevel>=0)
					{
						int expLost=(CMath.s_int(whatToDo)+(2*XPLevel))/2;
						rejuvedMOB.tell("^*You regain "+expLost+" experience points.^?^.");
						CMLib.leveler().postExperience(rejuvedMOB,null,null,expLost,false);
					}
				}
				else
				if(whatToDo.length()<3)
					continue;
				else
				if(XPLevel>=0)
				{
					double lvl=(double)body.phyStats().level();
					for(int l=body.phyStats().level();l<rejuvedMOB.phyStats().level();l++)
						lvl=lvl/2.0;
					int expRestored=(int)Math.round(((100.0+(2.0*((double)XPLevel)))*lvl)/2.0);
					rejuvedMOB.tell("^*You regain "+expRestored+" experience points.^?^.");
					CMLib.leveler().postExperience(rejuvedMOB,null,null,expRestored,false);
				}
			}
			return true;
		}
		else
			corpseRoom.show(tellMob,body,CMMsg.MSG_OK_VISUAL,"<T-NAME> twitch(es) for a moment, but the spirit is too far gone.");
		return false;
	}
	
	public long[][] compileConditionalRange(List<String> condV, int numDigits, final int startOfRange, final int endOfRange)
	{
		long[][] finalSet = new long[endOfRange - startOfRange + 1][];
		for(String cond : condV)
		{
			Vector<String> V=CMParms.parse(cond.trim());
			if(V.size()<2) continue;
			long[] vals=new long[numDigits];
			for(int i=0;i<numDigits;i++)
				if(i+1<V.size())
					vals[i]=CMath.s_long(V.elementAt(i+1));
			cond=((String)V.firstElement()).trim();
			int start=startOfRange;
			int finish=endOfRange;
			if(cond.startsWith("<="))
				finish=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith(">="))
				start=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith("=="))
			{
				start=CMath.s_int(cond.substring(2).trim());
				finish=start;
			}
			else
			if(cond.startsWith("="))
			{
				start=CMath.s_int(cond.substring(1).trim());
				finish=start;
			}
			else
			if(cond.startsWith(">"))
				start=CMath.s_int(cond.substring(1).trim())+1;
			else
			if(cond.startsWith("<"))
				finish=CMath.s_int(cond.substring(1).trim())-1;

			if((start>=startOfRange)&&(finish<=endOfRange)&&(start<=finish))
			{
				for(int s=start;s<=finish;s++)
				{
					if(finalSet[s-startOfRange]==null)
						finalSet[s-startOfRange] = vals;
				}
			}
		}
		return finalSet;
	}
	
	public String builtPrompt(MOB mob)
	{
		StringBuffer buf=new StringBuffer("\n\r");
		String prompt=mob.playerStats().getPrompt();
		String promptUp=null;
		int c=0;
		while(c<prompt.length())
			if((prompt.charAt(c)=='%')&&(c<(prompt.length()-1)))
			{
				switch(prompt.charAt(++c))
				{
				case '-':
					if(c<(prompt.length()-2))
					{
						if(promptUp==null) promptUp=prompt.toUpperCase();
						String promptSub=promptUp.substring(c+1);
						Wearable.CODES wcodes = Wearable.CODES.instance();
						boolean isFound=false;
						for(long code : wcodes.all())
							if(promptSub.startsWith(wcodes.nameup(code)))
							{
								c+=1+wcodes.nameup(code).length();
								Item I=mob.fetchFirstWornItem(code);
								if(I!=null)
									buf.append(I.name());
								isFound=true;
								break;
							}
						if(!isFound)
						{
							CharStats.CODES ccodes = CharStats.CODES.instance();
							for(int code : ccodes.all())
								if(promptSub.startsWith(ccodes.name(code)))
								{
									c+=1+ccodes.name(code).length();
									buf.append(mob.charStats().getStat(code));
									isFound=true;
									break;
								}
							if(!isFound)
							for(int code : ccodes.all())
								if(promptSub.startsWith("BASE "+ccodes.name(code)))
								{
									buf.append(mob.baseCharStats().getStat(code));
									c+=6+ccodes.name(code).length();
									isFound=true;
									break;
								}
						}
						if(!isFound)
						{
							for(String s : mob.phyStats().getStatCodes())
								if(promptSub.startsWith(s))
								{
									c+=1+s.length();
									buf.append(mob.phyStats().getStat(s));
									isFound=true;
									break;
								}
							if(!isFound)
							for(String s : mob.basePhyStats().getStatCodes())
								if(promptSub.startsWith("BASE "+s))
								{
									c+=6+s.length();
									buf.append(mob.basePhyStats().getStat(s));
									isFound=true;
									break;
								}
						}
						if(!isFound)
						{
							for(final String s : mob.curState().getStatCodes())
								if(promptSub.startsWith(s))
								{
									c+=1+s.length();
									buf.append(mob.curState().getStat(s));
									isFound=true;
									break;
								}
							if(!isFound)
							for(final String s : mob.maxState().getStatCodes())
								if(promptSub.startsWith("MAX "+s))
								{
									c+=5+s.length();
									buf.append(mob.maxState().getStat(s));
									isFound=true;
									break;
								}
							if(!isFound)
							for(final String s : mob.baseState().getStatCodes())
								if(promptSub.startsWith("BASE "+s))
								{
									c+=6+s.length();
									buf.append(mob.baseState().getStat(s));
									isFound=true;
									break;
								}
						}
					}
					break;
				case 'a': { buf.append(CMLib.factions().getRangePercent(CMLib.factions().AlignID(),mob.fetchFaction(CMLib.factions().AlignID()))+"%"); c++; break; }
				case 'A': { final Faction.FRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),mob.fetchFaction(CMLib.factions().AlignID()));buf.append((FR!=null)?FR.name():""+mob.fetchFaction(CMLib.factions().AlignID())); c++; break;}
				case 'B': { buf.append("\n\r"); c++; break;}
				case 'c': { buf.append(mob.numItems()); c++; break;}
				case 'C': { buf.append(mob.maxItems()); c++; break;}
				case 'd': {   final MOB victim=mob.getVictim();
							  if((mob.isInCombat())&&(victim!=null))
								  buf.append(""+mob.rangeToTarget());
							  c++; break; }
				case 'D': {   final Item I=mob.fetchWieldedItem();
							  if((I instanceof Weapon)&&(((Weapon)I).requiresAmmunition()))
								  buf.append(""+((Weapon)I).ammunitionRemaining());
							  break;
						  }
				case 'e': {   MOB victim=mob.getVictim();
							  if((mob.isInCombat())&&(victim!=null)&&(CMLib.flags().canBeSeenBy(victim,mob)))
								  buf.append(victim.displayName(mob));
							  c++; break; }
				case 'E': {   MOB victim=mob.getVictim();
							  if((mob.isInCombat())&&(victim!=null)&&(!victim.amDead())&&(CMLib.flags().canBeSeenBy(victim,mob)))
								  buf.append(victim.healthText(mob)+"\n\r");
							  c++; break; }
				case 'g': { buf.append((int)Math.round(Math.floor(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)/CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(mob))))); c++; break;}
				case 'G': { buf.append(CMLib.beanCounter().nameCurrencyShort(mob,CMLib.beanCounter().getTotalAbsoluteNativeValue(mob))); c++; break;}
				case 'h': { buf.append("^<Hp^>"+mob.curState().getHitPoints()+"^</Hp^>"); c++; break;}
				case 'H': { buf.append("^<MaxHp^>"+mob.maxState().getHitPoints()+"^</MaxHp^>"); c++; break;}
				case 'I': {   if((CMLib.flags().isCloaked(mob))
							  &&(((mob.phyStats().disposition()&PhyStats.IS_NOT_SEEN)!=0)))
								  buf.append("Wizinvisible");
							  else
							  if(CMLib.flags().isCloaked(mob))
								  buf.append("Cloaked");
							  else
							  if(!CMLib.flags().isSeen(mob))
								  buf.append("Undetectable");
							  else
							  if(CMLib.flags().isInvisible(mob)&&CMLib.flags().isHidden(mob))
								  buf.append("Hidden/Invisible");
							  else
							  if(CMLib.flags().isInvisible(mob))
								  buf.append("Invisible");
							  else
							  if(CMLib.flags().isHidden(mob))
								  buf.append("Hidden");
							  c++; break;}
				case 'K':
				case 'k': { MOB tank=mob;
							if((tank.getVictim()!=null)
							&&(tank.getVictim().getVictim()!=null)
							&&(tank.getVictim().getVictim()!=mob))
								tank=tank.getVictim().getVictim();
							if(((c+1)<prompt.length())&&(tank!=null))
								switch(prompt.charAt(c+1))
								{
									case 'h': { buf.append(tank.curState().getHitPoints()); c++; break;}
									case 'H': { buf.append(tank.maxState().getHitPoints()); c++; break;}
									case 'm': { buf.append(tank.curState().getMana()); c++; break;}
									case 'M': { buf.append(tank.maxState().getMana()); c++; break;}
									case 'v': { buf.append(tank.curState().getMovement()); c++; break;}
									case 'V': { buf.append(tank.maxState().getMovement()); c++; break;}
									case 'e': {   buf.append(tank.displayName(mob)); c++; break;}
									case 'E': {   if((mob.isInCombat())&&(CMLib.flags().canBeSeenBy(tank,mob)))
													  buf.append(tank.healthText(mob)+"\n\r");
												  c++;
												  break;
											  }
								}
							c++;
							break;
						  }
				case 'm': { buf.append("^<Mana^>"+mob.curState().getMana()+"^</Mana^>"); c++; break;}
				case 'M': { buf.append("^<MaxMana^>"+mob.maxState().getMana()+"^</MaxMana^>"); c++; break;}
				case 'r': {   if(mob.location()!=null)
							  buf.append(mob.location().displayText());
							  c++; break; }
				case 'R': {   if((mob.location()!=null)&&CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SYSMSGS))
							  buf.append(mob.location().roomID());
							  c++; break; }
				case 'v': { buf.append("^<Move^>"+mob.curState().getMovement()+"^</Move^>"); c++; break;}
				case 'V': { buf.append("^<MaxMove^>"+mob.maxState().getMovement()+"^</MaxMove^>"); c++; break;}
				case 'w': { buf.append(mob.phyStats().weight()); c++; break;}
				case 'W': { buf.append(mob.maxCarry()); c++; break;}
				case 'x': { buf.append(mob.getExperience()); c++; break;}
				case 'X': {
							  if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
								buf.append("N/A");
							  else
								buf.append(mob.getExpNeededLevel());
							  c++; break;
						  }
				case 'z': {   if(mob.location()!=null)
								  buf.append(mob.location().getArea().name());
							  c++; break; }
				case 't': { 	 if(mob.location()!=null)
								  buf.append(CMStrings.capitalizeAndLower(TimeClock.TOD_DESC[mob.location().getArea().getTimeObj().getTODCode()].toLowerCase()));
							  c++; break;
						  }
				case 'T': { 	 if(mob.location()!=null)
								  buf.append(mob.location().getArea().getTimeObj().getTimeOfDay());
							  c++; break;
						  }
				case '@': { 	 if(mob.location()!=null)
								  buf.append(mob.location().getArea().getClimateObj().weatherDescription(mob.location()));
							  c++; break;
						  }
				default:{ buf.append("%"+prompt.charAt(c)); c++; break;}
				}
			}
			else
				buf.append(prompt.charAt(c++));
		return buf.toString();
	}
}

