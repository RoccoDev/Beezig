package eu.beezig.core.news;

import com.google.common.base.Splitter;
import eu.beezig.core.Beezig;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;
import eu.the5zig.util.minecraft.ChatColor;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NewsManager {
    private static final Splitter NEWLINE = Splitter.on('\n');
    private Map<NewsType, Set<NewsEntry>> loadedNews;
    private Map<NewsType, Set<NewsEntry>> unreadNews;
    private final LastLogin lastLogin;

    public NewsManager() {
        lastLogin = new LastLogin();
        try {
            lastLogin.read();
            lastLogin.update();
        } catch (IOException e) {
            ExceptionHandler.catchException(e, "Last login read");
            return;
        }
        NewsParser parser = new NewsParser();
        for(NewsType type : NewsType.values()) {
            if(type != NewsType.HIVE_FORUMS) parser.download(type);
        }
        this.loadedNews = parser.done();
    }

    public void readUnread() {
        if(unreadNews == null || unreadNews.isEmpty()) {
            Message.error(Message.translate("error.news.unread"));
            return;
        }
        Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.accent() + "§l" + Message.translate("news.title")));
        sendNews(unreadNews, false);
        unreadNews.clear();
    }

    public void sendUpdates() {
        Map<NewsType, Set<NewsEntry>> unread = getUnreadNews(lastLogin.getLastLogin());
        boolean hasUpdates = unread.entrySet().stream().anyMatch(e -> !e.getValue().isEmpty());
        if(!hasUpdates) return;
        WorldTask.submit(() -> {
            Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.accent() + "§l" + Message.translate("news.title")));
            sendNews(unread, true);
        });
    }

    public Set<NewsEntry> downloadForums() {
        NewsParser parser = new NewsParser();
        parser.download(NewsType.HIVE_FORUMS);
        return ((TreeSet)parser.done().getOrDefault(NewsType.HIVE_FORUMS, new TreeSet<>())).descendingSet();
    }

    private void sendNews(Map<NewsType, Set<NewsEntry>> unread, boolean limitUnread) {
        boolean first = true;
        for (Map.Entry<NewsType, Set<NewsEntry>> entry : unread.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            if(first) first = false;
            else Beezig.api().messagePlayer("\n"); // 2 newlines
            Beezig.api().messagePlayer(StringUtils.centerWithSpaces("§l" + entry.getKey().translateName()));
            if(limitUnread && entry.getValue().size() > 2) {
                if(unreadNews == null) unreadNews = new EnumMap<>(NewsType.class);
                unreadNews.put(entry.getKey(), entry.getValue());
                MessageComponent read = new MessageComponent(Message.infoPrefix() + Message.translate("news.unread"));
                read.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(Color.accent() + Message.translate("news.unread"))));
                read.getStyle().setOnClick(new MessageAction(MessageAction.Action.RUN_COMMAND, "/beezig news"));
                Beezig.api().messagePlayerComponent(read, false);
                continue;
            }
            boolean firstNews = true;
            for (NewsEntry news : entry.getValue()) {
                if(firstNews) firstNews = false;
                else Beezig.api().messagePlayer(""); // 1 newline
                Beezig.api().messagePlayer(StringUtils.centerWithSpaces(Color.primary() + ChatColor.UNDERLINE + news.getTitle()));
                List<String> lines = NEWLINE.splitToList(WordUtils.wrap(news.getContent() == null ? "?" : news.getContent(), 50));
                int count = 0;
                for(String line : lines) {
                    Beezig.api().messagePlayer(StringUtils.centerWithSpaces(Color.accent() + line));
                    if(count++ >= 4) break;
                }
                if(news.getLink() != null) {
                    MessageComponent readMore = new MessageComponent(StringUtils.centerWithSpaces(Color.primary() + "[" + Message.translate("btn.news.more.name") + "]"));
                    readMore.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(Color.accent() + Message.translate("btn.news.more.desc"))));
                    readMore.getStyle().setOnClick(new MessageAction(MessageAction.Action.OPEN_URL, news.getLink()));
                    Beezig.api().messagePlayerComponent(readMore, false);
                }
            }
        }
        MessageComponent credits = new MessageComponent(StringUtils.linedCenterText(Color.primary(), "§7" + Message.translate("news.credits")));
        credits.getStyle().setOnClick(new MessageAction(MessageAction.Action.OPEN_URL, NewsUrls.CREDITS));
        credits.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent("§7" + Message.translate("news.credits.desc"))));
        Beezig.api().messagePlayerComponent(credits, false);
    }

    public Map<NewsType, Set<NewsEntry>> getLoadedNews() {
        return loadedNews;
    }

    private Map<NewsType, Set<NewsEntry>> getUnreadNews(Date compareTo) {
        return loadedNews.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            TreeSet<NewsEntry> entries = new TreeSet<>(e.getValue());
            for(Iterator<NewsEntry> iter = entries.descendingIterator(); iter.hasNext();) {
                NewsEntry entry = iter.next();
                if(!entry.isPersistent() && entry.getPubDate().compareTo(compareTo) < 0) {
                    iter.remove();
                    entries.headSet(entry, false).clear();
                    break;
                }
            }
            return entries;
        }));
    }
}
