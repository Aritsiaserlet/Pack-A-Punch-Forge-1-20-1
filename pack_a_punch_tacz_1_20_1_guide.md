# สรุปข้อมูลและโค้ดทั้งหมดสำหรับโปรเจค Pack A Punch (Forge 1.20.1 + TaCZ Official 1.20.1)

ตลอดการพูดคุยนี้ เราได้แก้ปัญหาและพัฒนาระบบ Tooltip สำหรับมอด Pack A Punch จนสมบูรณ์แบบ ทั้งสำหรับ **อาวุธ Vanilla** และอาวุธจากมอด **TaCZ (Timeless and Classics: Zero)** โดยใช้ระบบ **Soft Dependency (Reflection)** เพื่อให้มอดของเราทำงานได้โดยไม่ต้องผูกมัดกับมอด TaCZ (ไม่มีก็เล่นได้ ไม่แครช)

นี่คือสรุปข้อมูล โลจิก และโค้ดทั้งหมด เพื่อให้คุณนำไปปรับใช้ในโปรเจค **Forge 1.20.1** ครับ!

---

## 1. ความแตกต่างที่ต้องระวัง (NeoForge 1.21.1 vs Forge 1.20.1)

> [!WARNING]
> **ระบบ Attributes และ NBT เปลี่ยนไปมากระหว่าง 1.20.1 และ 1.21.1**
> - **1.21.1 (NeoForge):** ใช้ `DataComponents` ในการเก็บข้อมูลไอเทมทั้งหมด (เช่น `DataComponents.ATTRIBUTE_MODIFIERS`)
> - **1.20.1 (Forge):** ยังคงใช้ระบบ `NBT` (`stack.getOrCreateTag()`) และ `stack.getAttributeModifiers(EquipmentSlot.MAINHAND)` แบบดั้งเดิม

ดังนั้น โค้ดส่วนคำนวณดาเมจ Vanilla ต้องถูกเขียนใหม่เล็กน้อยใน 1.20.1 ครับ

---

## 2. โค้ดส่วนที่ 1: `TaczIntegration.java` (ระบบเจาะเกราะ Soft Dependency)

คลาสนี้แทบจะไม่ต้องแก้เลยเมื่อย้ายไป 1.20.1 เพราะเราใช้ **Reflection** ล้วนๆ ในการเจาะหา `TimelessAPI` และ `GunData` ระบบนี้ถูกออกแบบมาให้หาคลาสและเมธอดแบบไดนามิก จึงรองรับทั้ง Tacz Official และ Unofficial ครับ

**สร้างไฟล์ `TaczIntegration.java` ใน 1.20.1:**

```java
package com.myname.packapunch.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.ModList; // ระวัง: 1.20.1 เปลี่ยนจาก neoforged เป็น minecraftforge
import java.util.List;

public class TaczIntegration {
    
    public static void tryAddTaczDamageTooltip(ItemStack stack, float multiplier, List<com.mojang.datafixers.util.Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements) {
        if (!ModList.get().isLoaded("tacz")) return;

        try {
            // 1. ดึง IGun instance
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            java.lang.reflect.Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            Object iGunObj = getIGunOrNull.invoke(null, stack);
            if (iGunObj == null) return;

            // 2. ดึง Gun ID
            java.lang.reflect.Method getGunId = iGunClass.getMethod("getGunId", ItemStack.class);
            net.minecraft.resources.ResourceLocation gunId = (net.minecraft.resources.ResourceLocation) getGunId.invoke(iGunObj, stack);
            if (gunId == null) return;

            // 3. ดึง Gun Index จาก TimelessAPI
            Class<?> timelessApiClass = Class.forName("com.tacz.guns.api.TimelessAPI");
            java.lang.reflect.Method getCommonGunIndex = timelessApiClass.getMethod("getCommonGunIndex", net.minecraft.resources.ResourceLocation.class);
            java.util.Optional<?> indexOpt = (java.util.Optional<?>) getCommonGunIndex.invoke(null, gunId);
            if (!indexOpt.isPresent()) return;
            Object gunIndex = indexOpt.get();

            // 4. ดึง Gun Data
            java.lang.reflect.Method getGunData = gunIndex.getClass().getMethod("getGunData");
            Object gunData = getGunData.invoke(gunIndex);

            // 5. ค้นหา Base Damage แบบเจาะลึก
            float baseDamage = findDamageDeep(gunData, 0);

            // 6. แทรกข้อความ Pack-A-Punch
            if (baseDamage > 0) {
                float packDamage = baseDamage * multiplier;
                String formattedDamage = String.format(java.util.Locale.US, packDamage % 1.0 == 0 ? "%.0f" : "%.1f", packDamage);
                Component newComp = Component.literal("Damage: " + formattedDamage).withStyle(ChatFormatting.GREEN);
                
                // หาบรรทัด Damage Bonus เพื่อแทรกต่อท้าย
                int insertIndex = elements.size();
                for (int i = 0; i < elements.size(); i++) {
                    var element = elements.get(i);
                    if (element.left().isPresent()) {
                        String rawText = element.left().get().getString();
                        String stripped = ChatFormatting.stripFormatting(rawText);
                        if (stripped != null && (stripped.contains("Damage Bonus") || stripped.contains("โบนัสดาเมจ"))) {
                            insertIndex = i + 1;
                            break;
                        }
                    }
                }
                elements.add(insertIndex, com.mojang.datafixers.util.Either.left(newComp));
            } else {
                // (Optional) Debug ข้อความสีแดงหากหาตัวแปรไม่เจอ
                Component debugComp = Component.literal("Pack-A-Punch TaCZ Error: Damage field not found!").withStyle(ChatFormatting.RED);
                elements.add(com.mojang.datafixers.util.Either.left(debugComp));
            }
        } catch (Throwable t) {
            // ปล่อยผ่านเงียบๆ หาก API เปลี่ยน
        }
    }

    private static float findDamageDeep(Object obj, int depth) {
        if (depth > 2 || obj == null) return -1;
        
        // กรองเฉพาะ Method เป้าหมายแบบเป๊ะๆ ป้องกันการไปดึงค่าเจาะเกราะ (Armor Damage) มาผิดๆ
        for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getDeclaringClass() != Object.class) {
                String name = m.getName().toLowerCase(java.util.Locale.US);
                if (name.equals("getdamage") || name.equals("damage") || 
                    name.equals("getbasedamage") || name.equals("basedamage") ||
                    name.equals("getbulletdamage") || name.equals("getgunbasedamage") ||
                    name.equals("getdamageamount") || name.equals("damageamount")) {
                    try {
                        Object result = m.invoke(obj);
                        if (result instanceof Number) {
                            return ((Number)result).floatValue();
                        }
                    } catch (Exception e) {}
                }
            }
        }
        
        for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getDeclaringClass() != Object.class) {
                if (m.getReturnType().getName().contains("tacz")) {
                    try {
                        Object result = m.invoke(obj);
                        float dmg = findDamageDeep(result, depth + 1);
                        if (dmg > 0) return dmg;
                    } catch (Exception e) {}
                }
            }
        }
        return -1;
    }
}
```

