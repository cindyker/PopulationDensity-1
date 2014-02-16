package me.offluffy.populationdensity.tasks;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.EntityHelper;
import me.offluffy.populationdensity.utils.EntityHelper.EntityGroup;
import me.offluffy.populationdensity.utils.Log;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.List;

public class EntityScanTask implements Runnable {

    @Override
    public void run() {
        List<String> logEntries = new ArrayList<String>();
        int gac = 0, gmc = 0, gvc = 0, ggc = 0, gdc = 0; // Global entity counts
        int gar = 0, gmr = 0, gvr = 0, ggr = 0, gdr = 0; // Count how many have been removed

        //scan loaded chunks for chunks with too many monsters or items, and remove the superfluous
        if (ConfigData.limitEntities) {
            if (ConfigData.managedWorld == null) {
                Log.severe("Managed world is null");
                return;
            }
            Chunk[] chunks = ConfigData.managedWorld.getLoadedChunks();

            if (chunks.length > 0)
                Log.log("Scanning for excess entities...");

            for (Chunk chunk : chunks) {
                Entity[] entities = chunk.getEntities();
                // Per chunk entity counts
                int cac = 0, cmc = 0, cvc = 0, cgc = 0, cdc = 0;
                for (Entity entity : entities) {
                    if (EntityHelper.isAnimal(entity.getType())) {
                        cac++;
                        gac++;
                        if (cac > ConfigData.maxAnimals) { gar++; }
                    } else if (EntityHelper.isMonster(entity.getType())) {
                        cmc++;
                        gmc++;
                        if (cmc > ConfigData.maxMonsters) {
                            entity.remove();
                            gmr++;
                        }
                    } else if (EntityHelper.isVillager(entity.getType())) {
                        cvc++;
                        gvc++;
                        if (cvc > ConfigData.maxVillagers) {
                            entity.remove();
                            gvr++;
                        }
                    } else if (EntityHelper.isGolem(entity.getType())) {
                        cgc++;
                        ggc++;
                        if (cgc > ConfigData.maxGolems) {
                            entity.remove();
                            ggr++;
                        }
                    } else if (EntityHelper.isDrop(entity.getType())) {
                        cdc++;
                        gdc++;
                        if (cdc > ConfigData.maxDrops) {
                            entity.remove();
                            gdr++;
                        }
                    }
                }
                // Animals should be removed in a more player-friendly manner
                if (cac > ConfigData.maxAnimals) {
                    List<Entity> ents = EntityHelper.getTypeInChunk(chunk, EntityGroup.ANIMAL);
                    int diff = ents.size() - ConfigData.maxAnimals;
                    int remv = 0;
                    for (int e = 0; e < ents.size(); e++) {
                        if (remv >= diff) {
                            if (ents.get(e) instanceof Tameable) {
                                if (!((Tameable) ents.get(e)).isTamed()) { // Avoid killing tamed animals
                                    ents.get(e).remove();
                                    ents.remove(e);
                                    remv++;
                                }
                            }
                        } else { break; }
                    }
                    if (remv >= diff) { // If there are still too many
                        for (int e = 0; e < ents.size(); e++) {
                            if (remv >= diff) {
                                ents.get(e).remove(); // Just remove untamed ones too
                                ents.remove(e);
                                remv++;
                            } else { break; }
                        }
                    }
                }
            }

            //deliver report
            if (chunks.length > 0) {
                if (ConfigData.printEntityResults) {
                    logEntries.add("/+============[ Entity Scan Result ]============");
                    logEntries.add("|| Animals ..... " + gac + " / " + ConfigData.maxAnimals + " (Removed " + gar + ")");
                    logEntries.add("|| Monsters .... " + gmc + " / " + ConfigData.maxMonsters + " (Removed " + gmr + ")");
                    logEntries.add("|| Villagers ... " + gvc + " / " + ConfigData.maxVillagers + " (Removed " + gvr + ")");
                    logEntries.add("|| Golems ...... " + ggc + " / " + ConfigData.maxGolems + " (Removed " + ggr + ")");
                    logEntries.add("|| Drops ....... " + gdc + " / " + ConfigData.maxDrops + " (Removed " + gdr + ")");
                    logEntries.add("\\+==============================================");
                }
            }

            //now that we're done, notify the main thread
            ScanResultsTask resultsTask = new ScanResultsTask(logEntries, false);
            PopulationDensity.inst.getServer().getScheduler().scheduleSyncDelayedTask(PopulationDensity.inst, resultsTask, 5L);
        }
    }

}
