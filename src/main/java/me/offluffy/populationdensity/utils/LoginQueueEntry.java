package me.offluffy.populationdensity.utils;

public class LoginQueueEntry {
	private String playerName;
	private int priority;
	private long lastRefreshed;
	
	public LoginQueueEntry(String playerName, int priority, long lastRefreshed) {
		this.priority = priority;
		this.playerName = playerName;
		this.lastRefreshed = lastRefreshed;
	}
	
	public String getPlayerName() { return playerName; }
	public int getPriority() { return priority; }
	public long getLastRefreshed() { return lastRefreshed; }
	public void setLastRefreshed(long time) {lastRefreshed = time; }
	public void setPriority(int priority) { this.priority = priority; }
}