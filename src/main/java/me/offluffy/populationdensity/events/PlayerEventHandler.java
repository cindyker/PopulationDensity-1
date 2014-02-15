/*
    PopulationDensity Server Plugin for Minecraft
    Copyright (C) 2011 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.offluffy.populationdensity.events;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.tasks.PlaceNewPlayerTask;
import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Log;
import me.offluffy.populationdensity.utils.LoginQueueEntry;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.PlayerData;
import me.offluffy.populationdensity.utils.PlayerHelper;
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerEventHandler implements Listener {
	// queue of players waiting to join the server
	public ArrayList<LoginQueueEntry> loginQueue = new ArrayList<LoginQueueEntry>();

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		if (!ConfigData.enableLoginQueue)
			return;

		if (e.getResult() != Result.ALLOWED)
			return;

		Player player = e.getPlayer();
		PlayerData playerData = PlayerHelper.getPlayerData(player.getName());
		
		Log.debug("");
		Log.debug("QUEUE STATUS================");
		Log.debug("");
		
		for(int i = 0; i < this.loginQueue.size(); i++) {
			LoginQueueEntry
			entry = this.loginQueue.get(i); DateFormat timeFormat =
			DateFormat.getTimeInstance(DateFormat.SHORT);
			Log.debug("\t" + entry.getPlayerName() + " " +
			entry.getPriority() + " " + timeFormat.format((new
			Date(entry.getLastRefreshed()))));
		}
		
		Log.debug("");
		Log.debug("END QUEUE STATUS================");
		Log.debug("");
		
		Log.debug("attempting to log in " + player.getName());

		Player[] playersOnline = PopulationDensity.inst.getServer().getOnlinePlayers();
		int totalSlots = PopulationDensity.inst.getServer().getMaxPlayers();

		// determine player's effective priority
		int effectivePriority = playerData.loginPriority;

		// PopulationDensity.AddLogEntry("\tlogin priority " +
		// playerData.loginPriority);

		// if the player last disconnected within the last two minutes, treat
		// the player with very high priority
		Calendar twoMinutesAgo = Calendar.getInstance();
		twoMinutesAgo.add(Calendar.MINUTE, -2);
		if (playerData.lastDisconnect.compareTo(twoMinutesAgo.getTime()) == 1 && playerData.loginPriority < 99)
			effectivePriority = 99;

		// cap priority at 100
		if (effectivePriority > 100)
			effectivePriority = 100;

		// PopulationDensity.AddLogEntry("\teffective priority " +
		// effectivePriority);

		// if the player has maximum priority
		if (effectivePriority > 99)
			// PopulationDensity.AddLogEntry("\thas admin level priority");
			// if there's room, log him in without consulting the queue
			if (playersOnline.length <= totalSlots - 2)
				// PopulationDensity.AddLogEntry("\tserver has room, so instant login");
				return;

		// scan the queue for the player, removing any expired queue entries
		long nowTimestamp = Calendar.getInstance().getTimeInMillis();

		int queuePosition = -1;
		for (int i = 0; i < this.loginQueue.size(); i++) {
			LoginQueueEntry entry = this.loginQueue.get(i);

			// if this entry has expired, remove it
			if ((nowTimestamp - entry.getLastRefreshed()) > 180000 /* three minutes */)
				// PopulationDensity.AddLogEntry("\t\tremoved expired entry for "
				// + entry.playerName);
				this.loginQueue.remove(i--);

			// otherwise compare the name in the entry
			else if (entry.getPlayerName().equals(player.getName())) {
				queuePosition = i;
				// PopulationDensity.AddLogEntry("\t\trefreshed existing entry at position "
				// + queuePosition);
				entry.setLastRefreshed(nowTimestamp);
				break;
			}
		}

		// if not in the queue, find the appropriate place in the queue to
		// insert
		if (queuePosition == -1) {
			// PopulationDensity.AddLogEntry("\tnot in the queue ");
			if (this.loginQueue.size() == 0) {
				// PopulationDensity.AddLogEntry("\tqueue empty, will insert in position 0");
				queuePosition = 0;
			} else {
				// PopulationDensity.AddLogEntry("\tsearching for best place based on rank");
				for (int i = this.loginQueue.size() - 1; i >= 0; i--) {
					LoginQueueEntry entry = this.loginQueue.get(i);

					if (entry.getPriority() >= effectivePriority) {
						queuePosition = i + 1;
						// PopulationDensity.AddLogEntry("\tinserting in position"
						// + queuePosition + " behind " + entry.playerName +
						// ", pri " + entry.priority);
						break;
					}
				}

				if (queuePosition == -1)
					queuePosition = 0;
			}

			this.loginQueue.add(queuePosition,
					new LoginQueueEntry(player.getName(), effectivePriority,
							nowTimestamp));
		}

		// PopulationDensity.AddLogEntry("\tplayer count " +
		// playersOnline.length + " / " + totalSlots);

		// if the player can log in
		int count = totalSlots - 1 - playersOnline.length;
			count -= ConfigData.reservedSlotsForAdmins;
		if (count > queuePosition) {
			// PopulationDensity.AddLogEntry("\tcan log in now, removed from queue");

			// remove from queue
			this.loginQueue.remove(queuePosition);

			// allow login
			return;
		} else {
			// otherwise, kick, notify about position in queue, and give
			// instructions
			// PopulationDensity.AddLogEntry("\tcant log in yet");
			e.setResult(Result.KICK_FULL);
			String kickMessage = ConfigData.queueMessage;
			kickMessage = kickMessage.replace("%queuePosition%",
					String.valueOf(queuePosition + 1));
			kickMessage = kickMessage.replace("%queueLength%",
					String.valueOf(this.loginQueue.size()));
			e.setKickMessage(""
					+ (queuePosition + 1)
					+ " of "
					+ this.loginQueue.size()
					+ " in queue.  Reconnect within 3 minutes to keep your place.  :)");
			e.disallow(e.getResult(), e.getKickMessage());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		Player joiningPlayer = e.getPlayer();
		
		PlayerHelper.resetIdleTimer(joiningPlayer);
		
		PlayerData playerData = PlayerHelper.getPlayerData(joiningPlayer.getName());
		if (playerData.lastObservedLocation == null)
			playerData.lastObservedLocation = joiningPlayer.getLocation();

		// if the player doesn't have a home region yet (he hasn't logged in
		// since the plugin was installed)
		Region homeRegion = playerData.homeRegion;
		if (homeRegion == null) {
			// his home region is the open region
			Region openRegion = Region
					.getOpenRegion();
			playerData.homeRegion = openRegion;
			PlayerHelper.savePlayerData(joiningPlayer.getName(), playerData);
			Log.log("Assigned new player "
					+ joiningPlayer.getName() + " to region "
					+ openRegion.getName() + " at "
					+ openRegion.toString() + ".");

			// entirely new players who've not visited the server before will
			// spawn at the default spawn
			// if configured as such, teleport him there in a few seconds (this delay avoids a bukkit issue with teleporting during login)
			// because the world takes a while to load after login, he'll never
			// know he was teleported
			Location centerOfHomeRegion = playerData.homeRegion.getCenter();
			Lib.loadChunk(centerOfHomeRegion.getBlockX(), centerOfHomeRegion.getBlockZ());
			if (ConfigData.newPlayersSpawnInHomeRegion && joiningPlayer.getLocation().distanceSquared(joiningPlayer.getWorld().getSpawnLocation()) < 625) {
				joiningPlayer.sendMessage(Clr.HEAD + "You will be placed into a region shortly");
				PlaceNewPlayerTask task = new PlaceNewPlayerTask(joiningPlayer, playerData.homeRegion);
				PopulationDensity.inst.getServer().getScheduler().scheduleSyncDelayedTask(PopulationDensity.inst, task, 20L * 3 /*about 3 seconds*/);
			}

			return;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.onPlayerDisconnect(e.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerKicked(PlayerKickEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}

	private void onPlayerDisconnect(Player player) {
		PlayerData playerData = PlayerHelper.getPlayerData(player.getName());

		// note logout timestamp
		playerData.lastDisconnect = Calendar.getInstance().getTime();
		
		//note login priority based on permissions
		// assert permission-based priority
		if (Lib.perm(player, "populationdensity.prioritylogin") && playerData.loginPriority < 25)
			playerData.loginPriority = 25;

		if (Lib.perm(player, "populationdensity.elitelogin") && playerData.loginPriority < 50)
			playerData.loginPriority = 50;

		// if the player has kicktologin permission, treat the player with
		// highest priority
		if (Lib.perm(player, "populationdensity.adminlogin"))
			playerData.loginPriority = 100;
		
		PlayerHelper.savePlayerData(player.getName(), playerData);

		// cancel any existing afk check task
		if (playerData.afkCheckTaskID >= 0) {
			PopulationDensity.inst.getServer().getScheduler().cancelTask(playerData.afkCheckTaskID);
			playerData.afkCheckTaskID = -1;
		}
		
		// clear any cached data for this player in the data store
		PlayerHelper.clearCachedPlayerData(player);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (!ConfigData.respawnInHomeRegion)
			return;

		Player player = e.getPlayer();

		// if it's NOT a bed respawn, redirect it to the player's home region
		// this keeps players near where they live, even when they die (haha)
		if (e.isBedSpawn())
			return;
		
		PlayerData playerData = PlayerHelper.getPlayerData(player.getName());
		
		// find the center of his home region
		Location homeRegionCenter = playerData.homeRegion.getCenter();

		// aim for two blocks above the highest block and teleport
		homeRegionCenter.setY(homeRegionCenter.getWorld().getHighestBlockYAt(homeRegionCenter) + 2);
		e.setRespawnLocation(homeRegionCenter);
		
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (Lib.perm(e.getPlayer(), "populationdensity.useteleportsign")) {
				Block b = e.getClickedBlock();
				if (Lib.compareMats(b.getType(),Material.SIGN, Material.SIGN_POST, Material.WALL_SIGN)) {
					String[] lines = ((Sign)b.getState()).getLines();
					if (lines[0].equals("\u00A71[ Region ]\u00A7l")) {
						if (lines[1] != "" && lines[1] != null) {
							Region region = Region.getRegion(lines[1].replace("\u00A74",""));
							if (region != null) {
								// You may need to look at the BlockEventHandler.java
								// and SignChangeEvent in order to get what this is for
								if (lines[2].equals(getSequence("c6eab15")) && lines[3].equals(getSequence("lrolnmk")))
									PlayerHelper.teleportPlayer(e.getPlayer(), region, false);
							}
						}
					}
				}
			} else {
				e.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to use teleport signs!");
			}
		}
	}
	
	// This inserts \u00A7 before each char
	public String getSequence(String chars) {
		String s = "";
		for (char c : chars.toCharArray())
			s += "\u00A7" + c;
		return s;
	}
}
