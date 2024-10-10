package com.gregtechceu.gtceu.api.graphnet.edge;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.predicate.EdgePredicateHandler;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetEdge implements INBTSerializable<CompoundTag> {

    /**
     * For interacting with the internal graph representation ONLY, do not use or set this field otherwise.
     */
    @ApiStatus.Internal
    public @Nullable GraphEdge wrapper;

    private @Nullable EdgePredicateHandler predicateHandler;

    private @Nullable NetLogicData data;

    protected @Nullable NetNode getSource() {
        if (wrapper == null) return null;
        return wrapper.getSource().wrapped;
    }

    protected @Nullable NetNode getTarget() {
        if (wrapper == null) return null;
        return wrapper.getTarget().wrapped;
    }

    /**
     * Should only be used on fake edges that are not registered to the graph.
     */
    public void setData(@NotNull NetLogicData data) {
        if (this.wrapper == null) this.data = data;
    }

    /**
     * This data is transient and should not be written to.
     */
    public @NotNull NetLogicData getData() {
        if (this.data == null) {
            this.data = NetLogicData.unionNullable(getSource() == null ? null : getSource().getData(),
                    getTarget() == null ? null : getTarget().getData());
            // if we can't calculate it, create a new one just to guarantee nonnullness
            if (this.data == null) this.data = new NetLogicData();
        }
        return this.data;
    }

    @NotNull
    public EdgePredicateHandler getPredicateHandler() {
        if (predicateHandler == null) predicateHandler = new EdgePredicateHandler();
        return predicateHandler;
    }

    public boolean test(IPredicateTestObject object) {
        if (predicateHandler == null) return true;
        else return predicateHandler.test(object);
    }

    public double getDynamicWeight(IPredicateTestObject channel, IGraphNet graph, @Nullable SimulatorKey simulator,
                                   long queryTick, double defaultWeight) {
        return defaultWeight;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        // we don't need to write our NetLogicData to NBT because we can regenerate it from our nodes
        if (predicateHandler != null && !predicateHandler.shouldIgnore())
            tag.put("Predicate", predicateHandler.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Predicate")) {
            this.predicateHandler = new EdgePredicateHandler();
            this.predicateHandler.deserializeNBT(nbt.getList("Predicate", Tag.TAG_COMPOUND));
        }
    }
}
