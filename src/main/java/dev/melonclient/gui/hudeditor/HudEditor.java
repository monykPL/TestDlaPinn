/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.gui.hudeditor;

import dev.melonclient.Melon;
import dev.melonclient.config.ConfigSaver;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.gui.hudeditor.impl.impl.*;
import dev.melonclient.gui.hudeditor.impl.impl.keystrokes.KeystrokesHud;
import dev.melonclient.gui.modmenu.ModMenu;
import dev.melonclient.helpers.ResolutionHelper;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.animation.Animate;
import dev.melonclient.helpers.animation.Easing;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

public class HudEditor extends GuiScreen {

    private final ArrayList<HudMod> hudModList = new ArrayList<>();

    private final Animate animateLogo = new Animate();
    private final Animate animateSnapping = new Animate();
    private final Animate animate = new Animate();

    // czy coś się przestawiło od ostatniego zapisu - żeby nie zapisywać pliku
    // co klatkę bez potrzeby, tylko wtedy gdy faktycznie trzeba
    private boolean dirty;

    private int counter;
    private int index;
    private final int offset;

    public HudEditor() {
        counter = 0;
        index = 10;
        offset = 10;
        init();
        animateLogo.setEase(Easing.CUBIC_OUT).setMin(0).setMax(70).setSpeed(100).setReversed(false);
        animateSnapping.setEase(Easing.CUBIC_OUT).setMin(0).setMax(50).setSpeed(100).setReversed(false);
        animate.setEase(Easing.LINEAR).setMin(0).setMax(25).setSpeed(200);
    }

