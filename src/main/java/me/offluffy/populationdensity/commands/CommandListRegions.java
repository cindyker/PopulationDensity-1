package me.offluffy.populationdensity.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandListRegions extends PDCmd {
	public CommandListRegions() {
		super("listregions", "List all the current regions", "listregions");
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
		
		String regions = "plugins" + File.separator + "PopulationDensityData" + File.separator + "RegionData";
		File regFiles = new File(regions);
		
		List<String> regNames = new ArrayList<String>();
		
		for (File child : regFiles.listFiles())
			if (child.getName().matches("^[a-z]{1,15}$"))
				if (!regNames.contains(child.getName()))
					regNames.add(child.getName());
		
		if (!regNames.isEmpty()) {
			sender.sendMessage(Clr.TITLE + "PopuationDensity Regions:");
			for (String r : regNames) {
				Region n = Region.getRegion(r);
				int xPos = (400 * n.getX())+200;
				int yPos = (400 * n.getZ())+200;
				sender.sendMessage(Clr.HEAD + "> " + r + Clr.NOTE + " (" + xPos + ", " + yPos + ")");
			}
		}
		return true;
	}
}
