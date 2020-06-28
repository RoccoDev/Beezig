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

package eu.beezig.core.automessage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.regex.Pattern;

public class Trigger {

    public enum Type {
        @SerializedName("CHAT")
        CHAT,
        @SerializedName("TITLE")
        TITLE,
        @SerializedName("SUBTITLE")
        SUBTITLE
    }

    @SerializedName("type")
    @Expose
    private Type type;
    @SerializedName("trigger")
    @Expose
    private String trigger;

    public boolean doesTrigger(String message, Type type){
        return Pattern.matches(trigger, message) && this.type == type;
    }

}
