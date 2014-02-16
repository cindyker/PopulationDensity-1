package me.offluffy.populationdensity.tasks;

import me.offluffy.populationdensity.utils.Region;

public class AddRegionPostTask implements Runnable {
    private Region region;
    private boolean updateNeighboringRegions;

    public AddRegionPostTask(Region region, boolean updateNeighboringRegions) {
        this.region = region;
        this.updateNeighboringRegions = updateNeighboringRegions;
    }

    @Override
    public void run() {
        region.addPost(updateNeighboringRegions);
    }
}
