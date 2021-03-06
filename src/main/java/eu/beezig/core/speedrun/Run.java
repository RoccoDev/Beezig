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

package eu.beezig.core.speedrun;

import eu.beezig.core.Beezig;
import eu.beezig.core.server.modes.DR;
import eu.beezig.core.speedrun.config.SpeedrunConfig;
import eu.beezig.core.speedrun.config.SpeedrunSerializer;
import eu.beezig.core.speedrun.render.TimerModule;
import eu.beezig.core.speedrun.render.TimerRenderer;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.text.Message;
import livesplitcore.*;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Run {
    public static final String GAME_NAME = "Minecraft: The Hive - DeathRun";
    public static final String CATEGORY = "Any%";
    private final String humanMapName;

    private livesplitcore.Run api;
    private Timer timer;
    private final File splits;
    private TimerRenderer renderer;
    private final SpeedrunConfig config;
    private TimerComparison comparison = TimerComparison.PERSONAL_BEST;

    // Components
    private final GeneralLayoutSettings settings;
    private final DetailedTimerComponent detailedTimerComponent;
    private final SplitsComponent splitsComponent;
    private final PreviousSegmentComponent previousSegmentComponent;
    private final SumOfBestComponent sumOfBestComponent;
    private final PossibleTimeSaveComponent possibleTimeSaveComponent;
    private float rainbowHue;
    private int rainbowColor;

    // Lock required to make sure we don't try to fetch a state when the timer/run is being closed (= dropped on the Rust side)
    private final Object LOCK = new Object();

    public Run(String mapName, DR.MapData data, String humanMapName) throws IOException {
        this.humanMapName = humanMapName;
        splits = new File(Beezig.get().getBeezigDir(), "dr/splits/" + FilenameUtils.getName(mapName) + ".lss");
        if(!splits.exists()) {
            splits.getParentFile().mkdirs();
            splits.createNewFile();
        }
        config = new SpeedrunConfig(this);
        try {
            SpeedrunSerializer.read(config);
        } catch (ParseException e) {
            Message.error(Message.translate("error.speedrun.config.load"));
            ExceptionHandler.catchException(e, "Speedrun config load");
        }
        try(InputStream stream = Files.newInputStream(splits.toPath())) {
            ParseRunResult result = livesplitcore.Run.parse(stream, splits.getAbsolutePath(), false);
            if(!result.parsedSuccessfully()) throw new IOException("Couldn't parse run");
            api = result.unwrap();
        }
        api.setGameName(GAME_NAME);
        api.setCategoryName(CATEGORY);
        for (int i = (int) api.len(); i < data.checkpoints; i++) {
            loadSegment("Checkpoint #" + (i + 1));
        }
        RunEditor editor = RunEditor.create(api);
        editor.setPlatformName(mapName);
        api = editor.finish();
        timer = Timer.create(api.copy());
        setComparison(config.getComparison());
        renderer = new TimerRenderer(this, loadModules());
        settings = GeneralLayoutSettings.createDefault();

        // Components
        detailedTimerComponent = new DetailedTimerComponent();
        splitsComponent = new SplitsComponent();
        previousSegmentComponent = new PreviousSegmentComponent();
        sumOfBestComponent = new SumOfBestComponent();
        possibleTimeSaveComponent = new PossibleTimeSaveComponent();
    }

    private List<? extends TimerModule> loadModules() {
        List<TimerModule> result = new ArrayList<>();
        String[] enabled = config.getModules();
        for(String module : enabled) {
            try {
                result.add(SpeedrunModules.registry.get(module).getConstructor().newInstance());
            } catch (ReflectiveOperationException ex) {
                ExceptionHandler.catchException(ex);
            }
        }
        return result;
    }

    public void setComparison(TimerComparison comparison) {
        // Hacky workaround: since we can't directly set the index with the Java API, we have to do this.
        int distance = comparison.getEnumKey() - this.comparison.getEnumKey();
        if(distance > 0) {
            for(int i = 0; i < distance; i++) timer.switchToNextComparison();
        } else if(distance < 0) {
            for(int i = 0; i > distance; i--) timer.switchToPreviousComparison();
        }
        this.comparison = comparison;
    }

    public TimerComparison getComparison() {
        return comparison;
    }

    public void reloadConfig() {
        renderer = new TimerRenderer(this, loadModules());
    }

    public String getHumanMapName() {
        return humanMapName;
    }

    public TimerRenderer getRenderer() {
        return renderer;
    }

    public SpeedrunConfig getConfig() {
        return config;
    }

    private void loadSegment(String name) {
        api.pushSegment(new Segment(name));
    }

    public void start() {
        if(isTimerRunning()) throw new IllegalStateException("Timer is already running");
        timer.start();
    }

    public void split() {
        if(!isTimerRunning()) return;
        timer.split();
    }

    public void forceEnd(TimeSpanRef gameTime) {
        if(!isTimerRunning()) return;
        for(int i = 0; i < api.len(); i++) timer.skipSplit();
        timer.split();
        timer.setGameTime(gameTime);
    }

    public Object getStateLock() {
        return LOCK;
    }

    /**
     * Runs every tick, updates the color for the rainbow effect
     */
    public void tick() {
        if(config != null && config.isRainbowBestSegment()) {
            // Extract opacity from regular color
            int alpha = config.getBestSegmentColor() >>> 24;
            if((rainbowHue += 0.02) > 1f) rainbowHue = 0f;
            int rgb = Color.HSBtoRGB(rainbowHue, 1f, 1f);
            // Add opacity to generated color
            this.rainbowColor = (alpha << 24) | rgb;
        }
    }

    public int getRainbowColor() {
        return rainbowColor;
    }

    /**
     * Resets the ongoing timer, or does nothing if the timer isn't running.
     * @param saveAttempt whether the current attempt should be saved to disk
     */
    public void reset(boolean saveAttempt) {
        if(!isTimerRunning()) return;
        timer.reset(saveAttempt);
    }

    public double getSeconds() {
        if(!isTimerRunning()) return 0D;
        return timer.currentTime().realTime().totalSeconds();
    }

    public DetailedTimerComponentState getDetailedTimerState() {
        if(timer == null || detailedTimerComponent == null) return null;
        return detailedTimerComponent.state(timer, settings);
    }

    public SplitsComponentState getSplitsState() {
        if(timer == null || splitsComponent == null) return null;
        return splitsComponent.state(timer, settings);
    }

    public PreviousSegmentComponentState getPreviousSegmentState() {
        if(timer == null || previousSegmentComponent == null) return null;
        return previousSegmentComponent.state(timer, settings);
    }

    public SumOfBestComponentState getSumOfBestState() {
        if(timer == null || sumOfBestComponent == null) return null;
        return sumOfBestComponent.state(timer);
    }

    public PossibleTimeSaveComponentState getPossibleTimeSaveState() {
        if(timer == null || possibleTimeSaveComponent == null) return null;
        return possibleTimeSaveComponent.state(timer);
    }

    public void save() {
        if(timer != null) {
            try {
                Files.write(splits.toPath(), Collections.singleton(timer.saveAsLss()), Charset.defaultCharset());
            } catch (IOException e) {
                ExceptionHandler.catchException(e, "Run save");
            }
        }
    }

    public void endNow() {
        synchronized (LOCK) {
            if (timer != null) timer.close();
            timer = null;
            api.close();
        }
    }

    public boolean isTimerRunning() {
        return timer != null && timer.currentPhase() == 1; // TimerPhase::Running
    }
}
