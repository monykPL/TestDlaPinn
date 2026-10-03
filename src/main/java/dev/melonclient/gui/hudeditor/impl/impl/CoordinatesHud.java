package dev.melonclient.gui.hudeditor.impl.impl;

import dev.melonclient.Melon;
import dev.melonclient.gui.Style;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.hudeditor.impl.HudMod;
import dev.melonclient.helpers.render.GLHelper;
import dev.melonclient.helpers.render.Helper2D;
import dev.melonclient.helpers.MathHelper;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CoordinatesHud extends HudMod {

    public CoordinatesHud(String name, int x, int y) {
        super(name, x, y);
        setW(120);
        setH(60);
    }

    @Override
    public void renderMod(int mouseX, int mouseY) {
        GLHelper.startScale(getX(), getY(), getSize());
        if (Melon.INSTANCE.modManager.getMod(getName()).isToggled()) {
            if (isModern()) {
                if (isBackground()) {
                    Helper2D.drawRoundedRectangle(getX(), getY(), getW(), getH(), 2, Style.getColor(50).getRGB(), 0);
                }
                Melon.INSTANCE.fontHelper.size20.drawString("X: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posX, 1), getX() + 5, getY() + 5, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString("Y: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posY, 1), getX() + 5, getY() + 5 + 14, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString("Z: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posZ, 1), getX() + 5, getY() + 5 + 28, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString(isBiome() ? "Biome: " + getBiomeName() : "", getX() + 5, getY() + 5 + 42, getColor());
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), Style.getColor(50).getRGB());
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString("X: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posX, 1), getX() + 5, getY() + 5, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString("Y: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posY, 1), getX() + 5, getY() + 5 + 14, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString("Z: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posZ, 1), getX() + 5, getY() + 5 + 28, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString(isBiome() ? "Biome: " + getBiomeName() : "", getX() + 5, getY() + 5 + 42, getColor());
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
                Melon.INSTANCE.fontHelper.size20.drawString("X: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posX, 1), getX() + 5, getY() + 5, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString("Y: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posY, 1), getX() + 5, getY() + 5 + 14, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString("Z: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posZ, 1), getX() + 5, getY() + 5 + 28, getColor());
                Melon.INSTANCE.fontHelper.size20.drawString(isBiome() ? "Biome: " + getBiomeName() : "", getX() + 5, getY() + 5 + 42, getColor());
            } else {
                if (isBackground()) {
                    Helper2D.drawRectangle(getX(), getY(), getW(), getH(), 0x50000000);
                }
                Melon.INSTANCE.mc.fontRendererObj.drawString("X: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posX, 1), getX() + 5, getY() + 5, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString("Y: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posY, 1), getX() + 5, getY() + 5 + 14, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString("Z: " + MathHelper.round(Melon.INSTANCE.mc.thePlayer.posZ, 1), getX() + 5, getY() + 5 + 28, getColor());
                Melon.INSTANCE.mc.fontRendererObj.drawString(isBiome() ? "Biome: " + getBiomeName() : "", getX() + 5, getY() + 5 + 42, getColor());
            }
        }
        GLHelper.endScale();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (isBiome()) {
            setW(120);
            setH(60);
        } else {
            setW(70);
            setH(45);
        }
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

    private boolean isBiome() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Biome").isCheckToggled();
    }

    private String getBiomeName() {
        return Melon.INSTANCE.mc.theWorld.getBiomeGenForCoords(new BlockPos(Melon.INSTANCE.mc.thePlayer.posX, Melon.INSTANCE.mc.thePlayer.posY, Melon.INSTANCE.mc.thePlayer.posZ)).biomeName;
    }
}

