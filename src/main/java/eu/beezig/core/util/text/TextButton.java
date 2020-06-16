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

package eu.beezig.core.util.text;

import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;

public class TextButton extends MessageComponent {
    public TextButton(String nameKey, String descKey, String color) {
        super(String.format("%s[%s]", color, Message.translate(nameKey)));
        MessageComponent desc = new MessageComponent(color + Message.translate(descKey));
        getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, desc));
    }

    public void doSuggestCommand(String command) {
        getStyle().setOnClick(new MessageAction(MessageAction.Action.SUGGEST_COMMAND, command));
    }

    public void doRunCommand(String command) {
        getStyle().setOnClick(new MessageAction(MessageAction.Action.RUN_COMMAND, command));
    }

    public void doOpenFile(String file) {
        getStyle().setOnClick(new MessageAction(MessageAction.Action.OPEN_FILE, file));
    }
}
