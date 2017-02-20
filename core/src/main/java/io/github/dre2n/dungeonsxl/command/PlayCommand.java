/*
 * Copyright (C) 2012-2017 Frank Baumann
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
package io.github.dre2n.dungeonsxl.command;

import io.github.dre2n.commons.command.BRCommand;
import io.github.dre2n.commons.util.messageutil.MessageUtil;
import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.config.DMessages;
import io.github.dre2n.dungeonsxl.dungeon.Dungeon;
import io.github.dre2n.dungeonsxl.event.dgroup.DGroupCreateEvent;
import io.github.dre2n.dungeonsxl.game.Game;
import io.github.dre2n.dungeonsxl.player.DGamePlayer;
import io.github.dre2n.dungeonsxl.player.DGlobalPlayer;
import io.github.dre2n.dungeonsxl.player.DGroup;
import io.github.dre2n.dungeonsxl.player.DInstancePlayer;
import io.github.dre2n.dungeonsxl.player.DPermissions;
import io.github.dre2n.dungeonsxl.world.DResourceWorld;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class PlayCommand extends BRCommand {

    public PlayCommand() {
        setCommand("play");
        setMinArgs(1);
        setMaxArgs(1);
        setHelp(DMessages.HELP_CMD_PLAY.getMessage());
        setPermission(DPermissions.PLAY.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        DGlobalPlayer dPlayer = DungeonsXL.getDPlayers().getByPlayer(player);
        if (dPlayer instanceof DInstancePlayer) {
            MessageUtil.sendMessage(player, DMessages.ERROR_LEAVE_DUNGEON.getMessage());
            return;
        }

        Dungeon dungeon = DungeonsXL.getDungeons().getByName(args[1]);
        if (dungeon == null) {
            DResourceWorld resource = DungeonsXL.getDWorlds().getResourceByName(args[1]);
            if (resource != null) {
                dungeon = new Dungeon(resource);
            } else {
                MessageUtil.sendMessage(player, DMessages.ERROR_DUNGEON_NOT_EXIST.getMessage(args[1]));
                return;
            }
        }

        DGroup dGroup = DGroup.getByPlayer(player);
        if (dGroup == null || dGroup.isPlaying()) {
            dGroup = new DGroup(player, dungeon);
            DGroupCreateEvent event = new DGroupCreateEvent(dGroup, player, DGroupCreateEvent.Cause.COMMAND);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                DungeonsXL.getDGroups().remove(dGroup);
                dGroup = null;
            }
        }
        if (!dGroup.getCaptain().equals(player) && !DPermissions.hasPermission(player, DPermissions.BYPASS)) {
            MessageUtil.sendMessage(player, DMessages.ERROR_NOT_CAPTAIN.getMessage());
            return;
        }
        dGroup.setDungeon(dungeon);

        new Game(dGroup, dungeon.getMap());
        for (Player groupPlayer : dGroup.getPlayers()) {
            DGamePlayer.create(groupPlayer, dGroup.getGameWorld());
        }
    }

}