    public void init() {
        addHudMod(new SprintHud("ToggleSprint", index, offset));
        addHudMod(new SneakHud("ToggleSneak", index, offset));
        addHudMod(new FpsHud("FPS", index, offset));
        addHudMod(new KeystrokesHud("Keystrokes", index, offset));
        addHudMod(new ArmorHud("Armor Status", index, offset));
        addHudMod(new CoordinatesHud("Coordinates", index, offset));
        addHudMod(new ServerAddressHud("Server Address", index, offset));
        addHudMod(new PingHud("Ping", index, offset));
        addHudMod(new CpsHud("CPS", index, offset));
        addHudMod(new PotionHud("Potion Status", index, offset));
        addHudMod(new TimeHud("Time", index, offset));
        addHudMod(new SpeedIndicatorHud("Speed Indicator", index, offset));
        addHudMod(new BlockinfoHud("BlockInfo", index, offset));
        addHudMod(new ReachdisplayHud("ReachDisplay", index, offset));
        addHudMod(new DayCounterHud("Day Counter", index, offset));
        addHudMod(new ScoreboardHud("Scoreboard", index, offset));
        addHudMod(new BossbarHud("Bossbar", index, offset));
        addHudMod(new DirectionHud("Direction", index, offset));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean roundedCorners = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();
        int color = Melon.INSTANCE.optionManager.getOptionByName("Color").getColor().getRGB();

        Helper2D.drawRectangle(0, 0, width, height, 0x70000000);

        animateLogo.update();
        GLHelper.startScissor(0, height / 2 - 78, width, 73);
        Melon.INSTANCE.fontHelper.size40.drawString(
                "MelonClient",
                width / 2f - Melon.INSTANCE.fontHelper.size40.getStringWidth("MelonClient") / 2f,
                height / 2f + 36 - animateLogo.getValueI(),
                color
        );
        Helper2D.drawPicture(
                width / 2 - 25,
                height / 2 - 8 - animateLogo.getValueI(),
                50, 50, Style.getColor(70).getRGB(), "melonlogo.png"
        );
        GLHelper.endScissor();

        animate.update().setReversed(!MathHelper.withinBox(width / 2 - 50, height / 2 - 6, 100, 20, mouseX, mouseY));

        Helper2D.drawRoundedRectangle(
                width / 2 - 50,
                height / 2 - 6,
                100, 20, 2,
                Style.getColor(animate.getValueI() + 30).getRGB(),
                roundedCorners ? 0 : -1
        );
        Melon.INSTANCE.fontHelper.size20.drawString(
                "Open Mods",
                width / 2f - Melon.INSTANCE.fontHelper.size20.getStringWidth("Open Mods") / 2f,
                height / 2f,
                color
        );

        for (HudMod hudMod : hudModList) {
            hudMod.renderMod(mouseX, mouseY);
            hudMod.updatePosition(mouseX, mouseY);

            if (hudMod.withinMod(mouseX, mouseY)) {
                int scroll = Mouse.getDWheel();
                if (scroll > 0 && hudMod.getSize() < 2) {
                    hudMod.setSize(hudMod.getSize() + 0.1f);
                    dirty = true;
                } else if (scroll < 0 && hudMod.getSize() > 0.5f) {
                    hudMod.setSize(hudMod.getSize() - 0.1f);
                    dirty = true;
                }
            }

            if (hudMod.isResizingViaHandle()) {
                hudMod.updateResize(mouseY);
                dirty = true;
            }

            if (hudMod.getX() < 0) {
                hudMod.setX(0);
            } else if (hudMod.getX() + hudMod.getW() * hudMod.getSize() > ResolutionHelper.getWidth()) {
                hudMod.setX((int) (ResolutionHelper.getWidth() - hudMod.getW() * hudMod.getSize()));
            }

            if (hudMod.getY() < 0) {
                hudMod.setY(0);
            } else if (hudMod.getY() + hudMod.getH() * hudMod.getSize() > ResolutionHelper.getHeight()) {
                hudMod.setY((int) (ResolutionHelper.getHeight() - hudMod.getH() * hudMod.getSize()));
            }

            for (HudMod sHudMod : hudModList) {
                if (
                        Melon.INSTANCE.modManager.getMod(sHudMod.getName()).isToggled() &&
                                hudMod.isDragging() &&
                                !sHudMod.equals(hudMod) &&
                                !sHudMod.equals(hudMod) &&
                                Style.isSnapping()
                ) {
                    SnapPosition snap = new SnapPosition();
                    snap.setSnapping(true);
                    int snapRange = 5;
                    if (MathHelper.withinBoundsRange(hudMod.getX(), sHudMod.getX(), snapRange))
                        snap.setAll(sHudMod.getX(), sHudMod.getX(), false);
                    else if (MathHelper.withinBoundsRange(hudMod.getX() + hudMod.getW() * hudMod.getSize(), sHudMod.getX() + sHudMod.getW() * sHudMod.getSize(), snapRange))
                        snap.setAll(sHudMod.getX() + sHudMod.getW() * sHudMod.getSize(), sHudMod.getX() + sHudMod.getW() * sHudMod.getSize() - hudMod.getW() * hudMod.getSize(), false);
                    else if (MathHelper.withinBoundsRange(hudMod.getX() + hudMod.getW() * hudMod.getSize(), sHudMod.getX(), snapRange))
                        snap.setAll(sHudMod.getX(), sHudMod.getX() - hudMod.getW() * hudMod.getSize(), false);
                    else if (MathHelper.withinBoundsRange(hudMod.getX(), sHudMod.getX() + sHudMod.getW() * sHudMod.getSize(), snapRange))
                        snap.setAll(sHudMod.getX() + sHudMod.getW() * sHudMod.getSize(), sHudMod.getX() + sHudMod.getW() * sHudMod.getSize(), false);
                    else if (MathHelper.withinBoundsRange(hudMod.getY(), sHudMod.getY(), snapRange))
                        snap.setAll(sHudMod.getY(), sHudMod.getY(), true);
                    else if (MathHelper.withinBoundsRange(hudMod.getY() + hudMod.getH() * hudMod.getSize(), sHudMod.getY() + sHudMod.getH() * sHudMod.getSize(), snapRange))
                        snap.setAll(sHudMod.getY() + sHudMod.getH() * sHudMod.getSize(), sHudMod.getY() + sHudMod.getH() * sHudMod.getSize() - hudMod.getH() * hudMod.getSize(), true);
                    else if (MathHelper.withinBoundsRange(hudMod.getY() + hudMod.getH() * hudMod.getSize(), sHudMod.getY(), snapRange))
                        snap.setAll(sHudMod.getY(), sHudMod.getY() - hudMod.getH() * hudMod.getSize(), true);
                    else if (MathHelper.withinBoundsRange(hudMod.getY(), sHudMod.getY() + sHudMod.getH() * sHudMod.getSize(), snapRange))
                        snap.setAll(sHudMod.getY() + sHudMod.getH() * sHudMod.getSize(), sHudMod.getY() + sHudMod.getH() * sHudMod.getSize(), true);
                    else
                        snap.setSnapping(false);

                    if (snap.isSnapping()) {
                        if (!snap.isHorizontal()) {
                            Helper2D.drawRectangle((int) snap.getsPos(), 0, 1, ResolutionHelper.getHeight(), 0x60ffffff);
                            hudMod.setX((int) snap.getPos());
                        } else {
                            Helper2D.drawRectangle(0, (int) snap.getsPos(), ResolutionHelper.getWidth(), 1, 0x60ffffff);
                            hudMod.setY((int) snap.getPos());
                        }
                    }
                }
            }
        }

        animateSnapping.update();
        Helper2D.drawRoundedRectangle(10, height - 50, 40, 40, 2, Style.getColor(40).getRGB(), roundedCorners ? 0 : -1);
        Helper2D.drawPicture(15, height - 45, 30, 30, color, Style.isDarkMode() ? "icon/dark.png" : "icon/light.png");
        Helper2D.drawRoundedRectangle(60, height - animateSnapping.getValueI(), 40, 40, 2, Style.getColor(40).getRGB(), roundedCorners ? 0 : -1);
        Helper2D.drawPicture(65, height + 5 - animateSnapping.getValueI(), 30, 30, color, Style.isSnapping() ? "icon/grid.png" : "icon/nogrid.png");
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (HudMod hudMod : hudModList) {
            if (mouseButton == 0) {
                if (hudMod.isOverResizeHandle(mouseX, mouseY)) {
                    hudMod.startResizeViaHandle(mouseY);
                } else if (hudMod.withinMod(mouseX, mouseY)) {
                    hudMod.setDragging(true);
                    hudMod.setOffsetX(mouseX - hudMod.getX());
                    hudMod.setOffsetY(mouseY - hudMod.getY());
                }
            } else if (mouseButton == 1 && Style.isSnapping() && hudMod.withinMod(mouseX, mouseY)) {
                if (Melon.INSTANCE.modManager.getMod(hudMod.getName()).isToggled()) {
                    Melon.INSTANCE.modManager.getMod(hudMod.getName()).toggle();
                    dirty = true;
                }
            }
        }

        if (mouseButton == 0) {
            if (MathHelper.withinBox(width / 2 - 50, height / 2 - 6, 100, 20, mouseX, mouseY)) {
                mc.displayGuiScreen(new ModMenu());
            }

            if (MathHelper.withinBox(10, height - 50, 40, 40, mouseX, mouseY)) {
                Style.setDarkMode(!Style.isDarkMode());
            } else if (MathHelper.withinBox(60, height - 50, 40, 40, mouseX, mouseY)) {
                Style.setSnapping(!Style.isSnapping());
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Tu jest właściwa naprawa problemu z niezapisującymi się pozycjami:
     * WCZEŚNIEJ zapis do pliku działał TYLKO przy eleganckim zamknięciu gry
     * (shutdown hook) - jeśli gra się zawiesiła / crashnęła / została zabita
     * procesem, wszystko co przestawiłeś w tej sesji przepadało.
     *
     * Teraz zapisujemy OD RAZU po puszczeniu przycisku myszy, jeśli coś się
     * faktycznie przestawiło (przeciąganie pozycji, scroll-resize, resize
     * przez kwadracik, wyłączenie moda prawym klikiem) - więc nawet crash
     * zaraz potem już niczego nie skasuje.
     */
    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        boolean wasInteracting = false;
        for (HudMod hudMod : hudModList) {
            if (hudMod.isDragging() || hudMod.isResizingViaHandle()) {
                wasInteracting = true;
            }
            hudMod.setDragging(false);
            hudMod.stopResizeViaHandle();
        }

        if (wasInteracting) {
            dirty = true;
        }

        if (dirty) {
            saveQuietly();
            dirty = false;
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Dodatkowa siatka bezpieczeństwa - zapisz też przy zamknięciu edytora,
     * na wypadek gdyby coś się zmieniło w sposób nieobsłużony przez mouseReleased.
     */
    @Override
    public void onGuiClosed() {
        if (dirty) {
            saveQuietly();
            dirty = false;
        }
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        super.onGuiClosed();
    }

    private void saveQuietly() {
        try {
            ConfigSaver.saveConfig();
        } catch (Exception e) {
            System.out.println("[MelonClient] Nie udało się zapisać configu: " + e.getMessage());
        }
    }

    @Override
    public void initGui() {
        dev.melonclient.feature.option.Option blurOption = Melon.INSTANCE.optionManager.getOptionByName("Menu Blur");
        if (blurOption == null || blurOption.isCheckToggled()) {
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
        animateLogo.reset();
        animateSnapping.reset();
        super.initGui();
    }

    public ArrayList<HudMod> getHudMods() {
        return hudModList;
    }

    public void addHudMod(HudMod hudMod) {
        if (counter % 5 == 0) {
            index = 10;
        }
        hudModList.add(hudMod);
        index += hudMod.getW() + offset;
        counter++;
    }

    public HudMod getHudMod(String name) {
        for (HudMod hudMod : hudModList) {
            if (hudMod.getName().equalsIgnoreCase(name)) {
                return hudMod;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (Keyboard.isKeyDown(Melon.INSTANCE.optionManager.getOptionByName("ModMenu Keybinding").getKey())) {
            Melon.INSTANCE.mc.displayGuiScreen(Melon.INSTANCE.hudEditor);
        }
    }
}
