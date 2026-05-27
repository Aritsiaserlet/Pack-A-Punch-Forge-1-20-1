package com.myname.packapunch.registry;

import com.myname.packapunch.PackAPunchMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PackAPunchMod.MOD_ID);

    public static final RegistryObject<BlockItem> PACK_A_PUNCH_MACHINE =
            ITEMS.register("pack_a_punch_machine",
                    () -> new BlockItem(
                            ModBlocks.PACK_A_PUNCH_MACHINE.get(),
                            new Item.Properties()
                    )
            );
}
