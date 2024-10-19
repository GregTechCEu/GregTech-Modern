package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import net.minecraft.util.ExtraCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class ItemPipeProperties implements IMaterialProperty<ItemPipeProperties> {

    public static final Codec<ItemPipeProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("priority").forGetter(val -> val.priority),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("transfer_rate").forGetter(val -> val.transferRate))
            .apply(instance, ItemPipeProperties::new));

    /**
     * Items will try to take the path with the lowest priority
     */
    private int priority;

    /**
     * rate in stacks per sec
     */
    private float transferRate;

    public ItemPipeProperties(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    /**
     * Default property constructor.
     */
    public ItemPipeProperties() {
        this(1, 0.25f);
    }

    /**
     * Retrieves the priority of the item pipe
     *
     * @return The item pipe priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the Priority of the item pipe
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Retrieve the transfer rate of the item pipe
     *
     * @return The transfer rate of the item pipe
     */
    public float getTransferRate() {
        return transferRate;
    }

    /**
     * Sets the transfer rate of the item pipe
     *
     * @param transferRate The transfer rate
     */
    public void setTransferRate(float transferRate) {
        this.transferRate = transferRate;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

        if (properties.hasProperty(PropertyKey.FLUID_PIPE)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperties that = (ItemPipeProperties) o;
        return priority == that.priority && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, transferRate);
    }

    @Override
    public String toString() {
        return "ItemPipeProperties{" +
                "priority=" + priority +
                ", transferRate=" + transferRate +
                '}';
    }
}
