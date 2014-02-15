package me.offluffy.populationdensity.commands;

import java.util.HashMap;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandPopulationDensity extends PDCmd {
	public CommandPopulationDensity() {
		super("populationdensity", "Help command", "help");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!Lib.perm(sender, getPerm())) {
			if (sender.isOp())
				sender.sendMessage(Messages.noPerm(getPerm()));
			else
				Messages.send(sender, Message.NO_PERM);
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(Clr.TITLE + "Population Density" + Clr.NOTE + " (v" + PopulationDensity.inst.getDescription().getVersion() + ")");
			sender.sendMessage(Clr.NORM + "Original Author: " + Clr.NORM + "bigscary  " + Clr.HEAD + "Current: OffLuffy");
			sender.sendMessage(Clr.NORM + "For more help, type " + Clr.HEAD + "/pd help");
		} else {
			if (args[0].equalsIgnoreCase("help")) {
				HashMap<String, PDCmd> cmds = PDCmd.getCommands();
				for (PDCmd pc : cmds.values())
					if (Lib.perm(sender, pc.getPerm()))
						sender.sendMessage(Clr.HEAD + "/" + pc.getLabel() + ChatColor.RESET + ChatColor.BOLD + " > " + Clr.NORM + pc.getDesc());
			}
		}
		return true;
	}
}
