package eu.beezig.core.net.profile;

import eu.beezig.core.Beezig;
import eu.beezig.hiveapi.wrapper.utils.download.Downloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class TimezoneUtils {
    public static CompletableFuture<String> getTimezone() {
        try {
            return Downloader.getJsonObject(new URL("http://ip-api.com/json/?fields=status,message,timezone")).thenApplyAsync(json -> {
               if("success".equals(json.getString("status"))) {
                   return json.getString("timezone");
               }
               return null;
            });
        } catch (MalformedURLException e) {
            Beezig.logger.error("Malformed Timezone URL", e);
        }
        return null;
    }
}
