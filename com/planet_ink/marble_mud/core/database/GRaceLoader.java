package com.planet_ink.marble_mud.core.database;
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
import com.planet_ink.marble_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
public class GRaceLoader
{
	protected DBConnector DB=null;
	public GRaceLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public void DBDeleteRace(String raceID)
	{
		DB.update("DELETE FROM CMGRAC WHERE CMRCID='"+raceID+"'");
	}
	public void DBCreateRace(String raceID, String data)
	{
		DB.updateWithClobs(
		 "INSERT INTO CMGRAC ("
		 +"CMRCID, "
		 +"CMRDAT "
		 +") values ("
		 +"'"+raceID+"',"
		 +"?"
		 +")", 
		 data+" ");
	}
	public List<DatabaseEngine.AckRecord> DBReadRaces()
	{
		DBConnection D=null;
		List<DatabaseEngine.AckRecord> rows=new Vector<DatabaseEngine.AckRecord>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMGRAC");
			while(R.next())
			{
				DatabaseEngine.AckRecord ack=new DatabaseEngine.AckRecord(
						DBConnections.getRes(R,"CMRCID"),
						DBConnections.getRes(R,"CMRDAT"),
						"GenRace");
				rows.add(ack);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

}
