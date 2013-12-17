package me.offluffy.populationdensity.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.offluffy.populationdensity.PopulationDensity;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class ConfigData {
	public final static String dataLayerFolderPath = PopulationDensity.inst.getDataFolder().getAbsolutePath();
	public final static String playerDataFolderPath = dataLayerFolderPath + File.separator + "PlayerData";
	public final static String regionDataFolderPath = dataLayerFolderPath + File.separator + "RegionData";
	public final static String namesFilePath = dataLayerFolderPath + File.separator + "regionNames.yml";
	
	public static World cityWorld;
	public static World managedWorld;
	public static String queueMessage;
	
	public static String [] mainSignContent;
	public static String [] northSignContent;
	public static String [] southSignContent;
	public static String [] eastSignContent;
	public static String [] westSignContent;
	
	public static boolean allowTeleportation;
	public static boolean teleportFromAnywhere;
	public static boolean teleSigns;
	public static boolean newPlayersSpawnInHomeRegion;
	public static boolean respawnInHomeRegion;
	public static boolean enableLoginQueue;
	public static boolean buildNamedPosts;
	public static boolean buildWildPosts;
	public static boolean regrowGrass;
	public static boolean respawnAnimals;
	public static boolean regrowTrees;
	public static boolean scanOnRestart;
	public static boolean limitEntities;
	public static boolean printResourceResults;
	public static boolean printEntityResults;
	public static boolean debugLogging;
	
	public static double densityRatio;
	
	public static int postTeleportRadius;
	public static int maxIdleMinutes;
	public static int minimumPlayersOnlineForIdleBoot;
	public static int reservedSlotsForAdmins;
	public static int maxAnimals;
	public static int maxMonsters;
	public static int maxVillagers;
	public static int maxGolems;
	public static int maxDrops;
	public static int minimumRegionPostY;
	public static int postProtectionRadius;
	public static int regionScanHours;
	public static int entityScanHours;
	
	public ConfigData() {
		
		PopulationDensity.config = ConfigUpdater.update();
		if(PopulationDensity.config == null) {
			Log.debug("Config is null!");
			return;
		}
		
		//prepare default setting for managed world...
		List<String> defaultManagedWorldNames = new ArrayList<String>();
		
		//build a list of normal environment worlds 
		List<World> worlds = Bukkit.getWorlds();
		ArrayList<World> normalWorlds = new ArrayList<World>();
		for(int i = 0; i < worlds.size(); i++)
			if(worlds.get(i).getEnvironment() == Environment.NORMAL)
				normalWorlds.add(worlds.get(i));
		
		//if there's only one, make it the default
		if(normalWorlds.size() == 1)
			defaultManagedWorldNames.add(normalWorlds.get(0).getName());
		
		if (!(PopulationDensity.config.getString("Worlds.ManagedWorld") == null) && Bukkit.getWorld(PopulationDensity.config.getString("Worlds.ManagedWorld")) != null)
			managedWorld = Bukkit.getWorld(PopulationDensity.config.getString("Worlds.ManagedWorld"));
		else {
			Log.severe("Cannot start, invalid world in config.yml");
			PopulationDensity.pm.disablePlugin(PopulationDensity.inst);
			return;
		}
		
		if (!managedWorld.getEnvironment().equals(Environment.NORMAL)) {
			Log.severe("Cannot start, world must be a normal environment");
			PopulationDensity.pm.disablePlugin(PopulationDensity.inst);
			return;
		}
		
		if (!(PopulationDensity.config.getString("Worlds.CityWorld") == null) && Bukkit.getWorld(PopulationDensity.config.getString("Worlds.CityWorld")) != null)
			cityWorld = Bukkit.getWorld(PopulationDensity.config.getString("Worlds.CityWorld"));
		else
			cityWorld = null;
		
		// READ Spawning Config Options
		newPlayersSpawnInHomeRegion = PopulationDensity.config.getBoolean("Spawning.NewPlayersSpawnInHomeRegion", true);
		respawnInHomeRegion = PopulationDensity.config.getBoolean("Spawning.RespawnInHomeRegion", true);
		
		// READ Teleporting Config Options
		allowTeleportation = PopulationDensity.config.getBoolean("Teleporting.AllowTeleportation", true);
		teleportFromAnywhere = PopulationDensity.config.getBoolean("Teleporting.TeleportFromAnywhere", false);
		teleSigns = PopulationDensity.config.getBoolean("Teleporting.TeleportViaOtherSigns", false);
		postTeleportRadius = PopulationDensity.config.getInt("Teleporting.PostTeleportRadius", 25);
		
		// READ Scanning Config Options
		densityRatio = PopulationDensity.config.getDouble("Scanning.DensityRatio", 1.0);
		regionScanHours = PopulationDensity.config.getInt("Scanning.HoursBetweenScans", 6);
		entityScanHours = PopulationDensity.config.getInt("Scanning.HoursBetweenEntityScans", 1);
		scanOnRestart = PopulationDensity.config.getBoolean("Scanning.ScanOnRestart", true);
		limitEntities = PopulationDensity.config.getBoolean("Scanning.LimitEntities", true);
		
		// READ ChunkLimits Config Options
		maxAnimals = PopulationDensity.config.getInt("ChunkLimits.MaximumAnimalsPerChunk", 20);
		maxMonsters = PopulationDensity.config.getInt("ChunkLimits.MaximumMonstersPerChunk", 8);
		maxVillagers = PopulationDensity.config.getInt("ChunkLimits.MaximumVillagersPerChunk", 5);
		maxGolems = PopulationDensity.config.getInt("ChunkLimits.MaximumGolemsPerChunk", 5);
		maxDrops = PopulationDensity.config.getInt("ChunkLimits.MaximumDropsPerChunk", 25);
		
		// READ Queueing Config Options
		enableLoginQueue = PopulationDensity.config.getBoolean("Queueing.LoginQueueEnabled", true);
		minimumPlayersOnlineForIdleBoot = PopulationDensity.config.getInt("Queueing.MinimumPlayersOnlineForIdleBoot", Bukkit.getMaxPlayers() / 2);

		queueMessage = PopulationDensity.config.getString("Queueing.LoginQueueMessage", "%queuePosition% of %queueLength% in queue.  Reconnect within 3 minutes to keep your place.  :)");
		reservedSlotsForAdmins = PopulationDensity.config.getInt("Queueing.ReservedSlotsForAdministrators", 1);
		if(reservedSlotsForAdmins < 0)
			reservedSlotsForAdmins = 0;
		
		// READ Misc Config Options
		maxIdleMinutes = PopulationDensity.config.getInt("Misc.MaxIdleMinutes", 10);
		regrowGrass = PopulationDensity.config.getBoolean("Misc.GrassRegrows", true);
		respawnAnimals = PopulationDensity.config.getBoolean("Misc.AnimalsRespawn", true);
		regrowTrees = PopulationDensity.config.getBoolean("Misc.TreesRegrow", true);
		printResourceResults = PopulationDensity.config.getBoolean("Misc.PrintResourceScanResults", true);
		printEntityResults = PopulationDensity.config.getBoolean("Misc.PrintEntityScanResults", false);
		debugLogging = PopulationDensity.config.getBoolean("Misc.EnableDebugLogging", false);
		
		// READ Regions Config Options
		buildNamedPosts = PopulationDensity.config.getBoolean("Regions.BuildNamedPosts", true);
		buildWildPosts = PopulationDensity.config.getBoolean("Regions.BuildWildernessPosts", true);
		minimumRegionPostY = PopulationDensity.config.getInt("Regions.MinimumRegionPostY", 62);
		postProtectionRadius = PopulationDensity.config.getInt("Regions.PostProtectionRadius", 10);
		
		//and write those values back and save. this ensures the config file is available on disk for editing
		
		// WRITE Worlds Config Options
		PopulationDensity.config.set("Worlds.CityWorldName", ((cityWorld == null)?"":cityWorld.getName()));
		PopulationDensity.config.set("Worlds.ManagedWorld", ((managedWorld == null)?"":managedWorld.getName()));
		
		// WRITE Spawning Config Options
		PopulationDensity.config.set("Spawning.NewPlayersSpawnInHomeRegion", newPlayersSpawnInHomeRegion);
		PopulationDensity.config.set("Spawning.RespawnInHomeRegion", respawnInHomeRegion);
		
		// WRITE Teleporting Config Options
		PopulationDensity.config.set("Teleporting.AllowTeleportation", allowTeleportation);
		PopulationDensity.config.set("Teleporting.TeleportFromAnywhere", teleportFromAnywhere);
		PopulationDensity.config.set("Teleporting.PostTeleportRadius", postTeleportRadius);
		PopulationDensity.config.set("Teleporting.TeleportViaOtherSigns", teleSigns);
		
		// WRITE Scanning Config Options
		PopulationDensity.config.set("Scanning.DensityRatio", densityRatio);
		PopulationDensity.config.set("Scanning.HoursBetweenScans", regionScanHours);
		PopulationDensity.config.set("Scanning.HoursBetweenEntityScans", entityScanHours);
		PopulationDensity.config.set("Scanning.ScanOnRestart", scanOnRestart);
		PopulationDensity.config.set("Scanning.LimitEntities", limitEntities);
		
		// WRITE ChunkLimits Config Options
		PopulationDensity.config.set("ChunkLimits.MaximumAnimalsPerChunk", maxAnimals);
		PopulationDensity.config.set("ChunkLimits.MaximumMonstersPerChunk", maxMonsters);
		PopulationDensity.config.set("ChunkLimits.MaximumVillagersPerChunk", maxVillagers);
		PopulationDensity.config.set("ChunkLimits.MaximumGolemsPerChunk", maxGolems);
		PopulationDensity.config.set("ChunkLimits.MaximumDropsPerChunk", maxDrops);
		
		// WRITE Queueing Config Options
		PopulationDensity.config.set("Queueing.MaxIdleMinutes", maxIdleMinutes);
		PopulationDensity.config.set("Queueing.LoginQueueEnabled", enableLoginQueue);
		PopulationDensity.config.set("Queueing.MinimumPlayersOnlineForIdleBoot", minimumPlayersOnlineForIdleBoot);
		PopulationDensity.config.set("Queueing.ReservedSlotsForAdministrators", reservedSlotsForAdmins);
		PopulationDensity.config.set("Queueing.LoginQueueMessage", queueMessage);
		
		// WRITE Misc Config Options
		PopulationDensity.config.set("Misc.GrassRegrows", regrowGrass);
		PopulationDensity.config.set("Misc.AnimalsRespawn", respawnAnimals);
		PopulationDensity.config.set("Misc.TreesRegrow", regrowTrees);
		PopulationDensity.config.set("Misc.PrintResourceScanResults", printResourceResults);
		PopulationDensity.config.set("Misc.PrintEntityScanResults", printEntityResults);
		PopulationDensity.config.set("Misc.EnableDebugLogging", debugLogging);
		
		// WRITE Regions Config Options
		PopulationDensity.config.set("Regions.PostProtectionRadius", postProtectionRadius);
		PopulationDensity.config.set("Regions.BuildNamedPosts", buildNamedPosts);
		PopulationDensity.config.set("Regions.BuildWindernessPosts", buildWildPosts);
		PopulationDensity.config.set("Regions.MinimumRegionPostY", minimumRegionPostY);
		
		// this is a combination load/preprocess/save for custom signs on the region posts
		mainSignContent = initializeSignContentConfig("Main", "", "Population", "Density", "");
		northSignContent = initializeSignContentConfig("North", "", "", "", "");
		southSignContent = initializeSignContentConfig("South", "", "", "", "");
		eastSignContent = initializeSignContentConfig("East", "", "", "", "");
		westSignContent = initializeSignContentConfig("West", "", "", "", "");
		
		// No more PopulationDensity nodes, remove them all
		PopulationDensity.config.set("PopulationDensity", null);
		
		Lib.saveFile(PopulationDensity.config, "config.yml");
		Log.debug("Saved config");
	}
	
	private String[] initializeSignContentConfig(String node, String ... defaultLines) {
		List<String> nodeData = PopulationDensity.config.getStringList("Regions.CustomSigns." + node);
		
		int i = 0;
		if(nodeData == null || nodeData.size() == 0)
			for(; i < defaultLines.length && i < 4; i++)
				nodeData.add(defaultLines[i]);
		
		for(i = nodeData.size(); i < 4; i++)
			nodeData.add("");
		
		PopulationDensity.config.set("Regions.CustomSigns." + node, nodeData);
		
		boolean emptySign = true;
		for(i = 0; i < 4; i++) {
			if(nodeData.get(i).length() > 0) {
				emptySign = false;
				break;
			}
		}
		
		if(!emptySign) {
			String [] returnArray = new String [4];
			for(i = 0; i < 4 && i < nodeData.size(); i++)
				returnArray[i] = nodeData.get(i);
			return returnArray;
		}
		return null;
	}
}
