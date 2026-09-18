package brachy.modularui.value.sync;

import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import brachy.modularui.widgets.slot.ModularSlot;

import io.netty.buffer.ByteBuf;
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

    public static <B extends ByteBuf, T> GenericSyncValue.Builder<B, T> generic(Class<T> type) {
        return GenericSyncValue.builder(type);
    }
}
