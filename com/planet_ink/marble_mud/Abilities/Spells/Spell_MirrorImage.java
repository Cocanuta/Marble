package com.planet_ink.marble_mud.Abilities.Spells;
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
@SuppressWarnings("rawtypes")
public class Spell_MirrorImage extends Spell
{
	public String ID() { return "Spell_MirrorImage"; }
	public String name(){return "Mirror Image";}
	public String displayText(){return "(Mirror Image spell)";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}

	private	Random randomizer = new Random(System.currentTimeMillis());
	protected int numberOfImages = 0;
	protected boolean notAgain=false;


	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob!=msg.source()))
		{
			if(numberOfImages <= 0)
			{
				unInvoke();
				return true;
			}
			int intAdjustment = (mob.charStats().getMaxStat(CharStats.STAT_INTELLIGENCE) - mob.charStats().getStat(CharStats.STAT_INTELLIGENCE))/2;
			if(intAdjustment < 1) intAdjustment = 1;
			
			int numberOfTargets = numberOfImages + intAdjustment;
			if(randomizer.nextInt() % numberOfTargets >= intAdjustment)
			{
				if(mob.location().show(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<T-NAME> attack(s) a mirrored image!"))
					numberOfImages--;
				return false;
			}
		}
		return true;
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		if(notAgain) return;

		MOB mob=(MOB)affected;
		if(msg.amISource(mob))
		{
			if((
				(CMath.bset(msg.othersMajor(),CMMsg.MASK_EYES))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MOVE))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MOUTH))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_HANDS)))
			&&(msg.othersMessage()!=null)
			&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE)
			&&(msg.othersMessage().length()>0))
			{
				notAgain=true;
				if(numberOfImages<=0)
					unInvoke();
				else
					for(int x=0;x<numberOfImages;x++)
						msg.addTrailerMsg(CMClass.getMsg(mob,msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,msg.othersMessage()));
			}
		}
		else
		if((msg.amITarget(mob.location())&&(!msg.amISource(mob))&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		&&((CMLib.flags().canBeSeenBy(mob,msg.source()))&&(mob.displayText(msg.source()).length()>0)))
		{
			StringBuffer Say=new StringBuffer("");
			boolean compress=CMath.bset(msg.source().getBitmap(),MOB.ATT_COMPRESS);
			for(int i=0;i<numberOfImages;i++)
			{
				Say.append("^M");
				if(compress) Say.append(CMLib.flags().colorCodes(mob,mob)+"^M ");
				if(mob.displayText(msg.source()).length()>0)
					Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob.displayText(msg.source()))));
				else
					Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob.name())));
				if(!compress)
					Say.append(CMLib.flags().colorCodes(mob,msg.source())+"^N\n\r");
				else
					Say.append("^N");
			}
			if(Say.toString().length()>0)
			{
				CMMsg msg2=CMClass.getMsg(msg.source(),null,this,CMMsg.MSG_OK_VISUAL,Say.toString(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				msg.addTrailerMsg(msg2);
			}
		}
		notAgain=false;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor()-1-getXLEVELLevel(invoker()));
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			numberOfImages=0;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your mirror images fade away.");
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> mirror images.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			numberOfImages = CMLib.dice().roll(1,(int)(Math.round(CMath.div(adjustedLevel(mob,asLevel),3.0))),2);
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"A spell forms around":"^S<S-NAME> incant(s) the reflective spell of")+" <T-NAME>, and suddenly " + numberOfImages + " copies appear.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
		{
			numberOfImages = 0;
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) reflectively, but nothing more happens.");
		}
		// return whether it worked
		return success;
	}
}
