package dev.melonclient.gui.titlescreen;

import dev.melonclient.Melon;
import dev.melonclient.gui.modmenu.ModMenu;
import dev.melonclient.gui.titlescreen.buttons.IconButton;
import dev.melonclient.gui.titlescreen.buttons.TextButton;
import dev.melonclient.helpers.font.GlyphPageFontRenderer;
import dev.melonclient.helpers.render.Helper2D;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;

import java.io.IOException;
import java.util.ArrayList;

public class TitleScreen extends Panorama {

    private final ArrayList<TextButton> textButtons = new ArrayList<>();
    private final ArrayList<IconButton> iconButtons = new ArrayList<>();

    public TitleScreen() {
        textButtons.add(new TextButton("Singleplayer", width / 2 - 75, height / 2));
        textButtons.add(new TextButton("Multiplayer", width / 2 - 75, height / 2 + 25));
        textButtons.add(new TextButton("Settings", width / 2 - 75, height / 2 + 50));

        // górny prawy róg - X (wysuwa się i pokazuje "Quit") + pędzel obok (otwiera menu panoram)
        iconButtons.add(new IconButton("cross.png", width - 25, 5, "Quit", false));
        iconButtons.add(new IconButton("brush.png", width - 51, 5));

        // dolny środek - koszulka (kosmetyki), logo klienta (mod menu), ludzik (account switcher)
        int bottomStartX = bottomRowStartX();
        iconButtons.add(new IconButton("shirt.png", bottomStartX, height - 25, "Kosmetyki", true));
        iconButtons.add(new IconButton("clientlogo.png", bottomStartX + 26, height - 25, "Client Settings", true));
        iconButtons.add(new IconButton("person.png", bottomStartX + 52, height - 25, "Account switcher", true));
    }

    /**
     * Liczy X startowe dla 3 dolnych ikon (20px + 6px odstępu), tak żeby cała
     * grupa była wyśrodkowana na dole ekranu.
     */
    private int bottomRowStartX() {
        int groupWidth = 3 * 20 + 2 * 6; // 3 przyciski, 2 odstępy po 6px
        return width / 2 - groupWidth / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int y = 0;
        for (TextButton textButton : textButtons) {
            textButton.renderButton(width / 2 - 75, height / 2 + y * 25, mouseX, mouseY);
            y++;
        }

        IconButton crossButton = null;
        IconButton brushButton = null;
        int bottomStartX = bottomRowStartX();

        for (IconButton iconButton : iconButtons) {
            switch (iconButton.getIcon()) {
                case "cross.png":
                    iconButton.renderButton(width - 25, 5, mouseX, mouseY);
                    crossButton = iconButton;
                    break;
                case "brush.png":
                    // renderowany na końcu - jego pozycja zależy od aktualnej szerokości X
                    brushButton = iconButton;
                    break;
                case "shirt.png":
                    iconButton.renderButton(bottomStartX, height - 25, mouseX, mouseY);
                    break;
                case "clientlogo.png":
                    iconButton.renderButton(bottomStartX + 26, height - 25, mouseX, mouseY);
                    break;
                case "person.png":
                    iconButton.renderButton(bottomStartX + 52, height - 25, mouseX, mouseY);
                    break;
            }
        }

        // pędzel zawsze tuż przy lewej krawędzi X - gdy X się wysuwa, pędzel przesuwa się razem z nim
        if (crossButton != null && brushButton != null) {
            int gap = 4;
            int brushX = crossButton.getX() - gap - brushButton.getW();
            brushButton.renderButton(brushX, 5, mouseX, mouseY);
        }

        drawLogo();
        drawCopyright();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (TextButton textButton : textButtons) {
            if (textButton.isHovered(mouseX, mouseY)) {
                switch (textButton.getText()) {
                    case "Singleplayer":
                        mc.displayGuiScreen(new GuiSelectWorld(this));
                        break;
                    case "Multiplayer":
                        mc.displayGuiScreen(new GuiMultiplayer(this));
                        break;
                    case "Settings":
                        mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                        break;
                }
            }
        }

        for (IconButton iconButton : iconButtons) {
            if (iconButton.isHovered(mouseX, mouseY)) {
                switch (iconButton.getIcon()) {
                    case "cross.png":
                        mc.shutdown();
                        break;
                    case "brush.png":
                        mc.displayGuiScreen(new PanoramaMenu(this));
                        break;
                    case "clientlogo.png":
                        mc.displayGuiScreen(new ModMenu());
                        break;
                    // "shirt.png", "person.png" - na razie bez akcji
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void drawLogo() {
        GlyphPageFontRenderer fontRenderer = Melon.INSTANCE.fontHelper.size40;
        fontRenderer.drawString("MelonClient", width / 2f - fontRenderer.getStringWidth("MelonClient") / 2f, height / 2f - 27.5f, -1);
        Helper2D.drawPicture(width / 2 - 30, height / 2 - 98, 60, 60, -1, "melonlogo.png");
    }

    private void drawCopyright() {
        GlyphPageFontRenderer fontRenderer = Melon.INSTANCE.fontHelper.size20;
        String copyright = "Copyright Mojang Studios. Do not distribute!";
        String text = "MelonClient " + Melon.modVersion;
        fontRenderer.drawString(copyright, width - fontRenderer.getStringWidth(copyright) - 2, height - fontRenderer.getFontHeight(), 0x50ffffff);
        fontRenderer.drawString(text, 4, height - fontRenderer.getFontHeight(), 0x50ffffff);
    }
}

