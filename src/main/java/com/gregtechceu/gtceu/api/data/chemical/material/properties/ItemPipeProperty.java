package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.utils.GTMath;
import lombok.Getter;
import lombok.Setter;

public class ItemPipeProperty implements IMaterialProperty {

    /**
     * Items will try to take the path with the lowest priority.
     */
    @Getter
    @Setter
    private int priority;

    /**
     * Transfer rate in stacks per sec
     */
    @Getter
    @Setter
    private float transferRate;

    public ItemPipeProperty(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    /**
     * Default property constructor.
     */
    public ItemPipeProperty() {
        this(1, 0.25f);
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
        ItemPipeProperty that = (ItemPipeProperty) o;
        return priority == that.priority && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return GTMath.hashInts(priority, Float.hashCode(transferRate));
    }

    @Override
    public String toString() {
        return "ItemPipeProperties{" +
                "priority=" + priority +
                ", transferRate=" + transferRate +
                '}';
    }
}
