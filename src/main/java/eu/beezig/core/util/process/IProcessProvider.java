package eu.beezig.core.util.process;

import java.io.IOException;
import java.util.List;

public interface IProcessProvider {
    List<String> getRunningProcesses() throws IOException;
}
