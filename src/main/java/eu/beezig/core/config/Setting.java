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

package eu.beezig.core.config;

public class Setting {
    private Object value;

    Setting(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public boolean getBoolean() {
        return (boolean) value;
    }

    public int getInt() {
        return (int) value;
    }

    public long getLong() {
        return (long) value;
    }

    public String getString() {
        return toString();
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if(value instanceof Enum) return ((Enum)value).name();
        return value.toString();
    }
}
