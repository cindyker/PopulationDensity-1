package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ReflectCommand
public class CommandCityRegion extends PDCmd {
    public CommandCityRegion() {
        super("cityregion", "Teleport to the City region", "cityteleport");
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

        // fail if player is null (not online player)
        if (player == null) {
            Messages.send(player, Message.NOT_ONLINE);
            return true;
        }

        //if city world isn't defined, this command isn't available
        if (ConfigData.cityWorld == null) {
            Messages.send(player, Message.NO_CITY);
            return true;
        }

        //otherwise teleportation is enabled, so consider config, player location, player permissions
        if (PlayerHelper.playerCanTeleport(player, true)) {
            Location spawn = ConfigData.cityWorld.getSpawnLocation();

            Block block = spawn.getBlock();
            while (block.getType() != Material.AIR)
                block = block.getRelative(BlockFace.UP);

            player.teleport(block.getLocation());
        }

        return true;
    }
}
