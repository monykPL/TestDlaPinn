package dev.melonclient.feature.mod.impl;

import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;

public class NoHurtCamMod extends Mod {

    public NoHurtCamMod() {
        super(
                "NoHurtCam",
                "Removes the camera shake effect when you take damage.",
                Type.Visual
        );
    }
}

