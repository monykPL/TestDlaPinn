package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

import java.awt.*;

public class CpsMod extends Mod {

    public CpsMod() {
        super(
                "CPS",
                "Shows your CPS on the HUD.",
                Type.Hud
        );

        String[] mode = {"Modern", "Legacy"};
        Melon.INSTANCE.settingManager.addSetting(new Setting("Mode", this, "Modern", 0, mode));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Background", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Right click", this, false));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Font Color", this, new Color(255, 255, 255), new Color(255, 0, 0), 0, new float[]{0, 0}));
    }
}

