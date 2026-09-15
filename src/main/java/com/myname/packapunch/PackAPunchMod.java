package com.myname.packapunch;

import com.mojang.logging.LogUtils;
import com.myname.packapunch.network.ModNetworking;
import com.myname.packapunch.registry.ModBlockEntityTypes;
import com.myname.packapunch.registry.ModBlocks;
import com.myname.packapunch.registry.ModCreativeTabs;
import com.myname.packapunch.registry.ModItems;
import com.myname.packapunch.registry.ModMenuTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(PackAPunchMod.MOD_ID)
public class PackAPunchMod {

    public static final String MOD_ID = "packapunch";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PackAPunchMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("[PackAPunch] Mod is loading...");

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, com.myname.packapunch.config.ModConfig.SPEC, "packapunch-common.toml");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);

        LOGGER.info("[PackAPunch] Mod loaded successfully!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[PackAPunch] Common setup complete.");
        event.enqueueWork(() -> {
            ModNetworking.register();
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }

    private void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            java.util.List<String> upgrades = new java.util.ArrayList<>(com.myname.packapunch.config.ModConfig.UPGRADES.get());
            ModNetworking.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer), 
                new com.myname.packapunch.network.ClientboundSyncConfigPacket(upgrades));
        }
    }
}
