package eu.beezig.core.server.modes;

import eu.beezig.core.Beezig;
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.config.Settings;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.util.text.Message;

public class SHU extends HiveMode {
    @Override
    public String getIdentifier() {
        return "shu";
    }

    @Override
    public String getName() {
        return "Arcade Shuffle";
    }

    @Override
    protected void onModeJoin() {
        super.onModeJoin();
        if(Settings.AUTOVOTE_SHUFFLE.get().getBoolean() && BeezigForge.isSupported()) {
            try {
                BeezigForge.get().autovoteShuffle(getAutovoteManager().getFavoriteMaps("shu"));
            } catch (Exception e) {
                Message.error(Message.translate("error.data_read"));
                Beezig.logger.error(e);
            }
        }
    }
}
