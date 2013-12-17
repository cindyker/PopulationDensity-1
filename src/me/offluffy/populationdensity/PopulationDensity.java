package me.offluffy.populationdensity;

import java.io.File;

import me.offluffy.populationdensity.events.BlockEventHandler;
import me.offluffy.populationdensity.events.EntityEventHandler;
import me.offluffy.populationdensity.events.PlayerEventHandler;
import me.offluffy.populationdensity.events.WorldEventHandler;
import me.offluffy.populationdensity.tasks.EntityScanTask;
import me.offluffy.populationdensity.tasks.ScanOpenRegionTask;
import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Log;
import me.offluffy.populationdensity.utils.NameData;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.Region;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PopulationDensity extends JavaPlugin {
	public static PopulationDensity inst;
	public static FileConfiguration config;
	public static String[] files = {"config.yml"};
	public static Permission perms;
	public static Economy econ;
	public static PluginManager pm;
	public final static int REGION_SIZE = 400;
	
	@Override
	public void onEnable() {
		inst = this;
		pm = getServer().getPluginManager();
		
		Lib.initFiles(files);
		config = Lib.loadFile(files[0]);
		new ConfigData();
		
		if (!setupPermissions() || !setupEconomy()) {
			Log.severe("Couldn't find Vault! Disabling " + getDescription().getName() + ".");
			pm.disablePlugin(this);
			return;
		}
		
		// Initialize DataStore, loads player and region data, and posts some stats to the log
		new File(ConfigData.playerDataFolderPath).mkdirs();
		new File(ConfigData.regionDataFolderPath).mkdirs();
		new NameData(); // Names list, loaded externally
		NameData.initNameData();
		initRegionSearch();
		
		pm.registerEvents(new BlockEventHandler(), this);
		pm.registerEvents(new EntityEventHandler(), this);
		pm.registerEvents(new PlayerEventHandler(), this);
		pm.registerEvents(new WorldEventHandler(), this);
		
		for (PDCmd pc : PDCmd.getCommands().values())
			getServer().getPluginCommand(pc.getLabel()).setExecutor(pc);
		
		//scan the open region for resources and open a new one as necessary
		//may open and close several regions before finally leaving an "acceptable" region open
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ScanOpenRegionTask(), 5L, ConfigData.regionScanHours * 60 * 60 * 20L);
		
		// Scan all loaded chunks for entities and compare them to the
		// specified limites in the config.yml. Remove excessive entities
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new EntityScanTask(), 20L, ConfigData.entityScanHours * 60 * 60 * 20L);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (pp != null)
			perms = pp.getProvider();
		return perms != null;
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> ep = getServer().getServicesManager().getRegistration(Economy.class);
		if (ep != null)
			econ = ep.getProvider();
		return econ != null;
	}
	
	private void initRegionSearch() {
		//study region data and initialize both this.openRegionCoordinates and this.nextRegionCoordinates
		Region.regionCount = Region.findNextRegion();
		
		//if no regions were loaded, create the first one
		if(Region.regionCount == 0) {
			Log.info("Please be patient while I search for a good new player starting point!");
			Log.info("This initial scan could take a while, especially for worlds where players have already been building.");
			Region.addRegion();			
		} else {
			Log.info("Counted " + Region.regionCount + " named regions.");
		}
		Log.debug("Open region: \"" + Region.getOpenRegion().getName() + "\" at " + Region.getOpenRegion().toString() + ".");
	}
}
