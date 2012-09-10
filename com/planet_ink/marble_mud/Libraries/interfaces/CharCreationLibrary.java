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
public interface CharCreationLibrary extends CMLibrary
{
	public void reRollStats(MOB mob, CharStats C);
	public boolean canChangeToThisClass(MOB mob, CharClass thisClass, int theme);
	// mob is optional
	public List<CharClass> classQualifies(MOB mob, int theme);
	// mob is optional
	public List<Race> raceQualifies(MOB mob, int theme);
	public boolean isOkName(String login);
	public void reloadTerminal(MOB mob);
	public void showTheNews(MOB mob);
	public boolean getRetireReason(final String mobName, final Session session);
	public void notifyFriends(MOB mob, String message);
	public LoginResult createCharacter(PlayerAccount acct, String login, Session session) throws java.io.IOException;
	public LoginResult loginSystem(Session session, LoginSession loginObj) throws java.io.IOException;
	public LoginResult doAccountMenu(PlayerAccount acct, Session session, boolean create) throws java.io.IOException;
	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean checkPlayerName);
	public void pageRooms(CMProps page, Map<String, String> table, String start);
	public void initStartRooms(CMProps page);
	public void initDeathRooms(CMProps page);
	public void initBodyRooms(CMProps page);
	public Room getDefaultStartRoom(MOB mob);
	public Room getDefaultDeathRoom(MOB mob);
	public Room getDefaultBodyRoom(MOB mob);
	
	public enum LoginState { START, LOGIN_NAME, ACCT_CHAR_PWORD, PLAYER_PASS_START, CREATE_ACCOUNT_CONFIRM, CREATE_CHAR_CONFIRM, 
							 PLAYER_PASS_RECEIVED, CONFIRM_EMAIL_PASSWORD, ACCT_CONVERT_CONFIRM}
	
	public static class LoginSession
	{
		public boolean 		 wizi	   =false;
		public LoginState 	 state	   =LoginState.START;
		public String 		 login	   =null;
		public PlayerAccount acct 	   =null;
		public String 		 lastInput =null;
		public String 		 password  =null;
		public int			 attempt   =0;
		public PlayerLibrary.ThinnerPlayer player = null;
	}
	
	public enum NewCharNameCheckResult { OK, NO_NEW_PLAYERS, NO_NEW_LOGINS, BAD_USED_NAME, CREATE_LIMIT_REACHED }
	
	public final static String DEFAULT_BADNAMES = " LIST DELETE QUIT NEW HERE YOU SHIT FUCK CUNT ALL FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP GOD JESUS CHRIST NOBODY SOMEBODY MESSIAH ADMIN SYSOP ";
	
	public enum LoginResult
	{
		NO_LOGIN, NORMAL_LOGIN, ACCOUNT_LOGIN, SESSION_SWAP, CCREATION_EXIT, ACCOUNT_CREATED, INPUT_REQUIRED
	}
}
