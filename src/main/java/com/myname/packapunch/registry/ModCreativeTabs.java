package com.myname.packapunch.registry;

import com.myname.packapunch.PackAPunchMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PackAPunchMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PACK_A_PUNCH_TAB =
            CREATIVE_MODE_TABS.register("pack_a_punch_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("creativetab.packapunch_tab"))
                            .icon(() -> new ItemStack(ModBlocks.PACK_A_PUNCH_MACHINE.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModBlocks.PACK_A_PUNCH_MACHINE.get());
                            })
                            .build()
            );
}
