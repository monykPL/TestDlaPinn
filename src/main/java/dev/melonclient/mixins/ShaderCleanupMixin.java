package dev.melonclient.mixins;

import dev.melonclient.gui.hudeditor.HudEditor;
import dev.melonclient.gui.modmenu.ModMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Siatka bezpieczeństwa dla shadera blura z Menu Blur (Client Mods / HUD Editor).
 *
 * Normalnie shader jest zdejmowany w onGuiClosed() TEGO ekranu, który go założył.
 * Problem: jeśli z jakiegokolwiek powodu ten konkretny onGuiClosed() się nie wykona
 * (wyjątek gdzieś po drodze, dziwna kolejność zamykania ekranów itp.), shader
 * zostaje aktywny w tle NA STAŁE - wpływając na renderowanie każdego kolejnego
 * ekranu, łącznie z ekwipunkiem, co mogło objawiać się czarnymi/znikającymi
 * fragmentami tła.
 *
 * Ten mixin łapie KAŻDE przejście na inny ekran (displayGuiScreen) i wymusza
 * zdjęcie shadera, jeśli nowy ekran to NIE jest HudEditor ani ModMenu - więc
 * nawet gdyby tamten mechanizm zawiódł, tutaj i tak zostanie posprzątane.
 */
@Mixin(Minecraft.class)
public abstract class ShaderCleanupMixin {

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void melonclient$forceClearBlurShader(GuiScreen guiScreenIn, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        boolean isBlurScreen = guiScreenIn instanceof HudEditor || guiScreenIn instanceof ModMenu;
        if (isBlurScreen) {
            return; // ten ekran sam zarządza swoim shaderem
        }

        if (mc.entityRenderer != null && mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }
}
