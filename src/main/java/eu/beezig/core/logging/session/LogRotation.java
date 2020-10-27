package eu.beezig.core.logging.session;

import eu.beezig.core.Beezig;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class LogRotation {
    private File[] getApplicableFolders() {
        File sessions = new File(Beezig.get().getBeezigDir(), "sessions");
        File[] subs = sessions.listFiles();
        if (subs == null) return new File[0];
        LocalDate expiry = LocalDate.now(ZoneId.systemDefault()).minus(30, ChronoUnit.DAYS);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Arrays.stream(subs).filter(f -> {
            if (!f.isDirectory()) return false;
            return LocalDate.parse(f.getName(), fmt).isBefore(expiry);
        }).sorted().toArray(File[]::new);
    }

    public void rotateLogs() throws IOException {
        File[] applicable = getApplicableFolders();
        if (applicable.length == 0) return;
        Beezig.logger.info("Rotating " + applicable.length + " session logs.");
        String fileName = applicable[0].getName() + "." + applicable[applicable.length - 1].getName() + ".tar.gz";
        File archive = new File(Beezig.get().getBeezigDir(), "sessions/" + FilenameUtils.getName(fileName));
        try (FileOutputStream os = new FileOutputStream(archive);
             OutputStream gzip = new GzipCompressorOutputStream(os);
             ArchiveOutputStream tarOut = new TarArchiveOutputStream(gzip)) {
            for (File folder : applicable) {
                File[] subs = folder.listFiles();
                if (subs == null) continue;
                for (File file : subs) {
                    if (!file.isFile()) continue;
                    TarArchiveEntry entry = new TarArchiveEntry(file, folder.getName() + "/" + file.getName());
                    entry.setSize(file.length());
                    tarOut.putArchiveEntry(entry);
                    try (FileInputStream is = new FileInputStream(file)) {
                        IOUtils.copy(is, tarOut);
                    }
                    tarOut.closeArchiveEntry();
                }
                FileUtils.deleteDirectory(folder);
            }
        }
    }
}
