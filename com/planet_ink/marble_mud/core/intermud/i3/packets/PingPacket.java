package com.planet_ink.marble_mud.core.intermud.i3.packets;
import com.planet_ink.marble_mud.core.intermud.i3.server.I3Server;
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

import java.util.Vector;

/**
 * Copyright (c) 2010-2012 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
@SuppressWarnings("rawtypes")
public class PingPacket extends Packet 
{
	public static volatile long lastPingResponse=0;
	
	public PingPacket()
	{
		super();
		type = Packet.PING_PACKET;
		target_mud=Intermud.getNameServer().name;
	}
	
	public PingPacket(Vector v)
	{
		super(v);
		type = Packet.PING_PACKET;
		target_mud=v.elementAt(4).toString();
	}

	public PingPacket(String mud)
	{
		super();
		type = Packet.PING_PACKET;
		target_mud=mud;
	}

	public void send() throws InvalidPacketException 
	{
		super.send();
	}

	public String toString() 
	{
		return "({\"ping-req\",5,\""+I3Server.getMudName()+"\",0,\""+target_mud+"\",0,0,})";
	}
}
