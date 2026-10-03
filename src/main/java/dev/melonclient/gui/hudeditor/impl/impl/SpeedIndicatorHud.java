package dev.melonclient.gui.hudeditor.impl.impl;

import dev.melonclient.Melon;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpeedIndicatorHud extends HudMod {

    public SpeedIndicatorHud(String name, int x, int y) {
        super(name, x, y);
        setW(60);
        setH(20);
    }

    @Override
    public void renderMod(int mouseX, int mouseY) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled()) {
            if (isModern()) {
                if (isBackground()) {
                    Helper2D.drawRoundedRectangle(getX(), getY(), getW(), getH(), 2, Style.getColor(50).getRGB(), 0);
                }
                Melon.INSTANCE.fontHelper.size20.drawString(
                        getMeterPerSecond() + " m/s",
                        getX() + getW() / 2f - (Melon.INSTANCE.fontHelper.size20.getStringWidth((getMeterPerSecond() + " m/s")) / 2f),
                        getY() + 6,
                        getColor()
                );
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), Style.getColor(50).getRGB());
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(
                        getMeterPerSecond() + " m/s",
                        getX() + getW() / 2 - (Melon.INSTANCE.mc.fontRendererObj.getStringWidth(getMeterPerSecond() + " m/s")) / 2,
                        getY() + 6,
                        getColor()
                );
            }
            super.renderMod(mouseX, mouseY);
        }
        GLHelper.endScale();
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Pre.Text e) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled() && !(Melon.INSTANCE.mc.currentScreen instanceof HudEditor)) {
            if (isModern()) {
                if (isBackground()) {
                    Helper2D.drawRoundedRectangle(getX(), getY(), getW(), getH(), 2, 0x50000000, 0);
                }
                Melon.INSTANCE.fontHelper.size20.drawString(
                        getMeterPerSecond() + " m/s",
                        getX() + getW() / 2f - (Melon.INSTANCE.fontHelper.size20.getStringWidth((getMeterPerSecond() + " m/s")) / 2f),
                        getY() + 6,
                        getColor()
                );
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), 0x50000000);
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(
                        getMeterPerSecond() + " m/s",
                        getX() + getW() / 2 - (Melon.INSTANCE.mc.fontRendererObj.getStringWidth(getMeterPerSecond() + " m/s")) / 2,
                        getY() + 6,
                        getColor()
                );
            }
        }
        GLHelper.endScale();
    }

    private double getMeterPerSecond() {
        double x = Melon.INSTANCE.mc.thePlayer.posX - Melon.INSTANCE.mc.thePlayer.prevPosX;
        double z = Melon.INSTANCE.mc.thePlayer.posZ - Melon.INSTANCE.mc.thePlayer.prevPosZ;
        double speed = net.minecraft.util.MathHelper.sqrt_double(x * x + z * z) * 20;
        return MathHelper.round(speed, 1);
    }

    private int getColor() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Font Color").getColor().getRGB();
    }

    private boolean isModern() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Mode").getCurrentMode().equalsIgnoreCase("Modern");
    }

    private boolean isBackground() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Background").isCheckToggled();
    }
}
