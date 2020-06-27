package eu.beezig.core.command.commands;

import eu.beezig.core.command.Command;

public class ReportsCommand implements Command {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(String[] args) {
        return false;
    }
}
