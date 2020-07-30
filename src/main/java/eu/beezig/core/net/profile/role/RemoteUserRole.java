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

public class RemoteUserRole implements UserRole {
    private int id;
    private String short_name, display_key, mc_prefix, display_name;

    private transient String cachedShortI18n, cachedI18n;

    @Override
    public int getIndex() {
        return id;
    }

    @Override
    public MessageComponent getDisplayComponent() {
        MessageComponent dot = new MessageComponent(mc_prefix + " Ⓑ");
        dot.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(getDisplayName())));
        return dot;
    }

    @Override
    public String getShortName() {
        return mc_prefix + "(§lⒷ " + mc_prefix + parseShortName() + ")";
    }

    private String parseShortName() {
        if(cachedShortI18n != null) return cachedShortI18n;
        if(display_key == null) return short_name;
        String key = display_key + ".short";
        String translated = Message.translate(key);
        if(!translated.equals(key)) cachedShortI18n = translated;
        else cachedShortI18n = short_name;
        return cachedShortI18n;
    }

    @Override
    public String getDisplayName() {
        if(cachedI18n != null) return cachedI18n;
        if(display_key == null) return display_name;
        String translated = Message.translate(display_key);
        if(!translated.equals(display_key)) cachedI18n = translated;
        else cachedI18n = display_name;
        return mc_prefix + cachedI18n;
    }
}
