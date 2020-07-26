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
import eu.beezig.core.Constants;
import eu.beezig.core.command.Command;
import eu.beezig.core.util.SystemInfo;
import eu.beezig.core.util.text.Message;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class BeezigCommand implements Command {
    @Override
    public String getName() {
        return "beezig";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/beezig"};
    }

    @Override
    public boolean execute(String[] args) {
        if(args.length == 0) {
            Message.bar();
            Message.info(String.format("Running Beezig %s (%s)", Constants.VERSION, Beezig.getVersion ()));
            Message.bar();
        }
        else {
            String mode = args[0];
            if("info".equalsIgnoreCase(mode)) {
                String sysInfo = SystemInfo.getSystemInfo();
                StringSelection sel = new StringSelection(sysInfo);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                Message.info("Debug info has been copied to your clipboard.");
            }
        }
        return true;
    }
}
