package eu.beezig.core.util.process.processes;

import eu.beezig.core.util.process.IProcess;

public enum ScreenRecorders implements IProcess {
    OBS("obs");

    private String[] aliases;

    ScreenRecorders(String... aliases) {
        this.aliases = aliases;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }
}
