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
import com.planet_ink.marble_mud.Libraries.interfaces.MaskingLibrary;
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
public class Prop_SpellAdder extends Property implements AbilityContainer, TriggeredAffect
{
	public String ID() { return "Prop_SpellAdder"; }
	public String name(){ return "Casting spells on oneself";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	protected Physical lastMOB=null;
	protected MOB invokerMOB=null;
	protected boolean processing=false;
	protected boolean uninvocable=true;
	protected int level=-1;
	protected int chanceToHappen=-1;
	protected List<Ability> spellV=null;
	protected MaskingLibrary.CompiledZapperMask compiledMask=null;
	protected List<Ability> unrevocableSpells = null;
	
	public long flags(){return Ability.FLAG_CASTER;}

	public int triggerMask()
	{ 
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	protected void finalize()
	{
		spellV=null;
		compiledMask=null;
		chanceToHappen=-1;
		unrevocableSpells=null;
		if((invokerMOB!=null)&&(invokerMOB.Name().equals("invoker")))
			invokerMOB.destroy();
	}
	
	public String getMaskString(String newText)
	{
		int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
			return newText.substring(maskindex+5).trim();
		return "";
	}
	
	public String getParmString(String newText)
	{
		int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
			return newText.substring(0,maskindex).trim();
		return newText;
	}
	
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		compiledMask=null;
		lastMOB=null;
		chanceToHappen=-1;
		String maskString=getMaskString(newText);
		if(maskString.length()>0)
			compiledMask=CMLib.masking().getPreCompiledMask(maskString);
	}
	
	public List<Ability> getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=new Vector();
		String names=getParmString(text());
		Vector set=CMParms.parseSemicolons(names,true);
		String thisOne=null;
		for(int s=0;s<set.size();s++)
		{
			thisOne=(String)set.elementAt(s);
			if(thisOne.equalsIgnoreCase("NOUNINVOKE"))
			{
				this.uninvocable=false;
				continue;
			}
			if(thisOne.toUpperCase().startsWith("LEVEL"))
			{
				level=CMParms.getParmInt(thisOne,"LEVEL",-1);
				if(level>=0)
					continue;
			}
			int pctDex=thisOne.indexOf("% ");
			if((pctDex>0) && (thisOne.substring(pctDex+1).trim().length()>0))
				thisOne=thisOne.substring(pctDex+1).trim();
			String parm="";
			if(thisOne.endsWith(")"))
			{
				int x=thisOne.indexOf('(');
				if(x>0)
				{
					parm=thisOne.substring(x+1,thisOne.length()-1);
					thisOne=thisOne.substring(0,x).trim();
				}
			}

			Ability A=CMClass.getAbility(thisOne);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
			{
				A=(Ability)A.copyOf();
				A.setMiscText(parm);
				spellV.add(A);
			}
		}
		return spellV;
	}

	public boolean didHappen()
	{
		if(chanceToHappen<0)
		{
			String parmString=getParmString(text());
			int x=parmString.indexOf('%');
			if(x<0)
			{
				chanceToHappen=100;
				return true;
			}
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(parmString.charAt(x)))
					tot+=CMath.s_int(""+parmString.charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			chanceToHappen=tot;
		}
		if(CMLib.dice().rollPercentage()<=chanceToHappen)
			return true;
		return false;
	}
	
	public Map<String, String> makeMySpellsH(List<Ability> V)
	{
		Hashtable<String, String> spellH=new Hashtable<String, String>();
		for(int v=0;v<V.size();v++)
			spellH.put(((Ability)V.get(v)).ID(),((Ability)V.get(v)).ID());
		return spellH;
	}


	public MOB getBestInvokerMOB(Environmental target)
	{
		if(target instanceof MOB)
			return (MOB)target;
		if((target instanceof Item)&&(((Item)target).owner()!=null)&&(((Item)target).owner() instanceof MOB))
			return (MOB)((Item)target).owner();
		return null;
	}
	
	public MOB getInvokerMOB(Environmental source, Environmental target)
	{
		MOB mob=getBestInvokerMOB(affected);
		if(mob==null) mob=getBestInvokerMOB(source);
		if(mob==null) mob=getBestInvokerMOB(target);
		if(mob==null) mob=invokerMOB;
		if(mob==null)
		{
			Room R=CMLib.map().roomLocation(target);
			if(R==null) R=CMLib.map().roomLocation(target);
			if(R==null) R=CMLib.map().getRandomRoom();
			mob=CMLib.map().getFactoryMOB(R);
			mob.setName("invoker");
			mob.basePhyStats().setLevel(affected.phyStats().level());
			mob.phyStats().setLevel(affected.phyStats().level());
		}
		invokerMOB=mob;
		return invokerMOB;
	}

