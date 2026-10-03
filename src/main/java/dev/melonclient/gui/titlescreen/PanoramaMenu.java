package dev.melonclient.gui.titlescreen;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.animation.Animate;
import dev.melonclient.helpers.animation.Easing;
import dev.melonclient.helpers.render.Helper2D;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Puste menu otwierane pędzelkiem obok X. Na środku pokazuje 4 motywy tła
 * (panoramy) do wyboru oraz przełącznik blura tła w menu głównym.
 *
 * WAŻNE: czcionka tego moda (GlyphPage) ma wygenerowane glify TYLKO dla
 * znaków o kodach 0-255 (zwykły ASCII/Latin-1). Nie wolno tu używać ani
 * symboli Unicode (np. ptaszków "✓"), ani polskich znaków z ogonkami
 * (ą, ć, ę, ł, ń, ó, ś, ź, ż) - font_helper wywali IllegalArgumentException
 * i wykrzaczy grę. Stąd angielskie etykiety i ramka zamiast checkmarka.
 */
public class PanoramaMenu extends Panorama {

    private static final Color COLOR_IDLE = new Color(35, 35, 38, 190);
    private static final Color COLOR_HOVER = new Color(255, 255, 255, 235);
    private static final Color SELECTED_BORDER = new Color(255, 255, 255, 255);

    private static final int SLOT_SIZE = 64;
    private static final int SLOT_GAP = 16;

    private final GuiScreen parentScreen;
    private final ArrayList<ThemeSlot> slots = new ArrayList<>();

    private int checkboxX, checkboxY;
    private static final int CHECKBOX_SIZE = 14;

