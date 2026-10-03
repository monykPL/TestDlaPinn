package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

public class ScoreboardMod extends Mod {

    public ScoreboardMod() {
        super(
                "Scoreboard",
                "Adds Tweaks to the Scoreboard",
                Type.Tweaks
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Background", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Remove Red Numbers", this, false));
    }
}

