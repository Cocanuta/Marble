package com.planet_ink.marble_mud.WebMacros;
import com.planet_ink.marble_mud.core.interfaces.*;
import com.planet_ink.marble_mud.core.*;
import com.planet_ink.marble_mud.core.collections.*;
import com.planet_ink.marble_mud.Abilities.interfaces.*;
import com.planet_ink.marble_mud.Areas.interfaces.*;
import com.planet_ink.marble_mud.Behaviors.interfaces.*;
import com.planet_ink.marble_mud.CharClasses.interfaces.*;
import com.planet_ink.marble_mud.Libraries.interfaces.*;
import com.planet_ink.marble_mud.Common.interfaces.*;
import com.planet_ink.marble_mud.Exits.interfaces.*;
import com.planet_ink.marble_mud.Items.interfaces.*;
import com.planet_ink.marble_mud.Locales.interfaces.*;
import com.planet_ink.marble_mud.MOBS.interfaces.*;
import com.planet_ink.marble_mud.Races.interfaces.*;
import java.util.*;
import java.lang.reflect.Method;
import java.net.*;



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
public class Authenticate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()    {return false;}
	private static final long ONE_REAL_DAY=(long)1000*60*60*24;

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		if((parms!=null)&&(parms.containsKey("AUTH")))
		{
			try
			{
				return URLEncoder.encode(Encrypt(getLogin(httpReq))+"-"+Encrypt(getPassword(httpReq)),"UTF-8");
			}
			catch(Exception u)
			{
				return "false";
			}
		}
		String login=getLogin(httpReq);
		if((parms!=null)&&(parms.containsKey("SETPLAYER")))
			httpReq.addRequestParameters("PLAYER",login);
		if((parms!=null)&&(parms.containsKey("SETACCOUNT"))&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0))
		{
			MOB mob=getAuthenticatedMob(httpReq);
			if((mob!=null)&&(mob.playerStats()!=null)&&(mob.playerStats().getAccount()!=null))
				httpReq.addRequestParameters("ACCOUNT",mob.playerStats().getAccount().accountName());
		}
		if(authenticated(httpReq,login,getPassword(httpReq)))
			return "true";
		return "false";
	}
	
	protected static byte[] FILTER=null;
	public static byte[] getFilter()
	{
		if(FILTER==null)
		{
			// this is marblemud's unsophisticated xor(mac address) encryption system.
			byte[] filterc = new String("wrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad").getBytes(); 
			FILTER=new byte[256];
			try
			{
				for(int i=0;i<256;i++)
					FILTER[i]=filterc[i % filterc.length];
				String domain=CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN);
				if(domain.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=domain.charAt(i % domain.length());
				String name=CMProps.getVar(CMProps.SYSTEM_MUDNAME);
				if(name.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=name.charAt(i % name.length());
				String email=CMProps.getVar(CMProps.SYSTEM_ADMINEMAIL);
				if(email.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=email.charAt(i % email.length());
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				byte[] mac = ni.getHardwareAddress();
				if(mac != null)
				{
					for(int i=0;i<256;i++)
						FILTER[i]^=Math.abs(mac[i % mac.length]);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return FILTER;
	}
	
	protected static byte[] EnDeCrypt(byte[] bytes)
	{
		byte[] FILTER=getFilter();
		for ( int i = 0, j = 0; i < bytes.length; i++, j++ ) 
		{
		   if ( j >= FILTER.length ) j = 0;
		   bytes[i]=(byte)((bytes[i] ^ FILTER[j]) & 0xff); 
		}
		return bytes;
	}

	protected static String Encrypt(String ENCRYPTME)
	{
		try
		{
			final byte[] buf=B64Encoder.B64encodeBytes(EnDeCrypt(ENCRYPTME.getBytes()),B64Encoder.DONT_BREAK_LINES).getBytes();
			final StringBuilder s=new StringBuilder("");
			for(byte b : buf)
			{
				String s2=Integer.toHexString(b);
				while(s2.length()<2)s2="0"+s2;
				s.append(s2);
			}
			return s.toString();
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	protected static String Decrypt(String DECRYPTME)
	{
		try
		{
			byte[] buf=new byte[DECRYPTME.length()/2];
			for(int i=0;i<DECRYPTME.length();i+=2)
				buf[i/2]=(byte)(Integer.parseInt(DECRYPTME.substring(i,i+2),16) & 0xff);
			return new String(EnDeCrypt(B64Encoder.B64decode(new String(buf))));
		}
		catch(Exception e)
		{
			return "";
		}
	}

	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=CMLib.players().getLoadPlayer(login);
		if((mob!=null)
		&&(mob.playerStats()!=null)
		&&(mob.playerStats().password().equalsIgnoreCase(password))
		&&(mob.Name().trim().length()>0)
		&&(!CMSecurity.isBanned(mob.Name())))
		{
			final long lastLogin = System.currentTimeMillis() - mob.playerStats().lastDateTime();
			if(lastLogin > ONE_REAL_DAY)
				mob.playerStats().setLastDateTime(System.currentTimeMillis());
			return true;
		}
		if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
		{
			final PlayerAccount acct=CMLib.players().getLoadAccount(login);
			if((acct!=null)
			&&(acct.password().equalsIgnoreCase(password))
			&&(!CMSecurity.isBanned(acct.accountName())))
			{
				final long lastLogin = System.currentTimeMillis() - acct.lastDateTime();
				if(lastLogin > ONE_REAL_DAY)
					acct.setLastDateTime(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	public static MOB getAuthenticatedMob(ExternalHTTPRequests httpReq)
	{
		if(httpReq.getRequestObjects().containsKey("AUTHENTICATED_USER"))
		{
			Object o=httpReq.getRequestObjects().get("AUTHENTICATED_USER");
			if(!(o instanceof MOB)) return null;
			return (MOB)o;
		}
		MOB mob=null;
		String login = getLogin(httpReq);
		if((login != null)&&(login.length()>0))
		{
			String password = getPassword(httpReq);
			mob=CMLib.players().getLoadPlayer(login);
			if((mob==null)||(mob.playerStats()==null))
			{
				if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
				{
					PlayerAccount acct=CMLib.players().getLoadAccount(login);
					if((acct!=null)
					&&(acct.password().equalsIgnoreCase(password))
					&&(!CMSecurity.isBanned(acct.accountName())))
						mob=acct.getAccountMob();
					else
						mob=null;
				}
				else
					mob=null;
			}
			else
			if((!mob.playerStats().password().equalsIgnoreCase(password))
			||(mob.Name().trim().length()==0)
			||(CMSecurity.isBanned(mob.Name()))
			||((mob.playerStats().getAccount()!=null)&&(CMSecurity.isBanned(mob.playerStats().getAccount().accountName()))))
				mob=null;
		}
		if(mob!=null)
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",mob);
		else
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",new Object());
		return mob;
	}
	
	public static String getLogin(ExternalHTTPRequests httpReq)
	{
		String login=httpReq.getRequestParameter("LOGIN");
		if((login!=null)&&(login.length()>0))
		{
			if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
			{
				PlayerAccount acct = CMLib.players().getLoadAccount(login);
				if(acct != null)
				{
					MOB highestM = null;
					final String playerName=acct.findPlayer(login);
					if(playerName!=null)
					{
						login=playerName;
						highestM=CMLib.players().getLoadPlayer(login);
					}
					else
					for(Enumeration<MOB> m = acct.getLoadPlayers();m.hasMoreElements();)
					{
						MOB M=m.nextElement();
						if((highestM==null)
						||((M!=null)&&(M.basePhyStats().level()>highestM.basePhyStats().level())))
							highestM = M;
					}
					if(highestM!=null)
					{
						if(!highestM.Name().equals(login))
							httpReq.addRequestParameters("LOGIN", highestM.Name());
						return highestM.Name();
					}
				}
			}
			return login;
		}
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0) 
			login=Decrypt(auth.substring(0,x));
		return login;
	}

	public static String getPassword(ExternalHTTPRequests httpReq)
	{
		String password=httpReq.getRequestParameter("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0) 
			password=Decrypt(auth.substring(x+1));
		return password;
	}
}
