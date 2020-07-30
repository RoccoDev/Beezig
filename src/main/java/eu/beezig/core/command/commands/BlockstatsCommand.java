/*
 * Copyright (C) 2017-2020 Beezig Team
 *
 * This file is part of Beezig.
 *
 * Beezig is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beezig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beezig.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.the5zig.util.minecraft.ChatColor;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Locale;
import java.util.Map;

public class BlockstatsCommand implements Command {

    @Override
    public String getName() {
        return "blockstats";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/hidebs"};
    }

    @Override
    public boolean execute(String[] args) {
        String display = args.length == 0 ? Beezig.user().getName() : args[0];
        String player = args.length == 0 ? UUIDUtils.strip(Beezig.user().getId()) : args[0];
        Profiles.hide(player).thenAcceptAsync(api -> {
            Map<String, Long> rawExp = api.getBlockExperience();
            Map<String, Long> blockExp = api.getBlockLevels();
            Beezig.api().messagePlayer(StringUtils.linedCenterText("§7", Color.primary()
                    + Beezig.api().translate("msg.blockstats", Color.accent() + display + Color.primary())));
            rawExp.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
                int level = Math.toIntExact(blockExp.get(entry.getKey()));
                ChatColor color = getLevelColor(level);
                int percent = getNextPercent(Math.toIntExact(entry.getValue()), level);
                Message.info(String.format("%s: %s%d +%d%%", transformBlockName(entry.getKey()),
                        color, level, percent));
            });
            Beezig.api().messagePlayer(StringUtils.linedCenterText("§7", Color.primary() + Message.translate("cmd.blockstats")));
        }).exceptionally(e -> {
            Beezig.logger.error("Error in blockstats", e);
            Message.error(Message.translate("error.blockstats"));
           return null;
        });
        return true;
    }

    private static String transformBlockName(String raw) {
        String parsed = WordUtils.capitalize(raw.toLowerCase(Locale.ROOT).replace("_", " "));
        switch (parsed) {
            case "Wood":
                parsed = "Oak Planks";
                break;
            case "Wood:1":
                parsed = "Wood";
                break;
            case "Wood:3":
                parsed = "Jungle Planks";
                break;
            case "Stone:6":
                parsed = "Polished Andesite";
                break;
            case "Prismarine:2":
                parsed = "Dark Prismarine";
                break;
        }
        return parsed;
    }

    private static ChatColor getLevelColor(int level) {
        if (level < 5) return ChatColor.GRAY;
        if (level < 10) return ChatColor.AQUA;
        if (level < 15) return ChatColor.GREEN;
        if (level < 20) return ChatColor.YELLOW;
        if (level < 25) return ChatColor.GOLD;
        if (level < 30) return ChatColor.RED;
        if (level < 35) return ChatColor.LIGHT_PURPLE;
        if (level < 40) return ChatColor.AQUA;
        if (level < 45) return ChatColor.DARK_GREEN;
        if (level < 50) return ChatColor.DARK_PURPLE;
        if (level == 50) return ChatColor.DARK_BLUE;
        return ChatColor.WHITE;
    }

    private static int getNextPercent(int experience, int level) {
        experience = experience - 50;
        int prevXP = 0;
        int i = 1;
        while (i < level) {
            prevXP = 50 * i + prevXP;
            i++;
        }
        int nextXP = 50 * i + prevXP;
        int diff = nextXP - prevXP;
        experience = experience - prevXP;
        double percent = Math.floor(((double) experience / (double) diff) * 100d);
        return (int) percent;
    }
}
