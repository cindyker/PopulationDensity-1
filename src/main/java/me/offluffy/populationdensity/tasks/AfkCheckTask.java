package me.offluffy.populationdensity.tasks;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Log;
import me.offluffy.populationdensity.utils.PlayerData;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class AfkCheckTask implements Runnable {
    private Player player;
    private PlayerData playerData;

    public AfkCheckTask(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    @Override
    public void run() {
        if (!player.isOnline()) return;
        boolean kick = false;
        if (player.getVehicle() instanceof Minecart) {
            if (playerData.wasInMinecartLastRound) kick = true;
            playerData.wasInMinecartLastRound = true;
        } else {
            playerData.wasInMinecartLastRound = false;
        }
        try {
            if (playerData.lastObservedLocation != null && (playerData.lastObservedLocation.distance(player.getLocation()) < 3))
                kick = true;
        } catch (IllegalArgumentException ignored) {}

        int playersOnline = PopulationDensity.inst.getServer().getOnlinePlayers().length;
        if (!Lib.perm(player, "populationdensity.idle") && kick && ConfigData.minimumPlayersOnlineForIdleBoot <= playersOnline) {
            Log.log("Kicked " + player.getName() + " for idling.");
            player.kickPlayer("Kicked for idling, to make room for active players.");
            return;
        }
        playerData.lastObservedLocation = player.getLocation();
        playerData.afkCheckTaskID = PopulationDensity.inst.getServer().getScheduler().scheduleSyncDelayedTask(PopulationDensity.inst, this, 20L * 60 * ConfigData.maxIdleMinutes);
    }
}
