package dev.melonclient.mixins;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pomija renderowanie bytów (moby, gracze, itemy) które są dalej od kamery
 * niż ustawiony dystans - Minecraft 1.8.9 domyślnie tnie tylko teren,
 * a nie odległe byty, więc to realny zysk FPS na zatłoczonych serwerach.
 *
 * Zabezpieczenia:
 * - master-toggle "Entity Culling" (domyślnie WŁĄCZONY, ale łatwo wyłączyć w ModMenu
 *   gdyby coś wyglądało źle - patrz punkt 4.1.3 z ROADMAP.md: "Per-Module Kill-Switches")
 * - nigdy nie ucina encji, na której siedzi kamera (np. w trzeciej osobie)
 * - jeśli z jakiegoś powodu "livingPlayer" jest null (np. ekran ładowania), po
 *   prostu nic nie robimy - zachowanie identyczne jak w czystym Forge
 */
@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {

    @Shadow
    public Entity livingPlayer;

    @Inject(method = "renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z", at = @At("HEAD"), cancellable = true)
    private void melonclient$cullDistantEntities(Entity entity, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (livingPlayer == null || entity == livingPlayer) {
            return;
        }

        Option cullingOption = Melon.INSTANCE.optionManager.getOptionByName("Entity Culling");
        if (cullingOption == null || !cullingOption.isCheckToggled()) {
            return; // funkcja wyłączona - zachowanie 1:1 jak czysty Forge
        }

        Option distanceOption = Melon.INSTANCE.optionManager.getOptionByName("Entity Render Distance");
        float maxDistance = distanceOption != null ? distanceOption.getCurrentNumber() : 64f;

        double distanceSq = livingPlayer.getDistanceSqToEntity(entity);
        double maxDistanceSq = (double) maxDistance * (double) maxDistance;

        if (distanceSq > maxDistanceSq) {
            cir.setReturnValue(false);
        }
    }
}

