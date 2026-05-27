package com.myname.packapunch.client;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.registry.ModMenuTypes;
import com.myname.packapunch.screen.PackAPunchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = PackAPunchMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    ModMenuTypes.PACK_A_PUNCH_MACHINE.get(),
                    PackAPunchScreen::new
            );
        });

        PackAPunchMod.LOGGER.info("[PackAPunch] Screen registered for Pack-a-Punch machine.");
    }
}
