package com.myname.packapunch.registry;

import com.myname.packapunch.PackAPunchMod;
import com.myname.packapunch.menu.PackAPunchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PackAPunchMod.MOD_ID);

    public static final RegistryObject<MenuType<PackAPunchMenu>> PACK_A_PUNCH_MACHINE = 
            MENU_TYPES.register("pack_a_punch_machine",
                    () -> IForgeMenuType.create(PackAPunchMenu::new)
            );
}
