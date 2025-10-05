package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.common.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class UIFactories {

    public static BlockEntityUIFactory blockEntity() {
        return BlockEntityUIFactory.INSTANCE;
    }

    public static SidedBlockEntityUIFactory sidedBlockEntity() {
        return SidedBlockEntityUIFactory.INSTANCE;
    }

    public static ItemUIFactory item() {
        return ItemUIFactory.INSTANCE;
    }

    public static MachineUIFactory machine() {
        return MachineUIFactory.INSTANCE;
    }

    public static CoverUIFactory cover() {
        return CoverUIFactory.INSTANCE;
    }

    public static SimpleUIFactory createSimple(ResourceLocation name, IUIHolder<GuiData> holder) {
        return new SimpleUIFactory(name, holder);
    }

    public static SimpleUIFactory createSimple(ResourceLocation name, Supplier<IUIHolder<GuiData>> holder) {
        return new SimpleUIFactory(name, holder);
    }

    @ApiStatus.Internal
    public static void init() {
        GuiManager.registerFactory(blockEntity());
        GuiManager.registerFactory(sidedBlockEntity());
        GuiManager.registerFactory(item());
        GuiManager.registerFactory(machine());
        GuiManager.registerFactory(cover());
    }

    private UIFactories() {}
}
