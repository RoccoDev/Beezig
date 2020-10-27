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

package eu.beezig.core;

import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version>{
    private final String version;
    private final String commit;
    private final int commits;
    private final String type;
    private final OffsetDateTime date;
    private final Pattern versionPattern = Pattern.compile("^(\\d+)\\.(\\d)\\.(\\d)$");

    public Version(JsonObject version) throws IllegalArgumentException {
        if (version == null || !version.has("version") || !version.has("commit") ||
            !version.has("commits") || !version.has("type") || !version.has("date"))
            throw new IllegalArgumentException("Version JSON Object is invalid");
        try {
            this.version = version.get("version").getAsString();
            commit = version.get("commit").getAsString();
            commits = version.get("commits").getAsInt();
            type = FilenameUtils.getName(version.get("type").getAsString()); // Escape url stuff
            date = OffsetDateTime.parse(version.get("date").getAsString());
        } catch(Exception e) {
            throw new IllegalArgumentException("Version JSON Object is invalid", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Version) {
            Version v = (Version) o;
            return v.version.equals(version) && v.commits == commits;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // Use the 17 and 31 hash from "Effective Java"
        int ret = 17;
        ret = 31 * ret + version.hashCode();
        ret = 31 * ret + commits;
        return ret;
    }

    @Override
    public int compareTo(Version version) {
        if (this.version.equals(version.version)) {
            // Only compare commits
            return Integer.compare(this.commits, version.commits);
        } else {
            // Only compare the version string
            Matcher thisMatcher = versionPattern.matcher(this.version), matcher = versionPattern.matcher(version.version);
            if (!thisMatcher.matches() || !matcher.matches()) {
                throw new IllegalArgumentException("Cannot compare versions as (at least) one of them has an invalid version string");
            }
            int thisMajor = Integer.parseInt(thisMatcher.group(1));
            int thisMinor = Integer.parseInt(thisMatcher.group(2));
            int thisPatch = Integer.parseInt(thisMatcher.group(3));
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));
            if (thisMajor != major)
                return Integer.compare(thisMajor, major);
            else if (thisMinor != minor)
                return Integer.compare(thisMinor, minor);
            else
                return Integer.compare(thisPatch, patch);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getCommit() {
        return commit;
    }

    public int getCommits() {
        return commits;
    }

    public String getType() {
        return type;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public int getVersionsBehind(Version version) {
        if (this.version.equals(version.version))
            return Math.abs(version.commits - commits);
        return -1;
    }

    public String getVersionDisplay() {
        return type.equals("release") ? "release" : commits + "-" + commit.substring(0, 8);
    }
}
