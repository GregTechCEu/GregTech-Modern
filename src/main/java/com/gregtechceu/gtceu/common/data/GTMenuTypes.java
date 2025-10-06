package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.client.mui.screen.ContainerScreenWrapper;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;

import com.tterrag.registrate.util.entry.MenuEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTMenuTypes {

    @SuppressWarnings("deprecation")
    public static final MenuEntry<ModularContainerMenu> MODULAR_CONTAINER = REGISTRATE
            .<ModularContainerMenu, ContainerScreenWrapper>menu("modular",
                    ModularContainerMenu::new, () -> ContainerScreenWrapper::new)
            .register();

    public static void init() {}
}
