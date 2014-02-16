package me.offluffy.populationdensity.utils;

import me.offluffy.populationdensity.PopulationDensity;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author OffLuffy
 */
public class Lib {

    private static PopulationDensity inst = PopulationDensity.inst;

    /**
     * Checks if the Player has any of the listed perms
     *
     * @param player The Player to check
     * @param perms  The list of perms to check
     * @return Returns true if the player has any of the perms
     */
    public static boolean perm(Player player, String... perms) {
        for (String p : perms) if (PopulationDensity.perms.has(player, p)) return true;
        return false;
    }

    /**
     * Checks if the CommandSender has any of the listed perms
     *
     * @param sender The CommandSender to check
     * @param perms  The list of perms to check
     * @return Returns true if the CommandSender has any of the perms
     */
    public static boolean perm(CommandSender sender, String... perms) {
        for (String p : perms) if (PopulationDensity.perms.has(sender, p)) return true;
        return false;
    }

    /**
     * Checks a String query against a list of Strings
     *
     * @param query   The string to check
     * @param matches The list of Strings to check the query against
     * @return Returns true if the query matches any String from matches (case-insensitive)
     */
    public static boolean eq(String query, String... matches) {
        for (String s : matches) if (query.equalsIgnoreCase(s)) return true;
        return false;
    }

    /**
     * Checks through Vault's groups to see if a group matching the query exists
     *
     * @param query The name of the group to check for
     * @return Returns true if a group's name matching the query exists (case-insensitive)
     */
    public static boolean groupExists(String query) {
        for (String group : PopulationDensity.perms.getGroups()) if (group.equalsIgnoreCase(query)) return true;
        return false;
    }

    /**
     * Loads the files from the jar's src folder and copies them to the plugin's directory
     *
     * @param files String array of files
     * @return True when files are loaded, false if failed
     */
    public static boolean initFiles(String... files) {
        String fn = "";
        try {
            for (String file : files) {
                fn = file;
                File curFile = new File(inst.getDataFolder(), file);
                if (!curFile.exists()) {
                    curFile.getParentFile().mkdirs();
                    copy(inst.getResource(file), curFile);
                }
            }
            return true;
        } catch (Exception e) {
            Log.warn("There was an error creating a file: " + fn);
            return false;
        }
    }

    /**
     * Loads a file into memory as a MemoryConfiguration
     *
     * @param file The file to load, then delete after loading
     * @return The MemoryConfiguration resulting from loading the file
     */
    public static MemoryConfiguration loadResource(String file) {
        initFiles(file);
        FileConfiguration fc = loadFile(file);
        MemoryConfiguration mc = new MemoryConfiguration(fc);
        deleteFile(file);
        return mc;
    }

    /**
     * Loads a file from the plugin's directory
     *
     * @param file File to load
     * @return The FileConfiguration that was loaded
     */
    public static FileConfiguration loadFile(String file) {
        File f = new File(inst.getDataFolder(), file);
        FileConfiguration fc = new YamlConfiguration();
        try {
            fc.load(f);
            fc.save(f);
        } catch (Exception e) {
            Log.warn("There was an error loading file: " + f);
        }
        return fc;
    }

    /**
     * Loads an array of files, and stores them into a parallel array of FileConfigurations
     *
     * @param fileNames The list of file names to load
     * @param files     The parallel array of FileConfigurations to load the files into
     */
    public static void loadFiles(String[] fileNames, FileConfiguration... files) {
        for (int i = 0; i < fileNames.length; i++) files[i] = loadFile(fileNames[i]);
    }

    /**
     * Saves the specified FileConfiguration in the plugin's directory using the file name given
     *
     * @param fc   The data to save
     * @param file The file to save the data to
     */
    public static void saveFile(FileConfiguration fc, String file) {
        File f = new File(inst.getDataFolder(), file);
        try {
            fc.save(f);
        } catch (Exception e) {
            Log.warn("There was an error saving file: " + f);
        }
    }

    /**
     * Reloads the specified file
     *
     * @param fileName The file to load from disc
     * @return Returns the FileConfiguration of the newly loaded file
     */
    public static FileConfiguration reloadFile(String fileName) {
        File file = new File(PopulationDensity.inst.getDataFolder(), fileName);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        InputStream defaults = PopulationDensity.inst.getResource(fileName);
        if (defaults != null) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(defaults);
            fc.setDefaults(def);
        }
        return fc;
    }

    /**
     * Ensures a piece of the managed world is loaded into server memory
     *
     * @param x The BLOCK X coordinate
     * @param z The BLOCK Z coordinate
     */
    public static void loadChunk(int x, int z) {
        Location location = new Location(ConfigData.managedWorld, x, 5, z);
        Chunk chunk = ConfigData.managedWorld.getChunkAt(location);
        if (!chunk.isLoaded()) chunk.load(true);
    }

    /**
     * capitalizes a string, used to make region names pretty
     *
     * @param string The string to capatalize
     */
    public static String capitalize(String string) {
        if (string == null || string.length() == 0) return string;
        if (string.length() == 1) return string.toUpperCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Compares a Material to a list of other materials.
     *
     * @param match The Material to check against the query Materials
     * @param query The Materials the match is looking to equal
     * @return true if the match Material equals any of the query Materials
     */
    public static boolean compareMats(Material match, Material... query) {
        for (Material m : query) if (match.equals(m)) return true;
        return false;
    }

    private static boolean deleteFile(String file) {
        File f = new File(inst.getDataFolder(), file);
        return f.exists() && f.delete();
    }

    private static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        out.flush();
        out.close();
        in.close();
    }
}
