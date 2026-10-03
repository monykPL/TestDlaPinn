/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient;

import dev.melonclient.config.ConfigLoader;
import dev.melonclient.config.ConfigSaver;
import dev.melonclient.feature.discord.DiscordManager;
import dev.melonclient.feature.mod.ModManager;
import dev.melonclient.feature.option.OptionManager;
import dev.melonclient.feature.setting.SettingManager;
import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.helpers.CpsHelper;
import dev.melonclient.helpers.MessageHelper;
import dev.melonclient.helpers.font.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

@Mod(
        modid = Melon.modID,
        name = Melon.modName,
        version = Melon.modVersion,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class Melon {

    @Mod.Instance()
    public static Melon INSTANCE;

    public static final String modID = "melonclient";
    public static final String modName = "MelonClient";
    public static final String modVersion = "1.4.1 [1.8.9]";

    public Minecraft mc = Minecraft.getMinecraft();

    public ModManager modManager;
    public SettingManager settingManager;
    public HudEditor hudEditor;
    public OptionManager optionManager;
    public FontHelper fontHelper;
    public CpsHelper cpsHelper;
    public MessageHelper messageHelper;
    public DiscordManager discordManager;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Display.setTitle(Melon.modName + " " + Melon.modVersion);
        registerEvents(
                cpsHelper = new CpsHelper(),
                settingManager = new SettingManager(),
                modManager = new ModManager(),
                optionManager = new OptionManager(),
                hudEditor = new HudEditor(),
                fontHelper = new FontHelper(),
                messageHelper = new MessageHelper()
        );

        discordManager = new DiscordManager();
        MinecraftForge.EVENT_BUS.register(discordManager);

        try {
            ConfigSaver.migrateLegacyConfig();
            if (!ConfigSaver.configExists()) {
                ConfigSaver.saveConfig();
            }
            ConfigLoader.loadConfig();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        fontHelper.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ConfigSaver.saveConfig();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (discordManager != null) {
                discordManager.shutdown();
            }
        }));
    }

    private void registerEvents(Object... events) {
        for (Object event : events) {
            MinecraftForge.EVENT_BUS.register(event);
        }
    }
}
