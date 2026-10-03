package dev.melonclient.helpers.font;

import dev.melonclient.Melon;

public class FontHelper {

    private String font;

    public GlyphPageFontRenderer size15;
    public GlyphPageFontRenderer size20;
    public GlyphPageFontRenderer size30;
    public GlyphPageFontRenderer size40;

    public void init() {
        font = Melon.INSTANCE.optionManager.getOptionByName("Font Changer").getCurrentMode();
        size15 = GlyphPageFontRenderer.create(font, 15, true, true, true);
        size20 = GlyphPageFontRenderer.create(font, 20, true, true, true);
        size30 = GlyphPageFontRenderer.create(font, 30, true, true, true);
        size40 = GlyphPageFontRenderer.create(font, 40, true, true, true);
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }
}

