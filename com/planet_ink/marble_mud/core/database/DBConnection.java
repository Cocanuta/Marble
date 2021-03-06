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

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

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
public class DBConnection
{
	/** Connection object being used*/
	private Connection myConnection=null;
	
	/** (new) resultset being used currently */
	private ResultSet myResultSet=null;
	
	/** (new) statement object being used currently */
	private Statement myStatement=null;
	
	/** (new) statement object being used currently */
	private PreparedStatement myPreparedStatement=null;
	
	/** Whether this dbconnection is being used */
	protected boolean inUse;
	
	/** if any SQL errors occur, they are here.**/
	protected String lastError=null;
	
	/** last time the connection was queried/executed.**/
	private long lastQueryTime=System.currentTimeMillis();
	
	/** when this connection was put into use**/
	private long lastPutInUseTime=System.currentTimeMillis();
	
	/** number of failures in a row */
	protected int failuresInARow=0;
	
	protected boolean sqlserver=false;
	
	protected boolean isReusable=false;
	
	/** parent container of this connection **/
	private DBConnections myParent=null;	
	
	/** for tracking the last sql statement made */
	protected String lastSQL = "";
	
	/** for remembering whether this is a fakeDB connection */
	private Boolean isFakeDB = null;
	
	public static enum FetchType {EMPTY,STATEMENT,PREPAREDSTATEMENT}
	
	/** 
	 * construction
	 * 
	 * <br><br><b>Usage:</b> DBConnection("","","");
	 * @param DBClass    JDBC Class
	 * @param DBService    ODBC SERVICE
	 * @param DBUser	ODBC LOGIN USERNAME
	 * @param DBPass	ODBC LOGIN PASSWORD
	 * @param DBReuse   Whether the connection can be reused.
	 */
	public DBConnection(DBConnections parent,
						String DBClass,
						String DBService, 
						String DBUser, 
						String DBPass,
						boolean DBReuse)
		throws SQLException
	{
		myParent=parent;
		if((DBClass==null)||(DBClass.length()==0))
			DBClass="sun.jdbc.odbc.JdbcOdbcDriver";
		try
		{
			Class.forName(DBClass);
		}
		catch(ClassNotFoundException ce)
		{
			ce.printStackTrace();
		}
		sqlserver=true;
		isReusable=DBReuse;
		java.util.Properties p = new java.util.Properties();
		if((DBUser!=null)&&(DBUser.length()>0))
		{
			p.put("user",DBUser);
			p.put("password",DBPass);
		}
		p.put("SetBigStringTryClob", "true");
		myConnection=DriverManager.getConnection(DBService,p);
		//Log.debugOut("New connection made to :"+DBService+" using "+DBClass); 	   
		sqlserver=false;
		inUse=false;
	}
	
	public String catalog()
	{
		try{
			return myConnection.getCatalog();
		}
		catch(Exception e){}
		return "";
	}
	
	public boolean isFakeDB()
	{
		if(isFakeDB==null)
		{
			String catalog = catalog();
			if(catalog==null)
				isFakeDB=Boolean.FALSE;
			else
				isFakeDB = Boolean.valueOf(catalog.equalsIgnoreCase("FAKEDB"));
		}
		return isFakeDB.booleanValue();
	}
	
	/** 
	 * shut down this connection totally
	 * 
	 * <br><br><b>Usage:</b> close()
	 */
	public void close()
	{
		try{
			if(myStatement!=null)
				myStatement.close();
		}catch(SQLException e){}
		try{
			if(myPreparedStatement!=null)
				myPreparedStatement.close();
		}catch(SQLException e){}
		try{
			if(myConnection!=null)
				myConnection.close();
		}catch(SQLException e){}
		myConnection=null;
		myStatement=null;
		myPreparedStatement=null;
		myParent=null;
	}
	
