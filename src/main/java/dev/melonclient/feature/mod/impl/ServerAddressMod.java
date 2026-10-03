package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

import java.awt.*;

public class ServerAddressMod extends Mod {

    public ServerAddressMod() {
        super(
                "Server Address",
                "Shows the address of the Server you are currently on.",
                Type.Hud
        );

        String[] mode = {"Modern", "Legacy"};
        Melon.INSTANCE.settingManager.addSetting(new Setting("Mode", this, "Modern", 0, mode));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Background", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Font Color", this, new Color(255, 255, 255), new Color(255, 0, 0), 0, new float[]{0, 0}));
    }
}
