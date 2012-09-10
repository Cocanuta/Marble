package com.planet_ink.marble_mud.Behaviors;
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
public class ItemIdentifier extends StdBehavior
{
	public String ID(){return "ItemIdentifier";}
	
	private LinkedList<CMath.CompiledOperation> costFormula = null;
	
	public String accountForYourself()
	{ 
		return "item identifying for a price";
	}

	protected double cost(Item item)
	{
		if(costFormula != null)
		{
			double[] vars = {item.phyStats().level(), item.value(), item.usesRemaining(), CMLib.flags().isABonusItems(item)?1.0:0.0,item.basePhyStats().level(), item.baseGoldValue(),0,0,0,0,0};
			return CMath.parseMathExpression(costFormula, vars, 0.0);
		}
		else
			return 500+(item.phyStats().level()*20);
	}

	public void setParms(String parms)
	{
		super.setParms(parms);
		String formulaString = CMParms.getParmStr(parms,"COST","500 + (@x1 * 20)");
		costFormula = null;
		if(formulaString.trim().length()>0)
		{
			try
			{
				costFormula = CMath.compileMathExpression(formulaString);
			}
			catch(Exception e)
			{
				Log.errOut(ID(),"Error compiling formula: " + formulaString);
			}
		}
	}
	
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			double cost=cost((Item)msg.tool());
			if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),observer)<((double)cost))
			{
				String costStr=CMLib.beanCounter().nameCurrencyShort(observer,(double)cost);
				CMLib.commands().postSay(observer,source,"You'll need "+costStr+" for me to identify that.",true,false);
				return false;
			}
			return true;
		}
		return true;
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		MOB source=msg.source();

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(!(msg.tool() instanceof Coins))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			Item I = (Item)msg.tool();
			double cost=cost(I);
			CMLib.beanCounter().subtractMoney(source,CMLib.beanCounter().getCurrency(observer),(double)cost);
			String costStr=CMLib.beanCounter().nameCurrencyLong(observer,(double)cost);
			source.recoverPhyStats();
			CMMsg newMsg=CMClass.getMsg(msg.source(),observer,null,CMMsg.MSG_OK_ACTION,"<S-NAME> give(s) "+costStr+" to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,I,null,CMMsg.MSG_EXAMINE,"<S-NAME> examine(s) <T-NAME> very closely.");
			msg.addTrailerMsg(newMsg);
			StringBuffer up=new StringBuffer(I.name()+" is made of "+RawMaterial.CODES.NAME(I.material()).toLowerCase()+".\n\r");
			if((I instanceof Armor)&&(((Armor)I).phyStats().height()>0))
				up.append("It is a size "+((Armor)I).phyStats().height()+".\n\r");
			int weight=I.phyStats().weight();
			if((weight!=I.basePhyStats().weight())&&(I instanceof Container))
				up.append("It weighs "+I.basePhyStats().weight()+" pounds empty and "+weight+" pounds right now.\n\r");
			else
				up.append("It weighs "+weight+" pounds.\n\r");
			if(I instanceof Weapon)
			{
				Weapon w=(Weapon)I;
				up.append("It is a "+Weapon.CLASS_DESCS[w.weaponClassification()].toLowerCase()+" weapon.\n\r");
				up.append("It does "+Weapon.TYPE_DESCS[w.weaponType()].toLowerCase()+" damage.\n\r");
			}
			up.append(((Item)I).secretIdentity());
			newMsg=CMClass.getMsg(observer,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+up.toString()+"'^?.");
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,source,I,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=CMClass.getMsg(observer,I,null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}
