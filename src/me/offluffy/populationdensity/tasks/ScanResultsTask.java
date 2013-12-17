package me.offluffy.populationdensity.tasks;

import java.util.List;

import me.offluffy.populationdensity.utils.Log;
import me.offluffy.populationdensity.utils.Region;

public class ScanResultsTask implements Runnable {
	private List<String> logEntries;
	private boolean openNewRegion;
	
	public ScanResultsTask(List<String> logEntries, boolean openNewRegion) {
		this.logEntries = logEntries;
		this.openNewRegion = openNewRegion;
	}
	
	@Override
	public void run() {
		//collect garbage
		System.gc();
		
		for (String s : logEntries)
			Log.log(s);
		
		if(this.openNewRegion) {
			Region newRegion = Region.addRegion();
			newRegion.scanRegion(true);
		}		
	}
}
