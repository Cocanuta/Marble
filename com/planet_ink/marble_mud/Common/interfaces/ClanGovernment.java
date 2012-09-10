package com.planet_ink.marble_mud.Common.interfaces;
import java.util.*;

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

/**
 * A class defining the characteristics of a clan government,
 * and its membership.
 * @author bzimmerman
 */
public interface ClanGovernment extends Modifiable, CMCommon
{
	/**
	 * Gets the iD.
	 *
	 * @return the iD
	 */
	public int getID();
	
	/**
	 * Sets the iD.
	 *
	 * @param iD the new iD
	 */
	public void setID(int iD);
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name);
	
	/**
	 * Gets the auto role.
	 *
	 * @return the auto role
	 */
	public int getAutoRole();
	
	/**
	 * Sets the auto role.
	 *
	 * @param autoRole the new auto role
	 */
	public void setAutoRole(int autoRole);
	
	/**
	 * Gets the accept pos.
	 *
	 * @return the accept pos
	 */
	public int getAcceptPos();
	
	/**
	 * Sets the accept pos.
	 *
	 * @param acceptPos the new accept pos
	 */
	public void setAcceptPos(int acceptPos);
	
	/**
	 * Gets the short desc.
	 *
	 * @return the short desc
	 */
	public String getShortDesc();
	
	/**
	 * Sets the short desc.
	 *
	 * @param shortDesc the new short desc
	 */
	public void setShortDesc(String shortDesc);
	
	/**
	 * Gets the long desc.
	 *
	 * @return the long desc
	 */
	public String getLongDesc();
	
	/**
	 * Sets the long desc.
	 *
	 * @param longDesc the new long desc
	 */
	public void setLongDesc(String longDesc);
	
	/**
	 * Gets the required mask str.
	 *
	 * @return the required mask str
	 */
	public String getRequiredMaskStr();
	
	/**
	 * Sets the required mask str.
	 *
	 * @param requiredMaskStr the new required mask str
	 */
	public void setRequiredMaskStr(String requiredMaskStr);
	
	/**
	 * Checks if is public.
	 *
	 * @return true, if is public
	 */
	public boolean isPublic();
	
	/**
	 * Sets the public.
	 *
	 * @param isPublic the new public
	 */
	public void setPublic(boolean isPublic);
	
	/**
	 * Checks if is family only.
	 *
	 * @return true, if is family only
	 */
	public boolean isFamilyOnly();
	
	/**
	 * Sets the family only.
	 *
	 * @param isFamilyOnly the new family only
	 */
	public void setFamilyOnly(boolean isFamilyOnly);
	
	/**
	 * Gets the override min members.
	 *
	 * @return the override min members
	 */
	public Integer getOverrideMinMembers();
	
	/**
	 * Sets the override min members.
	 *
	 * @param overrideMinMembers the new override min members
	 */
	public void setOverrideMinMembers(Integer overrideMinMembers);
	
	/**
	 * Checks if is conquest enabled.
	 *
	 * @return true, if is conquest enabled
	 */
	public boolean isConquestEnabled();
	
	/**
	 * Sets the conquest enabled.
	 *
	 * @param conquestEnabled the new conquest enabled
	 */
	public void setConquestEnabled(boolean conquestEnabled);
	
	/**
	 * Checks if is conquest item loyalty.
	 *
	 * @return true, if is conquest item loyalty
	 */
	public boolean isConquestItemLoyalty();
	
	/**
	 * Sets the conquest item loyalty.
	 *
	 * @param conquestItemLoyalty the new conquest item loyalty
	 */
	public void setConquestItemLoyalty(boolean conquestItemLoyalty);
	
	/**
	 * Checks if is conquest by worship.
	 *
	 * @return true, if is conquest by worship
	 */
	public boolean isConquestByWorship();
	
	/**
	 * Sets the conquest by worship.
	 *
	 * @param conquestByWorship the new conquest by worship
	 */
	public void setConquestByWorship(boolean conquestByWorship);
	
	/**
	 * Gets the max vote days.
	 *
	 * @return the max vote days
	 */
	public int getMaxVoteDays();
	
	/**
	 * Sets the max vote days.
	 *
	 * @param maxVoteDays the new max vote days
	 */
	public void setMaxVoteDays(int maxVoteDays);
	
	/**
	 * Gets the vote quorum pct.
	 *
	 * @return the vote quorum pct
	 */
	public int getVoteQuorumPct();
	
	/**
	 * Sets the vote quorum pct.
	 *
	 * @param voteQuorumPct the new vote quorum pct
	 */
	public void setVoteQuorumPct(int voteQuorumPct);
	
	/**
	 * Gets the xp calculation formula.
	 *
	 * @return the xp calculation formula
	 */
	public String getXpCalculationFormulaStr();
	
	/**
	 * Sets the xp calculation formula.
	 *
	 * @param xpCalculationFormula the new xp calculation formula
	 */
	public void setXpCalculationFormulaStr(String xpCalculationFormulaStr);
	
	/**
	 * 
	 * @return
	 */
	public LinkedList<CMath.CompiledOperation> getXPCalculationFormula();
	
	/**
	 * Checks if is default.
	 *
	 * @return true, if is default
	 */
	public boolean isDefault();
	
	/**
	 * Sets the default.
	 *
	 * @param isDefault the new default
	 */
	public void setDefault(boolean isDefault);
	
	/**
	 * Gets the positions.
	 *
	 * @return the positions
	 */
	public ClanPosition[] getPositions();
	
	/**
	 * Sets the positions.
	 *
	 * @param positions the new positions
	 */
	public void setPositions(ClanPosition[] positions);
	
	/**
	 * Gets the auto promote by.
	 *
	 * @return the auto promote by
	 */
	public Clan.AutoPromoteFlag getAutoPromoteBy();
	
	/**
	 * Sets the auto promote by.
	 *
	 * @param autoPromoteBy the new auto promote by
	 */
	public void setAutoPromoteBy(Clan.AutoPromoteFlag autoPromoteBy);
	
	/**
	 * Gets the level progression.
	 *
	 * @return the level progression
	 */
	public int[] getLevelProgression();
	
	/**
	 * Sets the level progression.
	 *
	 * @param levelProgression the new level progression
	 */
	public void setLevelProgression(int[] levelProgression);
	
	/**
	 * Gets the help str.
	 *
	 * @return the help str
	 */
	public String getHelpStr();
	
	/**
	 * Adds the position.
	 *
	 * @return the clan position
	 */
	public ClanPosition addPosition();
	
	/**
	 * Del position.
	 *
	 * @param pos the pos
	 */
	public void delPosition(ClanPosition pos);
	
	/**
	 * Gets the position.
	 *
	 * @param pos the pos
	 * @return the position
	 */
	public ClanPosition getPosition(String pos);
	
	/**
	 * Return the list of abilities owned by someone
	 * who is part of a clan of the given level.
	 * @param level clan level
	 * @return list of abilities
	 */
	public List<Ability> getClanLevelAbilities(Integer level);
	
	/**
	 * Return the list of effects owned by someone
	 * who is part of a clan of the given level.
	 * @param mob the mob affected
	 * @param level clan level
	 * @return list of abilities
	 */
	public ChameleonList<Ability> getClanLevelEffects(MOB mob, Integer level);
	
	/**
	 * Return the size of the list of effects owned by someone
	 * who is a part of this clan of the given level. Much more
	 * efficient than getting the whole list and checking its size.
	 * @param mob the mob affected
	 * @param level the clan level
	 * @return the size of the list of abilities
	 */
	public int getClanLevelEffectsSize(MOB mob, Integer level);
}