    public PanoramaMenu(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;

        // "available = false" = na razie brak grafik, kliknięcie nic nie robi (ale się podświetla)
        slots.add(new ThemeSlot("Winter", "Winter", "menu/preview_winter.png", true));
        slots.add(new ThemeSlot("Normal", "Normal", "menu/preview_normal.png", false));
        slots.add(new ThemeSlot("Summer", "Summer", "menu/preview_summer.png", false));
        slots.add(new ThemeSlot("Vanilla", "Vanilla", "menu/preview_vanilla.png", true));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Option themeOption = Melon.INSTANCE.optionManager.getOptionByName("Panorama Theme");
        String currentTheme = themeOption != null ? themeOption.getCurrentMode() : "Winter";

        int groupWidth = slots.size() * SLOT_SIZE + (slots.size() - 1) * SLOT_GAP;
        int startX = width / 2 - groupWidth / 2;
        int slotY = height / 2 - 90;

        boolean rounded = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();

        for (int i = 0; i < slots.size(); i++) {
            ThemeSlot slot = slots.get(i);
            slot.x = startX + i * (SLOT_SIZE + SLOT_GAP);
            slot.y = slotY;
            slot.render(mouseX, mouseY, rounded, slot.mode.equalsIgnoreCase(currentTheme));
        }

        // przełącznik blura, wyśrodkowany pod rzędem motywów - "Background Blur", bez polskich znakow
        String label = "Background Blur";
        int labelWidth = Melon.INSTANCE.fontHelper.size20.getStringWidth(label);
        int rowWidth = CHECKBOX_SIZE + 6 + labelWidth;
        checkboxX = width / 2 - rowWidth / 2;
        checkboxY = slotY + SLOT_SIZE + 26;

        Option blurOption = Melon.INSTANCE.optionManager.getOptionByName("Background Blur");
        boolean blurOn = blurOption != null && blurOption.isCheckToggled();

        Helper2D.drawRoundedRectangle(checkboxX, checkboxY, CHECKBOX_SIZE, CHECKBOX_SIZE, 2,
                (blurOn ? COLOR_HOVER : COLOR_IDLE).getRGB(), rounded ? 0 : -1);
        Melon.INSTANCE.fontHelper.size20.drawString(label, checkboxX + CHECKBOX_SIZE + 6, checkboxY - 1, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        Option themeOption = Melon.INSTANCE.optionManager.getOptionByName("Panorama Theme");

        for (ThemeSlot slot : slots) {
            if (slot.available && MathHelper.withinBox(slot.x, slot.y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                if (themeOption != null) {
                    themeOption.setCurrentMode(slot.mode);
                }
            }
        }

        if (MathHelper.withinBox(checkboxX, checkboxY, CHECKBOX_SIZE, CHECKBOX_SIZE, mouseX, mouseY)) {
            Option blurOption = Melon.INSTANCE.optionManager.getOptionByName("Background Blur");
            if (blurOption != null) {
                blurOption.setCheckToggled(!blurOption.isCheckToggled());
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Jeden kafelek motywu: podglądowy obrazek (na razie placeholder, dopóki
     * nie wgrasz grafik), podświetlenie na hover, ramka wokół aktualnie
     * wybranego motywu i podpis pod spodem.
     */
    private static class ThemeSlot {

        final String label;
        final String mode;         // musi się zgadzać z wartością w Option("Panorama Theme", ...)
        final String previewImage; // ścieżka pod assets/<modid>/, np. "menu/preview_winter.png"
        final boolean available;   // false = grafiki jeszcze nie ma, klik nic nie robi

        final Animate fillAnimate = new Animate();
        int x, y;

        ThemeSlot(String label, String mode, String previewImage, boolean available) {
            this.label = label;
            this.mode = mode;
            this.previewImage = previewImage;
            this.available = available;
            fillAnimate.setEase(Easing.LINEAR).setMin(0).setMax(25).setSpeed(200);
        }

        void render(int mouseX, int mouseY, boolean rounded, boolean selected) {
            boolean hovered = available && MathHelper.withinBox(x, y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY);
            fillAnimate.update().setReversed(!hovered);
            float fillT = fillAnimate.getValueF() / fillAnimate.getMax();

            // slot bez gotowej grafiki jest lekko przygaszony, żeby było widać że "jeszcze nie ma"
            int baseAlpha = available ? 190 : 90;
            Color idle = new Color(35, 35, 38, baseAlpha);

            Helper2D.drawRoundedRectangle(x, y, SLOT_SIZE, SLOT_SIZE, 5, idle.getRGB(), rounded ? 0 : -1);

            if (fillT > 0f) {
                int fillW = (int) (SLOT_SIZE * fillT);
                int fillH = (int) (SLOT_SIZE * fillT);
                int fillX = x + (SLOT_SIZE - fillW) / 2;
                int fillY = y + (SLOT_SIZE - fillH) / 2;
                Helper2D.drawRoundedRectangle(fillX, fillY, fillW, fillH, Math.min(5, Math.min(fillW, fillH) / 2),
                        lerpColor(idle, COLOR_HOVER, fillT), rounded ? 0 : -1);
            }

            // podgląd - dopóki nie dodasz pliku, silnik pokaże swój domyślny brakujący tekstur (fioletowo-czarna krata)
            Helper2D.drawPicture(x + 4, y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8, -1, previewImage);

            // zaznaczenie wybranego motywu - RAMKA zamiast tekstowego checkmarka
            // (czcionka moda nie ma glifu dla "✓", więc rysujemy cienką obwódkę)
            if (selected) {
                int border = SELECTED_BORDER.getRGB();
                Helper2D.drawRectangle(x, y, SLOT_SIZE, 2, border);                     // góra
                Helper2D.drawRectangle(x, y + SLOT_SIZE - 2, SLOT_SIZE, 2, border);      // dół
                Helper2D.drawRectangle(x, y, 2, SLOT_SIZE, border);                     // lewo
                Helper2D.drawRectangle(x + SLOT_SIZE - 2, y, 2, SLOT_SIZE, border);      // prawo
            }

            int labelWidth = Melon.INSTANCE.fontHelper.size20.getStringWidth(label);
            Melon.INSTANCE.fontHelper.size20.drawString(label, x + SLOT_SIZE / 2f - labelWidth / 2f, y + SLOT_SIZE + 4, -1);
        }

        private static int lerpColor(Color a, Color b, float t) {
            t = Math.max(0f, Math.min(1f, t));
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
            return new Color(r, g, bl, al).getRGB();
        }
    }
}

