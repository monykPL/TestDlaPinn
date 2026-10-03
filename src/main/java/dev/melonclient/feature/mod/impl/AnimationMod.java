package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AnimationMod extends Mod {

    public AnimationMod() {
        super(
                "Animation",
                "1.7 Animations in 1.8.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Block Animation", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Eat/Drink Animation", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Bow Animation", this, true));
        Melon.INSTANCE.settingManager.addSetting(new Setting("Fishing Rod", this, true));
    }

    @SubscribeEvent
    public void onAnimation(TickEvent.ClientTickEvent e) {
        if (Melon.INSTANCE.mc.theWorld == null || Melon.INSTANCE.mc.thePlayer == null) return;
        if (e.phase == TickEvent.Phase.START) return;
        ItemStack heldItem = Melon.INSTANCE.mc.thePlayer.getHeldItem();
        if (Melon.INSTANCE.modManager.getMod("Animation").isToggled() && heldItem != null) {
            if (Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Block Animation").isCheckToggled() && heldItem.getItemUseAction() == EnumAction.BLOCK) {
                attemptSwing();
            } else if (Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Eat/Drink Animation").isCheckToggled() && heldItem.getItemUseAction() == EnumAction.DRINK) {
                attemptSwing();
            } else if (Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Bow Animation").isCheckToggled() && heldItem.getItemUseAction() == EnumAction.BOW) {
                attemptSwing();
            }
        }
    }

    /**
     * Swings the player's arm if you're holding the attack and use item keys at the same time and looking at a block.
     */
    private void attemptSwing() {
        if (Melon.INSTANCE.mc.thePlayer.getItemInUseCount() > 0) {
            final boolean mouseDown = Melon.INSTANCE.mc.gameSettings.keyBindAttack.isKeyDown() &&
                    Melon.INSTANCE.mc.gameSettings.keyBindUseItem.isKeyDown();
            if (mouseDown && Melon.INSTANCE.mc.objectMouseOver != null && Melon.INSTANCE.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                forceSwingArm();
            }
        }
    }

    /**
     * Forces the player to swing their arm.
     */
    private void forceSwingArm() {
        EntityPlayerSP player = Melon.INSTANCE.mc.thePlayer;
        int swingEnd = player.isPotionActive(Potion.digSpeed) ?
                (6 - (1 + player.getActivePotionEffect(Potion.digSpeed).getAmplifier())) : (player.isPotionActive(Potion.digSlowdown) ?
                (6 + (1 + player.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2) : 6);
        if (!player.isSwingInProgress || player.swingProgressInt >= swingEnd / 2 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
        }
    }
}

