package com.planet_ink.marble_mud.Libraries.interfaces;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.exceptions.*;
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
import com.planet_ink.marble_mud.Libraries.CMChannels;
import com.planet_ink.marble_mud.Libraries.EnglishParser;
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
public interface EnglishParsing extends CMLibrary
{
	public boolean isAnArticle(String s);
	public String cleanArticles(String s);
	public String stripPunctuation(String str);
	public String makePlural(String str);
	public String getFirstWord(final String str);
	public String properIndefiniteArticle(String str);
	public String toEnglishStringList(final String[] V);
	public String insertUnColoredAdjective(String str, String adjective);
	public String startWithAorAn(String str);
	public CMObject findCommand(MOB mob, List<String> commands);
	public boolean evokedBy(Ability thisAbility, String thisWord);
	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord);
	public String getAnEvokeWord(MOB mob, String word);
	public Ability getToEvoke(MOB mob, List<String> commands);
	public boolean preEvoke(MOB mob, List<String> commands, int secondsElapsed, double actionsRemaining);
	public void evoke(MOB mob, Vector<String> commands);
	public boolean containsString(final String toSrchStr, final String srchStr);
	public String bumpDotNumber(String srchStr);
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly);
	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, int wornFilter, boolean exactOnly);
	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, int wornFilter, boolean exactOnly);
	public int getContextNumber(Environmental[] list, Environmental E);
	public int getContextNumber(Collection<? extends Environmental> list, Environmental E);
	public int getContextNumber(ItemCollection cont, Environmental E);
	public String getContextName(Collection<? extends Environmental> list, Environmental E);
	public String getContextName(Environmental[] list, Environmental E);
	public String getContextName(ItemCollection cont, Environmental E);
	public int getContextSameNumber(Environmental[] list, Environmental E);
	public int getContextSameNumber(Collection<? extends Environmental> list, Environmental E);
	public int getContextSameNumber(ItemCollection cont, Environmental E);
	public String getContextSameName(Collection<? extends Environmental> list, Environmental E);
	public String getContextSameName(Environmental[] list, Environmental E);
	public String getContextSameName(ItemCollection cont, Environmental E);
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, int wornFilter, boolean exactOnly);
	public Environmental parseShopkeeper(MOB mob, List<String> commands, String error);
	public List<Item> fetchItemList(Environmental from, MOB mob, Item container, List<String> commands, int preferredLoc, boolean visionMatters);
	public long numPossibleGold(Environmental mine, String itemID);
	public String numPossibleGoldCurrency(Environmental mine, String itemID);
	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID);
	public Object[] parseMoneyStringSDL(MOB mob, String amount, String correctCurrency);
	public long getMillisMultiplierByName(String timeName);
	public String matchAnyCurrencySet(String itemID);
	public double matchAnyDenomination(String currency, String itemID);
	public Item possibleRoomGold(MOB seer, Room room, Container container, String itemID);
	public Item bestPossibleGold(MOB mob, Container container, String itemID);
	public List<Container> possibleContainers(MOB mob, List<String> commands, int wornFilter, boolean withContentOnly);
	public Item possibleContainer(MOB mob, List<String> commands, boolean withStuff, int wornFilter);
	public String returnTime(long millis, long ticks);
	public int calculateMaxToGive(MOB mob, List<String> commands, boolean breakPackages, Environmental checkWhat, boolean getOnly);
}
