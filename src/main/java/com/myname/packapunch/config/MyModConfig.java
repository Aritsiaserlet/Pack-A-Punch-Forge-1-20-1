package com.myname.packapunch.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class MyModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    // Unified Upgrade List
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UPGRADES;

    static {
        BUILDER.push("UpgradeSettings");

        // Unified List: format is "multiplier;item_id;count"
        UPGRADES = BUILDER
                .comment(
                        "List of upgrades. You can add as many as you want (up to 100+).",
                        "Format: <multiplier>;<item_id>;<cost_amount>",
                        "Example: 1.2;minecraft:diamond_block;12"
                )
                .defineListAllowEmpty("upgrades", List.of(
                        "1.2;minecraft:diamond_block;12",
                        "1.5;minecraft:diamond_block;24",
                        "2.0;minecraft:netherite_block;2"
                ), obj -> obj instanceof String);

        BUILDER.pop();
        COMMON_SPEC = BUILDER.build();
    }
}
