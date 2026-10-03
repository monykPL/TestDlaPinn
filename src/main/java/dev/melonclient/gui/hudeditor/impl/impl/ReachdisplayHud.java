package dev.melonclient.gui.hudeditor.impl.impl;

import dev.melonclient.Melon;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ReachdisplayHud extends HudMod {

    private double range;

    public ReachdisplayHud(String name, int x, int y) {
        super(name, x, y);
        setW(80);
        setH(20);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (Melon.INSTANCE.mc.objectMouseOver != null &&
                Melon.INSTANCE.mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY)
        ) {
            Vec3 vec3 = Melon.INSTANCE.mc.getRenderViewEntity().getPositionEyes(1);
            range = Melon.INSTANCE.mc.objectMouseOver.hitVec.distanceTo(vec3);
        }
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
                        MathHelper.round(range, 2) + " Blocks",
                        getX() + getW() / 2f - (Melon.INSTANCE.fontHelper.size20.getStringWidth(MathHelper.round(range, 2) + " Blocks")) / 2f,
                        getY() + 6,
                        getColor()
                );
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), Style.getColor(50).getRGB());
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(
                        MathHelper.round(range, 2) + " Blocks",
                        getX() + getW() / 2 - (Melon.INSTANCE.mc.fontRendererObj.getStringWidth(MathHelper.round(range, 2) + " Blocks")) / 2,
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
                        MathHelper.round(range, 2) + " Blocks",
                        getX() + getW() / 2f - (Melon.INSTANCE.fontHelper.size20.getStringWidth(MathHelper.round(range, 2) + " Blocks")) / 2f,
                        getY() + 6,
                        getColor()
                );
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), 0x50000000);
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString(
                        MathHelper.round(range, 2) + " Blocks",
                        getX() + getW() / 2 - (Melon.INSTANCE.mc.fontRendererObj.getStringWidth(MathHelper.round(range, 2) + " Blocks")) / 2,
                        getY() + 6,
                        getColor()
                );
            }
        }
        GLHelper.endScale();
    }

    public int getColor() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Font Color").getColor().getRGB();
    }

    private boolean isModern() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Mode").getCurrentMode().equalsIgnoreCase("Modern");
    }

    private boolean isBackground() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Background").isCheckToggled();
    }
}

