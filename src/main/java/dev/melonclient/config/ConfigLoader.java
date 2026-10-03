/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.config;

import com.google.gson.Gson;
import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.option.Option;
import dev.melonclient.feature.setting.Setting;
import dev.melonclient.gui.Style;
import dev.melonclient.helpers.OSHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ConfigLoader {

    public static void loadConfig() throws FileNotFoundException {
        applyConfig(readConfig(new File(OSHelper.getMelonDirectory() + "config.json")));
    }

    /**
     * Wczytuje nazwany config z podfolderu configs/ (ten sam zapisywany przez
     * ConfigSaver.saveConfigAs) - np. dostany od kolegi i wrzucony do tego
     * samego folderu.
     */
    public static void loadConfigByName(String name) throws FileNotFoundException {
        File file = new File(ConfigSaver.getConfigsDirectory() + name + ".json");
        applyConfig(readConfig(file));
    }

    private static Config readConfig(File file) throws FileNotFoundException {
        FileReader reader = new FileReader(file);
        return new Gson().fromJson(reader, Config.class);
    }

    /**
     * Dopasowanie zapisanych wpisów do aktualnych modów/opcji/ustawień
     * odbywa się PO NAZWIE, nie po numerze pozycji na liście - dzięki temu
     * dodawanie/usuwanie/przestawianie opcji między wersjami moda nie psuje
     * już losowych ustawień przy wczytywaniu starszego pliku.
     */
    private static void applyConfig(Config config) {
        for (Mod mod : Melon.INSTANCE.modManager.getMods()) {
            ModConfig modConfig = findModConfigByName(config.getConfig(), mod.getName());
            if (modConfig == null) {
                continue;
            }

            mod.setToggled(modConfig.isToggled());

            ArrayList<Setting> clientSettings = Melon.INSTANCE.settingManager.getSettingsByMod(mod);
            for (Setting configSetting : modConfig.getSettings()) {
                Setting clientSetting = findSettingByName(clientSettings, configSetting.getName());
                if (clientSetting == null) {
                    continue;
                }
                applySettingValue(configSetting, clientSetting);
            }

            if (Melon.INSTANCE.hudEditor.getHudMod(mod.getName()) != null && modConfig.getPositions() != null) {
                Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).setX(modConfig.getPositions()[0]);
                Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).setY(modConfig.getPositions()[1]);
                Melon.INSTANCE.hudEditor.getHudMod(mod.getName()).setSize(modConfig.getSize());
            }
        }

        for (Option clientOption : Melon.INSTANCE.optionManager.getOptions()) {
            Option configOption = findOptionByName(config.getOptionsConfigList(), clientOption.getName());
            if (configOption == null) {
                continue;
            }
            applyOptionValue(configOption, clientOption);
        }

        Style.setDarkMode(config.isDarkMode());
        Style.setSnapping(config.isSnapping());
    }

    private static ModConfig findModConfigByName(ArrayList<ModConfig> list, String name) {
        for (ModConfig modConfig : list) {
            if (modConfig.getName().equalsIgnoreCase(name)) {
                return modConfig;
            }
        }
        return null;
    }

    private static Setting findSettingByName(ArrayList<Setting> list, String name) {
        for (Setting setting : list) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    private static Option findOptionByName(ArrayList<Option> list, String name) {
        for (Option option : list) {
            if (option.getName().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }

    private static void applySettingValue(Setting configSetting, Setting clientSetting) {
        switch (configSetting.getMode()) {
            case "CheckBox":
                clientSetting.setCheckToggled(configSetting.isCheckToggled());
                break;
            case "Slider":
                clientSetting.setCurrentNumber(configSetting.getCurrentNumber());
                break;
            case "ModePicker":
                clientSetting.setCurrentMode(configSetting.getCurrentMode());
                clientSetting.setModeIndex(configSetting.getModeIndex());
                break;
            case "ColorPicker":
                clientSetting.setColor(configSetting.getColor());
                clientSetting.setSideColor(configSetting.getSideColor());
                clientSetting.setSideSlider(configSetting.getSideSlider());
                clientSetting.setMainSlider(configSetting.getMainSlider());
                break;
            case "CellGrid":
                clientSetting.setCells(configSetting.getCells());
                break;
            case "Keybinding":
                clientSetting.setKey(configSetting.getKey());
                break;
        }
    }

    private static void applyOptionValue(Option configOption, Option clientOption) {
        switch (configOption.getMode()) {
            case "CheckBox":
                clientOption.setCheckToggled(configOption.isCheckToggled());
                break;
            case "Slider":
                clientOption.setCurrentNumber(configOption.getCurrentNumber());
                break;
            case "ModePicker":
                clientOption.setCurrentMode(configOption.getCurrentMode());
                clientOption.setModeIndex(configOption.getModeIndex());
                break;
            case "ColorPicker":
                clientOption.setColor(configOption.getColor());
                clientOption.setSideColor(configOption.getSideColor());
                clientOption.setSideSlider(configOption.getSideSlider());
                clientOption.setMainSlider(configOption.getMainSlider());
                break;
            case "CellGrid":
                clientOption.setCells(configOption.getCells());
                break;
            case "Keybinding":
                clientOption.setKey(configOption.getKey());
                break;
        }
    }
}
