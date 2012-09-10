package com.planet_ink.marble_mud.Commands;
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
import com.planet_ink.marble_mud.Libraries.interfaces.MaskingLibrary;
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
@SuppressWarnings("rawtypes")
public class Where extends StdCommand
{
	public Where(){}

	private final String[] access={"WHERE"};
	public String[] getAccessWords(){return access;}
	
	protected void whereAdd(DVector V, Area area, int i)
	{
		if(V.contains(area)) return;

		for(int v=0;v<V.size();v++)
		{
			if(((Integer)V.elementAt(v,2)).intValue()>i)
			{
				V.insertElementAt(v,area,Integer.valueOf(i));
				return;
			}
		}
		V.addElement(area,Integer.valueOf(i));
	}

	public boolean canShowTo(MOB showTo, MOB show)
	{
		if((show!=null)
		&&(show.session()!=null)
		&&(showTo!=null)
		&&(((show.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
			||((CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.WIZINV))
				&&(showTo.phyStats().level()>=show.phyStats().level()))))
			return true;
		return false;
	}

	public String cataMark(Environmental E)
	{
		if(E==null) return "";
		if(CMLib.catalog().isCatalogObj(E))
			return "^g";
		return "";
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean overrideSet = false;
		if((commands.size()>1)&&(commands.elementAt(1).equals("!")))
			overrideSet=commands.remove(commands.elementAt(1));
		if((CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.WHERE))
		&&(!overrideSet))
		{
			StringBuffer lines=new StringBuffer("^x");
			lines.append(CMStrings.padRight("Name",17)+"| ");
			lines.append(CMStrings.padRight("Location",17)+"^.^N\n\r");
			String who=CMParms.combineWithQuotes(commands,1);
			if(who.length()==0)
			{
				for(Session S : CMLib.sessions().localOnlineIterable())
				{
					MOB mob2=S.mob();
					if(canShowTo(mob,mob2))
					{
						lines.append("^!"+CMStrings.padRight(mob2.Name(),17)+"^N| ");
						if(S.mob().location() != null )
						{
							lines.append(S.mob().location().displayText());
							lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(S.mob().location())+"^</LSTROOMID^>)");
						}
						else
							lines.append("^!(no location)^?");
						lines.append("\n\r");
					}
					else
					{
						lines.append(CMStrings.padRight("NAMELESS",17)+"| ");
						lines.append("NOWHERE");
						lines.append("\n\r");
					}
				}
			}
			else
			{

				boolean mobOnly=false;
				boolean itemOnly=false;
				boolean roomOnly=false;
				boolean exitOnly=false;
				boolean zapperMask=false;
				boolean zapperMask2=false;
				boolean areaFlag=false;
				MaskingLibrary.CompiledZapperMask compiledZapperMask=null;
				if(who.toUpperCase().startsWith("AREA "))
				{
					areaFlag=true;
					who=who.substring(5).trim();
				}

				if((who.toUpperCase().startsWith("ROOM "))
				||(who.toUpperCase().startsWith("ROOMS ")))
				{
					roomOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("EXIT "))
				||(who.toUpperCase().startsWith("EXITS ")))
				{
					exitOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("ITEM "))
				||(who.toUpperCase().startsWith("ITEMS ")))
				{
					itemOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("MOB "))
				||(who.toUpperCase().startsWith("MOBS ")))
				{
					mobOnly=true;
					who=who.substring(4).trim();
				}
				else
				if(who.toUpperCase().startsWith("MOBMASK ")||who.toUpperCase().startsWith("MOBMASK="))
				{
					mobOnly=true;
					zapperMask=true;
					who=who.substring(8).trim();
					mob.tell("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
					compiledZapperMask=CMLib.masking().maskCompile(who);
				}
				else
				if(who.toUpperCase().startsWith("ITEMMASK ")||who.toUpperCase().startsWith("ITEMMASK="))
				{
					itemOnly=true;
					zapperMask=true;
					who=who.substring(9).trim();
					mob.tell("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
					compiledZapperMask=CMLib.masking().maskCompile(who);
				}
				else
				if(who.toUpperCase().startsWith("MOBMASK2 ")||who.toUpperCase().startsWith("MOBMASK2="))
				{
					mobOnly=true;
					zapperMask2=true;
					mob.tell("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
					who=who.substring(9).trim();
				}
				else
				if(who.toUpperCase().startsWith("ITEMMASK2 ")||who.toUpperCase().startsWith("ITEMMASK2="))
				{
					itemOnly=true;
					zapperMask2=true;
					mob.tell("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
					who=who.substring(10).trim();
				}

				Enumeration<Room> r=(roomOnly||exitOnly)?CMLib.map().rooms():CMLib.map().roomsFilled();
				if(who.toUpperCase().startsWith("AREA ")||areaFlag)
					r=(roomOnly||exitOnly)?mob.location().getArea().getProperMap():mob.location().getArea().getFilledProperMap();
				if(who.toUpperCase().startsWith("AREA "))
					who=who.substring(5).trim();
				Room R = null;

				try
				{
					for(;r.hasMoreElements();)
					{
						R=(Room)r.nextElement();
						if((R!=null)&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))&&(CMLib.flags().canAccess(mob,R.getArea())))
						{
							if((!mobOnly)&&(!itemOnly)&&(!exitOnly))
								if(CMLib.english().containsString(R.displayText(),who)
								||CMLib.english().containsString(R.description(),who))
								{
									lines.append("^!"+CMStrings.padRight("*",17)+"^?| ");
									lines.append(R.roomTitle(mob));
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							if(exitOnly)
							{
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									Exit E=R.getRawExit(d);
									if((E!=null)
									&&(((E.Name().length()>0)&&(CMLib.english().containsString(E.Name(),who)))
											||((E.doorName().length()>0)&& CMLib.english().containsString(E.doorName(),who))
											||(CMLib.english().containsString(E.viewableText(mob,R).toString(),who))))
									{
										lines.append("^!"+CMStrings.padRight(Directions.getDirectionName(d),17)+"^N| ");
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
							}
							if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
								for(int i=0;i<R.numItems();i++)
								{
									Item I=R.getItem(i);
									if((zapperMask)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
											lines.append(R.roomTitle(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask2)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(who,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
											lines.append(R.roomTitle(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((CMLib.english().containsString(I.name(),who))
									||(CMLib.english().containsString(I.displayText(),who))
									||(CMLib.english().containsString(I.description(),who)))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
										lines.append(R.roomTitle(mob));
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
							for(int m=0;m<R.numInhabitants();m++)
							{
								MOB M=R.fetchInhabitant(m);
								if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
								{
									if((!itemOnly)&&(!roomOnly)&&(!exitOnly))
										if((zapperMask)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,M,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(),17)+"^N| ");
												lines.append(R.roomTitle(mob));
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(who,M,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(),17)+"^N| ");
												lines.append(R.roomTitle(mob));
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((CMLib.english().containsString(M.name(),who))
										||(CMLib.english().containsString(M.displayText(),who))
										||(CMLib.english().containsString(M.description(),who)))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(),17)+"^N| ");
											lines.append(R.roomTitle(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
									{
										for(int i=0;i<M.numItems();i++)
										{
											Item I=M.getItem(i);
											if((zapperMask)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
													lines.append("INV: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(who,I,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
													lines.append("INV: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((CMLib.english().containsString(I.name(),who))
											||(CMLib.english().containsString(I.displayText(),who))
											||(CMLib.english().containsString(I.description(),who)))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(),17)+"^N| ");
												lines.append("INV: "+cataMark(M)+M.name()+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
										if(SK!=null)
										for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
										{
											Environmental E=(Environmental)i.next();
											if((zapperMask)&&(E instanceof Item)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask)&&(E instanceof MOB)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(E instanceof Item)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(who,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(E instanceof MOB)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(who,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name()+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((CMLib.english().containsString(E.name(),who))
											||(CMLib.english().containsString(E.displayText(),who))
											||(CMLib.english().containsString(E.description(),who)))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name()+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
									}
								}
							}
						}
					}
				}catch(NoSuchElementException nse){}
			}
			mob.tell(lines.toString()+"^.");
		}
		else
		{
			int alignment=mob.fetchFaction(CMLib.factions().AlignID());
			for(int i=commands.size()-1;i>=0;i--)
			{
				String s=(String)commands.elementAt(i);
				if(s.equalsIgnoreCase("good"))
				{
					alignment=CMLib.factions().getAlignThingie(Faction.ALIGN_GOOD);
					commands.removeElementAt(i);
				}
				else
				if(s.equalsIgnoreCase("neutral"))
				{
					alignment=CMLib.factions().getAlignThingie(Faction.ALIGN_NEUTRAL);
					commands.removeElementAt(i);
				}
				else
				if(s.equalsIgnoreCase("evil"))
				{
					alignment=CMLib.factions().getAlignThingie(Faction.ALIGN_EVIL);
					commands.removeElementAt(i);
				}
			}

			int adjust=CMath.s_int(CMParms.combine(commands,1));
			DVector levelsVec=new DVector(2);
			DVector mobsVec=new DVector(2);
			DVector alignVec=new DVector(2);
			int moblevel=mob.phyStats().level()+adjust;
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(CMLib.flags().canBeLocated(A))
				&&(A.getAreaIStats()!=null))
				{
					int median=A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
					int medianDiff=0;
					int diffLimit=6;
					if((median<(moblevel+diffLimit))
					&&((median>=(moblevel-diffLimit))))
					{
						if(mob.phyStats().level()>=median)
							medianDiff=(int)Math.round(9.0*CMath.div(median,moblevel));
						else
							medianDiff=(int)Math.round(10.0*CMath.div(moblevel,median));
					}
					whereAdd(levelsVec,A,medianDiff);

					whereAdd(mobsVec,A,A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]);

					int align=A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()];
					int alignDiff=((int)Math.abs((double)(alignment-align)));
					whereAdd(alignVec,A,alignDiff);
				}
			}
			StringBuffer msg=new StringBuffer("You are currently in: ^H"+mob.location().getArea().name()+"^?\n\r");
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))&&(mob.playerStats()!=null))
				msg.append("You have explored "+mob.playerStats().percentVisited(mob,mob.location().getArea())+"% of this area and "+mob.playerStats().percentVisited(mob,null)+"% of the world.\n\r");
			DVector scores=new DVector(2);
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(CMLib.flags().canAccess(mob,A))
				{
					int index=levelsVec.indexOf(A);
					if(index>=0)
					{
						Integer I=(Integer)levelsVec.elementAt(index,2);
						if((I!=null)&&(I.intValue()!=0))
						{
							int score=(index+1);
							index=mobsVec.indexOf(A);
							if(index>=0)
								score+=(index+1);

							index=alignVec.indexOf(A);
							if(index>=0)
								score+=(index+1);
							whereAdd(scores,A,score);
						}
					}
				}
			}
			msg.append("\n\r^HThe best areas for you to try appear to be: ^?\n\r\n\r");
			msg.append("^x"+CMStrings.padRight("Area Name",35)+CMStrings.padRight("Level",6)+CMStrings.padRight("Alignment",20)+CMStrings.padRight("Pop",10)+"^.^?\n\r");
			for(int i=scores.size()-1;((i>=0)&&(i>=(scores.size()-15)));i--)
			{
				Area A=(Area)scores.elementAt(i,1);
				int lvl=A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				int align=A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()];
				
				msg.append(CMStrings.padRight(A.name(),35))
				   .append(CMStrings.padRight(Integer.toString(lvl),6))
				   .append(CMStrings.padRight(CMLib.factions().getRange(CMLib.factions().AlignID(), align).name(),20))
				   .append(CMStrings.padRight(Integer.toString(A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]),10))
				   .append("\n\r");
			}
			msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
			if(!mob.isMonster())
				mob.session().colorOnlyPrintln(msg.toString()+"\n\r");
		}
		return false;
	}

	public boolean canBeOrdered(){return true;}


}
