/*
 * Copyright (C) 2017-2021 Beezig Team
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

package eu.beezig.core.util.process;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.notification.NotificationManager;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.process.processes.ScreenRecorders;
import eu.beezig.core.util.process.providers.UnixProcessProvider;
import eu.beezig.core.util.process.providers.WindowsProcessProvider;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.TextButton;
import eu.the5zig.mod.util.component.MessageComponent;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProcessManager {
    private IProcessProvider provider;
    private Set<IProcess> processes = new HashSet<>();

    @SuppressWarnings("FutureReturnValueIgnored")
    public ProcessManager() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if(os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix"))
            this.provider = new UnixProcessProvider();
        else if(os.contains("win")) this.provider = new WindowsProcessProvider();
        Beezig.get().getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                updateProcesses();
            } catch (IOException e) {
                ExceptionHandler.catchException(e);
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    private void updateProcesses() throws IOException {
        if(provider == null) return;
        List<String> current = provider.getRunningProcesses();
        Set<IProcess> currentProcesses = new HashSet<>();
        for(ScreenRecorders recorder : ScreenRecorders.values()) {
            for(String alias : recorder.getAliases()) {
                if(current.contains(alias)) {
                    currentProcesses.add(recorder);
                    break;
                }
            }
            // If no match is found in the current list, check if it was in the previous list. If so, that means the process was closed.
            if(processes.contains(recorder)) onProcessStop(recorder);
        }
        Set<IProcess> added = new HashSet<>(currentProcesses);
        added.removeAll(processes);
        for(IProcess newProc : added) {
            onProcessStart(newProc);
        }
        processes = currentProcesses;
    }

    private void onProcessStart(IProcess process) {
        if(process instanceof ScreenRecorders) {
            if(Settings.RECORD_DND.get().getBoolean()) {
                WorldTask.submit(() -> {
                    Message.info(Beezig.api().translate("msg.record.dnd", Color.accent() + ((ScreenRecorders) process).name() + Color.primary()));
                    Beezig.get().getNotificationManager().setDoNotDisturb(true, NotificationManager.ActivationCause.PROCESS);
                });
            }
            else {
                MessageComponent main = new MessageComponent(Message.infoPrefix() + Beezig.api().translate("msg.record.found",
                        Color.accent() + ((ScreenRecorders) process).name() + Color.primary()) + "\n");
                TextButton enableNow = new TextButton("btn.record_dnd.name", "btn.record_dnd.desc", "§a");
                enableNow.doRunCommand("/dnd on process");
                TextButton enableAlways = new TextButton("btn.record_setting.name", "btn.record_setting.desc", "§e");
                enableAlways.doRunCommand("/bsettings record.dnd true");
                main.getSiblings().add(enableNow);
                main.getSiblings().add(new MessageComponent(" "));
                main.getSiblings().add(enableAlways);
                WorldTask.submit(() -> Beezig.api().messagePlayerComponent(main, false));
            }
        }
    }

    private void onProcessStop(IProcess process) {
        if(process instanceof ScreenRecorders) {
            NotificationManager notificationManager = Beezig.get().getNotificationManager();
            if(notificationManager.isDoNotDisturb() && notificationManager.getDoNotDisturbCause() == NotificationManager.ActivationCause.PROCESS)
                notificationManager.setDoNotDisturb(false, NotificationManager.ActivationCause.PROCESS);
        }
    }
}