	public List convertToV2(List<Ability> spellsV, Physical target)
	{
		List VTOO=new Vector();
		for(int v=0;v<spellsV.size();v++)
		{
			Ability A=(Ability)spellsV.get(v);
			Ability EA=(target!=null)?target.fetchEffect(A.ID()):null;
			if((EA==null)&&(didHappen()))
			{
				String t=A.text();
				A=(Ability)A.copyOf();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					int x=t.indexOf('/');
					if(x<0)
					{
						V2=CMParms.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=CMParms.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				VTOO.add(A);
				VTOO.add(V2);
			}
		}
		return VTOO;
	}
	
	public boolean addMeIfNeccessary(PhysicalAgent source, Physical target, boolean makeLongLasting, int asLevel)
	{
		List<Ability> V=getMySpellsV();
		if((target==null)
		||(V.size()==0)
		||((compiledMask!=null)
			&&(!CMLib.masking().maskCheck(compiledMask,target,true))))
				return false;
		List VTOO=convertToV2(V,target);
		if(VTOO.size()==0) return false;
		MOB qualMOB=getInvokerMOB(source,target);
		for(int v=0;v<VTOO.size();v+=2)
		{
			Ability A=(Ability)VTOO.get(v);
			Vector V2=(Vector)VTOO.get(v+1);
			if(level >= 0)
				asLevel = level;
			else
			if(asLevel <=0)
				asLevel = (affected!=null)?affected.phyStats().level():0;
			A.invoke(qualMOB,V2,target,true,asLevel);
			Ability EA=target.fetchEffect(A.ID());
			lastMOB=target;
			// this needs to go here because otherwise it makes non-item-invoked spells long lasting,
			// which means they dont go away when item is removed.
			if((EA!=null)&&(makeLongLasting))
			{
				EA.makeLongLasting();
				if(!uninvocable) {
					EA.makeNonUninvokable();
					if(unrevocableSpells == null)
						unrevocableSpells = new Vector();
					unrevocableSpells.add(EA);
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String s=CMParms.combine(commands,0);
		if(s.length()>0) setMiscText(s);
		if(givenTarget!=null)
			addMeIfNeccessary(mob,givenTarget,false,asLevel);
		else
		{
			List<Ability> V=getMySpellsV();
			commands.clear();
			commands.addAll(convertToV2(V,null));
		}
		return true;
	}
	
	public String accountForYourself()
	{ return spellAccountingsWithMask("Casts "," on the first one who enters.");}
	
	public void removeMyAffectsFromLastMOB()
	{
		removeMyAffectsFrom(lastMOB);
		lastMOB=null;
	}
	
	public void setAffectedOne(Physical P) 
	{
		super.setAffectedOne(P);
		if(P == null)
		{
			removeMyAffectsFromLastMOB();
			finalize();
		}
	}
	
	public void removeMyAffectsFrom(Physical P)
	{
		if(P==null)return;
		
		int x=0;
		Vector eff=new Vector();
		Ability thisAffect=null;
		for(x=0;x<P.numEffects();x++) // personal
		{
			thisAffect=P.fetchEffect(x);
			if(thisAffect!=null)
				eff.addElement(thisAffect);
		}
		if(eff.size()>0)
		{
			Map<String,String> h=makeMySpellsH(getMySpellsV());
			if(unrevocableSpells != null)
			{
				for(int v=unrevocableSpells.size()-1;v>=0;v--)
				{
					thisAffect = (Ability)unrevocableSpells.get(v);
					if(h.containsKey(thisAffect.ID()))
						P.delEffect(thisAffect);
				}
			}
			else
			for(x=0;x<eff.size();x++)
			{
				thisAffect=(Ability)eff.elementAt(x);
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)
				&&(thisAffect.invoker()==getInvokerMOB(P,P))) {
					thisAffect.unInvoke();
					if((!uninvocable)&&(!thisAffect.canBeUninvoked()))
						P.delEffect(thisAffect);
				}
			}
			unrevocableSpells = null;
		}
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((affected instanceof Room)||(affected instanceof Area))
		{
			if((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
				removeMyAffectsFrom(msg.source());
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
				addMeIfNeccessary(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(host,msg);
	}

	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		if(processing) return;
		if((affected instanceof MOB)
		   ||(affected instanceof Item))
		{
			processing=true;
			if((lastMOB!=null)
			 &&(host!=lastMOB))
				removeMyAffectsFrom(lastMOB);

			if((lastMOB==null)&&(host instanceof PhysicalAgent))
				addMeIfNeccessary((PhysicalAgent)host,(Physical)host,true,0);
			processing=false;
		}
	}
	
	public String spellAccountingsWithMask(String pre, String post)
	{
		List<Ability> spellList=getMySpellsV();
		String id="";
		for(int v=0;v<spellList.size();v++)
		{
			Ability A=(Ability)spellList.get(v);
			if(spellList.size()==1)
				id+=A.name();
			else
			if(v==(spellList.size()-1))
				id+="and "+A.name();
			else
				id+=A.name()+", ";
		}
		if(spellList.size()>0)
			id=pre+id+post;
		String maskString=getMaskString(text());
		if(maskString.length()>0)
			id+="  Restrictions: "+CMLib.masking().maskDesc(maskString);
		return id;
	}

	public void addAbility(Ability to){}
	public void delAbility(Ability to){}
	public int numAbilities()
	{
		return getMySpellsV().size();
	}
	public Ability fetchAbility(int index)
	{
		List<Ability> spellsV=getMySpellsV();
		if(spellsV.size()==0) return null;
		if((index<0)||(index>=spellsV.size()))
			return null;
		try
		{
			return spellsV.get(index);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	public Ability fetchAbility(String ID)
	{
		for(Enumeration<Ability> a=abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A==null) continue;
			if(A.ID().equalsIgnoreCase(ID))
				return A;
		}
		return null;
	}
	public Ability fetchRandomAbility()
	{
		List<Ability> spellsV=getMySpellsV();
		if(spellsV.size()==0) return null;
		return spellsV.get(CMLib.dice().roll(1, spellsV.size(), -1));
	}
	public Enumeration<Ability> abilities(){
		return new IteratorEnumeration<Ability>(getMySpellsV().iterator());
	}
	public void delAllAbilities(){ setMiscText("");}
	public int numAllAbilities() { return numAbilities();}
	public Enumeration<Ability> allAbilities(){ return abilities();}

}
