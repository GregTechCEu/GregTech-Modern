package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.*;

public class SyncHandlers {

    private SyncHandlers() {}

    public static IntSyncValue intNumber(IntSupplier getter, IntConsumer setter) {
        return new IntSyncValue(getter, setter);
    }

    public static LongSyncValue longNumber(LongSupplier getter, LongConsumer setter) {
        return new LongSyncValue(getter, setter);
    }

    public static BooleanSyncValue bool(BooleanSupplier getter, BooleanConsumer setter) {
        return new BooleanSyncValue(getter, setter);
    }

    public static DoubleSyncValue doubleNumber(DoubleSupplier getter, DoubleConsumer setter) {
        return new DoubleSyncValue(getter, setter);
    }

    public static StringSyncValue string(Supplier<String> getter, Consumer<String> setter) {
        return new StringSyncValue(getter, setter);
    }

    public static ModularSlot itemSlot(IItemHandlerModifiable inventory, int index) {
        return new ModularSlot(inventory, index);
    }

    public static FluidSlotSyncHandler fluidSlot(IFluidTank fluidTank) {
        return new FluidSlotSyncHandler(fluidTank);
    }

    public static <T extends Enum<T>> EnumSyncValue<T> enumValue(Class<T> clazz, Supplier<T> getter,
                                                                 Consumer<T> setter) {
        return new EnumSyncValue<>(clazz, getter, setter);
    }
}
