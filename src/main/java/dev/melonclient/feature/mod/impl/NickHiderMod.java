package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

public class NickHiderMod extends Mod {

    public NickHiderMod() {
        super(
                "NickHider",
                "Hides your nickname in game by replacing it.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Nickname", this, "Name", "You", 3));
    }

    public static String replaceNickname(String nick) {
        if(Melon.INSTANCE != null) {
            if (Melon.INSTANCE.modManager != null) {
                if (Melon.INSTANCE.modManager.getMod("NickHider").isToggled()) {
                    return nick.replace(
                            Melon.INSTANCE.mc.getSession().getUsername(),
                            Melon.INSTANCE.settingManager.getSettingByModAndName("NickHider", "Nickname").getText()
                    );
                }
            }
        }
        return nick;
    }
}

