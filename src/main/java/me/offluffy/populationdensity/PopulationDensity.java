package me.offluffy.populationdensity;

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

import java.io.File;

public class PopulationDensity extends JavaPlugin {
    public final static int REGION_SIZE = 400;
    public static PopulationDensity inst;
    public static FileConfiguration config;
    public static String[] files = {"config.yml"};
    public static Permission perms;
    public static Economy econ;
    public static PluginManager pm;

    @Override
    public void onEnable() {
        inst = this;
        pm = this.getServer().getPluginManager();

        Lib.initFiles(files);
        config = Lib.loadFile(files[0]);
        new ConfigData();

        if (!setupPermissions() || !setupEconomy()) {
            Log.severe("Couldn't find Vault! Disabling " + getDescription().getName() + ".");
            this.setEnabled(false);
            return;
        }

        // Initialize DataStore, loads player and region data, and posts some stats to the log
        if (!new File(ConfigData.playerDataFolderPath).mkdirs())
            Log.warn("Couldn't create folders: " + ConfigData.playerDataFolderPath);
        if (!new File(ConfigData.regionDataFolderPath).mkdirs())
            Log.warn("Couldn't create folders: " + ConfigData.regionDataFolderPath);
        new NameData(); // Names list, loaded externally
        NameData.initNameData();
        this.initRegionSearch();

        pm.registerEvents(new BlockEventHandler(), this);
        pm.registerEvents(new EntityEventHandler(), this);
        pm.registerEvents(new PlayerEventHandler(), this);
        pm.registerEvents(new WorldEventHandler(), this);

        for (PDCmd pc : PDCmd.getCommands().values()) this.getServer().getPluginCommand(pc.getLabel()).setExecutor(pc);

        //scan the open region for resources and open a new one as necessary
        //may open and close several regions before finally leaving an "acceptable" region open
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScanOpenRegionTask(), 5L, ConfigData.regionScanHours * 60 * 60 * 20L);

        // Scan all loaded chunks for entities and compare them to the
        // specified limites in the config.yml. Remove excessive entities
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityScanTask(), 20L, ConfigData.entityScanHours * 60 * 60 * 20L);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (pp != null) perms = pp.getProvider();
        return perms != null;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> ep = getServer().getServicesManager().getRegistration(Economy.class);
        if (ep != null) econ = ep.getProvider();
        return econ != null;
    }

    private void initRegionSearch() {
        //study region data and initialize both this.openRegionCoordinates and this.nextRegionCoordinates
        Region.regionCount = Region.findNextRegion();

        //if no regions were loaded, create the first one
        if (Region.regionCount == 0) {
            Log.info("Please be patient while I search for a good new player starting point!");
            Log.info("This initial scan could take a while, especially for worlds where players have already been building.");
            Region.addRegion();
        } else {
            Log.info("Counted " + Region.regionCount + " named regions.");
        }
        Log.debug("Open region: \"" + Region.getOpenRegion().getName() + "\" at " + Region.getOpenRegion().toString() + ".");
    }
}
