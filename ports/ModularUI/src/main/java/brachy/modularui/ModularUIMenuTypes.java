package brachy.modularui;

import brachy.modularui.screen.ModularContainerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModularUIMenuTypes {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ModularUI.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ModularContainerMenu>> MODULAR_CONTAINER = MENU_TYPES.register(
            "modular",
            () -> IMenuTypeExtension.create(ModularContainerMenu::new));

    public static void register(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
