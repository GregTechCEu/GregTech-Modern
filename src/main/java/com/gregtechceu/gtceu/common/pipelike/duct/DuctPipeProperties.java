package com.gregtechceu.gtceu.common.pipelike.duct;

import lombok.Getter;
import lombok.Setter;

public class DuctPipeProperties {

    /**
     * rate in stacks per sec
     */
    @Getter
    @Setter
    private float transferRate;

    public DuctPipeProperties(float transferRate) {
        this.transferRate = transferRate;
    }

    /**
     * Default property constructor.
     */
    public DuctPipeProperties() {
        this(0.25f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DuctPipeProperties that = (DuctPipeProperties) o;
        return Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(transferRate);
    }

    @Override
    public String toString() {
        return "DuctPipeProperties{" +
                "transferRate=" + transferRate +
                '}';
    }
}
