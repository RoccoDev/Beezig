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

package eu.beezig.core.net.profile.role;

import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;

import java.util.Locale;

public enum DefaultUserRoles implements UserRole {
    NONE(-1, "§r"),
    USER(0, "§7"),
    TRANSLATOR(50, "§6"),
    CONTRIBUTOR(51, "§5"),
    DAILY_WINNER(52, "§a"),
    DEVELOPER(100, "§b");

    private int index;
    private String prefix;

    DefaultUserRoles(int index, String prefix) {
        this.index = index;
        this.prefix = prefix;
    }

    static DefaultUserRoles fromIndex(int index) {
        for(DefaultUserRoles role : DefaultUserRoles.values()) {
            if(role.index == index) return role;
        }
        return null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getDisplayName() {
        return prefix + Message.translate("role." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public MessageComponent getDisplayComponent() {
        MessageComponent dot = new MessageComponent(prefix + " Ⓑ");
        dot.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(getDisplayName())));
        return dot;
    }

    @Override
    public String getShortName() {
        if(this == USER) return prefix + "§lⒷ";
        return prefix + "(§lⒷ " + prefix + Message.translate("role." + name().toLowerCase(Locale.ROOT) + ".short") + ")";
    }
}
