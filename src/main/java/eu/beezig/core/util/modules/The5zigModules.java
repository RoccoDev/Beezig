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

package eu.beezig.core.util.modules;

import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.text.Message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class The5zigModules implements IModulesProvider {
    private Method openGui;
    private Object vars;
    private Constructor modulesGui;

    public The5zigModules() throws ReflectiveOperationException {
        Class mod = Class.forName("eu.the5zig.mod.The5zigMod");
        Class vars = Class.forName("eu.the5zig.mod.util.IVariables");
        Class gui = Class.forName("eu.the5zig.mod.gui.Gui");
        Class screen = Class.forName("eu.the5zig.mod.gui.GuiModules");
        openGui = vars.getMethod("displayScreen", gui);
        Method getVars = mod.getMethod("getVars");
        this.vars = getVars.invoke(null);
        this.modulesGui = screen.getConstructor(gui);
    }

    @Override
    public void openModulesGui() {
        try {
            openGui.invoke(vars, modulesGui.newInstance(new Object[] {null}));
        } catch (ReflectiveOperationException e) {
            ExceptionHandler.catchException(e);
            Message.error(Message.translate("error.hint.modules"));
        }
    }
}
