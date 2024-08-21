package com.gregtechceu.gtceu.api.graphnet;

import com.gregtechceu.gtceu.api.graphnet.alg.iter.IteratorFactory;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public abstract class NetNode implements ITagSerializable<CompoundTag>, IContentChangeAware {

    @Getter
    @Setter
    public Runnable onContentsChanged = () -> {};

    /**
     * For interacting with the internal graph representation ONLY, do not use or set this field otherwise.
     */
    @ApiStatus.Internal
    public GraphVertex wrapper;

    private boolean isActive = false;

    private final @NotNull IGraphNet net;
    private final @NotNull NetLogicData data;
    private @Nullable NetGroup group = null;

    @Nullable
    private IteratorFactory<? extends INetPath<?, ?>> pathCache = null;

    public NetNode(@NotNull IGraphNet net) {
        this.net = net;
        this.data = net.getDefaultNodeData();
    }

    public @NotNull IGraphNet getNet() {
        return net;
    }

    /**
     * Determines whether the node should be treated as a valid destination of pathing algorithms
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets whether the node should be treated as a valid destination of pathing algorithms
     */
    public void setActive(boolean active) {
        if (isActive != active) {
            isActive = active;
            NetGroup group = getGroupUnsafe();
            if (group != null) group.clearPathCaches();
            else this.clearPathCache();
            if (onContentsChanged != null) {
                onContentsChanged.run();
            }
        }
    }

    public @NotNull NetLogicData getData() {
        return data;
    }

    public boolean traverse(long queryTick, boolean simulate) {
        return true;
    }

    @Nullable
    public Iterator<? extends INetPath<?, ?>> getPathCache(IPredicateTestObject testObject,
                                                           @Nullable SimulatorKey simulator, long queryTick) {
        if (pathCache == null) return null;
        return pathCache.newIterator(net.getGraph(), testObject, simulator, queryTick);
    }

    /**
     * Sets the path cache to the provided iterator factory. Returns itself for convenience.
     *
     * @param pathCache The new cache.
     * @return The new cache.
     */
    public NetNode setPathCache(IteratorFactory<? extends INetPath<?, ?>> pathCache) {
        this.pathCache = pathCache;
        return this;
    }

    public void clearPathCache() {
        this.pathCache = null;
    }

    @NotNull
    public NetGroup getGroupSafe() {
        if (this.group == null) {
            new NetGroup(this.getNet()).addNode(this);
            // addNodes automatically sets our group to the new group
        }
        return this.group;
    }

    @Nullable
    public NetGroup getGroupUnsafe() {
        return this.group;
    }

    public NetGroup setGroup(NetGroup group) {
        this.group = group;
        return group;
    }

    /**
     * Use this to remove references that would keep this node from being collected by the garbage collector.
     * This is called when a node is removed from the graph and should be discarded.
     */
    public void onRemove() {}

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Data", this.data.serializeNBT());
        tag.putBoolean("IsActive", this.isActive());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.isActive = nbt.getBoolean("IsActive");
        this.data.clearData();
        this.data.deserializeNBT((ListTag) nbt.get("Data"));
    }

    /**
     * Used to determine if two nodes are equal, for graph purposes.
     * Should not change over the lifetime of a node, except when {@link #deserializeNBT(CompoundTag)} is called.
     * 
     * @return equivalency data. Needs to work with {@link Objects#equals(Object, Object)}
     */
    public abstract Object getEquivalencyData();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetNode node = (NetNode) o;
        return Objects.equals(getEquivalencyData(), node.getEquivalencyData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEquivalencyData());
    }
}
