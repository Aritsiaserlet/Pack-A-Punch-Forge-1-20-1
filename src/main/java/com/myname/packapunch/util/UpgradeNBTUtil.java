package com.myname.packapunch.util;

import net.minecraft.world.item.ItemStack;


/**
 * Utility class for managing Pack-a-Punch upgrade NBT data on ItemStacks.
 * Replaces the 1.21.1 Data Components system with 1.20.1 NBT.
 */
public class UpgradeNBTUtil {

    private static final String NBT_KEY_UPGRADE_LEVEL = "packapunch_upgrade_level";

    /**
     * Gets the current upgrade level of the given item.
     * @param stack The item to check.
     * @return The upgrade level (0 if not upgraded).
     */
    public static int getUpgradeLevel(ItemStack stack) {
        var tag = stack.getTag();
        if (stack.isEmpty() || tag == null) {
            return 0;
        }
        return tag.getInt(NBT_KEY_UPGRADE_LEVEL);
    }

    /**
     * Sets the upgrade level of the given item.
     * @param stack The item to modify.
     * @param level The new upgrade level.
     */
    public static void setUpgradeLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putInt(NBT_KEY_UPGRADE_LEVEL, level);
    }

    /**
     * Checks if the item has been upgraded.
     * @param stack The item to check.
     * @return True if upgraded (level > 0), false otherwise.
     */
    public static boolean isUpgraded(ItemStack stack) {
        return getUpgradeLevel(stack) > 0;
    }
}
