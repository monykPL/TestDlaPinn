package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoGGMod extends Mod {

    public AutoGGMod() {
        super(
                "AutoGG",
                "Automatically sends a chat message and/or respawns as soon as the death screen opens.",
                Type.Mechanic
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Send Message", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Message", this, "gg", "gg", 2));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Auto Respawn", this, true));
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (!(e.gui instanceof GuiGameOver) || Melon.INSTANCE.mc.thePlayer == null) {
            return;
        }

        if (Melon.INSTANCE.settingManager.getSettingByModAndName("AutoGG", "Send Message").isCheckToggled()) {
            String message = Melon.INSTANCE.settingManager.getSettingByModAndName("AutoGG", "Message").getText();
            if (message != null && !message.isEmpty()) {
                Melon.INSTANCE.mc.thePlayer.sendChatMessage(message);
            }
        }

        if (Melon.INSTANCE.settingManager.getSettingByModAndName("AutoGG", "Auto Respawn").isCheckToggled()) {
            Melon.INSTANCE.mc.thePlayer.respawnPlayer();
        }
    }
}

