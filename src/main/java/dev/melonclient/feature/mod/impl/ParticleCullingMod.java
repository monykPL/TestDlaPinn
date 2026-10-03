package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;

public class ParticleCullingMod extends Mod {

    public ParticleCullingMod() {
        super(
                "ParticleCulling",
                "Skips rendering particles outside the camera's field of view to improve FPS in particle-heavy fights.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Cull Angle", this, 150, 70));
    }

    public static float getCullAngle() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName("ParticleCulling", "Cull Angle").getCurrentNumber();
    }
}

