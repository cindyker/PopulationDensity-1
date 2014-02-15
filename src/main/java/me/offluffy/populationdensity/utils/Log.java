package me.offluffy.populationdensity.utils;

import me.offluffy.populationdensity.PopulationDensity;

import org.bukkit.Bukkit;


public class Log {
	
	private static PopulationDensity inst = PopulationDensity.inst;
	
	public enum Level {
		INFO('a'), WARN('6'), SEVERE('4'), DEBUG('d');
		private char c;
		Level(char color) {
			c = color;
		}
		public String getColor() {
			return "\u00A7" + c;
		}
	}
	
	/**
	 * Log a message with INFO level (Green text)
	 * @param msg The message to log
	 * @see Level
	 */
	public static void info(String msg) {
		log(msg, Level.INFO);
	}

	/**
	 * Log a message with WARN level (Orange text)
	 * @param msg The message to log
	 * @see Level
	 */
	public static void warn(String msg) {
		log(msg, Level.WARN);
	}
	
	/**
	 * Log a message with SEVERE level (Red text)
	 * @param msg The message to log
	 * @see Level
	 */
	public static void severe(String msg) {
		log(msg, Level.SEVERE);
	}
	
	/**
	 * Log a message with DEBUG level (Magenta text), only prints if dubug is enabled in config
	 * @param msg The message to log
	 * @see Level
	 */
	public static void debug(String msg) {
		if (PopulationDensity.config.getBoolean("Misc.EnableDebugLogging", false))
			log(msg, Level.DEBUG);
	}
	
	/**
	 * Log a message with no log level or color
	 * @param msg The message to log
	 */
	public static void log(String msg) {
		Bukkit.getConsoleSender().sendMessage("[" + inst.getDescription().getName() + "] " + msg);
	}
	
	/**
	 * Log a message with the specified log level
	 * @param msg The Message to log
	 * @param level The Level which the message will be logged
	 * @see Level
	 */
	private static void log(String msg, Level level) {
		Bukkit.getConsoleSender().sendMessage(level.getColor() + "[" + inst.getDescription().getName() + "] " + msg);
	}
}