	/** 
	 * set up this connection for use
	 * 
	 * <br><br><b>Usage:</b> use("begin transaction")
	 * @param openerSQL    Any SQL string you'd like to send
	 * @return boolean    The connection being used
	 */
	public synchronized boolean use(String openerSQL)
	{
		if((!inUse)&&(ready())&&(!isProbablyDead()))
		{
			lastError=null;
			try
			{
				myPreparedStatement=null;
				sqlserver=true;
				myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			}
			catch(SQLException e)
			{
				myConnection=null;
				failuresInARow++;
				sqlserver=false;
				return false;
			}
		
			sqlserver=false;
			try
			{
				if(!openerSQL.equals(""))
				{
					lastSQL=openerSQL;
					lastQueryTime=System.currentTimeMillis();
					myStatement.executeUpdate(openerSQL);
				}
			}
			catch(SQLException e)
			{
				return false;
				// not a real error?!
			}
		
			lastPutInUseTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		return false;
	}
	
	
	/** 
	 * set up this connection for use
	 * 
	 * <br><br><b>Usage:</b> useEmpty()
	 * @return boolean    The connection being used
	 */
	public synchronized boolean useEmpty()
	{
		if((!inUse)&&(ready())&&(!isProbablyDead()))
		{
			lastError=null;
			myPreparedStatement=null;
			sqlserver=true;
			myStatement=null;
			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			inUse=true;
			return true;
		}
		return false;
	}
	
	/** 
	 * set up this connection for use as a prepared statement
	 * 
	 * <br><br><b>Usage:</b> usePrepared("SQL String")
	 * @param SQL    Any SQL string you'd like to use
	 * @return boolean    The connection being used
	 */
	public synchronized boolean usePrepared(String SQL)
	{
		if((!inUse)&&(ready()))
		{
		
			lastError=null;
		
			try
			{
				myStatement=null;
				sqlserver=true;
				lastSQL=SQL;
				myPreparedStatement=myConnection.prepareStatement(SQL);
				sqlserver=false;
			}
			catch(SQLException e)
			{
				sqlserver=false;
				myConnection=null;
				failuresInARow++;
				return false;
			}
		
			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			failuresInARow=0;
			inUse=true;
			return true;
		}
		return false;
	}
	
	
	/** 
	 * set up this connection for use as a prepared statement
	 * Requires an already in use connection.
	 * 
	 * <br><br><b>Usage:</b> rePrepare("SQL String")
	 * @param SQL    Any SQL string you'd like to use
	 * @return boolean    The connection being used
	 */
	public synchronized boolean rePrepare(String SQL)
	{
		if(inUse)
		{
			closeStatements("");
			lastError=null;
			try
			{
				myStatement=null;
				sqlserver=true;
				lastSQL=SQL;
				myPreparedStatement=myConnection.prepareStatement(SQL);
				sqlserver=false;
			}
			catch(SQLException e)
			{
				Log.errOut("DBConnection",e.getMessage());
				sqlserver=false;
				myConnection=null;
				failuresInARow++;
				return false;
			}
		
			sqlserver=false;
			lastPutInUseTime=System.currentTimeMillis();
			failuresInARow=0;
			inUse=true;
			return true;
		}
		return false;
	}

	protected void closeStatements(String Closer)
	{
		try
		{
			if(!Closer.equals(""))
				if(myStatement!=null)
				{
					lastSQL=Closer;
					lastQueryTime=System.currentTimeMillis();
					myStatement.executeUpdate(Closer);
				}
			if(myResultSet!=null)
			{
				myResultSet.close();
				myResultSet=null;
			}
			if(myPreparedStatement!=null)
			{
				myPreparedStatement.close();
				myPreparedStatement=null;
			}
			if(myStatement!=null)
			{
				myStatement.close();
				myStatement=null;
			}
			if(myConnection!=null)
				myConnection.commit();
		}
		catch(SQLException e)
		{
			// not a real error?
		}
	}
	
	/** 
	 * report this connection as being free
	 * 
	 * <br><br><b>Usage:</b> doneUsing("roll back");
	 * @param Closer	Any SQL string you'd like to send
	 */
	protected void doneUsing(String Closer)
	{
		closeStatements(Closer);
		if(!isReusable) close();
		inUse=false;
	}
	
	/**
	 * Return the time, in millis, when this connection
	 * was last returned.
	 * @return
	 */
	public long getLastQueryTime()
	{
		return lastQueryTime;
	}
	
	/** 
	 * execute a query, returning the resultset
	 * 
	 * <br><br><b>Usage:</b> R=query("SELECT STATEMENT");
	 * @param queryString    SQL query-style string
	 * @return ResultSet	The results of the query
	 */
	public ResultSet query(String queryString)
		throws SQLException
	{
		lastSQL=queryString;
		ResultSet R=null;
		if((inUse)&&(ready())) 
		{
			try
			{
				sqlserver=true;
				lastQueryTime=System.currentTimeMillis();
				if(myStatement!=null)
					R=myStatement.executeQuery(queryString);
				else
					lastError="DBConnection Statement not open.";
				sqlserver=false;
			}
			catch(SQLException sqle)
			{
				sqlserver=false;
				failuresInARow++;
				lastError=""+sqle;
				if(isProbablyDead())
				{
					if(myParent!=null)
						myParent.resetConnections();
						
				}
				throw sqle;
			}
			sqlserver=false;
		}
		else
		{
			lastError="DBConnection not ready.";
		}
		sqlserver=false;
		failuresInARow=0;
		lastPutInUseTime=System.currentTimeMillis();
		if(myParent!=null) 
			myParent.clearErrors();
		myResultSet=R;
		return R;
	}
	
	public void setPreparedClobs(String[] vals) throws SQLException
	{
		if(getPreparedStatement()==null)
		{
			return;
		}
		for(int t=0;t<vals.length;t++)
			if(vals[t]==null)
				getPreparedStatement().setNull(t+1, java.sql.Types.CLOB);
			else
				getPreparedStatement().setString(t+1, vals[t]);
	}
	
	/** 
	 * execute an sql update, returning the status
	 * 
	 * <br><br><b>Usage:</b> update("UPDATE STATEMENT");
	 * @param updateString    SQL update-style string
	 * @param retryNum    a retry number
	 * @return int    The status of the update
	 */
	public int update(String updateString, int retryNum)
		throws SQLException
	{
		lastSQL=updateString;
		int responseCode=-1;
		if((inUse)&&(ready()))
		{
			try
			{
				sqlserver=true;
				lastQueryTime=System.currentTimeMillis();
				if(myStatement!=null)
					responseCode=myStatement.executeUpdate(updateString);
				else
				if(myPreparedStatement!=null)
					responseCode=myPreparedStatement.executeUpdate();
				else
				{
					myStatement=myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					responseCode=myStatement.executeUpdate(updateString);
				}
				sqlserver=false;
			}
			catch(SQLException sqle)
			{
				sqlserver=false;
				if((sqle.getMessage()==null)
				||(sqle.getMessage().toUpperCase().indexOf("PRIMARY KEY")<0))
					failuresInARow++;
				lastError=""+sqle;
				Log.errOut("DBConnection",updateString+": "+sqle);
				if((myParent!=null) && (myStatement != null))
					myParent.enQueueError(updateString,""+sqle,""+(retryNum+1));
				if(isProbablyDead())
					if(myParent!=null)
						myParent.resetConnections();
				throw sqle;
			}
		}
		
		sqlserver=false;
		lastPutInUseTime=System.currentTimeMillis();
		failuresInARow=0;
		if(myParent!=null) 
			myParent.clearErrors();
		return responseCode;
	}
	
	/** 
	 * returns whether this connection is ready for use
	 * 
	 * <br><br><b>Usage:</b> ready();
	 * @return boolean    Whether this connection is ready
	 */
	public boolean ready()
	{
		return (myConnection!=null);
	}
	
	/** 
	 * returns whether this connection is in use
	 * 
	 * <br><br><b>Usage:</b> inUse();
	 * @return boolean    Whether this connection is in use
	 */
	public boolean inUse()
	{
		return inUse;
	}
	
	/** 
	 * known errors should not be a reason to report a dead state
	 * 
	 * <br><br><b>Usage:</b> clearFailures();
	 */
	public void clearFailures()
	{
		failuresInARow=0;
	}
	
	public boolean inSQLServerCommunication()
	{
		return sqlserver;
	}
	
	/** 
	 * returns whether this connection is *probably* dead
	 * 
	 * <br><br><b>Usage:</b> isProbablyDead();
	 * @return boolean    Whether this connection is probably dead
	 */
	public boolean isProbablyDead()
	{
		try
		{
			if((myConnection==null)||(myConnection.isClosed())||(failuresInARow>2))
				return true;
			return false;
		}
		catch(SQLException e)
		{
			return true;
		}
	}
	
	/** 
	 * returns whether this connection is *probably* locked up
	 * 
	 * <br><br><b>Usage:</b> isProbablyLockedUp();
	 * @return boolean    Whether this connection is locked up
	 */
	public boolean isProbablyLockedUp()
	{
		long twominsAgo=System.currentTimeMillis()-(2*60*1000);
		if((lastPutInUseTime<twominsAgo)&&inUse) 
			return true;
		return false;
	}
	
	/** 
	 * returns an error if there was one
	 * 
	 * <br><br><b>Usage:</b> getLastError();
	 * @return String    The last error SQL string, if any
	 */
	public String getLastError()
	{
		if(lastError==null)
			return "";
		return lastError;
	}
	
	
	/** 
	 * returns the prepared statement, if creates
	 * 
	 * <br><br><b>Usage:</b> getPreparedStatement();
	 * @return PreparedStatement	the prepared statement
	 */
	public PreparedStatement getPreparedStatement()
	{
		return myPreparedStatement;
	}
}
