/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.config;

import com.google.gson.Gson;
import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.option.Option;
import dev.melonclient.gui.Style;
import dev.melonclient.helpers.OSHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ConfigSaver {

    public static void migrateLegacyConfig() {
        if (configExists()) {
            return;
        }

        File legacyFile = new File(OSHelper.getLegacyMelonDirectory() + "config.json");
        if (!legacyFile.exists()) {
            return;
        }

        try {
            createDir();
            Files.copy(legacyFile.toPath(), new File(OSHelper.getMelonDirectory() + "config.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void saveConfig() throws IOException {
        writeConfigToFile(new File(OSHelper.getMelonDirectory() + "config.json"));
    }

    /**
     * Zapisuje AKTUALNE ustawienia jako nowy, nazwany plik w podfolderze
     * configs/ - to jest zwykły, samodzielny plik JSON, więc można go po
     * prostu wysłać koledze (Discord, dysk itp.) i wrzucić do jego
     * .minecraft/melonclient/configs/, żeby użył tych samych ustawień.
     *
     * @param name nazwa configu (bez rozszerzenia .json, dodawane automatycznie)
     */
    public static void saveConfigAs(String name) throws IOException {
        createConfigsDir();
        String safeName = sanitizeName(name);
        writeConfigToFile(new File(getConfigsDirectory() + safeName + ".json"));
    }

    public static String[] listSavedConfigs() {
        createConfigsDir();
        File dir = new File(getConfigsDirectory());
        File[] files = dir.listFiles((d, fileName) -> fileName.toLowerCase().endsWith(".json"));
        if (files == null) {
            return new String[0];
        }
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            names[i] = fileName.substring(0, fileName.length() - ".json".length());
        }
        return names;
    }

    public static boolean deleteConfig(String name) {
        File file = new File(getConfigsDirectory() + sanitizeName(name) + ".json");
        return file.exists() && file.delete();
    }

    public static String getConfigsDirectory() {
        return OSHelper.getMelonDirectory() + "configs" + File.separator;
    }

    /**
     * Usuwa znaki, które nie mogą być użyte w nazwie pliku na Windowsie
     * (żeby "Speed Build v2" albo coś z dziwnymi znakami nie wywaliło zapisu).
     */
    private static String sanitizeName(String name) {
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.isEmpty() ? "config" : cleaned;
    }

    private static void writeConfigToFile(File targetFile) throws IOException {
        if (!targetFile.getParentFile().exists()) {
            Files.createDirectories(targetFile.getParentFile().toPath());
        }

        FileWriter writer = new FileWriter(targetFile);

        Config config = buildCurrentConfig();

        String json = new Gson().toJson(config);
        writer.write(json);
        writer.close();
    }

    private static Config buildCurrentConfig() {
        Config config = new Config();

        for (Mod mod : Melon.INSTANCE.modManager.getMods()) {
            ModConfig modConfig = new ModConfig(
                    mod.getName(),
                    mod.isToggled(),
                    Melon.INSTANCE.settingManager.getSettingsByMod(mod),
                    Melon.INSTANCE.hudEditor.getHudMod(mod.getName()) != null ?
                            new int[]{Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).getX(), Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).getY()} :
                            new int[]{0, 0},
                    Melon.INSTANCE.hudEditor.getHudMod(mod.getName()) != null ?
                            Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).getSize() : 1
            );
            config.addConfig(modConfig);
        }

        for (Option option : Melon.INSTANCE.optionManager.getOptions()) {
            config.addConfigOption(option);
        }

        config.setDarkMode(Style.isDarkMode());
        config.setSnapping(Style.isSnapping());

        return config;
    }

    private static void createDir() {
        File file = new File(OSHelper.getMelonDirectory());
        if (!file.exists()) {
            try {
                Files.createDirectory(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        createFile();
    }

    private static void createConfigsDir() {
        createDir();
        File dir = new File(getConfigsDirectory());
        if (!dir.exists()) {
            try {
                Files.createDirectories(dir.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void createFile() {
        File file = new File(OSHelper.getMelonDirectory() + "config.json");
        if (!file.exists()) {
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean configExists() {
        File file = new File(OSHelper.getMelonDirectory() + "config.json");
        return file.exists();
    }
}
