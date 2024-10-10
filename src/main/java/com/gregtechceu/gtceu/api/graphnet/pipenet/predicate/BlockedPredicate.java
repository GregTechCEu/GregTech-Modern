package com.gregtechceu.gtceu.api.graphnet.pipenet.predicate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.predicate.EdgePredicate;
import com.gregtechceu.gtceu.api.graphnet.predicate.NetPredicateType;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.ByteTag;

import org.jetbrains.annotations.NotNull;

public final class BlockedPredicate extends EdgePredicate<BlockedPredicate, ByteTag> {

    private static final BlockedPredicate INSTANCE = new BlockedPredicate();

    public static final NetPredicateType<BlockedPredicate> TYPE = new NetPredicateType<>(GTCEu.MOD_ID, "Blocked",
            () -> INSTANCE, INSTANCE);

    @Override
    public @NotNull NetPredicateType<BlockedPredicate> getType() {
        return TYPE;
    }

    @Override
    public ByteTag serializeNBT() {
        return ByteTag.valueOf((byte) 0);
    }

    @Override
    public void deserializeNBT(ByteTag nbt) {}

    @Override
    public boolean andy() {
        return true;
    }

    @Override
    public boolean test(IPredicateTestObject object) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockedPredicate;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
