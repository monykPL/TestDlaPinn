package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiTweaksMod extends Mod {

    public GuiTweaksMod() {
        super(
                "Gui Tweaks",
                "Adds Tweaks to the Gui like blur and transparency.",
                Type.Tweaks
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Blur Background", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Darken Background", this, true));
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Blur Background").isCheckToggled()) {
            if (!(e.gui instanceof GuiChat)) {
                try {
                    Melon.INSTANCE.mc.entityRenderer.loadShader(
                            new ResourceLocation("shaders/post/blur.json"));
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
            }
            if (e.gui == null) {
                if (Melon.INSTANCE.mc.entityRenderer.getShaderGroup() != null) {
                    Melon.INSTANCE.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                }
            }
        }
    }
}
