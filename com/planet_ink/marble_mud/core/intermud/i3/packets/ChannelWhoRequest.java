package com.planet_ink.marble_mud.core.intermud.i3.packets;
import com.planet_ink.marble_mud.core.intermud.i3.server.*;
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
 * Copyright (c) 1996 George Reese
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
public class ChannelWhoRequest extends Packet {
	public String channel = null;

	public ChannelWhoRequest()
	{
		super();
		type = Packet.CHAN_WHO_REQ;
	}
	public ChannelWhoRequest(Vector v) throws InvalidPacketException {
		super(v);
		try {
			type = Packet.CHAN_WHO_REQ;
			channel = (String)v.elementAt(6);
			channel = Intermud.getLocalChannel(channel);
		}
		catch( ClassCastException e ) {
			throw new InvalidPacketException();
		}
	}

	public void send() throws InvalidPacketException {
		if( sender_name == null || target_mud == null || sender_mud == null  || channel == null) {
			throw new InvalidPacketException();
		}
		channel = Intermud.getRemoteChannel(channel);
		super.send();
	}

	public String toString() {
		String str="({\"chan-who-req\",5,\"" + I3Server.getMudName() +
			   "\",\"" + sender_name + "\",\"" + target_mud + "\",0,\"" + channel + "\",})";
		return str;
	}
}
