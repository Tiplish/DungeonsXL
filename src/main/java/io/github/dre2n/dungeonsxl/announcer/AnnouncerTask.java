/*
 * Copyright (C) 2012-2018 Frank Baumann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.dre2n.dungeonsxl.announcer;

import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.player.DGlobalPlayer;
import io.github.dre2n.dungeonsxl.player.DInstancePlayer;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Daniel Saukel
 */
public class AnnouncerTask extends BukkitRunnable {

    private List<Announcer> announcers;
    private int index;

    public AnnouncerTask(AnnouncerCache announcers) {
        this.announcers = announcers.getAnnouncers();
        index = 0;
    }

    @Override
    public void run() {
        Announcer announcer = announcers.get(index);
        List<String> worlds = announcer.getWorlds();
        for (Player player : Bukkit.getOnlinePlayers()) {
            DGlobalPlayer dPlayer = DungeonsXL.getInstance().getDPlayers().getByPlayer(player);
            if (!(dPlayer instanceof DInstancePlayer) && dPlayer.isAnnouncerEnabled()) {
                if (worlds.isEmpty() || worlds.contains(player.getWorld().getName())) {
                    announcer.send(player);
                }
            }
        }

        index++;
        if (index == announcers.size()) {
            index = 0;
        }
    }

}
