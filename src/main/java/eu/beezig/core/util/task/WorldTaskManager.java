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

package eu.beezig.core.util.task;

import eu.beezig.core.Beezig;
import eu.beezig.core.util.ExceptionHandler;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.TickEvent;

import java.util.ArrayDeque;

public class WorldTaskManager {
    private ArrayDeque<WorldTask> tasks = new ArrayDeque<>();

    @EventHandler
    public void onTick(TickEvent evt) {
        if(Beezig.api().isInWorld()) {
            while(!tasks.isEmpty()) {
                WorldTask task = tasks.poll();
                try {
                    task.run();
                } catch (Exception e) {
                    ExceptionHandler.catchException(e, "WorldTask");
                }
            }
        }
    }

    void submit(WorldTask task) {
        tasks.add(task);
    }
}