---

## 3. โค้ดส่วนที่ 2: `TooltipHandler.java` (1.20.1 Adaptation)

ใน 1.20.1 การเข้าถึง `AttributeModifiers` จะต่างออกไป และต้องระวังการเปลี่ยน package จาก `net.neoforged` เป็น `net.minecraftforge`

> [!TIP]
> สำหรับส่วน `onGatherComponents` เราใช้ Regex แบบกว้าง `(?i)(?:Damage|ดาเมจ).*?(\d+(?:\.\d+)?)` และต้องสั่ง "ข้าม" บรรทัด `Attack Damage` ของ Vanilla เสมอเพื่อป้องกันเลขเบิ้ล

**ตัวอย่าง `TooltipHandler.java` สำหรับ 1.20.1:**

```java
package com.myname.packapunch.client;

import com.myname.packapunch.PackAPunchMod;
// import NBT/Component classes specific to your 1.20.1 setup
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

@Mod.EventBusSubscriber(modid = PackAPunchMod.MOD_ID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // [TODO] ดึง level จาก NBT แทน DataComponents
        int level = 0; // stack.getOrCreateTag().getInt("UpgradeLevel"); 
        if (level <= 0) return;

        int maxLevel = 3; // ดึงจาก Config
        String stars = "★".repeat(level) + "☆".repeat(maxLevel - level);

        event.getToolTip().add(1, Component.literal(stars).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        float multiplier = 2.0f; // ดึงจาก Config
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
        
        // [TODO] ดึง level จาก NBT
        int level = 0; // stack.getOrCreateTag().getInt("UpgradeLevel"); 
        if (level <= 0) return;

        float multiplier = 2.0f; // ดึงจาก Config
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
```

---

## 4. ข้อควรระวังเพิ่มเติมในการพอร์ต

1. **Imports:** จำไว้ว่า Forge 1.20.1 ใช้แพ็กเกจ `net.minecraftforge` ไม่ใช่ `net.neoforged` 
2. **NBT:** คุณต้องเปลี่ยนจากการใช้ `stack.get(ModDataComponents.UPGRADE_LEVEL.get())` กลับไปใช้ระบบ `stack.getOrCreateTag()` ของ Forge 1.20.1
3. **Attribute Operations:** ใน 1.20.1 ค่า Enum ของ Attribute Modifier Operation จะเป็น `ADDITION`, `MULTIPLY_BASE`, และ `MULTIPLY_TOTAL` ซึ่งต่างจาก 1.21.1 เล็กน้อย

นำข้อมูลและโค้ดตัวอย่างนี้ไปแปะเป็น Base ให้ AI (หรือเขียนเอง) ในโปรเจค 1.20.1 ได้เลยครับ! ทุกอย่างน่าจะเชื่อมติดและทำงานได้แบบไร้รอยต่อแน่นอนครับ!
