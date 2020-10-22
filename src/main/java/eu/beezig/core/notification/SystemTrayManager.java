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

package eu.beezig.core.notification;

import eu.beezig.core.Beezig;
import eu.beezig.core.util.ExceptionHandler;

import java.awt.*;

public class SystemTrayManager {
    private TrayIcon mainIcon;
    private boolean supported;

    public SystemTrayManager() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            mainIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(Beezig.class.getResource("/img/logo.png")), "Beezig");
            mainIcon.setImageAutoSize(true);
            mainIcon.setToolTip("Beezig");
            tray.add(mainIcon);
            supported = true;
        } catch (UnsupportedOperationException ignored) {
            Beezig.logger.warn("System tray is NOT supported.");
        } catch (AWTException e) {
            ExceptionHandler.catchException(e);
        }
    }

    public void sendNotification(IncomingMessage message) {
        Beezig.api().playSound("note.pling", 1f);
        if(!supported) return;
        mainIcon.displayMessage(Beezig.api().translate(message.getType() == NotificationManager.MessageType.BROADCAST
                        ? "msg.notify.incoming.broadcast" : "msg.notify.incoming", message.getSender()), message.getMessage(),
                TrayIcon.MessageType.INFO);
    }
}
