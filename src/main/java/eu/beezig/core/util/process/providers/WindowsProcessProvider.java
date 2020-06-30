package eu.beezig.core.util.process.providers;

import eu.beezig.core.util.process.IProcessProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsProcessProvider implements IProcessProvider {
    private static final Pattern PROCESS_NAME_REGEX = Pattern.compile("\"(.+?)\\.exe\"");

    @Override
    public List<String> getRunningProcesses() throws IOException {
        Process process = new ProcessBuilder("tasklist", "/FI", "Status eq Running", "/FO", "CSV").start();
        LineIterator iter = IOUtils.lineIterator(process.getInputStream(), Charset.defaultCharset());
        List<String> result = new ArrayList<>();
        while(iter.hasNext()) {
            String line = iter.nextLine();
            Matcher m = PROCESS_NAME_REGEX.matcher(line);
            if(m.find()) {
                result.add(m.group(1));
            }
        }
        return result;
    }
}
