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
package io.github.dre2n.dungeonsxl.command;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.command.DRECommand;
import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.config.DMessage;
import io.github.dre2n.dungeonsxl.dungeon.Dungeon;
import io.github.dre2n.dungeonsxl.dungeon.DungeonConfig;
import io.github.dre2n.dungeonsxl.global.GameSign;
import io.github.dre2n.dungeonsxl.global.GlobalProtection;
import io.github.dre2n.dungeonsxl.global.GroupSign;
import io.github.dre2n.dungeonsxl.player.DPermission;
import io.github.dre2n.dungeonsxl.world.DResourceWorld;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Daniel Saukel
 */
public class RenameCommand extends DRECommand {

    DungeonsXL plugin = DungeonsXL.getInstance();

    public RenameCommand() {
        setCommand("rename");
        setMinArgs(2);
        setMaxArgs(2);
        setHelp(DMessage.HELP_CMD_RENAME.getMessage());
        setPermission(DPermission.RENAME.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        DResourceWorld resource = plugin.getDWorlds().getResourceByName(args[1]);
        if (resource == null) {
            MessageUtil.sendMessage(sender, DMessage.ERROR_NO_SUCH_MAP.getMessage(args[1]));
            return;
        }

        resource.setName(args[2]);
        resource.getFolder().renameTo(new File(DungeonsXL.MAPS, args[2]));
        resource.getSignData().updateFile(resource);

        for (Dungeon dungeon : plugin.getDungeons().getDungeons()) {
            DungeonConfig dConfig = dungeon.getConfig();
            FileConfiguration config = dConfig.getConfig();
            File file = dConfig.getFile();

            if (dConfig.getStartFloor() == resource) {
                config.set("startFloor", args[2]);
            }

            if (dConfig.getEndFloor() == resource) {
                config.set("endFloor", args[2]);
            }

            List<String> list = config.getStringList("floors");
            int i = 0;
            for (DResourceWorld floor : dConfig.getFloors()) {
                if (floor == resource) {
                    list.set(i, args[2]);
                }
                i++;
            }
            config.set("floors", list);

            try {
                config.save(file);
            } catch (IOException ex) {
            }
        }

        boolean changed = false;
        for (GlobalProtection protection : plugin.getGlobalProtections().getProtections()) {
            if (protection instanceof GroupSign) {
                Dungeon dungeon = ((GroupSign) protection).getDungeon();
                if (dungeon.getName().equals(args[1])) {
                    dungeon.setName(args[2]);
                    changed = true;
                }

            } else if (protection instanceof GameSign) {
                Dungeon dungeon = ((GameSign) protection).getDungeon();
                if (dungeon.getName().equals(args[1])) {
                    dungeon.setName(args[2]);
                    changed = true;
                }
            }
        }

        if (changed) {
            plugin.getGlobalProtections().saveAll();
        }

        MessageUtil.sendMessage(sender, DMessage.CMD_RENAME_SUCCESS.getMessage(args[1], args[2]));
    }

}
