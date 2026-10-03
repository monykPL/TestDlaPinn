package dev.melonclient.helpers;

import dev.melonclient.Melon;
import net.minecraft.client.gui.ScaledResolution;

public class ResolutionHelper {

    private static ScaledResolution scaledResolution;

    public static int getHeight() {
        scaledResolution = new ScaledResolution(Melon.INSTANCE.mc);
        return scaledResolution.getScaledHeight();
    }

    public static int getWidth() {
        scaledResolution = new ScaledResolution(Melon.INSTANCE.mc);
        return scaledResolution.getScaledWidth();
    }

    public static int getFactor() {
        scaledResolution = new ScaledResolution(Melon.INSTANCE.mc);
        return scaledResolution.getScaleFactor();
    }
}
