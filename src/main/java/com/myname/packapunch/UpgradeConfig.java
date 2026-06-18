package com.myname.packapunch;

import com.myname.packapunch.config.MyModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class UpgradeConfig {

    public static int getMaxLevel() {
        return MyModConfig.UPGRADES.get().size();
    }

    public static boolean isMaxLevel(int level) {
        return level >= getMaxLevel();
    }

    private static String[] getParsedLevel(int level) {
        List<? extends String> upgrades = MyModConfig.UPGRADES.get();
        if (level > 0 && level <= upgrades.size()) {
            String line = upgrades.get(level - 1);
            return line.split(";");
        }
        return new String[0];
    }

    @SuppressWarnings({"removal", "null"})
    public static Item getItemForLevel(int nextLevel) {
        String[] parts = getParsedLevel(nextLevel);
        if (parts.length >= 2) {
            String itemId = parts[1].trim();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            return item != null ? item : Items.AIR;
        }
        return Items.AIR;
    }

    public static int getCostForLevel(int nextLevel) {
        String[] parts = getParsedLevel(nextLevel);
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    public static float getMultiplierForLevel(int level) {
        if (level <= 0) return 1.0f;
        String[] parts = getParsedLevel(level);
        if (parts.length >= 1) {
            try {
                return Float.parseFloat(parts[0].trim());
            } catch (NumberFormatException e) {
                return 1.0f;
            }
        }
        return 1.0f;
    }
}
