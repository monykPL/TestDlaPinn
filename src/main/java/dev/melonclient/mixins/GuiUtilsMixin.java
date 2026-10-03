/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.mixins;

import dev.melonclient.Melon;
import dev.melonclient.helpers.hud.ScrollHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * BUG NAPRAWIONY: poprzednia wersja robiła GlStateManager.pushMatrix() przed
 * narysowaniem tła tooltipa, a popMatrix() dopiero przy wywołaniu
 * enableLighting() GDZIEŚ DALEJ w tej samej metodzie. To działa tylko jeśli
 * metoda ZAWSZE dotrze do tamtego konkretnego miejsca - a jeśli jest jakakolwiek
 * ścieżka w drawHoveringText, która kończy się wcześniej (np. dla pewnych
 * przedmiotów/tooltipów), push się wykonywał, a pop nigdy - trwale
 * rozsynchronizowując stos macierzy OpenGL. Efekt: WSZYSTKO rysowane później
 * (łącznie z przezroczystą warstwą szkła w kolejnych klatkach) renderowało się
 * z przesuniętą, zepsutą transformacją.
 *
 * Teraz: flaga trzyma informację "czy w TYM wywołaniu faktycznie zrobiliśmy
 * push", a pop wykonuje się na KAŻDYM wyjściu z metody (@At("RETURN") łapie
 * wszystkie return, także te wcześniejsze) - i to tylko jeśli push faktycznie
 * się wydarzył. Push i pop są teraz gwarantowanie zbalansowane niezależnie od
 * tego, którą ścieżką metoda się zakończy.
 */
@Mixin(GuiUtils.class)
public abstract class GuiUtilsMixin {

    private static final ScrollHelper scrollHelper = new ScrollHelper(0, 0, 35, 300);
    private static boolean melonclient$pushedMatrix = false;

    @Inject(method = "drawHoveringText", at = @At("HEAD"), remap = false)
    private static void melonclient$resetFlag(
            List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci
    ) {
        melonclient$pushedMatrix = false;
    }

    @Inject(
            method = "drawHoveringText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/client/config/GuiUtils;drawGradientRect(IIIIIII)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            ), locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            remap = false
    )
    private static void drawHoveringTextStart(
            List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci,
            int tooltipTextWidth, boolean needsWrap, int titleLinesCount, int tooltipX, int tooltipY, int tooltipHeight, int zLevel, int backgroundColor
    ) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, getTooltipPosition(tooltipY, tooltipHeight, screenHeight), 0);
        melonclient$pushedMatrix = true;
    }

    /**
     * Zamiast wiązać się z konkretnym wywołaniem enableLighting() w środku
     * metody - łapiemy KAŻDE wyjście z drawHoveringText (@At("RETURN") łapie
     * wszystkie return, nie tylko ten "główny"), i zdejmujemy macierz TYLKO
     * jeśli w tym wywołaniu faktycznie ją założyliśmy.
     */
    @Inject(method = "drawHoveringText", at = @At("RETURN"), remap = false)
    private static void melonclient$guaranteedPop(
            List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci
    ) {
        if (melonclient$pushedMatrix) {
            GlStateManager.popMatrix();
            melonclient$pushedMatrix = false;
        }
    }

    private static float getTooltipPosition(int tooltipY, int tooltipHeight, int screenHeight) {
        boolean shouldScroll = tooltipY < 0;
        scrollHelper.update();

        float scroll;
        float newToolTipHeight = tooltipHeight + 20;
        if (shouldScroll && Melon.INSTANCE.modManager.getMod("ScrollTooltips").isToggled()) {
            scrollHelper.updateScroll();
            scrollHelper.setHeight(newToolTipHeight);
            scrollHelper.setMaxScroll(screenHeight);
            scroll = scrollHelper.getCalculatedScroll() + (newToolTipHeight - screenHeight);
        } else {
            scroll = 0;
        }

        return scroll;
    }
}
