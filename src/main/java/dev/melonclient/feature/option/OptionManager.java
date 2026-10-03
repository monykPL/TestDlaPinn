/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.feature.option;

import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class OptionManager {

    public ArrayList<Option> optionList  = new ArrayList<>();

    public OptionManager() {
        init();
    }

    public void init() {
        addOption(new Option("Style"));
        addOption(new Option("Font Changer", "Arial", 0,
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        addOption(new Option("Color", new Color(255, 255, 255), new Color(255, 0, 0), 0, new float[]{0, 0}));

        addOption(new Option("Misc"));
        addOption(new Option("Minimal View Bobbing", true));
        addOption(new Option("Fire Height", 50, 0));
        addOption(new Option("Disable Custom Title Screen", false));

        addOption(new Option("Performance"));
        addOption(new Option("Rounded Corners", true));

        addOption(new Option("Optimization"));
        addOption(new Option("Entity Culling", true));
        addOption(new Option("Entity Render Distance", 128, 64));
        addOption(new Option("Particle Limiter", true));
        addOption(new Option("Max Particles Per Tick", 1000, 200));
        // zostawia tylko cząsteczki trafień i chodzenia, usuwa resztę (w tym rozwalanie bloków)
        addOption(new Option("Reduce Particles", false));
        // zatrzymuje animację ognia (fire_layer_0/1) na jednej klatce
        addOption(new Option("Freeze Fire Animation", false));
        addOption(new Option("Menu Blur", true));

        addOption(new Option("Panorama"));
        addOption(new Option("Panorama Theme", "Winter", 0, new String[]{"Winter", "Normal", "Summer", "Vanilla"}));
        addOption(new Option("Background Blur", true));

        addOption(new Option("Integrations"));
        addOption(new Option("Discord RPC", true));

        addOption(new Option("Controls"));
        addOption(new Option("ModMenu Keybinding", Keyboard.KEY_RSHIFT));
    }

    public void addOption(Option option) {
        optionList.add(option);
    }

    public ArrayList<Option> getOptions() {
        return optionList;
    }

    public Option getOptionByName(String name) {
        for (Option option : optionList) {
            if (option.getName().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }
}
