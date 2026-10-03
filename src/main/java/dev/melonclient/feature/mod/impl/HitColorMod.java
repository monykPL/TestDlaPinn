package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

import java.awt.*;

public class HitColorMod extends Mod {
    public HitColorMod() {
        super(
                "Hit Color",
                "Changes the color of damaged entities.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Damage Color", this, new Color(255, 0, 0), new Color(255, 0, 0), 0, new float[]{145, 0}));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Alpha", this, 255, 80));
    }
}

