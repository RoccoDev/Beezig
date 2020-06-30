package eu.beezig.core.util.process.providers;

import eu.beezig.core.util.process.IProcessProvider;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class UnixProcessProvider implements IProcessProvider {
    @Override
    public List<String> getRunningProcesses() throws IOException {
        Process process = new ProcessBuilder("ps", "acxo", "command").start();
        return IOUtils.readLines(process.getInputStream(), Charset.defaultCharset());
    }
}
