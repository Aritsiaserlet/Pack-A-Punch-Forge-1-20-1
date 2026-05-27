package com.myname.packapunch.client;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.util.UpgradeNBTUtil;
import com.myname.packapunch.UpgradeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.world.entity.EquipmentSlot;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = PackAPunchMod.MOD_ID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        int level = UpgradeNBTUtil.getUpgradeLevel(stack);
        if (level <= 0) {
            return;
        }

        int maxLevel = UpgradeConfig.MAX_LEVEL;
        String stars = "★".repeat(level) + "☆".repeat(maxLevel - level);

        event.getToolTip().add(1,
                Component.literal(stars)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        float multiplier = UpgradeConfig.getMultiplierForLevel(level);
        event.getToolTip().add(2, Component.literal("Damage Bonus: x" + multiplier).withStyle(ChatFormatting.YELLOW));

        int insertIndex = 3;

        double playerBaseDamage = 1.0;
        double addValue = 0.0;
        double addMultipliedBase = 0.0;
        double addMultipliedTotal = 0.0;
        boolean hasAttackDamageModifier = false;

        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        if (!modifiers.isEmpty()) {
            for (var entry : modifiers.get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                hasAttackDamageModifier = true;
                var op = entry.getOperation();
                if (op == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                    addValue += entry.getAmount();
                } else if (op == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE) {
                    addMultipliedBase += entry.getAmount();
                } else if (op == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    addMultipliedTotal += entry.getAmount();
                }
            }
        }

        if (hasAttackDamageModifier) {
            double totalBaseDamage = (playerBaseDamage + addValue) * (1.0 + addMultipliedBase) * (1.0 + addMultipliedTotal);
            double finalDamage = totalBaseDamage * multiplier;
            String formattedDamage = String.format("%.1f", finalDamage);
            event.getToolTip().add(insertIndex++, Component.literal("Current Damage: " + formattedDamage).withStyle(ChatFormatting.GREEN));
        }

        event.getToolTip().add(insertIndex++, Component.empty());
        event.getToolTip().add(insertIndex++, Component.literal("Next Upgrade:").withStyle(ChatFormatting.GRAY));

        if (UpgradeConfig.isMaxLevel(level)) {
            event.getToolTip().add(insertIndex,
                    Component.literal("MAX LEVEL")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            int nextCost = UpgradeConfig.getCostForLevel(level + 1);
            net.minecraft.world.item.Item reqItem = UpgradeConfig.getItemForLevel(level + 1);
            String reqItemName = reqItem.getDescription().getString();
            
            event.getToolTip().add(insertIndex,
                    Component.literal(nextCost + " " + reqItemName)
                            .withStyle(ChatFormatting.AQUA));
        }
    }
}
