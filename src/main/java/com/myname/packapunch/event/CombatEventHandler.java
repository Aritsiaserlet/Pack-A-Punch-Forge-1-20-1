package com.myname.packapunch.event;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.util.UpgradeNBTUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;


@Mod.EventBusSubscriber(modid = PackAPunchMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getEntity();

        if (sourceEntity instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();

            int level = UpgradeNBTUtil.getUpgradeLevel(weapon);
            if (level > 0) {
                float multiplier = getDamageMultiplier(level);
                
                float originalDamage = event.getAmount();
                event.setAmount(originalDamage * multiplier);
            }
        }
    }

    public static float getDamageMultiplier(int level) {
        int clampedLevel = Math.max(0, Math.min(level, com.myname.packapunch.UpgradeConfig.getMaxLevel()));
        return com.myname.packapunch.UpgradeConfig.getMultiplierForLevel(clampedLevel);
    }
}
