package me.offluffy.populationdensity.utils;

import org.bukkit.command.CommandExecutor;

import java.util.HashMap;

// A few methods and what-not to make processing commands and arguments a bit easier
public abstract class PDCmd implements CommandExecutor {
    public static HashMap<String, PDCmd> commands = new HashMap<String, PDCmd>();
    private String label, desc, perm;

    public PDCmd(String label, String desc, String perm) {
        this.desc = desc;
        this.perm = perm;
        this.label = label;
        commands.put(label, this);
    }

    public static HashMap<String, PDCmd> getCommands() { return commands; }

    public String getDesc() { return desc; }

    public String getPerm() { return perm; }

    public String getLabel() { return label; }
}
