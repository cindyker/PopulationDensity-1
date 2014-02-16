package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerData;
import me.offluffy.populationdensity.utils.PlayerHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ReflectCommand
public class CommandAcceptRegionInvite extends PDCmd {
    public CommandAcceptRegionInvite() {
        super("acceptregioninvite", "Accept a user's region invite", "acceptinvite");
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
        PlayerData playerData = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (ConfigData.managedWorld == null) {
                Messages.send(player, Message.NO_WORLD);
                return true;
            }
            playerData = PlayerHelper.getPlayerData(player.getName());
        }

        // fail if player is null (not online player)
        if (player == null) {
            Messages.send(player, Message.NOT_ONLINE); // TODO: Fix. Will produce NPE
            return true;
        }

        //if he doesn't have an invitation, tell him so
        if (playerData.regionInvitation == null) {
            player.sendMessage(Clr.ERR + "You haven't been invited to visit any regions. Someone must invite you with /InviteToRegion");
            return true;
        } else if (PlayerHelper.playerCanTeleport(player, false)) {
            PlayerHelper.teleportPlayer(player, playerData.regionInvitation, false);
        }

        return true;
    }
}
