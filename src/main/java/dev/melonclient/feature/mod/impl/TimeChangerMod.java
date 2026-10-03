package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

public class TimeChangerMod extends Mod {
    public TimeChangerMod() {
        super(
                "TimeChanger",
                "Changes the time of the current World visually.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Offset", this, 12000, 0));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Speed", this, 50, 1));
    }
}

