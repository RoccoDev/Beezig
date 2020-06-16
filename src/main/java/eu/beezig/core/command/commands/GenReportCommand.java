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
import eu.beezig.core.logging.reports.DailyReport;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.TextButton;
import freemarker.template.TemplateException;

import java.io.IOException;

public class GenReportCommand implements Command {
    @Override
    public String getName() {
        return "genreport";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bgenreport"};
    }

    @Override
    public boolean execute(String[] args) {
        if(args.length != 1) {
            sendUsage("/bgenreport [date]");
            return true;
        }
        if(Beezig.get().getTemporaryPointsManager() != null) {
            try {
                DailyReport report = Beezig.get().getTemporaryPointsManager().getDailyReportManager().generateDailyReport(args[0]);
                TextButton btn = new TextButton("Open", "Open the file", Color.accent());
                btn.doOpenFile(report.getOpenURI());
                Beezig.api().messagePlayerComponent(btn, false);
            } catch (IOException | TemplateException e) {
                Beezig.logger.error("Couldn't generate report", e);
                Message.error(Message.translate("error.data_read"));
            }
        }
        return true;
    }
}
