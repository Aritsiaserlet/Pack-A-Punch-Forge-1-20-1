package com.myname.packapunch.registry;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.blockentity.PackAPunchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PackAPunchMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<PackAPunchBlockEntity>> PACK_A_PUNCH_MACHINE = 
            BLOCK_ENTITY_TYPES.register("pack_a_punch_machine",
                    () -> BlockEntityType.Builder
                            .of(PackAPunchBlockEntity::new, ModBlocks.PACK_A_PUNCH_MACHINE.get())
                            .build(null)
            );
}
