/*
    PopulationDensity Server Plugin for Minecraft
    Copyright (C) 2011 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.offluffy.populationdensity.tasks;

import me.offluffy.populationdensity.utils.PlayerHelper;
import me.offluffy.populationdensity.utils.Region;
import org.bukkit.entity.Player;

//created during new player login to teleport that player to his home region after a short delay
public class PlaceNewPlayerTask implements Runnable {
    private Player player;
    private Region region;

    public PlaceNewPlayerTask(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    @Override
    public void run() {
        PlayerHelper.teleportPlayer(player, region, true);
    }
}
