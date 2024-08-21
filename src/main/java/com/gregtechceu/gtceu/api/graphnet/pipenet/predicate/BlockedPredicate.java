package com.gregtechceu.gtceu.api.graphnet.pipenet.predicate;

import com.gregtechceu.gtceu.api.graphnet.predicate.EdgePredicate;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.ByteTag;
import org.jetbrains.annotations.NotNull;

public final class BlockedPredicate extends EdgePredicate<BlockedPredicate, ByteTag> {

    public static final BlockedPredicate INSTANCE = new BlockedPredicate();

    private BlockedPredicate() {
        super("Blocked");
    }

    @Override
    @Deprecated
    public @NotNull BlockedPredicate getNew() {
        return INSTANCE;
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
