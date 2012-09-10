package com.planet_ink.marble_mud.Libraries.interfaces;
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
import com.planet_ink.marble_mud.Libraries.MUDZapper;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
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
public interface HelpLibrary extends CMLibrary
{
	public List<String> getTopics(boolean archonHelp, boolean standardHelp);
	public String getActualUsage(Ability A, int which, MOB forMOB);
	public String fixHelp(String tag, String str, MOB forMOB);
	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp);
	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp, boolean noFix);
	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB);
	public StringBuilder getHelpList(String helpStr,  Properties rHelpFile1, Properties rHelpFile2, MOB forMOB);
	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix);
	public Properties getArcHelpFile();
	public Properties getHelpFile();
	public void unloadHelpFile(MOB mob);
	public boolean isPlayerSkill(String helpStr);
	public void addHelpEntry(String ID, String text, boolean archon);
}
