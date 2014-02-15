package me.offluffy.populationdensity.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.tasks.AfkCheckTask;
import me.offluffy.populationdensity.utils.Messages.Clr;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerHelper {
	private static HashMap<String, PlayerData> nameDataMap = new HashMap<String, PlayerData>();
	
	public static void addData(String player, PlayerData data) {
		nameDataMap.put(player, data);
	}
	
	public static void removeData(String player) {
		if (nameDataMap.containsKey(player)) {
			nameDataMap.remove(player);
		}
	}
	
	/**
	 * examines configuration, player permissions, and player location to determine whether or not to allow a teleport
	 * @param player The Player object to check
	 * @param isHomeOrCityTeleport Whether or not the location is a home or city teleport
	 * @return True if the Player is allowed to teleport
	 */
	// XXX Modify with per-teleport permission checking and messages
	public static boolean playerCanTeleport(Player player, boolean isHomeOrCityTeleport) {
		//if the player has the permission for teleportation, always allow it
		if(Lib.perm(player, "populationdensity.teleportanywhere"))
			return true;
		
		//if teleportation from anywhere is enabled, always allow it
		if(ConfigData.teleportFromAnywhere)
			return true;
		
		//avoid teleporting from other worlds
		if(!player.getWorld().equals(ConfigData.managedWorld)) {
			player.sendMessage(Clr.ERR + "You can't teleport from here!");
			return false;
		}
		
		//when teleportation isn't allowed, the only exceptions are city to home, and home to city
		if(!ConfigData.allowTeleportation) {
			if(!isHomeOrCityTeleport) {
				player.sendMessage(Clr.ERR + "You're limited to /HomeRegion and /CityRegion here.");
				return false;
			}
			
			//if close to home post, go for it
			PlayerData playerData = getPlayerData(player.getName());
			Location homeCenter = playerData.homeRegion.getCenter();
			if(homeCenter.distanceSquared(player.getLocation()) < 100)
				return true;
			
			//if city is defined and close to city post, go for it
			if(nearCityPost(player))
				return true;
			
			player.sendMessage(Clr.ERR + "You can't teleport from here!");
			return false;
		} else { //otherwise, any post is acceptable to teleport from or to
			Region currentRegion = Region.fromLocation(player.getLocation());
			Location currentCenter = currentRegion.getCenter();
			if(currentCenter.distanceSquared(player.getLocation()) < 100)
				return true;
			
			if(nearCityPost(player))
				return true;
			
			player.sendMessage(Clr.ERR + "You're not close enough to a region post to teleport.");
			player.sendMessage(Clr.ERR + "On the surface, look for a glowing yellow post on a stone platform.");
			return false;
		}
	}
	
	public static boolean nearCityPost(Player player) {
		if(ConfigData.cityWorld != null && player.getWorld().equals(ConfigData.cityWorld)) {
			//max distance == 0 indicates no distance maximum
			return (ConfigData.postTeleportRadius < 1 || player.getLocation().distance(ConfigData.cityWorld.getSpawnLocation()) < ConfigData.postTeleportRadius);
		}
		return false;
	}

	/**
	 * Teleports a player to a specific region of the managed world, notifying players of arrival/departure as necessary
	 * @param player The Player to teleport
	 * @param region The RegionCoordinates object which cooresponds to the region to be teleported to
	 * @param silent True will send the player a message that they have been teleported
	 * @see RegionCoordinates
	 */
	//players always land at the region's region post, which is placed on the surface at the center of the region
	public static void teleportPlayer(Player player, Region region, boolean silent) {
		//where specifically to send the player?
		Location teleportDestination = region.getCenter();
		double x = teleportDestination.getBlockX()+0.5;
		double z = teleportDestination.getBlockZ()+2.5;
		
		//make sure the chunk is loaded
		Lib.loadChunk((int)x, (int)z);
		
		//send him the chunk so his client knows about his destination
		teleportDestination.getWorld().refreshChunk((int)x, (int)z);
		
		//find a safe height, a couple of blocks above the surface		
		Block highestBlock = ConfigData.managedWorld.getHighestBlockAt((int)x, (int)z);
		teleportDestination = new Location(ConfigData.managedWorld, x, highestBlock.getY(), z, -180, 0);		
		
		String regName = Clr.HEAD + "the wilderness";
		if (Region.getRegionName(region) != null)
			regName = Clr.HEAD + Lib.capitalize(Region.getRegionName(region));
		
		//notify him
		if (!silent)
			player.sendMessage(Clr.NORM + "Teleporting you to " + regName);
		
		//send him
		player.teleport(teleportDestination);
	}
	
	/**
	 * Fetches the PlayerData object cached for a given user.
	 * This method will attempt to load the player's data if not already cached.
	 * @param player The player whose data to fetch from the data cache
	 * @return The PlayerData object which corresponds to the provided player, null if not found
	 * @see PlayerData
	 */
	public static PlayerData getPlayerData(String player) {
		//first, check the in-memory cache
		PlayerData data = PlayerHelper.nameDataMap.get(player);
		
		if(data != null)
			return data;
		
		//if not there, try to load the player from file		
		loadPlayerData(player);
		
		//check again
		data = PlayerHelper.nameDataMap.get(player);
		
		if(data != null) return data;
		
		return new PlayerData();
	}
	
	/**
	 * Loads a players data from file, stores it in a PlayerData object and adds it to the cache data list
	 * @param playerName The player whose data is to be loaded
	 * @see PlayerData
	 */
	private static void loadPlayerData(String playerName) {
		//load player data into memory		
		File playerFile = new File(ConfigData.playerDataFolderPath + File.separator + playerName);
		
		BufferedReader inStream = null;
		try {					
			PlayerData playerData = new PlayerData();
			inStream = new BufferedReader(new FileReader(playerFile.getAbsolutePath()));
						
			//first line is home region coordinates
			String homeCoords = inStream.readLine();
			
			//second line is date of last disconnection
			String lastDisconnectedString = inStream.readLine();
			
			//third line is login priority
			String rankString = inStream.readLine(); 
			
			//convert string representation of home coordinates to a proper object
			Region homeRegionCoordinates = new Region(homeCoords);
			playerData.homeRegion = homeRegionCoordinates;
			  
			//parse the last disconnect date string
			try {
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.ROOT);
				Date lastDisconnect = dateFormat.parse(lastDisconnectedString);
				playerData.lastDisconnect = lastDisconnect;
			} catch(Exception e) {
				playerData.lastDisconnect = Calendar.getInstance().getTime();
			}
			
			//parse priority string
			if(rankString == null || rankString.isEmpty())
				playerData.loginPriority = 0;
			else {
				try {
					playerData.loginPriority = Integer.parseInt(rankString);
				} catch(Exception e) {
					playerData.loginPriority = 0;
				}			
			}
			  
			//shove into memory for quick access
			PlayerHelper.nameDataMap.put(playerName, playerData);
		} catch(FileNotFoundException e) {
			//if the file isn't found, just don't do anything (probably a new-to-server player)
			return;
		} catch(Exception e) {
			//if there's any problem with the file's content, log an error message and skip it
			 Log.warn("Unable to load data for player \"" + playerName + "\": " + e.getMessage());			 
		}
		
		try {
			if(inStream != null) inStream.close();
		} catch(IOException exception) {}		
	}
	
	/**
	 * Writes the player's PlayerData object to file
	 * @param player The player whose data to save
	 * @param data The PlayerData object to write to file
	 * @see PlayerData
	 */
	public static void savePlayerData(String player, PlayerData data) {
		//save that data in memory
		PlayerHelper.addData(player, data);
		
		BufferedWriter outStream = null;
		try {
			//open the player's file
			File playerFile = new File(ConfigData.playerDataFolderPath + File.separator + player);
			playerFile.createNewFile();
			outStream = new BufferedWriter(new FileWriter(playerFile));
			
			//first line is home region coordinates
			outStream.write(data.homeRegion.toString());
			outStream.newLine();
			
			//second line is last disconnection date,
			//note use of the ROOT locale to avoid problems related to regional settings on the server being updated
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.ROOT);			
			outStream.write(dateFormat.format(data.lastDisconnect));
			outStream.newLine();
			
			//third line is login priority
			outStream.write(String.valueOf(data.loginPriority));
			outStream.newLine();
		} catch(Exception e) {
			//if any problem, log it
			Log.warn("Unexpected exception saving data for player \"" + player + "\": " + e.getMessage());
		}		
		
		try {
			//close the file
			if(outStream != null)
				outStream.close();
		} catch(IOException exception) {}
	}
	
	/**
	 * Removes cached data from the player data list
	 * @param player The player whose data is to be cleared
	 */
	public static void clearCachedPlayerData(Player player) {
		nameDataMap.remove(player.getName());		
	}
	
	public static void resetIdleTimer(Player player) {
		//if idle kick is disabled, don't do anything here
		if(ConfigData.maxIdleMinutes < 1) return;
		
		PlayerData playerData = PlayerHelper.getPlayerData(player.getName());
		
		//if there's a task already in the queue for this player, cancel it
		if(playerData.afkCheckTaskID >= 0)
			PopulationDensity.inst.getServer().getScheduler().cancelTask(playerData.afkCheckTaskID);
		
		//queue a new task for later
		//note: 20L ~ 1 second
		playerData.afkCheckTaskID = PopulationDensity.inst.getServer().getScheduler().scheduleSyncDelayedTask(PopulationDensity.inst, new AfkCheckTask(player, playerData), 20L * 60 * ConfigData.maxIdleMinutes);
	}
}
