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

package eu.beezig.core.speedrun.config;

import eu.beezig.core.api.SettingInfo;
import eu.beezig.core.speedrun.Run;
import eu.beezig.core.speedrun.SpeedrunModules;
import eu.beezig.core.speedrun.TimerComparison;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SpeedrunConfig {
    /**
     * The associated Run object
     */
    private final Run run;

    /**
     * Default color
     */
    private int defaultColor = 0xFFFFFFFF;

    /**
     * The runner is ahead of the comparison and is gaining even more time.
     */
    private int aheadGainingTimeColor = 0xFF00CC36;

    /**
     * The runner is ahead of the comparison, but is losing time.
     */
    private int aheadLosingTimeColor = 0xFF61D17F;

    /**
     * The runner is behind the comparison and is losing even more time.
     */
    private int behindLosingTimeColor = 0xFFCC0000;

    /**
     * The runner is behind the comparison, but is gaining back time.
     */
    private int behindGainingTimeColor = 0xFFD16161;

    /**
     * The runner achieved a best segment. (aka gold split)
     */
    private int bestSegmentColor = 0xFFFFD500;

    /**
     * There's no active attempt.
     */
    private int notRunningColor = 0xFFABABAB;

    /**
     * The timer is paused.
     */
    private int pausedColor = 0xFF7A7A7A;

    /**
     * The runner achieved a new Personal Best.
     */
    private int personalBestColor = 0xFF14A5FF;

    /**
     * The background for the current segment
     */
    private int currentSegmentColor = 0x8014A5FF;

    /**
     * The background color
     */
    private int backgroundColor = 0xFF000000;

    /**
     * The color for titles, prefixes etc.
     */
    private int prefixColor = 0xFFFFFFFF;

    /**
     * The enabled modules
     */
    private String[] modules = SpeedrunModules.defaultModules.toArray(new String[0]);

    /**
     * The module scale (percentage)
     */
    private int scale = 100;

    /**
     * Whether the best segment should use a rainbow animation
     */
    private boolean rainbowBestSegment = false;

    /**
     * The preferred comparison for the timer
     */
    private TimerComparison comparison = TimerComparison.PERSONAL_BEST;

    public SpeedrunConfig(Run run) {
        this.run = run;
    }

    public int getColor(String semantic) {
        switch (semantic) {
            case "Default":
                break;
            case "AheadGainingTime":
                return aheadGainingTimeColor;
            case "AheadLosingTime":
                return aheadLosingTimeColor;
            case "BehindGainingTime":
                return behindGainingTimeColor;
            case "BehindLosingTime":
                return behindLosingTimeColor;
            case "BestSegment":
                return rainbowBestSegment && run != null ? run.getRainbowColor() : bestSegmentColor;
            case "NotRunning":
                return notRunningColor;
            case "Paused":
                return pausedColor;
            case "PersonalBest":
                return personalBestColor;
        }
        return defaultColor;
    }

    public void putAll(List<SettingInfo> config) {
        for(SpeedrunConfigValues value : SpeedrunConfigValues.values()) {
            SettingInfo info = new SettingInfo();
            info.key = value.name();
            if(value == SpeedrunConfigValues.MODULES)
                info.value = ArrayUtils.addAll(modules, SpeedrunModules.registry.keySet().stream().map(s -> "Model: " + s).toArray(String[]::new));
            else info.value = value.get(this);
            info.name = value.translateName();
            info.desc = value.translateDescription();
            config.add(info);
        }
    }

    public void setAll(List<SettingInfo> config) {
        for(SettingInfo info : config) {
            SpeedrunConfigValues value = SpeedrunConfigValues.valueOf(info.key);
            value.setValue(this, info.value);
        }
    }

    public void setAheadGainingTimeColor(int aheadGainingTimeColor) {
        this.aheadGainingTimeColor = aheadGainingTimeColor;
    }

    public void setAheadLosingTimeColor(int aheadLosingTimeColor) {
        this.aheadLosingTimeColor = aheadLosingTimeColor;
    }

    public void setBehindLosingTimeColor(int behindLosingTimeColor) {
        this.behindLosingTimeColor = behindLosingTimeColor;
    }

    public void setBehindGainingTimeColor(int behindGainingTimeColor) {
        this.behindGainingTimeColor = behindGainingTimeColor;
    }

    public void setRainbowBestSegment(boolean rainbowBestSegment) {
        this.rainbowBestSegment = rainbowBestSegment;
    }

    public void setBestSegmentColor(int bestSegmentColor) {
        this.bestSegmentColor = bestSegmentColor;
    }

    public void setNotRunningColor(int notRunningColor) {
        this.notRunningColor = notRunningColor;
    }

    public void setPausedColor(int pausedColor) {
        this.pausedColor = pausedColor;
    }

    public void setPersonalBestColor(int personalBestColor) {
        this.personalBestColor = personalBestColor;
    }

    public void setDefault(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setCurrentSegmentColor(int currentSegmentColor) {
        this.currentSegmentColor = currentSegmentColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setPrefixColor(int prefixColor) {
        this.prefixColor = prefixColor;
    }

    public void setModules(String[] modules) {
        this.modules = modules;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setComparison(TimerComparison comparison) {
        this.comparison = comparison;
        if(run != null && run.isTimerRunning()) run.setComparison(comparison);
    }

    public int getCurrentSegmentColor() {
        return currentSegmentColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public int getAheadGainingTimeColor() {
        return aheadGainingTimeColor;
    }

    public int getAheadLosingTimeColor() {
        return aheadLosingTimeColor;
    }

    public int getBehindLosingTimeColor() {
        return behindLosingTimeColor;
    }

    public int getBehindGainingTimeColor() {
        return behindGainingTimeColor;
    }

    public int getBestSegmentColor() {
        return bestSegmentColor;
    }

    public int getNotRunningColor() {
        return notRunningColor;
    }

    public int getPausedColor() {
        return pausedColor;
    }

    public int getPersonalBestColor() {
        return personalBestColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getPrefixColor() {
        return prefixColor;
    }

    public String[] getModules() {
        return modules;
    }

    public int getScale() {
        return scale;
    }

    public boolean isRainbowBestSegment() {
        return rainbowBestSegment;
    }

    public TimerComparison getComparison() {
        return comparison;
    }
}
