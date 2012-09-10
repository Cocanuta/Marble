package com.planet_ink.marble_mud.Exits;
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
public class GenExit extends StdExit
{
	protected String 	name="a walkway";
	protected String 	description="Looks like an ordinary path from here to there.";
	protected String 	displayText="";
	protected String 	closedText="A barrier blocks the way.";

	protected String 	doorName="door";
	protected String 	closeName="close";
	protected String 	openName="open";

	protected boolean 	hasADoor=false;
	protected boolean 	doorDefaultsClosed=true;
	protected boolean 	hasALock=false;
	protected boolean 	doorDefaultsLocked=false;
	protected boolean 	isReadable=false;
	protected int 		openDelayTicks=45;

	protected String 	keyName="";
	
	
	public String ID(){	return "GenExit";}
	public GenExit()
	{
		super();
		name="a walkway";
		description="An ordinary looking way from here to there.";
		displayText="";
		closedText="a closed exit";
		doorName="exit";
		openName="open";
		closeName="close";
		keyName="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;

		openDelayTicks=45;
	}

	public boolean isGeneric(){return true;}
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
		isOpen=!doorDefaultsClosed;
		isLocked=doorDefaultsLocked;
	}

	public String Name(){ return name;}
	public void setName(String newName){name=newName;}
	public String displayText(){ return displayText;}
	public void setDisplayText(String newDisplayText){ displayText=newDisplayText;}
	public String description(){ return description;}
	public void setDescription(String newDescription){ description=newDescription;}
	public boolean hasADoor(){return hasADoor;}
	public boolean hasALock(){return hasALock;}
	public boolean defaultsLocked(){return doorDefaultsLocked;}
	public boolean defaultsClosed(){return doorDefaultsClosed;}
	public void setDoorsNLocks(boolean newHasADoor,
								  boolean newIsOpen,
								  boolean newDefaultsClosed,
								  boolean newHasALock,
								  boolean newIsLocked,
								  boolean newDefaultsLocked)
	{
		isOpen=newIsOpen;
		isLocked=newIsLocked;
		hasADoor=newHasADoor;
		hasALock=newHasALock;
		doorDefaultsClosed=newDefaultsClosed;
		doorDefaultsLocked=newDefaultsLocked;
	}

	public boolean isReadable(){ return isReadable;}

	public String doorName(){return doorName;}
	public String closeWord(){return closeName;}
	public String openWord(){return openName;}
	public String closedText(){return closedText;}
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText)
	{
		doorName=newDoorName;
		closeName=newCloseWord;
		openName=newOpenWord;
		closedText=newClosedText;
	}
	
	public String readableText(){ return (isReadable?keyName:"");}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public void setReadableText(String text) { keyName=temporaryDoorLink()+text; }

	public String keyName()	{ return keyName; }
	public void setKeyName(String newKeyName){keyName=temporaryDoorLink()+newKeyName;}

	public int openDelayTicks()	{ return openDelayTicks;}
	public void setOpenDelayTicks(int numTicks){openDelayTicks=numTicks;}
	
	public String temporaryDoorLink(){
		if(keyName.startsWith("{#"))
		{
			int x=keyName.indexOf("#}");
			if(x>=0)
				return keyName.substring(2,x);
		}
		return "";
	}
	public void setTemporaryDoorLink(String link)
	{
		if(keyName.startsWith("{#"))
		{
			int x=keyName.indexOf("#}");
			if(x>=0) keyName=keyName.substring(x+2);
		}
		if(link.length()>0)
			keyName="{#"+link+"#}"+keyName;
	}
}
