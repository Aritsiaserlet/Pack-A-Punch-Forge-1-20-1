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

        int maxLevel = com.myname.packapunch.UpgradeConfig.getMaxLevel();
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
            int nextLevel = level + 1;
            com.myname.packapunch.UpgradeTier tier = com.myname.packapunch.UpgradeConfig.getTier(nextLevel);
            int nextCost = tier.getCost();
            String reqName = "";
            switch (tier.getCurrencyType()) {
                case ITEM -> {
                    reqName = com.myname.packapunch.UpgradeConfig.getItemForLevel(nextLevel).getDescription().getString();
                }
                case SCOREBOARD -> {
                    reqName = tier.getId() + " (Score)";
                }
                case XP -> {
                    reqName = "Levels";
                }
            }
            
            event.getToolTip().add(insertIndex,
                    Component.literal(nextCost + " " + reqName)
                            .withStyle(ChatFormatting.AQUA));
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onGatherComponents(net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        
        int level = UpgradeNBTUtil.getUpgradeLevel(stack);
        if (level <= 0) return;
        
        float multiplier = com.myname.packapunch.UpgradeConfig.getMultiplierForLevel(level);

        java.util.regex.Pattern damagePattern = java.util.regex.Pattern.compile("(?i)(?:Damage|ดาเมจ).*?(\\d+(?:\\.\\d+)?)(?:\\s*[x×*]\\s*(\\d+))?");
        var elements = event.getTooltipElements();
        boolean foundTaczDamage = false;

        // Pass 1: พยายามหา Tooltip แบบมาตรฐานที่เกมอาจจะสร้างไว้
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            if (element.left().isPresent()) {
                net.minecraft.network.chat.FormattedText text = element.left().get();
                String rawText = text.getString();
                String strippedText = net.minecraft.ChatFormatting.stripFormatting(rawText);
                if (strippedText == null) strippedText = rawText;

                if (strippedText.contains("Damage Bonus") || strippedText.contains("โบนัสดาเมจ") ||
                    strippedText.contains("Attack Damage") || strippedText.contains("ดาเมจโจมตี")) {
                    continue;
                }

                java.util.regex.Matcher matcher = damagePattern.matcher(strippedText);
                if (matcher.find()) {
                    try {
                        String numStr = matcher.group(1);
                        String pelletStr = matcher.groupCount() >= 2 ? matcher.group(2) : null;
                        
                        double baseDam = Double.parseDouble(numStr);
                        double packDamage = baseDam * multiplier;
                        String formattedDamage = String.format(java.util.Locale.US, packDamage % 1.0 == 0 ? "%.0f" : "%.1f", packDamage);

                        String appendedText = " (" + formattedDamage;
                        if (pelletStr != null) {
                            appendedText += " × " + pelletStr;
                        }
                        appendedText += ")";

                        Component newComp;
                        if (text instanceof Component compText) {
                            newComp = compText.copy().append(
                                    Component.literal(appendedText).withStyle(ChatFormatting.GREEN)
                            );
                        } else {
                            newComp = Component.literal(rawText + appendedText).withStyle(ChatFormatting.DARK_GREEN);
                        }
                        
                        elements.set(i, com.mojang.datafixers.util.Either.left(newComp));
                        foundTaczDamage = true;
                    } catch (Exception e) {
                        // ปล่อยผ่าน
                    }
                }
            }
        }

        // Pass 2: เรียกใช้ระบบ TaczIntegration ของเรา ถ้าไม่เจอดาเมจจาก Pass 1
        if (!foundTaczDamage) {
            TaczIntegration.tryAddTaczDamageTooltip(stack, multiplier, elements);
        }
    }
}
