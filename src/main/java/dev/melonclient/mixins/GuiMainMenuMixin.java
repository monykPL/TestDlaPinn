package dev.melonclient.mixins;

import dev.melonclient.Melon;
import dev.melonclient.gui.titlescreen.TitleScreen;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class GuiMainMenuMixin {

    /**
     * Loads the custom Title Screen in dev.melonclient.gui.titlescreen
     */

    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo ci) {
        if (!Melon.INSTANCE.optionManager.getOptionByName("Disable Custom Title Screen").isCheckToggled()) {
            Melon.INSTANCE.mc.displayGuiScreen(new TitleScreen());
        }
    }
}

