package com.planet_ink.marble_mud.core.intermud.i3.packets;
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
/**
 * com.planet_ink.marble_mud.core.intermud.i3.packets.ImudServices
 * Copyright (c) 1996 George Reese
 * This source code may not be modified, copied,
 * redistributed, or used in any fashion without the
 * express written consent of George Reese.
 * 
 * The interface for a intermud services daemon
 */

import java.util.Vector;

/**
 * This interface prescribes methods that need to
 * be implemented by a class in the mudlib.  These
 * methods do mudlib specific handling of intermud
 * packets as well as provide the Imaginary Intermud 3
 * System with mudlib specific information.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public interface ImudServices {
	/**
	 * Handles an incoming I3 packet asynchronously.
	 * An implementation should make sure that asynchronously
	 * processing the incoming packet will not have any
	 * impact, otherwise you could end up with bizarre
	 * behaviour like an intermud chat line appearing
	 * in the middle of a room description.  If your
	 * mudlib is not prepared to handle multiple threads,
	 * just stack up incoming packets and pull them off
	 * the stack during your main thread of execution.
	 * @param packet the incoming packet
	 */
	public abstract void receive(Packet packet);

	/**
	 * @return an enumeration of channels this mud subscribes to
	 */
	public abstract java.util.Enumeration getChannels();

	/**
	 * Given a I3 channel name, this method should provide
	 * the local name for that channel.
	 * Example:
	 * <PRE>
	 * if( str.equals("imud_code") ) return "intercre";
	 * </PRE>
	 * @param str the remote name of the desired channel
	 * @return the local channel name for a remote channel
	 * @see #getRemoteChannel
	 */
	public abstract String getLocalChannel(String str);

	/**
	 * @return the software name and version
	 */
	public abstract String getMudVersion();

	/**
	 * @return the name of this mud
	 */
	public abstract String getMudName();

	/**
	 * @return the status of this mud
	 */
	public abstract String getMudState();
	/**
	 * @return the player port for this mud
	 */
	public abstract int getMudPort();

	/**
	 * Given a local channel name, returns the remote
	 * channel name.
	 * Example:
	 * <PRE>
	 * if( str.equals("intercre") ) return "imud_code";
	 * </PRE>
	 * @param str the local name of the desired channel
	 * @return the remote name of the specified local channel
	 */
	public abstract String getRemoteChannel(String str);
}
