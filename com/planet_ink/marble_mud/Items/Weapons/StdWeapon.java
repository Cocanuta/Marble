package com.planet_ink.marble_mud.Items.Weapons;
import com.planet_ink.marble_mud.Items.Basic.StdItem;
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
public class StdWeapon extends StdItem implements Weapon
{
	public String ID(){    return "StdWeapon";}
	protected int     weaponType=TYPE_NATURAL;
	protected int     weaponClassification=CLASS_NATURAL;
	protected boolean useExtendedMissString=false;
	protected int     minRange=0;
	protected int     maxRange=0;
	protected int     ammoCapacity=0;
	protected long    lastReloadTime=0;

	public StdWeapon()
	{
		super();

		setName("weapon");
		setDisplayText(" sits here.");
		setDescription("This is a deadly looking weapon.");
		wornLogicalAnd=false;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		basePhyStats().setAbility(0);
		baseGoldValue=15;
		material=RawMaterial.RESOURCE_STEEL;
		setUsesRemaining(100);
		recoverPhyStats();
	}


	public int weaponType(){return weaponType;}
	public int weaponClassification(){return weaponClassification;}
	public void setWeaponType(int newType){weaponType=newType;}
	public void setWeaponClassification(int newClassification){weaponClassification=newClassification;}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(phyStats().ability()>0)
			id=name()+" +"+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		else
		if(phyStats().ability()<0)
			id=name()+" "+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		return id+"\n\rAttack: "+phyStats().attackAdjustment()+", Damage: "+phyStats().damage();
	}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(amWearingAt(Wearable.WORN_WIELD))
		{
			if(phyStats().attackAdjustment()!=0)
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(phyStats().attackAdjustment()));
			if(phyStats().damage()!=0)
				affectableStats.setDamage(affectableStats.damage()+phyStats().damage());
		}
	}
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(phyStats().damage()!=0)
		{
			phyStats().setDamage(phyStats().damage()+(phyStats().ability()*2));
			phyStats().setAttackAdjustment(phyStats().attackAdjustment()+(phyStats().ability()*10));
		}
		if((subjectToWearAndTear())&&(usesRemaining()<100))
			phyStats().setDamage(((int)Math.round(CMath.mul(phyStats().damage(),CMath.div(usesRemaining(),100)))));
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(CMLib.flags().canBeSeenBy(this,msg.source()))
				{
					if(requiresAmmunition())
						msg.source().tell(ammunitionType()+" remaining: "+ammunitionRemaining()+"/"+ammunitionCapacity()+".");
					if((subjectToWearAndTear())&&(usesRemaining()<100))
						msg.source().tell(weaponHealth());
				}
				break;
			case CMMsg.TYP_RELOAD:
				if(msg.tool() instanceof Ammunition)
				{
					boolean recover=false;
					final Ammunition I=(Ammunition)msg.tool();
					int howMuchToTake=ammunitionCapacity();
					if(I.usesRemaining()<howMuchToTake)
						howMuchToTake=I.usesRemaining();
					setAmmoRemaining(howMuchToTake);
					I.setUsesRemaining(I.usesRemaining()-howMuchToTake);
					LinkedList<Ability> removeThese=new LinkedList<Ability>();
					for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
							removeThese.add(A);
					}
					for(Ability A : removeThese)
						delEffect(A);
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						if((A!=null)&&(A.isSavable())&&(fetchEffect(A.ID())==null))
						{
							A=(Ability)A.copyOf();
							A.setInvoker(null);
							A.setSavable(false);
							addEffect(A);
							recover=true;
						}
					}
					if(I.usesRemaining()<=0)
						I.destroy();
					if(recover) recoverOwner();
				}
				break;
			case CMMsg.TYP_UNLOAD:
				if(msg.tool() instanceof Ammunition)
				{
					final Ammunition ammo=(Ammunition)msg.tool();
					for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
						{
							final Ability ammoA=(Ability)A.copyOf();
							ammo.addNonUninvokableEffect(ammoA);
						}
					}
					setAmmoRemaining(0);
				}
				break;
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==this)
		&&(amWearingAt(Wearable.WORN_WIELD))
		&&(weaponClassification()!=Weapon.CLASS_NATURAL)
		&&(weaponType()!=Weapon.TYPE_NATURAL)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&((msg.value())>0)
		&&(owner()!=null)
		&&(owner() instanceof MOB)
		&&(msg.amISource((MOB)owner())))
		{
			int hurt=(msg.value());
			MOB tmob=(MOB)msg.target();
			if((hurt>(tmob.maxState().getHitPoints()/10)||(hurt>50))
			&&(tmob.curState().getHitPoints()>hurt))
			{
				if((!tmob.isMonster())
				   &&(CMLib.dice().rollPercentage()==1)
				   &&(CMLib.dice().rollPercentage()>(tmob.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
				   &&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					Ability A=null;
					if(subjectToWearAndTear()
					&&(usesRemaining()<25)
					&&((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
					{
						if(CMLib.dice().rollPercentage()>50)
							A=CMClass.getAbility("Disease_Lockjaw");
						else
							A=CMClass.getAbility("Disease_Tetanus");
					}
					else
						A=CMClass.getAbility("Disease_Infection");

					if((A!=null)&&(tmob.fetchEffect(A.ID())==null))
						A.invoke(msg.source(),tmob,true,phyStats().level());
				}
			}

			if((subjectToWearAndTear())
			&&(CMLib.dice().rollPercentage()==1)
			&&(msg.source().rangeToTarget()==0)
			&&(CMLib.dice().rollPercentage()>((phyStats().level()/2)+(10*phyStats().ability())+(CMLib.flags().isABonusItems(this)?20:0)))
			&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY))
			{
				setUsesRemaining(usesRemaining()-1);
				recoverPhyStats();
				if((usesRemaining()<10)
				&&(owner()!=null)
				&&(owner() instanceof MOB)
				&&(usesRemaining()>0))
					((MOB)owner()).tell(name()+" is nearly destroyed! ("+usesRemaining()+"%");
				else
				if((usesRemaining()<=0)
				&&(owner()!=null)
				&&(owner() instanceof MOB))
				{
					MOB owner=(MOB)owner();
					setUsesRemaining(100);
					msg.addTrailerMsg(CMClass.getMsg(((MOB)owner()),null,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" is destroyed!!^?",CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" being wielded by <S-NAME> is destroyed!^?"));
					unWear();
					destroy();
					owner.recoverPhyStats();
					owner.recoverCharStats();
					owner.recoverMaxState();
					if(owner.location()!=null)
						owner.location().recoverRoomStats();
				}
			}
		}
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(msg.tool()==this)
		   &&(requiresAmmunition())
		   &&(ammunitionCapacity()>0))
		{
			if(ammunitionRemaining()>ammunitionCapacity())
				setAmmoRemaining(ammunitionCapacity());
			if(ammunitionRemaining()<=0)
			{
				msg.source().tell("Your "+name()+" is out of "+ammunitionType()+".");
				if((msg.source().isMine(this))
				   &&(msg.source().location()!=null)
				   &&(CMLib.flags().aliveAwakeMobile(msg.source(),true))
				   &&(lastReloadTime < (System.currentTimeMillis() - CMProps.getTickMillis())))
				{
					lastReloadTime=System.currentTimeMillis();
					if((!msg.source().isMonster())||inventoryAmmoCheck(msg.source()))
						msg.source().enqueCommand(CMParms.parse("LOAD ALL \"$"+name()+"$\""), 0, 0);
					else
						msg.source().enqueCommand(CMParms.parse("REMOVE \"$"+name()+"$\""), 0, 0);
				}
				return false;
			}
			else
				setUsesRemaining(usesRemaining()-1);
		}
		return true;
	}

	public boolean inventoryAmmoCheck(MOB M)
	{
		if(M==null) return false;
		for(Enumeration<Item> i=M.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I!=null)&&(I instanceof Ammunition)&&(((Ammunition)I).ammunitionType().equalsIgnoreCase(ammunitionType())))
				return true;
		}
		return false;
	}
	
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	protected String weaponHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=95)
			return name()+" looks slightly used ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>=85)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is somewhat dull ("+usesRemaining()+"%)";
			default:
				 return name()+" is somewhat worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=75)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is dull ("+usesRemaining()+"%)";
			default:
				 return name()+" is worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>50)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" has some notches and chinks ("+usesRemaining()+"%)";
			default:
				return name()+" is damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>25)
			return name()+" is heavily damaged ("+usesRemaining()+"%)";
		else
			return name()+" is so damaged, it is practically harmless ("+usesRemaining()+"%)";
	}
	public String missString()
	{
		return CMLib.combat().standardMissString(weaponType,weaponClassification,name(),useExtendedMissString);
	}
	public String hitString(int damageAmount)
	{
		return CMLib.combat().standardHitString(weaponType, weaponClassification,damageAmount,name());
	}
	public int minRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMINRANGE))
			return 0;
		return minRange;
	}
	public int maxRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		return maxRange;
	}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	public boolean requiresAmmunition()
	{
		if((readableText()==null)||(this instanceof Wand))
			return false;
		return readableText().length()>0;
	}
	public void setAmmunitionType(String ammo)
	{
		if(!(this instanceof Wand))
			setReadableText(ammo);
	}
	public String ammunitionType()
	{
		return readableText();
	}
	public int ammunitionRemaining()
	{
		return usesRemaining();
	}
	public void setAmmoRemaining(int amount)
	{
		final int oldAmount=ammunitionRemaining();
		if(amount==Integer.MAX_VALUE)
			amount=20;
		setUsesRemaining(amount);
		final ItemPossessor myOwner=owner;
		if((oldAmount>0)
		&&(amount==0)
		&&(myOwner instanceof MOB)
		&&(ammunitionCapacity()>0))
		{
			boolean recover=false;
			for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
				{
					recover=true;
					delEffect(A);
				}
			}
			if(recover) recoverOwner();
		}
	}
	public int ammunitionCapacity(){return ammoCapacity;}
	public void setAmmoCapacity(int amount){ammoCapacity=amount;}
	public int value()
	{
		if((subjectToWearAndTear())&&(usesRemaining()<1000))
			return (int)Math.round(CMath.mul(super.value(),CMath.div(usesRemaining(),100)));
		return super.value();
	}
	public boolean subjectToWearAndTear()
	{
		return((!requiresAmmunition())
			&&(!(this instanceof Wand))
			&&(usesRemaining()<=1000)
			&&(usesRemaining()>=0));
	}
	
	public void recoverOwner()
	{
		final ItemPossessor myOwner=owner;
		if(myOwner instanceof MOB)
		{
			((MOB)myOwner).recoverCharStats();
			((MOB)myOwner).recoverMaxState();
			((MOB)myOwner).recoverPhyStats();
		}
		else
		if(myOwner!=null)
			myOwner.recoverPhyStats();
	}

}
