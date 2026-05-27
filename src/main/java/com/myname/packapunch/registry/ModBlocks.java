package com.myname.packapunch.registry;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.block.PackAPunchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PackAPunchMod.MOD_ID);

    public static final RegistryObject<PackAPunchBlock> PACK_A_PUNCH_MACHINE =
            BLOCKS.register("pack_a_punch_machine", () -> new PackAPunchBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(5.0f, 1200.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK)
            ));
}
