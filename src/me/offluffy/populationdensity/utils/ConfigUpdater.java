package me.offluffy.populationdensity.utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import me.offluffy.populationdensity.PopulationDensity;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

// This class is designed to update the config when it's re-organized.
public class ConfigUpdater {
	public static FileConfiguration update() {
		// Maps of where old config values are now stored
		HashMap<String, String> booleanMap = new HashMap<String, String>();
		HashMap<String, String> integerMap = new HashMap<String, String>();
		HashMap<String, String> stringMap = new HashMap<String, String>();
		HashMap<String, String> doubleMap = new HashMap<String, String>();
		HashMap<String, String> listMap = new HashMap<String, String>();
		MemoryConfiguration oldConfig = null;
		FileConfiguration newConfig = null;
		MemoryConfiguration modelConfig = null;
		boolean updated = false;
		
		Log.log("Checking config.yml for updates. Old values should be retained!");
		
		// TODO Check if PopulationDensityData folder exists and copy contents to new folder
		if ((new File("plugins" + File.separator + "PopulationDensityData")).exists()) {
			File oldData = new File("plugins" + File.separator + "PopulationDensityData");
			Log.debug("Old PopulationDensityData folder exists.");
			if (new File(oldData, "config.yml").exists())
				Log.debug("Old config.yml exists");
			if (new File(oldData, "PlayerData").exists())
				Log.debug("Old PlayerData exists");
			if (new File(oldData, "RegionData").exists())
				Log.debug("Old RegionData exists");
		}
		
		// Remap values! (List where values have moved)
		// New options will have the same new/old paths
		//             \/- Where the were before...                             \/- Where they are now!
// Old Booleans
		booleanMap.put("PopulationDensity.NewPlayersSpawnInHomeRegion", 		"Spawning.NewPlayersSpawnInHomeRegion");
		booleanMap.put("PopulationDensity.RespawnInHomeRegion", 				"Spawning.RespawnInHomeRegion");
		booleanMap.put("PopulationDensity.AllowTeleportation", 					"Teleporting.AllowTeleportation");
		booleanMap.put("PopulationDensity.TeleportFromAnywhere", 				"Teleporting.TeleportFromAnywhere");
		booleanMap.put("PopulationDensity.NewestRegionRequiresPermission", 		"Teleporting.NewestRegionRequiresPermission");
		booleanMap.put("PopulationDensity.ThinOvercrowdedAnimalsAndMonsters", 	"Scanning.LimitEntities");
		booleanMap.put("PopulationDensity.BuildRegionPosts", 					"Regions.BuildNamedPosts");
		booleanMap.put("PopulationDensity.LoginQueueEnabled", 					"Queueing.LoginQueueEnabled");
		booleanMap.put("PopulationDensity.GrassRegrows", 						"Misc.GrassRegrows");
		booleanMap.put("PopulationDensity.AnimalsRespawn", 						"Misc.AnimalsRespawn");
		booleanMap.put("PopulationDensity.TreesRegrow", 						"Misc.TreesRegrow");
// New Booleans
		booleanMap.put("Teleporting.TeleportViaOtherSigns", 					"Teleporting.TeleportViaOtherSigns");
		booleanMap.put("Misc.PrintResourceScanResults", 						"Misc.PrintResourceScanResults");
		booleanMap.put("Misc.PrintEntityScanResults", 							"Misc.PrintEntityScanResults");
		booleanMap.put("Misc.EnableDebugLogging",								"Misc.EnableDebugLogging");
		booleanMap.put("Regions.BuildWildernessPosts", 							"Regions.BuildWildernessPosts");
// Old Integers
		integerMap.put("PopulationDensity.MaxDistanceFromSpawnToUseHomeRegion", "Teleporting.PostTeleportRadius");
		integerMap.put("PopulationDensity.HoursBetweenScans", 					"Scanning.HoursBetweenResourceScans");
		integerMap.put("PopulationDensity.MinimumRegionPostY", 					"Regions.MinimumRegionPostY");
		integerMap.put("PopulationDensity.MaxIdleMinutes", 						"Queueing.MaxIdleMinutes");
		integerMap.put("PopulationDensity.MinimumPlayersOnlineForIdleBoot", 	"Queueing.MinimumPlayersOnlineForIdleBoot");
		integerMap.put("PopulationDensity.ReservedSlotsForAdministrators", 		"Queueing.ReservedSlotsForAdministrators");
// New Integers
		integerMap.put("Regions.PostProtectionRadius", 							"Regions.PostProtectionRadius");
		integerMap.put("Scanning.HoursBetweenEntityScans", 						"Scanning.HoursBetweenEntityScans");
		integerMap.put("ChunkLimits.MaximumAnimalsPerChunk", 					"ChunkLimits.MaximumAnimalsPerChunk");
		integerMap.put("ChunkLimits.MaximumMonstersPerChunk", 					"ChunkLimits.MaximumMonstersPerChunk");
		integerMap.put("ChunkLimits.MaximumVillagersPerChunk", 					"ChunkLimits.MaximumVillagersPerChunk");
		integerMap.put("ChunkLimits.MaximumGolemsPerChunk", 					"ChunkLimits.MaximumGolemsPerChunk");
		integerMap.put("ChunkLimits.MaximumDropsPerChunk", 						"ChunkLimits.MaximumDropsPerChunk");
// Old Doubles
		doubleMap.put("PopulationDensity.DensityRatio", 						"Scanning.DensityRatio");
// Old Strings
		stringMap.put("PopulationDensity.ManagedWorldName", 					"Worlds.ManagedWorld");
		stringMap.put("PopulationDensity.CityWorldName", 						"Worlds.CityWorld");
		stringMap.put("PopulationDensity.LoginQueueMessage", 					"Queueing.LoginQueueMessage");
// Old String Lists
		listMap.put("PopulationDensity.CustomSigns.Main", 						"Regions.CustomSigns.Main");
		listMap.put("PopulationDensity.CustomSigns.North", 						"Regions.CustomSigns.North");
		listMap.put("PopulationDensity.CustomSigns.South", 						"Regions.CustomSigns.South");
		listMap.put("PopulationDensity.CustomSigns.East", 						"Regions.CustomSigns.East");
		listMap.put("PopulationDensity.CustomSigns.West", 						"Regions.CustomSigns.West");
		
		// Now update them
		
		// The config that will be returned after update
		newConfig = PopulationDensity.config;
		
		// An instance of the old config loaded into memory
		oldConfig = new MemoryConfiguration(PopulationDensity.config);
		
		// A reference of default values and paths
		modelConfig = Lib.loadResource("config.yml");
		
		// Seperate loop for each data type, same process
		for (String s : booleanMap.keySet()) {
			// Does old config not have old option path?
			if (!oldConfig.contains(s)) {
				// Does old config not already have new option path?
				if (!oldConfig.contains(booleanMap.get(s))) {
					// updated boolean to determine what message to print after update!
					updated = true;
					newConfig.set(booleanMap.get(s), modelConfig.get(s));
				}
			} else { // Old config has the old option path, see if it needs updating!
				boolean old = oldConfig.getBoolean(s);
				// Copy old value if it doesn't have the new path already
				if (!oldConfig.contains(booleanMap.get(s))) {
					updated = true;
					newConfig.set(booleanMap.get(s), old);
				}
				// HashMap doesn't allow null or empty values, so old/new paths are identical if entirely new option!
				// If I didn't check for indentical paths here, the updater will always remove new options >.<
				// I'm treating paths case-sensitively, so equalsIgnoreCase() isn't necessary
				if (!booleanMap.get(s).equals(s)) {
					updated = true;
					newConfig.set(s, null);
				}
			}
		}
		
		for (String s : integerMap.keySet()) {
			if (!oldConfig.contains(s)) {
				if (!oldConfig.contains(integerMap.get(s))) {
					updated = true;
					newConfig.set(integerMap.get(s), modelConfig.get(s));
				}
			} else {
				int old = oldConfig.getInt(s);
				if (!oldConfig.contains(integerMap.get(s))) {
					updated = true;
					newConfig.set(integerMap.get(s), old);
				}
				if (!integerMap.get(s).equals(s)) {
					updated = true;
					newConfig.set(s, null);
				}
			}
		}
		
		for (String s : doubleMap.keySet()) {
			if (!oldConfig.contains(s)) {
				if (!oldConfig.contains(doubleMap.get(s))) {
					updated = true;
					newConfig.set(doubleMap.get(s), modelConfig.get(s));
				}
			} else {
				double old = oldConfig.getDouble(s);
				if (!oldConfig.contains(doubleMap.get(s))) {
					updated = true;
					newConfig.set(doubleMap.get(s), old);
				}
				if (!doubleMap.get(s).equals(s)) {
					updated = true;
					newConfig.set(s, null);
				}
			}
		}
		
		for (String s : stringMap.keySet()) {
			if (!oldConfig.contains(s)) {
				if (!oldConfig.contains(stringMap.get(s))) {
					updated = true;
					newConfig.set(stringMap.get(s), modelConfig.get(s));
				}
			} else {
				String old = oldConfig.getString(s);
				if (!oldConfig.contains(stringMap.get(s))) {
					updated = true;
					newConfig.set(stringMap.get(s), old);
				}
				if (!stringMap.get(s).equals(s)) {
					updated = true;
					newConfig.set(s, null);
				}
			}
		}
		
		for (String s : listMap.keySet()) {
			if (!oldConfig.contains(s)) {
				if (!oldConfig.contains(listMap.get(s))) {
					updated = true;
					newConfig.set(listMap.get(s), modelConfig.get(s));
				}
			} else {
				List<String> old = oldConfig.getStringList(s);
				if (!oldConfig.contains(listMap.get(s))) {
					updated = true;
					newConfig.set(listMap.get(s), old);
				}
				if (!listMap.get(s).equals(s)) {
					updated = true;
					newConfig.set(s, null);
				}
			}
		}
		
		if (updated)
			Log.log("Your config has been updated! Please note any new or moved config options!");
		else
			Log.log("Your config is up to date!");
		
		booleanMap.clear();
		integerMap.clear();
		stringMap.clear();
		doubleMap.clear();
		listMap.clear();
		
		return newConfig;
	}
}
