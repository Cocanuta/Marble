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
public interface ChannelsLibrary extends CMLibrary
{
	public final int QUEUE_SIZE=100;
	
	public int getNumChannels();
	public String getChannelMask(int i);
	public Set<ChannelFlag> getChannelFlags(int i);
	public String getChannelName(int i);
	public String getChannelColorOverride(int i);
	public List<ChannelMsg> getChannelQue(int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int i);
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly);
	public void channelQueUp(int i, CMMsg msg);
	public int getChannelIndex(String channelName);
	public int getChannelCodeNumber(String channelName);
	public String getChannelName(String channelName);
	public List<String> getFlaggedChannelNames(ChannelFlag flag);
	public String getExtraChannelDesc(String channelName);
	public String[][] imc2ChannelsArray();
	public String[][] iChannelsArray();
	public String[] getChannelNames();
	public List<Session> clearInvalidSnoopers(Session mySession, int channelCode);
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid);
	public int loadChannels(String list, String ilist, String imc2list);
	public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender);
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg);
	
	public static class ChannelMsg
	{
		public final CMMsg msg; 
		public long ts;
		public ChannelMsg(CMMsg msg){this.msg=msg; ts=System.currentTimeMillis();}
	}
	
	public static enum ChannelFlag {
		DEFAULT,SAMEAREA,CLANONLY,READONLY,
		EXECUTIONS,LOGINS,LOGOFFS,BIRTHS,MARRIAGES, 
		DIVORCES,CHRISTENINGS,LEVELS,DETAILEDLEVELS,DEATHS,DETAILEDDEATHS,
		CONQUESTS,CONCEPTIONS,NEWPLAYERS,LOSTLEVELS,PLAYERPURGES,CLANINFO,
		WARRANTS, PLAYERREADONLY, CLANALLYONLY
	}
}
