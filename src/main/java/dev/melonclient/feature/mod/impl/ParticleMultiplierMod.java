package dev.melonclient.feature.mod.impl;

import dev.melonclient.Melon;
import dev.melonclient.feature.mod.Mod;
import dev.melonclient.feature.mod.Type;
import dev.melonclient.feature.setting.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ParticleMultiplierMod extends Mod {

    public ParticleMultiplierMod() {
        super(
                "ParticleMultiplier",
                "Multiplies or adds Particles by a given amount.",
                Type.Visual
        );

        Melon.INSTANCE.settingManager.addSetting(new Setting("Particle Amount", this, 15, 5));
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (e.target instanceof EntityLivingBase) {
            boolean doCriticalDamage =
                    Melon.INSTANCE.mc.thePlayer.fallDistance > 0.0F &&
                    !Melon.INSTANCE.mc.thePlayer.onGround &&
                    !Melon.INSTANCE.mc.thePlayer.isOnLadder() &&
                    !Melon.INSTANCE.mc.thePlayer.isInWater() &&
                    !Melon.INSTANCE.mc.thePlayer.isPotionActive(Potion.blindness) &&
                            Melon.INSTANCE.mc.thePlayer.ridingEntity == null;

            for (int i = 0; i < getAmount(); i++) {
                Melon.INSTANCE.mc.thePlayer.onEnchantmentCritical(e.target);
                if (doCriticalDamage) {
                    Melon.INSTANCE.mc.thePlayer.onCriticalHit(e.target);
                }
            }

        }
    }

    private float getAmount() {
        return Melon.INSTANCE.settingManager.getSettingByModAndName(getName(), "Particle Amount").getCurrentNumber();
    }
}

