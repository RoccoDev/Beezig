package eu.beezig.core.report;

import eu.beezig.core.util.text.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumSet;
import java.util.Locale;

public enum ReportReason {
    SPEED(0, "Speed", "bhop"),
    AIMBOT(1, "Aimbot", "aim", "aimassist"),
    FLY(2, "Fly", "flying", "flight", "glide"),
    JESUS(3, "Jesus", "water", "waterwalk", "waterwalking"),
    NO_KNOCKBACK(4, "No Knockback", "kb", "nokb", "antikb", "velocity", "novelocity", "0kb"),
    KILLAURA(5, "Kill Aura", "aura", "ff", "ka", "forcefield"),
    NO_SLOWDOWN(6, "No Slowdown", "noslow", "keepsprint"),
    BLINK(7, "Blink", "tp"),
    XRAY(8, "Xray"),
    ESP(9, "ESP", "tracers", "wallhack", "chams"),
    DERP(10, "Derp"),
    SAFEWALK(11, "Safewalk"),
    SCAFFOLD(12, "Scaffold", "tower", "bridge", "scaffolding"),
    REACH(13, "Reach", "tpaura"),
    VOID_BOUNCE(14, "Void Bounce", "checkpoint", "antivoid"),
    PHASE(15, "Phase"),
    AUTOCLICKER(16, "Auto Clicker", "autoclicker", "autoclick"),
    INV_CLEAN(17, "Inventory Cleaner", "invclean", "invcleaner", "inventoryclean"),
    MOVEMENT_EMULATOR(18, "Movement Emulator", "bp"),

    GLITCHING(19, "Glitching", "glitch", "oom", "outofmap", "oob", "outofbounds"),
    ILLEGAL_TEAM(20, "Illegal Teaming", "team", "crossteam", "teaming", "crossteaming"),
    TIMV_RDM(21, "RDM", "rdmer", "rdming"),
    TIMV_BAIT(22, "Karma Baiting", "bait", "karmabait", "baiting"),
    TIMV_GHOST(23, "Ghosting", "ghost"),
    HARASSMENT(24, "Harassment", "harass", "abuse", "follow", "following"),
    TROLLING(25, "Trolling", "teamkill", "teamtroll", "teamkilling", "teamtrolling"),
    INAP_DRAW(26, "Inappropriate Drawing", "draw", "drawing"),
    INAP_NAME(27, "Inappropriate Name", "name", "badname"),
    INAP_SKIN(28, "Inappropriate Skin", "skin", "badskin"),
    TARGET(29, "Targetting", "target", "targeting");

    private int index;
    private String display;
    private String[] aliases;

    ReportReason(int index, String display, String... aliases) {
        this.index = index;
        this.display = display;
        this.aliases = aliases;
    }

    private static ReportReason getReason(String query) {
        if(query == null) return null;
        String parse = StringUtils.normalizeMapName(query);
        for(ReportReason reason : values()) {
            if(parse.equals(StringUtils.normalizeMapName(reason.display))) return reason;
            if(ArrayUtils.indexOf(reason.aliases, parse) != -1) return reason;
        }
        return null;
    }

    public static EnumSet<ReportReason> getReasons(String[] inputs) {
        EnumSet<ReportReason> reasons = EnumSet.noneOf(ReportReason.class);
        for(String reason : inputs) {
            ReportReason match = getReason(reason);
            if(match != null) reasons.add(match);
        }
        return reasons;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplay() {
        return display;
    }
}
