package me.offluffy.populationdensity.tasks;

import me.offluffy.populationdensity.utils.Region;

public class ScanOpenRegionTask implements Runnable {
	@Override
	public void run() {
		//start a scan on the currently open region
		Region.getOpenRegion().scanRegion(true);		
	}	
}
