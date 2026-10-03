/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.gui.hudeditor.impl;

import dev.melonclient.config.ConfigSaver;
import dev.melonclient.Melon;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.ResolutionHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.gui.Style;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class HudMod {

    private static final int HANDLE_SIZE = 8;

    private static long lastAutoSave = 0;
    private static final long AUTO_SAVE_COOLDOWN_MS = 3000;

    private String name;
    private int x, y, w, h;
    private int offsetX, offsetY;
    private boolean dragging;
    private float size;

    private boolean resizingViaHandle;
    private int resizeAnchorMouseY;
    private float resizeAnchorSize;

    // NOWE: pozycja zapamiętana jako procent (0-1) szerokości/wysokości ekranu,
    // niezależnie od surowych pikseli w x/y. Dzięki temu element zostaje w tym
    // samym WZGLĘDNYM miejscu na ekranie, nawet gdy F11/zmiana GUI Scale/zmiana
    // rozmiaru okna zmieni rzeczywistą rozdzielczość w pikselach - wcześniej
    // x/y trzymały tylko surowe piksele, więc "x=500" po zmianie rozdzielczości
    // ląduje w zupełnie innym miejscu względem krawędzi ekranu.
    private float xPercent = -1f;
    private float yPercent = -1f;
    private int lastScreenW = -1;
    private int lastScreenH = -1;

    public HudMod(String name, int x, int y) {
        MinecraftForge.EVENT_BUS.register(this);
        this.name = name;
        this.x = x;
        this.y = y;
        this.size = 1;
    }

    public void renderMod(int mouseX, int mouseY) {
        boolean hovered = withinMod(mouseX, mouseY);
        if (hovered || isDragging()) {
            Helper2D.drawOutlinedRectangle(getX() - 3, getY() - 3, getW() + 6, getH() + 6, 1, -1);
            Melon.INSTANCE.fontHelper.size20.drawString("Size: " + MathHelper.round(getSize(), 1),
                    getX() + getW() / 2f - Melon.INSTANCE.fontHelper.size20.getStringWidth("Size: " + MathHelper.round(getSize(), 1)) / 2f,
                    getY() + getH() + 10, -1
            );
        }

        if (Style.isSnapping() && (hovered || resizingViaHandle)) {
            for (int[] corner : getHandleCorners(false)) {
                boolean handleHovered = isOverResizeHandle(mouseX, mouseY);
                int color = (handleHovered || resizingViaHandle) ? -1 : 0xaaffffff;
                Helper2D.drawRectangle(corner[0], corner[1], HANDLE_SIZE, HANDLE_SIZE, color);
            }
        }
    }

    private int[][] getHandleCorners(boolean scaled) {
        int boxW = scaled ? (int) (w * size) : w;
        int boxH = scaled ? (int) (h * size) : h;
        int half = HANDLE_SIZE / 2;
        return new int[][]{
                {x - half, y - half},
                {x + boxW - half, y - half},
                {x - half, y + boxH - half},
                {x + boxW - half, y + boxH - half}
        };
    }

    public boolean isOverResizeHandle(int mouseX, int mouseY) {
        if (!Style.isSnapping()) {
            return false;
        }
        for (int[] corner : getHandleCorners(true)) {
            if (MathHelper.withinBox(corner[0], corner[1], HANDLE_SIZE, HANDLE_SIZE, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public void startResizeViaHandle(int mouseY) {
        this.resizingViaHandle = true;
        this.resizeAnchorMouseY = mouseY;
        this.resizeAnchorSize = this.size;
    }

    public void updateResize(int mouseY) {
        if (!resizingViaHandle) {
            return;
        }
        float delta = (resizeAnchorMouseY - mouseY) / 100f;
        float newSize = resizeAnchorSize + delta;
        if (newSize < 0.5f) newSize = 0.5f;
        if (newSize > 2f) newSize = 2f;
        this.size = newSize;
    }

    public void stopResizeViaHandle() {
        this.resizingViaHandle = false;
    }

    public boolean isResizingViaHandle() {
        return resizingViaHandle;
    }

    public boolean withinMod(int mouseX, int mouseY) {
        return MathHelper.withinBox(x, y, (int) (w * size), (int) (h * size), mouseX, mouseY);
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (isDragging()) {
            setX(mouseX - offsetX);
            setY(mouseY - offsetY);
        }
    }

    /**
     * Zapisuje AKTUALNĄ pozycję pikselową jako procent bieżącej rozdzielczości.
     * Wołane co tick, kiedy NIE trwa przeciąganie/resize - czyli zaraz po tym
     * jak użytkownik puści przycisk myszy, jego nowa pozycja staje się nowym
     * "punktem odniesienia" w procentach.
     */
    private void syncPercentFromPixels(int screenW, int screenH) {
        if (screenW > 0 && screenH > 0) {
            xPercent = (float) x / screenW;
            yPercent = (float) y / screenH;
        }
    }

    /**
     * Trzyma moda w tym samym WZGLĘDNYM miejscu na ekranie niezależnie od F11,
     * zmiany GUI Scale, czy zmiany rozmiaru okna - i dodatkowo, jako
     * zabezpieczenie, nie pozwala mu wystawać poza ekran (zmniejszając go,
     * jeśli w ogóle by się nie zmieścił). Jeśli coś faktycznie się zmieniło,
     * zapisuje to na dysk (z throttlingiem 3 sekundy).
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        int screenW = ResolutionHelper.getWidth();
        int screenH = ResolutionHelper.getHeight();
        if (screenW <= 0 || screenH <= 0 || w <= 0 || h <= 0) return;

        boolean changed = false;
        boolean interacting = dragging || resizingViaHandle;

        if (xPercent < 0 || yPercent < 0) {
            syncPercentFromPixels(screenW, screenH);
        }

        boolean resolutionChanged = (screenW != lastScreenW || screenH != lastScreenH) && lastScreenW > 0;

        if (resolutionChanged && !interacting) {
            x = Math.round(xPercent * screenW);
            y = Math.round(yPercent * screenH);
            changed = true;
        }

        lastScreenW = screenW;
        lastScreenH = screenH;

        float maxSizeForWidth = (float) screenW / w;
        float maxSizeForHeight = (float) screenH / h;
        float maxAllowedSize = Math.min(maxSizeForWidth, maxSizeForHeight);
        if (maxAllowedSize < 0.1f) maxAllowedSize = 0.1f;

        if (size > maxAllowedSize) {
            size = maxAllowedSize;
            changed = true;
        }

        int scaledW = (int) (w * size);
        int scaledH = (int) (h * size);

        if (!interacting) {
            if (x < 0) {
                x = 0;
                changed = true;
            } else if (x + scaledW > screenW) {
                x = Math.max(0, screenW - scaledW);
                changed = true;
            }

            if (y < 0) {
                y = 0;
                changed = true;
            } else if (y + scaledH > screenH) {
                y = Math.max(0, screenH - scaledH);
                changed = true;
            }

            syncPercentFromPixels(screenW, screenH);
        }

        if (changed) {
            long now = System.currentTimeMillis();
            if (now - lastAutoSave > AUTO_SAVE_COOLDOWN_MS) {
                lastAutoSave = now;
                try {
                    ConfigSaver.saveConfig();
                } catch (Exception ex) {
                    System.out.println("[MelonClient] Nie udalo sie zapisac configu po zmianie rozdzielczosci: " + ex.getMessage());
                }
            }
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
