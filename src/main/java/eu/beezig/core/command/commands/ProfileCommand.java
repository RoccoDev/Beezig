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
import eu.beezig.core.net.profile.OwnProfile;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;

public class ProfileCommand implements Command {
    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bprofile", "/mybeezig"};
    }

    @Override
    public boolean execute(String[] args) {
        OwnProfile profile = Beezig.net().getProfile();
        if(profile == null) {
            Message.error(Message.translate("error.profile"));
            return true;
        }
        Message.bar();
        Beezig.api().messagePlayer(" " + Color.primary() + Beezig.api().translate("msg.profile.id", Color.accent() + profile.getId() + Color.primary(),
                Color.accent() + Message.date(profile.getFirstLogin()) + Color.primary()));
        Beezig.api().messagePlayer(" " + Color.primary() + Beezig.api().translate("msg.profile.rank", profile.getRole().getDisplayName()));
        if(profile.getRegion() != null) {
            Beezig.api().messagePlayer(" " + Color.primary() + Beezig.api().translate("msg.profile.region", Color.accent() + profile.getRegion().getName()));
        }
        Message.bar();
        return true;
    }
}
