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
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;
import java.sql.ResultSet;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
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
public class DBConnector
{
	private DBConnections dbConnections=null;
	private String dbClass="";
	private String dbService="";
	private String dbUser="";
	private String dbPass="";
	private boolean dbReuse=false;
	private int numConnections=0;
	private int dbPingIntMins=0;
	private boolean doErrorQueueing=false;
	private boolean newErrorQueueing=false;
	
	public static final class DBPreparedBatchEntry
	{
		public DBPreparedBatchEntry(final String sql)
		{
			this.sql=sql;
			this.clobs=new String[][]{{}};
		}
		public DBPreparedBatchEntry(final String sql, final String[] clobs)
		{
			this.sql=sql;
			this.clobs=new String[][]{clobs};
		}
		public DBPreparedBatchEntry(final String sql, final String clobs)
		{
			this.sql=sql;
			this.clobs=new String[][]{{clobs}};
		}
		public DBPreparedBatchEntry(final String sql, final String[][] clobs)
		{
			this.sql=sql;
			this.clobs=clobs;
		}
		public final String sql;
		public final String[][] clobs;
	}
	
	public DBConnector (){super();}
	
	public DBConnector (String NEWDBClass,
						String NEWDBService, 
						String NEWDBUser, 
						String NEWDBPass, 
						int NEWnumConnections,
						int NEWdbPingIntMins,
						boolean NEWReuse,
						boolean NEWDoErrorQueueing,
						boolean NEWRetryErrorQueue)
	{
		super();
		dbClass=NEWDBClass;
		dbService=NEWDBService;
		dbUser=NEWDBUser;
		dbPass=NEWDBPass;
		numConnections=NEWnumConnections;
		doErrorQueueing=NEWDoErrorQueueing;
		newErrorQueueing=NEWRetryErrorQueue;
		dbReuse=NEWReuse;
		dbPingIntMins=NEWdbPingIntMins;
		if(dbPingIntMins<=0) dbPingIntMins=Integer.MAX_VALUE;
	}
	public void reconnect()
	{
		if(dbConnections!=null){ dbConnections.deregisterDriver(); dbConnections.killConnections();}
		dbConnections=new DBConnections(dbClass,dbService,dbUser,dbPass,numConnections,dbReuse,doErrorQueueing);
		if(dbConnections.amIOk()&&newErrorQueueing) dbConnections.retryQueuedErrors();
	}
	
	public String service(){ return dbService;}
	
