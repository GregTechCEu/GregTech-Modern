package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import brachy.modularui.screen.ModularContainerMenu;

public class GTMenuTypes {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            GTCEu.MOD_ID);

    public static final RegistryObject<MenuType<ModularContainerMenu>> MODULAR_CONTAINER = MENU_TYPES.register(
            "modular",
            () -> IForgeMenuType.create(ModularContainerMenu::new));

    public static void init(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
