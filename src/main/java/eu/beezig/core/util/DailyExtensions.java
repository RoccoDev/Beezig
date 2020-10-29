package eu.beezig.core.util;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.TextButton;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DailyExtensions {
    public static void send(List<Pair<String, String>> extensions) {
        int setting = Settings.DAILY_EXTENSIONS.get().getInt();
        if(setting <= 0) return;
        Beezig.cfg().setAsIs(Settings.DAILY_EXTENSIONS, setting - 1);
        try {
            Beezig.cfg().save();
        } catch (IOException e) {
            ExceptionHandler.catchException(e, "Daily extension - cfg save");
        }
        Pair<String, String> extension = extensions.get(ThreadLocalRandom.current().nextInt(extensions.size()));
        String name = extension.getKey();
        String url = "https://go.beezig.eu/" + extension.getValue();
        MessageComponent msg = new MessageComponent(Message.infoPrefix() + Beezig.api().translate("msg.daily.ext", Color.accent() + name + Color.primary()));
        msg.getStyle().setOnClick(new MessageAction(MessageAction.Action.OPEN_URL, url));
        msg.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(Color.accent() + Beezig.api().translate("msg.daily.ext.hint", Color.primary() + name + Color.accent()))));
        TextButton dismiss = new TextButton("btn.daily.ext.dismiss", "btn.daily.ext.dismiss.desc", "§c");
        dismiss.doRunCommand("/bsettings daily_extensions 0");
        MessageComponent dismissDisplay = new MessageComponent(Message.infoPrefix());
        dismissDisplay.getSiblings().add(dismiss);
        Beezig.api().messagePlayerComponent(msg, false);
        Beezig.api().messagePlayerComponent(dismissDisplay, false);
    }
}
