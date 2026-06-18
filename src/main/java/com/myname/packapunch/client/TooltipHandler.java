package com.myname.packapunch.client;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.util.UpgradeNBTUtil;
import com.myname.packapunch.UpgradeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = PackAPunchMod.MOD_ID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        int level = UpgradeNBTUtil.getUpgradeLevel(stack);
        if (level <= 0) {
            return;
        }

        int maxLevel = UpgradeConfig.getMaxLevel();
        String stars = "★".repeat(level) + "☆".repeat(maxLevel - level);

        event.getToolTip().add(1, Component.literal(stars).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        float multiplier = UpgradeConfig.getMultiplierForLevel(level);
        event.getToolTip().add(2, Component.literal("Damage Bonus: x" + multiplier).withStyle(ChatFormatting.YELLOW));

        // ── 1.20.1 Attribute System ──
        double playerBaseDamage = 1.0;
        double addValue = 0.0;
        double addMultipliedBase = 0.0;
        double addMultipliedTotal = 0.0;
        boolean hasAttackDamageModifier = false;

        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        if (modifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
            for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
                hasAttackDamageModifier = true;
                var op = modifier.getOperation();
                if (op == AttributeModifier.Operation.ADDITION) {
                    addValue += modifier.getAmount();
                } else if (op == AttributeModifier.Operation.MULTIPLY_BASE) {
                    addMultipliedBase += modifier.getAmount();
                } else if (op == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    addMultipliedTotal += modifier.getAmount();
                }
            }
        }

        double vanillaFinalDamage = 0;
        if (hasAttackDamageModifier) {
            double totalBaseDamage = (playerBaseDamage + addValue) * (1.0 + addMultipliedBase) * (1.0 + addMultipliedTotal);
            vanillaFinalDamage = totalBaseDamage * multiplier;
        }

        // ── แสกนแก้ตัวเลข Vanilla ──
        for (int i = 0; i < event.getToolTip().size(); i++) {
            Component tooltipLine = event.getToolTip().get(i);
            String rawText = tooltipLine.getString();
            String strippedText = ChatFormatting.stripFormatting(rawText);
            if (strippedText == null) strippedText = rawText;

            if (hasAttackDamageModifier && (strippedText.contains("Attack Damage") || strippedText.contains("ดาเมจโจมตี"))) {
                String formattedDamage = String.format(java.util.Locale.US, vanillaFinalDamage % 1.0 == 0 ? "%.0f" : "%.1f", vanillaFinalDamage);
                event.getToolTip().set(i, tooltipLine.copy().append(
                        Component.literal(" (" + formattedDamage + ")").withStyle(ChatFormatting.GREEN)
                ));
                hasAttackDamageModifier = false;
            }
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        
        int level = UpgradeNBTUtil.getUpgradeLevel(stack);
        if (level <= 0) return;

        float multiplier = UpgradeConfig.getMultiplierForLevel(level);
        java.util.regex.Pattern damagePattern = java.util.regex.Pattern.compile("(?i)(?:Damage|ดาเมจ).*?(\\d+(?:\\.\\d+)?)");
        var elements = event.getTooltipElements();
        boolean foundTaczDamage = false;

        // Pass 1: ดักจับคำว่า Damage จากมอดอื่นๆ ทั่วไปที่ไม่ได้วาด TooltipComponent พิเศษ
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            if (element.left().isPresent()) {
                net.minecraft.network.chat.FormattedText text = element.left().get();
                String rawText = text.getString();
                String strippedText = ChatFormatting.stripFormatting(rawText);
                if (strippedText == null) strippedText = rawText;

                // ข้ามบรรทัด Vanilla เพื่อป้องกันเลขเบิ้ล
                if (strippedText.contains("Damage Bonus") || strippedText.contains("โบนัสดาเมจ") ||
                    strippedText.contains("Attack Damage") || strippedText.contains("ดาเมจโจมตี")) {
                    continue;
                }

                java.util.regex.Matcher matcher = damagePattern.matcher(strippedText);
                if (matcher.find()) {
                    try {
                        String numStr = matcher.group(1);
                        double baseDam = Double.parseDouble(numStr);
                        double packDamage = baseDam * multiplier;
                        String formattedDamage = String.format(java.util.Locale.US, packDamage % 1.0 == 0 ? "%.0f" : "%.1f", packDamage);

                        Component newComp;
                        if (text instanceof Component compText) {
                            newComp = compText.copy().append(Component.literal(" (" + formattedDamage + ")").withStyle(ChatFormatting.GREEN));
                        } else {
                            newComp = Component.literal(rawText + " (" + formattedDamage + ")").withStyle(ChatFormatting.DARK_GREEN);
                        }
                        
                        elements.set(i, com.mojang.datafixers.util.Either.left(newComp));
                        foundTaczDamage = true;
                    } catch (Exception e) {}
                }
            }
        }

        // Pass 2: เรียกใช้ระบบเจาะเกราะ (Reflection) สำหรับ TaCZ
        if (!foundTaczDamage) {
            TaczIntegration.tryAddTaczDamageTooltip(stack, multiplier, elements);
        }
    }
}
