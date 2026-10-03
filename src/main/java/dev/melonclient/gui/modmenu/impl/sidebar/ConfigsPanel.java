/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.gui.modmenu.impl.sidebar;

import dev.melonclient.Melon;
import dev.melonclient.config.ConfigLoader;
import dev.melonclient.config.ConfigSaver;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.modmenu.impl.Panel;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.hud.ScrollHelper;
import dev.melonclient.helpers.animation.Animate;
import dev.melonclient.helpers.animation.Easing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Zakładka "Configs" - zamiast globalnych ustawień (Settings) pozwala zapisać
 * AKTUALNY stan wszystkich modów/opcji pod własną nazwą, wczytać go później,
 * albo usunąć. Każdy zapisany config to zwykły plik .json w podfolderze
 * configs/ - można go więc po prostu wysłać koledze.
 *
 * WAŻNE: czcionka tego moda (GlyphPage) ma glify TYLKO dla znaków 0-255,
 * więc polskie znaki z ogonkami (ą, ć, ę, ł, ń, ś, ź, ż - "ó" jest OK, to
 * 243) w ogóle nie mogą się tu pojawić - stąd wszystkie napisy poniżej są
 * celowo bez polskich znaków specjalnych.
 */
public class ConfigsPanel {

    private static final int ROW_HEIGHT = 26;

    private final Panel panel;
    private final TextBox nameBox = new TextBox("Nazwa configu...", 0, 0, 400, 20);
    private final ScrollHelper scrollHelper = new ScrollHelper(0, 300, 35, 300);
    private final Animate saveButtonAnimate = new Animate();

    private ArrayList<String> savedConfigs = new ArrayList<>();
    private String statusMessage = "";
    private long statusMessageUntil = 0;

    public ConfigsPanel(Panel panel) {
        this.panel = panel;
        saveButtonAnimate.setEase(Easing.LINEAR).setMin(0).setMax(20).setSpeed(200);
        refreshList();
    }

    private void refreshList() {
        savedConfigs = new ArrayList<>(Arrays.asList(ConfigSaver.listSavedConfigs()));
        savedConfigs.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public void render(int mouseX, int mouseY) {
        boolean rounded = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();
        int color = Melon.INSTANCE.optionManager.getOptionByName("Color").getColor().getRGB();

        int x = panel.getX();
        int y = panel.getY();
        int w = panel.getW();
        int h = panel.getH();

        int saveRowY = y + 40;
        int buttonW = 70;
        nameBox.renderTextBox(x + 10, saveRowY, mouseX, mouseY);

        boolean saveHovered = MathHelper.withinBox(x + w - buttonW - 10, saveRowY, buttonW, 20, mouseX, mouseY);
        Helper2D.drawRoundedRectangle(x + w - buttonW - 10, saveRowY, buttonW, 20, 2,
                Style.getColor(saveHovered ? 90 : 60).getRGB(), rounded ? 0 : -1);
        Melon.INSTANCE.fontHelper.size20.drawString("Save",
                x + w - buttonW - 10 + buttonW / 2f - Melon.INSTANCE.fontHelper.size20.getStringWidth("Save") / 2f,
                saveRowY + 5, color);

        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusMessageUntil) {
            Melon.INSTANCE.fontHelper.size20.drawString(statusMessage, x + 10, saveRowY + 24, 0x9000ff00);
        }

        int listY = y + 70;
        int listH = h + 200;

        GLHelper.startScissor(x, listY, w, listH);

        int totalHeight = savedConfigs.size() * ROW_HEIGHT;
        scrollHelper.setHeight(totalHeight);
        if (MathHelper.withinBox(x, listY, w, listH, mouseX, mouseY)) {
            scrollHelper.updateScroll();
        }
        scrollHelper.update();

        int rowY = listY + (int) scrollHelper.getCalculatedScroll();
        for (String name : savedConfigs) {
            boolean rowHovered = MathHelper.withinBox(x + 5, rowY, w - 10, ROW_HEIGHT - 4, mouseX, mouseY);

            Helper2D.drawRoundedRectangle(x + 5, rowY, w - 10, ROW_HEIGHT - 4, 2,
                    Style.getColor(rowHovered ? 60 : 40).getRGB(), rounded ? 0 : -1);
            Melon.INSTANCE.fontHelper.size20.drawString(name, x + 14, rowY + 7, color);

            int loadW = 55;
            int deleteW = 20;
            Helper2D.drawRoundedRectangle(x + w - loadW - deleteW - 20, rowY + 3, loadW, ROW_HEIGHT - 10, 2,
                    Style.getColor(80).getRGB(), rounded ? 0 : -1);
            Melon.INSTANCE.fontHelper.size20.drawString("Load",
                    x + w - loadW - deleteW - 20 + 4, rowY + 6, color);

            Helper2D.drawRoundedRectangle(x + w - deleteW - 12, rowY + 3, deleteW, ROW_HEIGHT - 10, 2,
                    Style.getColor(80).getRGB(), rounded ? 0 : -1);
            Helper2D.drawPicture(x + w - deleteW - 9, rowY + 6, 14, 14, color, "icon/cross.png");

            rowY += ROW_HEIGHT;
        }

        if (savedConfigs.isEmpty()) {
            String empty = "No saved configs yet - save one above.";
            Melon.INSTANCE.fontHelper.size20.drawString(empty, x + 10, listY + 10, 0x80ffffff);
        }

        GLHelper.endScissor();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        int x = panel.getX();
        int y = panel.getY();
        int w = panel.getW();

        int saveRowY = y + 40;
        int buttonW = 70;

        nameBox.setFocused(nameBox.isHovered(mouseX, mouseY));

        if (MathHelper.withinBox(x + w - buttonW - 10, saveRowY, buttonW, 20, mouseX, mouseY)) {
            saveCurrentAs(nameBox.getText());
            return;
        }

        int listY = y + 70;
        int rowY = listY + (int) scrollHelper.getCalculatedScroll();
        for (String name : new ArrayList<>(savedConfigs)) {
            int loadW = 55;
            int deleteW = 20;

            if (MathHelper.withinBox(x + w - loadW - deleteW - 20, rowY + 3, loadW, ROW_HEIGHT - 10, mouseX, mouseY)) {
                loadConfig(name);
                return;
            }
            if (MathHelper.withinBox(x + w - deleteW - 12, rowY + 3, deleteW, ROW_HEIGHT - 10, mouseX, mouseY)) {
                ConfigSaver.deleteConfig(name);
                refreshList();
                showStatus("Deleted: " + name);
                return;
            }
            rowY += ROW_HEIGHT;
        }
    }

    private void saveCurrentAs(String name) {
        if (name == null || name.trim().isEmpty()) {
            showStatus("Type a name first!");
            return;
        }
        try {
            ConfigSaver.saveConfigAs(name);
            refreshList();
            nameBox.setText("");
            showStatus("Saved as: " + name);
        } catch (IOException e) {
            showStatus("Save failed: " + e.getMessage());
        }
    }

    private void loadConfig(String name) {
        try {
            ConfigLoader.loadConfigByName(name);
            showStatus("Loaded: " + name);
        } catch (Exception e) {
            showStatus("Load failed: " + e.getMessage());
        }
    }

    private void showStatus(String message) {
        this.statusMessage = message;
        this.statusMessageUntil = System.currentTimeMillis() + 3000;
    }

    public void keyTyped(char typedChar, int keyCode) {
        nameBox.keyTyped(typedChar, keyCode);
    }
}
