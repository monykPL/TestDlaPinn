package dev.melonclient.feature.mod.impl;

import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;

public class ScrollTooltipsMod extends Mod {

    public ScrollTooltipsMod() {
        super(
                "ScrollTooltips",
                "Makes long tooltips which go offscreen, scrollable.",
                Type.Tweaks
        );
    }
}

