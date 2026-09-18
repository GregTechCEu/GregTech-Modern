package brachy.modularui.factory;

import net.minecraft.resources.Identifier;

import brachy.modularui.api.IUIHolder;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class UIFactories {

    public static BlockEntityUIFactory blockEntity() {
        return BlockEntityUIFactory.INSTANCE;
    }

    public static SidedBlockEntityUIFactory sidedBlockEntity() {
        return SidedBlockEntityUIFactory.INSTANCE;
    }

    public static EntityUIFactory entity() {
        return EntityUIFactory.INSTANCE;
    }

    public static PlayerInventoryUIFactory playerInventory() {
        return PlayerInventoryUIFactory.INSTANCE;
    }

    public static SimpleUIFactory createSimple(Identifier name, IUIHolder<GuiData> holder) {
        return new SimpleUIFactory(name, holder);
    }

    public static SimpleUIFactory createSimple(Identifier name, Supplier<IUIHolder<GuiData>> holder) {
        return new SimpleUIFactory(name, holder);
    }

    @ApiStatus.Internal
    public static void init() {
        GuiManager.registerFactory(blockEntity());
        GuiManager.registerFactory(sidedBlockEntity());
        GuiManager.registerFactory(entity());
        GuiManager.registerFactory(playerInventory());
    }

    private UIFactories() {}
}
