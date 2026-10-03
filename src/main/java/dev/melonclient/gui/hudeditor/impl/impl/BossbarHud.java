package dev.melonclient.gui.hudeditor.impl.impl;

import dev.melonclient.Melon;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.boss.BossStatus;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BossbarHud extends HudMod {

    public BossbarHud(String name, int x, int y) {
        super(name, x, y);
    }

    @Override
    public void renderMod(int mouseX, int mouseY) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled()) {
            renderBossHealthPlaceHolder();
            super.renderMod(mouseX, mouseY);
        }
        GLHelper.endScale();
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Pre.Text e) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled() && !(Melon.INSTANCE.mc.currentScreen instanceof HudEditor)) {
            if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
                renderBossHealth();
            }
        }
        GLHelper.endScale();
    }

    private void renderBossHealth() {
        BossStatus.statusBarTime--;
        int x = getX();
        int y = getY();
        int width = 182;
        int health = (int) (BossStatus.healthScale * (float) (width + 1));

        Melon.INSTANCE.mc.getTextureManager().bindTexture(Gui.icons);
        Helper2D.drawTexturedModalRect(x, y + 10, 0, 74, width, 5);
        Helper2D.drawTexturedModalRect(x, y + 10, 0, 74, width, 5);

        if (health > 0) {
            Helper2D.drawTexturedModalRect(x, y + 10, 0, 79, health, 5);
        }

        String s = BossStatus.bossName;
        Melon.INSTANCE.mc.fontRendererObj.drawStringWithShadow(s, x + width / 2f - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(s) / 2f, y, 16777215);

        setW(width);
        setH(15);
    }

    private void renderBossHealthPlaceHolder() {
        int x = getX();
        int y = getY();
        int width = 182;
        int health = 100;

        Melon.INSTANCE.mc.getTextureManager().bindTexture(Gui.icons);
        Helper2D.drawTexturedModalRect(x, y + 10, 0, 74, width, 5);
        Helper2D.drawTexturedModalRect(x, y + 10, 0, 74, width, 5);
        Helper2D.drawTexturedModalRect(x, y + 10, 0, 79, health, 5);

        String s = "BossBar";
        Melon.INSTANCE.mc.fontRendererObj.drawStringWithShadow(s, x + width / 2f - Melon.INSTANCE.mc.fontRendererObj.getStringWidth(s) / 2f, y, 16777215);

        setW(width);
        setH(15);
    }

    private boolean isModern() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Mode").getCurrentMode().equalsIgnoreCase("Modern");
    }
}

