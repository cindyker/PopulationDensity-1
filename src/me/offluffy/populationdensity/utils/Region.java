package me.offluffy.populationdensity.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.tasks.ScanRegionTask;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class Region {
	private int x, z;
	private String name;
	
	public static Region openRegion;
	public static Region nextRegion;
	public static int regionCount;

	/**
	 * Constructor; Initializes a new Region object with the given coordinates
	 * @param xCoord The X coordinate of the region
	 * @param zCoord the Z coordinate of the region
	 */
	public Region (int xCoord, int zCoord) {
		x = xCoord;
		z = zCoord;
		name = null;
	}
	
	private enum Direction {
		DOWN, LEFT, UP, RIGHT;
		public Direction getNext() {
			if (this == Direction.DOWN)
				return Direction.LEFT;
			else if (this == Direction.LEFT)
				return Direction.UP;
			else if (this == Direction.UP)
				return Direction.RIGHT;
			else
				return Direction.DOWN;
		}
	}
	
	/**
	 * Constructor; Converts a string representing region coordinates to a proper region object
	 * @param string The string representing the region coordinates
	 */
	public Region(String string) {
		String [] elements = string.split(" ");
	    
		String xString = elements[0];
	    String zString = elements[1];
	    
	    x = Integer.parseInt(xString);
	    z = Integer.parseInt(zString);
	    name = null;
	}
	
	/**
	 * Given a location, returns the coordinates of the region containing that location
	 * @param location The location to find a region
	 * @return A Region object associated with the found region or null if location is not in the managed world
	 */
	public static Region fromLocation(Location location) {
		if(!ConfigData.managedWorld.equals(location.getWorld()))
			return null;
		
		int x = location.getBlockX() / PopulationDensity.REGION_SIZE;
		if(location.getX() < 0) x--;
		
		int z = location.getBlockZ() / PopulationDensity.REGION_SIZE;
		if(location.getZ() < 0) z--;
		
		return new Region(x, z);		
	}
	
	/**
	 * @return The X coordinate of the Region object
	 */
	public int getX() { return x; }
	
	/**
	 * @return The Z coordinate of the Region object
	 */
	public int getZ() { return z; }
	
	/**
	 * @return The name of the region or null of a wilderness region
	 */
	public String getName() { return name; }
	
	/**
	 * Determines the center of a region
	 * @param region The region's center to find
	 * @return The Location of the center of the Region
	 */
	public Location getCenter() {
		int sz = PopulationDensity.REGION_SIZE;
		World w = ConfigData.managedWorld;
		x = (x * sz) + (sz / 2);
		z = (z * sz) + (sz / 2);
		Location center = new Location(w, x, 1, z);
		Lib.loadChunk(x,z);
		center = w.getHighestBlockAt(center).getLocation();
		return center;
	}
	
	/**
	 * Converts region coordinates to a handy string
	 * @return The String version of the coordinates for the RegionCoordinates object
	 */
	@Override
	public String toString() {
		return Integer.toString(this.x) + " " + Integer.toString(this.z);
	}
	
	/**
	 * Edits the world to create a region post at the center of the specified region	
	 * @param region The RegionCoordinates object associated with the region to create the post for
	 * @param updateNeighboringRegions Whether or not to update neighboring regions
	 * @see RegionCoordinates
	 */
	@SuppressWarnings("deprecation")
	public void addPost(boolean updateNeighboringRegions) {
		//if region post building is disabled, don't do anything
		if(!ConfigData.buildNamedPosts && !ConfigData.buildWildPosts)
			return;
		if ((getName() == null && !ConfigData.buildWildPosts) || (getName() != null && !ConfigData.buildNamedPosts))
			return;
		
		//find the center
		Location regionCenter = getCenter();
		int x = regionCenter.getBlockX();
		int z = regionCenter.getBlockZ();
		int y;

		//make sure data is loaded for that area, because we're about to request data about specific blocks there
		Lib.loadChunk(x, z);
		
		//sink lower until we find something solid
		//also ignore glowstone, in case there's already a post here!
		//race condition issue: chunks say they're loaded when they're not.  if it looks like the chunk isn't loaded, try again (up to five times)
		int retriesLeft = 5;
		boolean tryAgain;
		do {
			tryAgain = false;
			
			//find the highest block.  could be the surface, a tree, some grass...
			y = ConfigData.managedWorld.getHighestBlockYAt(x, z) + 1;
			
			//posts fall through trees, snow, and any existing post looking for the ground
			Material blockType;
			do {
				blockType = ConfigData.managedWorld.getBlockAt(x, --y, z).getType();
			} while(y > 2 && canFall(blockType));
			
			//if final y value is extremely small, it's probably wrong
			if(y < 5 && retriesLeft-- > 0) {
				tryAgain = true;
				try {
					Thread.sleep(500); //sleep half a second before restarting the loop
				} catch(InterruptedException e) {}
			}
		} while(tryAgain);
				
		//if y value is under sea level, correct it to sea level (no posts should be that difficult to find)
		if(y < ConfigData.minimumRegionPostY)
			y = ConfigData.minimumRegionPostY;
		
		//clear signs from the area, this ensures signs don't drop as items 
		//when the blocks they're attached to are destroyed in the next step
		for(int x1 = x - 2; x1 <= x + 2; x1++)
			for(int z1 = z - 2; z1 <= z + 2; z1++) 
				for(int y1 = y + 1; y1 <= y + 15; y1++)
					if (Lib.compareMats(ConfigData.managedWorld.getBlockAt(x1,y1,z1).getType(), Material.SIGN_POST, Material.SIGN, Material.WALL_SIGN))
						ConfigData.managedWorld.getBlockAt(x1,y1,z1).setType(Material.AIR);
		
		//clear above it - sometimes this shears trees in half (doh!)
		for(int x1 = x - 2; x1 <= x + 2; x1++)
			for(int z1 = z - 2; z1 <= z + 2; z1++)
				for(int y1 = y + 1; y1 < ConfigData.managedWorld.getMaxHeight(); y1++)
					ConfigData.managedWorld.getBlockAt(x1, y1, z1).setType(Material.AIR);
		
		
		//build a glowpost in the center
		for(int y1 = y; y1 <= y + 3; y1++)
			ConfigData.managedWorld.getBlockAt(x, y1, z).setTypeIdAndData(Material.GLOWSTONE.getId(), (byte)0, false);
		
		//build a stone platform
		for(int x1 = x - 2; x1 <= x + 2; x1++)
			for(int z1 = z - 2; z1 <= z + 2; z1++)
				ConfigData.managedWorld.getBlockAt(x1, y, z1).setTypeIdAndData(Material.SMOOTH_BRICK.getId(), (byte)0, false);
		
		//if the region has a name, build a sign on top
		String regionName = getName();
		if(regionName != null) {		
			regionName = Lib.capitalize(regionName);
			Block block = ConfigData.managedWorld.getBlockAt(x, y + 4, z);
			block.setTypeIdAndData(Material.SIGN_POST.getId(), (byte)0, false);
			org.bukkit.block.Sign sign = (org.bukkit.block.Sign)block.getState();
			sign.setLine(1, "Welcome to");
			sign.setLine(2, "\u00A71" + regionName);
			sign.update();
		}
		
		//add a sign for the region to the south
		regionName = new Region(getX() + 1, getZ()).getName();
		if(regionName == null) regionName = "\u00A74Wilderness";
		regionName = "\u00A71" + Lib.capitalize(regionName);
		
		Block block = ConfigData.managedWorld.getBlockAt(x, y + 2, z - 1);
		
		org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
		signData.setFacingDirection(BlockFace.NORTH);
		
		block.setType(Material.WALL_SIGN);
		block.setData(signData.getData());

		org.bukkit.block.Sign sign = (org.bukkit.block.Sign)block.getState();
		
		sign.setLine(0, "E");
		sign.setLine(1, "<--");
		sign.setLine(2, regionName);
		
		sign.update();
		
		//if a city world is defined, also add a /cityregion sign on the east side of the post
		if(ConfigData.cityWorld != null) {
			block = ConfigData.managedWorld.getBlockAt(x, y + 3, z - 1);
			
			signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
			signData.setFacingDirection(BlockFace.NORTH);

			block.setType(Material.WALL_SIGN);
			block.setData(signData.getData());
			
			sign = (Sign)block.getState();
			
			sign.setLine(0, "Visit the City:");
			sign.setLine(1, "/CityRegion");
			sign.setLine(2, "Return Home:");
			sign.setLine(3, "/HomeRegion");
			
			sign.update();
		}
		
		//add a sign for the region to the east
		regionName = new Region(getX(), getZ() - 1).getName();
		if(regionName == null) regionName = "\u00A74Wilderness";
		regionName = "\u00A71" + Lib.capitalize(regionName);
		
		block = ConfigData.managedWorld.getBlockAt(x - 1, y + 2, z);
		
		signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
		signData.setFacingDirection(BlockFace.WEST);

		block.setType(Material.WALL_SIGN);
		block.setData(signData.getData());
		
		sign = (org.bukkit.block.Sign)block.getState();
		
		sign.setLine(0, "N");
		sign.setLine(1, "<--");
		sign.setLine(2, regionName);
		
		sign.update();
		
		//if teleportation is enabled, also add a sign facing north for /visitregion and /invitetoregion
		if(ConfigData.allowTeleportation) {
			block = ConfigData.managedWorld.getBlockAt(x - 1, y + 3, z);
			
			signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
			signData.setFacingDirection(BlockFace.WEST);

			block.setType(Material.WALL_SIGN);
			block.setData(signData.getData());
			
			sign = (org.bukkit.block.Sign)block.getState();
			
			sign.setLine(0, "Visit Friends:");
			sign.setLine(1, "/VisitRegion");
			sign.setLine(2, "Invite Friends:");
			sign.setLine(3, "/InviteToRegion");
			
			sign.update();
		}
		
		//add a sign for the region to the south
		regionName = new Region(getX(), getZ() + 1).getName();
		if(regionName == null) regionName = "\u00A74Wilderness";
		regionName = "\u00A71" + Lib.capitalize(regionName);
		
		block = ConfigData.managedWorld.getBlockAt(x + 1, y + 2, z);
		
		signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
		signData.setFacingDirection(BlockFace.EAST);

		block.setType(Material.WALL_SIGN);
		block.setData(signData.getData());
		
		sign = (org.bukkit.block.Sign)block.getState();
		
		sign.setLine(0, "S");
		sign.setLine(1, "<--");
		sign.setLine(2, regionName);
		
		sign.update();
		
		//if teleportation is enabled, also add a sign facing south for /homeregion
		if(ConfigData.allowTeleportation) {
			block = ConfigData.managedWorld.getBlockAt(x + 1, y + 3, z);
			signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
			signData.setFacingDirection(BlockFace.EAST);

			block.setType(Material.WALL_SIGN);
			block.setData(signData.getData());
			
			sign = (org.bukkit.block.Sign)block.getState();
			
			sign.setLine(0, "Set Your Home:");
			sign.setLine(1, "/MoveIn");
			sign.setLine(2, "Return Home:");
			sign.setLine(3, "/HomeRegion");
			
			sign.update();
		}
		
		//add a sign for the region to the north
		regionName = new Region(getX() - 1, getZ()).getName();
		if(regionName == null) regionName = "\u00A74Wilderness";
		regionName = "\u00A71" + Lib.capitalize(regionName);
		
		block = ConfigData.managedWorld.getBlockAt(x, y + 2, z + 1);
		
		signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
		signData.setFacingDirection(BlockFace.SOUTH);

		block.setType(Material.WALL_SIGN);
		block.setData(signData.getData());
		
		sign = (org.bukkit.block.Sign)block.getState();
		
		sign.setLine(0, "W");
		sign.setLine(1, "<--");
		sign.setLine(2, regionName);
		
		sign.update();
		
		//if teleportation is enabled, also add a sign facing west for /newestregion and /randomregion
		if(ConfigData.allowTeleportation) {
			block = ConfigData.managedWorld.getBlockAt(x, y + 3, z + 1);

			signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
			signData.setFacingDirection(BlockFace.SOUTH);

			block.setType(Material.WALL_SIGN);
			block.setData(signData.getData());

			sign = (org.bukkit.block.Sign)block.getState();
			
			sign.setLine(0, "Adventure!");
			sign.setLine(2, "/RandomRegion");
			sign.setLine(3, "/NewestRegion");
			
			sign.update();
		}
		
		//custom signs
		
		if(ConfigData.mainSignContent != null) {
			block = ConfigData.managedWorld.getBlockAt(x, y + 3, z - 1);

			signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
			signData.setFacingDirection(BlockFace.NORTH);

			block.setType(Material.WALL_SIGN);
			block.setData(signData.getData());
			
			sign = (org.bukkit.block.Sign)block.getState();
			
			for(int i = 0; i < 4; i++)
				sign.setLine(i, ConfigData.mainSignContent[i]);
			
			sign.update();
		}
		
		// 62 lines whiddled down to 36 lines!
		HashMap<BlockFace, String[]> content = new HashMap<BlockFace, String[]>();
		content.put(BlockFace.WEST, ConfigData.northSignContent);
		content.put(BlockFace.EAST, ConfigData.southSignContent);
		content.put(BlockFace.NORTH, ConfigData.eastSignContent);
		content.put(BlockFace.SOUTH, ConfigData.westSignContent);
		
		for (BlockFace face : content.keySet()) {
			if (content.get(face) != null) {
				switch (face) {
					case WEST:
						block = ConfigData.managedWorld.getBlockAt(x - 1, y + 1, z);
						break;
					case EAST:
						block = ConfigData.managedWorld.getBlockAt(x + 1, y + 1, z);
						break;
					case NORTH:
						block = ConfigData.managedWorld.getBlockAt(x, y + 1, z - 1);
						break;
					case SOUTH:
						block = ConfigData.managedWorld.getBlockAt(x, y + 1, z + 1);
						break;
					default:
						break;
				}
				signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
				signData.setFacingDirection(face);

				block.setType(Material.WALL_SIGN);
				block.setData(signData.getData());
				
				sign = (org.bukkit.block.Sign)block.getState();
				
				for(int i = 0; i < 4; i++)
					sign.setLine(i, content.get(face)[i]);
				
				sign.update();
			}
		}
		content.clear();
		
		if(updateNeighboringRegions) {
			new Region(getX() - 1, getZ()).addPost(false);
			new Region(getX() + 1, getZ()).addPost(false);
			new Region(getX(), getZ() - 1).addPost(false);
			new Region(getX(), getZ() + 1).addPost(false);
		}
	}
	
	/**
	 * Names a region. Also deletes and re-writes region data to disk.
	 * @param coords The RegionCoordinates object that corresponds to the desired region
	 * @param name The name of the region
	 */
	public void nameRegion(String name) {
		//region names are always lowercase
		name = name.toLowerCase();
		this.name = name;
		
		//delete any existing data for the region at these coordinates
		String oldRegionName = getName();
		if(oldRegionName != null) {
			File oldRegionFile = new File(ConfigData.regionDataFolderPath + File.separator + toString());
			oldRegionFile.delete();
			
			File oldRegionNameFile = new File(ConfigData.regionDataFolderPath + File.separator + oldRegionName);
			oldRegionNameFile.delete();
		}

		//"create" the region by saving necessary data to disk
		//(region names to coordinates mappings aren't kept in memory because they're less often needed, and this way we keep it simple) 
		BufferedWriter outStream = null;
		try {
			//coordinates file contains the region's name
			File regionNameFile = new File(ConfigData.regionDataFolderPath + File.separator + name);
			regionNameFile.createNewFile();
			outStream = new BufferedWriter(new FileWriter(regionNameFile));
			outStream.write(toString());
			outStream.close();
			
			//name file contains the coordinates
			File regionCoordinatesFile = new File(ConfigData.regionDataFolderPath + File.separator + toString());
			regionCoordinatesFile.createNewFile();
			outStream = new BufferedWriter(new FileWriter(regionCoordinatesFile));
			outStream.write(name);
			outStream.close();			
		} catch(Exception e) {
			//in case of any problem, log the details
			Log.warn("Unexpected Exception: " + e.getMessage());
		}
		
		try {
			if(outStream != null)
				outStream.close();		
		} catch(IOException exception) {}
	}
	
	/**
	 * Rescan the current region
	 * @param openNewRegions Whether or not to open the next suitable region
	 */
	public void scanRegion(boolean openNewRegions) {
		scanRegion(this, openNewRegions);
	}
	
	/**
	 * Compares this Region object to another object
	 * @return true of two objects are equal (based on X & Z coords), false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Region)) return false;
		Region rc = (Region)obj;
		return new EqualsBuilder().
				append(x, rc.x).
				append(z, rc.z).
				isEquals();
	}
	
	/**
	 * HashCode override
	 * @return The int hashcode generated from this Region object's X & Z coords
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,31).
				append(x).
				append(z).
				toHashCode();
	}
	
	public static String getNewName() {
		String newRegionName; 
		int newRegionNumber = regionCount++ - 1;
		do {
			newRegionNumber++;
			int randIndex = newRegionNumber % NameData.regionNames.size();
			int nameSuffix = newRegionNumber / NameData.regionNames.size();
			newRegionName = NameData.regionNames.get(NameData.randNumList.get(randIndex));
			if(nameSuffix > 0)
				newRegionName += nameSuffix;
		} while (Region.getRegion(newRegionName) != null);
		return newRegionName;
	}
	
	/**
	 * Goes to disk to get the Region that go with a region name
	 * @param regionName The name of the region to search for
	 * @return The RegionCoordinates object associated with the found region, null if not found
	 * @see RegionCoordinates
	 */
	public static Region getRegion(String regionName) {
		File file = new File(ConfigData.regionDataFolderPath + File.separator + regionName.toLowerCase());
		BufferedReader inStream = null;
		Region coordinates = null;
		try {
			inStream = new BufferedReader(new FileReader(file));
			String coordinatesString = inStream.readLine();
			inStream.close();			
			coordinates = new Region(coordinatesString);
		} catch(FileNotFoundException e) {
		} catch(Exception e) {
			Log.warn("Unable to read region data at " + file.getAbsolutePath() + ": " + e.getMessage());			
		}
		try {
			if(inStream != null) inStream.close();
		} catch(IOException exception) {}
		return coordinates;
	}
	
	/**
	 * Goes to disk to get the name of a region, given its coordinates
	 * @param coordinates Coordinates of the region to search for
	 * @return The name of the region which exists at the given coordinates
	 */
	public static String getRegionName(Region coordinates) {
		File regionCoordinatesFile;
		
		BufferedReader inStream = null;
		String regionName = null;
		try {
			regionCoordinatesFile = new File(ConfigData.regionDataFolderPath + File.separator + coordinates.toString());			
			inStream = new BufferedReader(new FileReader(regionCoordinatesFile));
			
			//only one line in the file, the region name
			regionName = inStream.readLine();
		} catch(FileNotFoundException e) {
			//if the file doesn't exist, the region hasn't been named yet, so return null
			return null;
		} catch(Exception e) {
			//if any other problems, log the details
			Log.warn("Unable to read region data: " + e.getMessage());
			return null;
		}
		
		try {
			if(inStream != null) inStream.close();
		} catch(IOException exception) {}
		
		return regionName;
	}
	
	/**
	 * Scans the open region for resources and may close the region (and open a new one) if accessible resources are low, may repeat
	 * @param region The Region to scan
	 * @param openNewRegions Whether to close the current region and open the next suitable one
	 */
	@SuppressWarnings("deprecation")
	private static void scanRegion(Region region, boolean openNewRegions) {						
		Log.log("Examining available resources in region \"" + region.toString() + "\"...");						
		
		Location regionCenter = region.getCenter();
		int min_x = regionCenter.getBlockX() - PopulationDensity.REGION_SIZE / 2;
		int max_x = regionCenter.getBlockX() + PopulationDensity.REGION_SIZE / 2;			
		int min_z = regionCenter.getBlockZ() - PopulationDensity.REGION_SIZE / 2;
		int max_z = regionCenter.getBlockZ() + PopulationDensity.REGION_SIZE / 2;
		
		Chunk lesserBoundaryChunk = ConfigData.managedWorld.getChunkAt(new Location(ConfigData.managedWorld, min_x, 1, min_z));
		Chunk greaterBoundaryChunk = ConfigData.managedWorld.getChunkAt(new Location(ConfigData.managedWorld, max_x, 1, max_z));
				
		ChunkSnapshot [][] snapshots = new ChunkSnapshot[greaterBoundaryChunk.getX() - lesserBoundaryChunk.getX() + 1][greaterBoundaryChunk.getZ() - lesserBoundaryChunk.getZ() + 1];
		boolean snapshotIncomplete;
		do {
			snapshotIncomplete = false;
			for(int x = 0; x < snapshots.length; x++) {
				for(int z = 0; z < snapshots[0].length; z++) {
					Chunk chunk = ConfigData.managedWorld.getChunkAt(x + lesserBoundaryChunk.getX(), z + lesserBoundaryChunk.getZ());
					while(!chunk.load(true));
					ChunkSnapshot snapshot = chunk.getChunkSnapshot();
					
					//verify the snapshot by finding something that's not air
					boolean foundNonAir = false;
					for(int y = 0; y < ConfigData.managedWorld.getMaxHeight(); y++) {
						//if we find something, save the snapshot to the snapshot array
						if(snapshot.getBlockTypeId(0, y, 0) != Material.AIR.getId()) {
							foundNonAir = true;
							snapshots[x][z] = snapshot;
							break;
						}
					}
					//otherwise, plan to repeat this process again after sleeping a bit
					if(!foundNonAir)
						snapshotIncomplete = true;
				}
			}
			
			//if at least one snapshot was all air, sleep a second to let the chunk loader/generator
			//catch up, and then try again
			if(snapshotIncomplete) {
				try  {
					Thread.sleep(1000);
				} catch (InterruptedException e) { } 				
			}
			
		} while(snapshotIncomplete);
		
		//try to unload any chunks which don't have players nearby
		Chunk [] loadedChunks = ConfigData.managedWorld.getLoadedChunks();
		for(int i = 0; i < loadedChunks.length; i++)
			loadedChunks[i].unload(true, true);  //save = true, safe = true
		
		//collect garbage
		System.gc();
		
		//create a new task with this information, which will more completely scan the content of all the snapshots
		ScanRegionTask task = new ScanRegionTask(snapshots, openNewRegions);
		
		//run it in a separate thread		
		PopulationDensity.inst.getServer().getScheduler().runTaskLaterAsynchronously(PopulationDensity.inst, task, 5L);		
	}
	
	/**
	 * Adds a new region, assigning it a name and updating local variables accordingly
	 * @return RegionCoordinates object associated with the new region
	 * @see RegionCoordinates
	 */
	public static Region addRegion() {
		nextRegion.nameRegion(getNewName());
		findNextRegion();
		return openRegion;
	}
	
	/**
	 * Starting at region 0,0, spirals outward until an uninitialized region is found
	 * @return The number of regions iterated through, including the new region (starts from 0)
	 */
	public static int findNextRegion() {
		int x = 0; int z = 0;
		int regionCount = 0;
		Direction direction = Direction.DOWN;
		int sideLength = 1;
		int side = 0;
		Region.openRegion = null;
		Region.nextRegion = new Region(0, 0);
		while ((Region.getRegionName(nextRegion)) != null) {
			for (int i = 0; i < sideLength && Region.getRegionName(nextRegion) != null; i++) {
				regionCount++;
				if (direction == Direction.DOWN) z++;
				else if (direction == Direction.LEFT) x--;
				else if (direction == Direction.UP) z--;
				else x++;
				Region.openRegion = Region.nextRegion;
				Log.warn("Region selected");
				Region.nextRegion = new Region(x, z);
			}
			direction = direction.getNext();
			side++;
			if (side % 2 == 0) sideLength++;
		}
		return regionCount;
	}
	
	/**
	 * Picks a region at random (sort of)
	 * @param regionToAvoid Regions to exlude from potential region returns
	 * @return A random region
	 * @see RegionCoordinates
	 */
	public static Region getRandomRegion(Region regionToAvoid) {
		if(Region.regionCount < 2)
			return null;
		
		//initialize random number generator with a seed based the current time
		Random randomGenerator = new Random();
		
		//get a list of all the files in the region data folder
		//some of them are named after region names, others region coordinates
		File regionDataFolder = new File(ConfigData.regionDataFolderPath);
		File [] files = regionDataFolder.listFiles();			
		ArrayList<Region> regions = new ArrayList<Region>();
		
		for(int i = 0; i < files.length; i++) {				
			if(files[i].isFile()) { //avoid any folders
				try {
					//if the filename converts to region coordinates, add that region to the list of defined regions
					//(this constructor throws an exception if it can't do the conversion)
					Region regionCoordinates = new Region(files[i].getName());
					if(!regionCoordinates.equals(regionToAvoid))
						regions.add(regionCoordinates);
				} catch(Exception e) { /*catch for files named after region names*/ }					
			}
		}
		
		//pick one of those regions at random
		int randomRegion = randomGenerator.nextInt(regions.size());			
		return regions.get(randomRegion);			
	}

	/**
	 * Retrieves the open region's coordinates
	 * @return RegionCoordinates object associated with the current region
	 * @see RegionCoordinates
	 */
	public static Region getOpenRegion() {
		return openRegion;
	}
	
	private static boolean canFall(Material mat) {
		return (
			!mat.isSolid() ||
			mat.equals(Material.SIGN) ||
			mat.equals(Material.SIGN_POST) ||
			mat.equals(Material.WALL_SIGN) ||
			mat.equals(Material.LEAVES) || 
			mat.equals(Material.LOG) ||
			mat.equals(Material.GLOWSTONE)
		);
	}
}
