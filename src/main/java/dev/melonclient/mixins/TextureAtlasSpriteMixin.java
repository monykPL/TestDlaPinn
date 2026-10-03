package dev.melonclient.mixins;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pozwala zatrzymać animację ognia (fire_layer_0/fire_layer_1) - pomija
 * przejście do kolejnej klatki animacji dla tego konkretnego sprite'a,
 * więc tekstura zostaje "zamrożona" na aktualnej klatce zamiast się animować.
 * Reszta animowanych tekstur (woda, lawa, portal itd.) działa bez zmian.
 */
@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {

    public abstract String getIconName();

    @Inject(method = "updateAnimation()V", at = @At("HEAD"), cancellable = true)
    private void melonclient$freezeFire(CallbackInfo ci) {
        Option freezeOption = Melon.INSTANCE.optionManager.getOptionByName("Freeze Fire Animation");
        if (freezeOption == null || !freezeOption.isCheckToggled()) {
            return;
        }

        String iconName = getIconName();
        if (iconName != null && iconName.toLowerCase().contains("fire")) {
            ci.cancel();
        }
    }
}
