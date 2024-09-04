package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public final class FluidContainmentLogic extends NetLogicEntry<FluidContainmentLogic, CompoundTag> {

    public static final NetLogicEntryType<FluidContainmentLogic> TYPE = new NetLogicEntryType<>("FluidContainment", () -> new FluidContainmentLogic().contain(FluidState.LIQUID));

    private int maximumTemperature;

    private final Set<ResourceLocation> containableAttributes = new ObjectOpenHashSet<>();
    private @NotNull EnumSet<FluidState> containableStates = EnumSet.noneOf(FluidState.class);

    public FluidContainmentLogic() {
        super(TYPE);
    }

    public @NotNull FluidContainmentLogic getWith(Collection<FluidState> states,
                                                  @NotNull Collection<FluidAttribute> attributes,
                                                  int maximumTemperature) {
        FluidContainmentLogic logic = new FluidContainmentLogic();
        logic.containableStates.addAll(states);
        for (FluidAttribute attribute : attributes) {
            logic.contain(attribute);
        }
        logic.maximumTemperature = maximumTemperature;
        return logic;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic contain(FluidState state) {
        this.containableStates.add(state);
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic contain(@NotNull FluidAttribute attribute) {
        this.containableAttributes.add(attribute.getResourceLocation());
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic notContain(FluidState state) {
        this.containableStates.remove(state);
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic notContain(@NotNull FluidAttribute attribute) {
        this.containableAttributes.remove(attribute.getResourceLocation());
        return this;
    }

    public boolean contains(FluidState state) {
        return this.containableStates.contains(state);
    }

    public boolean contains(@NotNull FluidAttribute attribute) {
        return this.containableAttributes.contains(attribute.getResourceLocation());
    }

    public void setMaximumTemperature(int maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }

    public int getMaximumTemperature() {
        return maximumTemperature;
    }

    @Override
    public @NotNull FluidContainmentLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof FluidContainmentLogic logic) {
            if (this.containableAttributes.equals(logic.containableAttributes) &&
                    this.containableStates.equals(logic.containableStates)) {
                return this;
            } else {
                FluidContainmentLogic returnable = new FluidContainmentLogic();
                returnable.containableStates = EnumSet.copyOf(this.containableStates);
                returnable.containableStates.retainAll(logic.containableStates);
                returnable.containableAttributes.addAll(this.containableAttributes);
                returnable.containableAttributes.retainAll(logic.containableAttributes);
                return returnable;
            }
        }
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ResourceLocation loc : containableAttributes) {
            list.add(StringTag.valueOf(loc.toString()));
        }
        tag.put("Attributes", list);
        tag.putByteArray("States", GTUtil.setToMask(containableStates).toByteArray());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag list = nbt.getList("Attributes", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            containableAttributes.add(new ResourceLocation(list.getString(i)));
        }
        containableStates = GTUtil.maskToSet(FluidState.class, BitSet.valueOf(nbt.getByteArray("States")));
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {
        buf.writeVarInt(containableAttributes.size());
        for (ResourceLocation loc : containableAttributes) {
            buf.writeUtf(loc.toString());
        }
        buf.writeByteArray(GTUtil.setToMask(containableStates).toByteArray());
    }

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {
        int attributes = buf.readVarInt();
        for (int i = 0; i < attributes; i++) {
            containableAttributes.add(new ResourceLocation(buf.readUtf(255)));
        }
        containableStates = GTUtil.maskToSet(FluidState.class, BitSet.valueOf(buf.readByteArray(255)));
    }
}
