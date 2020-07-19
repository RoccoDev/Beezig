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

package eu.beezig.core.api;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public interface IBeezigService {
    // Callbacks (BeezigForge -> Beezig)
    void registerUserIndicator(Function<UUID, Integer> callback);
    void registerTitle(Function<String, String> callback);
    void registerFormatNumber(Function<Long, String> callback);
    void registerTranslate(Function<String, String> callback);

    // Functions (Beezig -> BeezigForge)
    void setOnHive(boolean update);
    void setCurrentGame(String game);
    void addCommands(List<Object> commands);
    void loadConfig(File beezigDir);
}
