package dev.melonclient.helpers;

import dev.melonclient.Melon;

import java.io.File;

public class OSHelper {

    private static final String currentOS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() { return (currentOS.contains("windows")); }
    public static boolean isMac() { return (currentOS.contains("mac")); }
    public static boolean isLinux() { return (currentOS.contains("linux")); }

    /**
     * Returns the Location of the .minecraft directory
     *
     * @return Directory
     */

    public static String getMinecraftDirectory() {
        return Melon.INSTANCE.mc.mcDataDir.getAbsolutePath() + File.separator;
    }

    public static String getMelonDirectory() {
        return getMinecraftDirectory() + "melonclient" + File.separator;
    }

    /**
     * Returns the location of the legacy pre-rebrand ".minecraft/melon" config directory
     *
     * @return Directory
     */

    public static String getLegacyMelonDirectory() {
        return getMinecraftDirectory() + "melon" + File.separator;
    }
}

