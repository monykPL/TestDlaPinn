package dev.melonclient.gui.titlescreen.buttons;

import dev.melonclient.Melon;
import dev.melonclient.helpers.MathHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.animation.Animate;
import dev.melonclient.helpers.animation.Easing;

import java.awt.*;

public class TextButton {

    // kolor spoczynkowy (szaro-czarny) i docelowy po najechaniu (biały)
    private static final Color COLOR_IDLE = new Color(35, 35, 38, 190);
    private static final Color COLOR_HOVER = new Color(255, 255, 255, 235);

    private final Animate animate = new Animate();

    private final String text;
    private int x, y;
    private final int w, h;

    public TextButton(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.w = 150;
        this.h = 20;
        animate.setEase(Easing.LINEAR).setMin(0).setMax(25).setSpeed(160);
    }

    /**
     * Renderuje przycisk z zaokrąglonymi rogami i animacją wypełnienia
     * rosnącą od środka (szaro-czarny -> biały) przy najechaniu myszką
     *
     * @param mouseX Aktualna pozycja X myszki
     * @param mouseY Aktualna pozycja Y myszki
     */
    public void renderButton(int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;

        boolean hovered = isHovered(mouseX, mouseY);
        animate.update().setReversed(!hovered);
        float t = animate.getValueF() / animate.getMax(); // 0 -> 1

        boolean rounded = Melon.INSTANCE.optionManager.getOptionByName("Rounded Corners").isCheckToggled();

        // tło - zawsze widoczne, ciemne, zaokrąglone
        Helper2D.drawRoundedRectangle(x, y, w, h, 4, COLOR_IDLE.getRGB(), rounded ? 0 : -1);

        // wypełnienie rosnące od środka na zewnątrz, kolor przechodzi w biel
        if (t > 0f) {
            int fillW = (int) (w * t);
            int fillH = (int) (h * t);
            int fillX = x + (w - fillW) / 2;
            int fillY = y + (h - fillH) / 2;

            Helper2D.drawRoundedRectangle(
                    fillX, fillY, fillW, fillH,
                    Math.min(4, Math.min(fillW, fillH) / 2),
                    lerpColor(COLOR_IDLE, COLOR_HOVER, t),
                    rounded ? 0 : -1
            );
        }

        // tekst ciemnieje w miarę jak tło robi się białe, żeby zawsze był czytelny
        int textColor = lerpColor(Color.WHITE, new Color(20, 20, 20), t);
        Melon.INSTANCE.fontHelper.size20.drawString(
                text,
                x + w / 2f - Melon.INSTANCE.fontHelper.size20.getStringWidth(text) / 2f,
                y + h / 2f - 4,
                textColor
        );
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return MathHelper.withinBox(x, y, w, h, mouseX, mouseY);
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

    public String getText() {
        return text;
    }
}

