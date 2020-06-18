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

package eu.beezig.core.autogg;

import org.json.simple.JSONObject;

import java.io.InvalidObjectException;
import java.util.regex.Pattern;

public class Trigger {

    public enum Type {
        CHAT,
        TITLE,
        SUBTITLE;

        public static Type getFromID(int id) {
            switch (id) {
                case 1:
                    return TITLE;
                case 2:
                    return SUBTITLE;
                default:
                    return CHAT;
            }
        }
    }

    private Type type;
    private String trigger;

    public Trigger(Type type, String trigger) {
        this.type = type;
        this.trigger = trigger;
    }

    public static Trigger fromJsonObject(JSONObject object) throws InvalidObjectException {
        // Retrieve "trigger" from JSONObject
        Object trigger = null;
        if (object.containsKey("trigger"))
            trigger = object.get("trigger");
        if (!(trigger instanceof String))
            throw new InvalidObjectException("Missing or malformed \"trigger\" in Trigger JSON object");
        String pattern = (String) trigger;
        // Retrieve "type" from JSONObject
        Object type = null;
        if (object.containsKey("type"))
            type = object.get("type");
        if (!(type instanceof Integer))
            throw new InvalidObjectException("Missing or malformed \"type\" in Trigger JSON object");
        return new Trigger(Type.getFromID((Integer) type), pattern);
    }

    public boolean doesTrigger(String message, Type type){
        return Pattern.matches(trigger, message) && this.type == type;
    }

}
