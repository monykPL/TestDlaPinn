package dev.melonclient.gui.titlescreen.buttons;

import dev.melonclient.Melon;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.animation.Animate;
import dev.melonclient.helpers.animation.Easing;

import java.awt.*;

public class IconButton {

    private static final Color COLOR_IDLE = new Color(35, 35, 38, 190);
    private static final Color COLOR_HOVER = new Color(255, 255, 255, 235);
    private static final Color TOOLTIP_BG = new Color(20, 20, 20, 230);

    private final Animate fillAnimate = new Animate();
    private final Animate slideAnimate = new Animate();

    private final String icon;
    private final String label;   // wysuwana etykieta w tle przycisku (np. X -> "Quit"); null = brak
    private final String tooltip; // dymek POKAZYWANY NAD przyciskiem po najechaniu; null = brak
    private int x, y;
    private final int w, h;
    private int extraW;      // dodatkowa szerokość, gdy etykieta jest w pełni wysunięta
    private int anchorRightX; // STAŁA prawa krawędź przycisku - ustalana raz na klatkę w renderButton,
                               // używana też przez isHovered(), żeby hitbox zawsze pokrywał się z rysunkiem

    /** Zwykły przycisk ikony - tylko podświetlenie na hover, bez etykiety i bez dymka. */
    public IconButton(String icon, int x, int y) {
        this(icon, x, y, null, false);
    }

    /**
     * @param text      treść etykiety/dymka (może być null)
     * @param asTooltip true = text renderowany jako dymek NAD przyciskiem (np. dolne ikony);
     *                  false = text renderowany jako wysuwana etykieta W przycisku (np. X)
     */
    public IconButton(String icon, int x, int y, String text, boolean asTooltip) {
        this.icon = icon;
        this.label = asTooltip ? null : text;
        this.tooltip = asTooltip ? text : null;
        this.x = x;
        this.y = y;
        this.w = 20;
        this.h = 20;
        this.anchorRightX = x + w;
        fillAnimate.setEase(Easing.LINEAR).setMin(0).setMax(25).setSpeed(200);
        slideAnimate.setEase(Easing.LINEAR).setMin(0).setMax(25).setSpeed(140);
    }

    public void renderButton(int x, int y, int mouseX, int mouseY) {
        this.y = y;
        this.anchorRightX = x + w; // stała kotwica prawej krawędzi na tę klatkę

        if (label != null && extraW == 0) {
            extraW = Melon.INSTANCE.fontHelper.size20.getStringWidth(label) + 16;
        }

        float slideT = slideAnimate.getValueF() / slideAnimate.getMax();
        int totalW = w + (int) (extraW * slideT);

        this.x = anchorRightX - totalW;

        boolean hovered = MathHelper.withinBox(this.x, y, totalW, h, mouseX, mouseY);
        fillAnimate.update().setReversed(!hovered);
        slideAnimate.update().setReversed(!hovered);
        float fillT = fillAnimate.getValueF() / fillAnimate.getMax();

        boolean rounded = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();
        int radius = 4;

        Helper2D.drawRoundedRectangle(this.x, y, totalW, h, radius, COLOR_IDLE.getRGB(), rounded ? 0 : -1);

        if (fillT > 0f) {
            int fillW = (int) (totalW * fillT);
            int fillH = (int) (h * fillT);
            int fillX = this.x + (totalW - fillW) / 2;
            int fillY = y + (h - fillH) / 2;

            Helper2D.drawRoundedRectangle(
                    fillX, fillY, fillW, fillH,
                    Math.min(radius, Math.min(fillW, fillH) / 2),
                    lerpColor(COLOR_IDLE, COLOR_HOVER, fillT),
                    rounded ? 0 : -1
            );
        }

        if (label != null && slideT > 0.05f) {
            int textColor = lerpColor(Color.WHITE, new Color(20, 20, 20), fillT);
            Melon.INSTANCE.fontHelper.size20.drawString(label, this.x + 8, y + h / 2f - 4, textColor);
        }

        // ikona zawsze przy prawej krawędzi, niezależnie od animacji wysuwania
        Helper2D.drawPicture(anchorRightX - w, y, w, h, 0xffffffff, "icon/" + icon);

        // dymek z opisem NAD przyciskiem - używany zamiast wysuwanej etykiety
        if (tooltip != null && hovered) {
            int tw = Melon.INSTANCE.fontHelper.size20.getStringWidth(tooltip);
            int bubbleW = tw + 12;
            int bubbleH = 16;
            int bubbleX = this.x + (totalW - bubbleW) / 2;
            int bubbleY = y - bubbleH - 6;

            Helper2D.drawRoundedRectangle(bubbleX, bubbleY, bubbleW, bubbleH, 3, TOOLTIP_BG.getRGB(), rounded ? 0 : -1);
            Melon.INSTANCE.fontHelper.size20.drawString(tooltip, bubbleX + 6, bubbleY + 4, -1);
        }
    }

    /**
     * Używa TEJ SAMEJ kotwicy (anchorRightX) co renderButton, więc hitbox zawsze
     * dokładnie pokrywa się z tym, co faktycznie widać na ekranie - niezależnie
     * od tego, czy przycisk jest w danej chwili wysunięty, czy nie.
     */
    public boolean isHovered(int mouseX, int mouseY) {
        float slideT = slideAnimate.getValueF() / slideAnimate.getMax();
        int totalW = w + (int) (extraW * slideT);
        return MathHelper.withinBox(anchorRightX - totalW, y, totalW, h, mouseX, mouseY);
    }

    private static int lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al).getRGB();
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getIcon() {
        return icon;
    }
}