	public int getRecordCount(DBConnection D, ResultSet R)
	{
		int recordCount=0;
		try
		{
			R.last(); 
			recordCount=R.getRow(); 
			R.beforeFirst();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return recordCount;
	}
	
	public boolean deregisterDriver()
	{ 
		if(dbConnections!=null) return dbConnections.deregisterDriver();
		return false;
	}
	public boolean isFakeDB()
	{
		return (dbConnections!=null)?dbConnections.isFakeDB():false;
	}
	
	public int update(final String[] updateStrings){ return (dbConnections!=null)?dbConnections.update(updateStrings):0;}
	public int update(final String updateString){ return (dbConnections!=null)?dbConnections.update(new String[]{updateString}):0;}
	public int updateWithClobs(String[] updateStrings, String[][][] values){ return (dbConnections!=null)?dbConnections.updateWithClobs(updateStrings,values):0;}
	public int updateWithClobs(final List<DBPreparedBatchEntry> entries) { return (dbConnections!=null)?dbConnections.updateWithClobs(entries):0; }
	public int updateWithClobs(final DBPreparedBatchEntry entry) { return updateWithClobs(entry.sql,entry.clobs); }
	public int updateWithClobs(final String updateString, final String... values) { return updateWithClobs(updateString, new String[][]{values}); }
	public int updateWithClobs(final String updateString, final String[][] values) { return (dbConnections!=null)?dbConnections.updateWithClobs(updateString, values):0; }
	
	public int queryRows(String queryString){ return (dbConnections!=null)?dbConnections.queryRows(queryString):0;}

	/** 
	 * Fetch a single, not in use DBConnection object. 
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 * 
	 * <br><br><b>Usage: DB=DBFetch();</b> 
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetch(){return (dbConnections!=null)?dbConnections.DBFetch():null;}
	

	/** 
	 * Fetch a single, not in use DBConnection object.  Must be rePrepared afterwards 
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 * 
	 * <br><br><b>Usage: DB=DBFetchEmpty();</b> 
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchEmpty(){return (dbConnections!=null)?dbConnections.DBFetchEmpty():null;}
	
	public int numConnectionsMade(){return (dbConnections!=null)?dbConnections.numConnectionsMade():0;}
	public int numDBConnectionsInUse(){ return (dbConnections!=null)?dbConnections.numInUse():0;}
	
	/** 
	 * Fetch a single, not in use DBConnection object. 
	 * You can then call DBConnection.query and DBConnection.update on this object.
	 * The user must ALWAYS call DBDone when done with the object.
	 * 
	 * <br><br><b>Usage: DB=DBFetchPrepared();</b> 
	 * @param SQL    The prepared statement SQL
	 * @return DBConnection    The DBConnection to use
	 */
	public DBConnection DBFetchPrepared(String SQL){ return (dbConnections!=null)?dbConnections.DBFetchPrepared(SQL):null;}
	/** 
	 * Return a DBConnection object fetched with DBFetch()
	 * 
	 * <br><br><b>Usage:</b> 
	 * @param D    The Database connection to return to the pool
	 */
	public void DBDone(DBConnection D){ if(dbConnections!=null) dbConnections.DBDone(D);}

	/** 
	 * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 * 
	 * <br><br><b>Usage:</b> str=getLongRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public String getRes(ResultSet Results, String Field)
	{ return DBConnections.getRes(Results,Field);}

	public String getResQuietly(ResultSet Results, String Field)
	{ return DBConnections.getResQuietly(Results, Field);}

	public String injectionClean(String s)
	{
		if(s==null) return null;
		return s.replace('\'', '`');
	}
	
	/** 
	  * When reading a database table, this routine will read in
	 * the given Field NAME, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 * 
	 * <br><br><b>Usage:</b> str=getLongRes(R,"FIELD");
	 * @param Results    The ResultSet object to use
	 * @param Field 	   Field name to return
	 * @return String    The value of the field being returned
	 */
	public long getLongRes(ResultSet Results, String Field)
	{ return DBConnections.getLongRes(Results,Field);}
	
	/** 
	 * When reading a database table, this routine will read in
	 * the given One index number, returning the value.  The value
	 * will be trim()ed, and will not be NULL.
	 * 
	 * <br><br><b>Usage:</b> str=getRes(R,1);
	 * @param Results    The ResultSet object to use
	 * @param One   	 Field number to return
	 * @return String    The value of the field being returned
	 */
	public String getRes(ResultSet Results, int One)
	{ return DBConnections.getRes(Results,One);}
	
	/** 
	 * Destroy all database connections, effectively
	 * shutting down this class.
	 * 
	 * <br><br><b>Usage:</b> killConnections();
	 */
	public void killConnections(){ if(dbConnections!=null) dbConnections.killConnections();}
	
	/** 
	 * Return the happiness level of the connections
	 * <br><br><b>Usage:</b> amIOk()
	 * @return boolean    true if ok, false if not ok
	 */
	public boolean amIOk(){ return (dbConnections!=null)?dbConnections.amIOk():false;}

	/**
	 * Pings all connections
	 * @param querySql the query to ping with
	 * @return the number of pings done
	 */
	public int pingAllConnections(final String querySql)
	{
		return (dbConnections!=null) ? dbConnections.pingAllConnections(querySql, dbPingIntMins * (60 * 1000)) : 0;
	}

	/**
	 * Pings all connections
	 * @param querySql the query to ping with
	 * @param overridePingIntMillis the age of a connection before a ping is necessary
	 * @return the number of pings done
	 */
	public int pingAllConnections(final String querySql, final long overridePingIntMillis)
	{
		return (dbConnections!=null) ? dbConnections.pingAllConnections(querySql, overridePingIntMillis) : 0;
	}

	/** 
	 * Queue up a failed write/update for later processing.
	 * 
	 * <br><br><b>Usage:</b> enQueueError("UPDATE SQL","error string");
	 * @param SQLString    UPDATE style SQL statement
	 * @param SQLError    The error message being reported
	 * @param count    The number of tries so far
	 */
	public void enQueueError(String SQLString, String SQLError, String count)
	{ if(dbConnections!=null)dbConnections.enQueueError(SQLString, SQLError,count);}
	
	
	/** 
	 * Queue up a failed write/update for later processing.
	 * 
	 * <br><br><b>Usage:</b> RetryQueuedErrors();
	 */
	public void retryQueuedErrors()
	{ if(dbConnections!=null)dbConnections.retryQueuedErrors();}
	
	/** list the connections 
	 * 
	 * <br><br><b>Usage:</b> listConnections(out);
	 * @param out    place to send the list out to
	 */
	public void listConnections(PrintStream out)
	{ if(dbConnections!=null)dbConnections.listConnections(out);}
	
	/** return a status string, or "" if everything is ok.
	 * 
	 * <br><br><b>Usage:</b> errorStatus();
	 * @return StringBuffer    complete error status
	 */
	public StringBuffer errorStatus()
	{ 
		if(dbConnections==null) return new StringBuffer("Not connected.");
		StringBuffer status=dbConnections.errorStatus();
		if(status.length()==0)
			return new StringBuffer("OK! Connections in use="+dbConnections.numInUse()+"/"+dbConnections.numConnectionsMade());
		return new StringBuffer("<BR>"+status.toString().replaceAll("\n","<BR>")+"Connections in use="+dbConnections.numInUse()+"/"+dbConnections.numConnectionsMade());
	}
}
