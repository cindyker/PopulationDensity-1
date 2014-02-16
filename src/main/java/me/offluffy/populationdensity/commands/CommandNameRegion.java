package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.Region;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ReflectCommand
public class CommandNameRegion extends PDCmd {
    public CommandNameRegion() {
        super("nameregion", "Name or rename the current region", "nameregion");
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

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (ConfigData.managedWorld == null) {
                Messages.send(player, Message.NO_WORLD);
                return true;
            }
        }

        // silently fail if player is null (not online player)
        if (player == null) {
            Messages.send(player, Message.NOT_ONLINE);
            return true;
        }

        Region currentRegion = Region.fromLocation(player.getLocation());
        if (currentRegion == null) {
            Messages.send(player, Message.NO_REGION);
            return true;
        }

        //validate argument
        if (args.length < 1)
            return false;
        if (args.length > 1) {
            player.sendMessage(Clr.ERR + "Region names may not include spaces.");
            return true;
        }

        String name = args[0];

        if (name.length() > 10) {
            player.sendMessage(Clr.ERR + "Region names can only be up to 10 letters long.");
            return true;
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c)) {
                player.sendMessage(Clr.ERR + "Region names may only include letters.");
                return true;
            }
        }

        if (Region.getRegion(name) != null) {
            player.sendMessage(Clr.ERR + "There's already a region by that name.");
            return true;
        }

        //name region
        currentRegion.nameRegion(name);

        //update post
        currentRegion.addPost(true);

        return true;
    }
}
