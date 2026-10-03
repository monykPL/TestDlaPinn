package dev.melonclient.mixins;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityCritFX;
import net.minecraft.client.particle.EntityCrit2FX;
import net.minecraft.client.particle.EntityFootStepFX;
import net.minecraft.client.particle.EntityFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dwie niezależne optymalizacje cząsteczek:
 *
 * 1) "Reduce Particles" - filtr PO TYPIE: zostawia tylko cząsteczki trafień
 *    (EntityCritFX/EntityCrit2FX) i chodzenia (EntityFootStepFX), wycina
 *    wszystko inne - w tym rozwalanie bloków (EntityDiggingFX/EntityBreakingFX),
 *    eksplozje, dym, portal, deszcz itd.
 *
 * 2) "Particle Limiter" - stary limit ile NOWYCH cząsteczek może powstać
 *    w jednym ticku (redukcja skoków GC przy burstach typu TNT/fajerwerki).
 *
 * Obie są niezależnymi, osobnymi przełącznikami - można włączyć jedną, drugą,
 * obie albo żadną.
 */
@Mixin(EffectRenderer.class)
public abstract class EffectRendererMixin {

    private int melonclient$particlesThisTick = 0;

    @Inject(method = "addEffect(Lnet/minecraft/client/particle/EntityFX;)V", at = @At("HEAD"), cancellable = true)
    private void melonclient$filterAndCapParticles(EntityFX effect, CallbackInfo ci) {
        Option reduceOption = Melon.INSTANCE.optionManager.getOptionByName("Reduce Particles");
        if (reduceOption != null && reduceOption.isCheckToggled()) {
            boolean alwaysAllowed = effect instanceof EntityCritFX
                    || effect instanceof EntityCrit2FX
                    || effect instanceof EntityFootStepFX;
            if (!alwaysAllowed) {
                ci.cancel();
                return;
            }
        }

        Option enabledOption = Melon.INSTANCE.optionManager.getOptionByName("Particle Limiter");
        if (enabledOption == null || !enabledOption.isCheckToggled()) {
            return;
        }

        Option maxOption = Melon.INSTANCE.optionManager.getOptionByName("Max Particles Per Tick");
        int max = maxOption != null ? (int) maxOption.getCurrentNumber() : 200;

        if (melonclient$particlesThisTick >= max) {
            ci.cancel();
            return;
        }

        melonclient$particlesThisTick++;
    }

    @Inject(method = "updateEffects()V", at = @At("HEAD"))
    private void melonclient$resetParticleCounter(CallbackInfo ci) {
        melonclient$particlesThisTick = 0;
    }
}
