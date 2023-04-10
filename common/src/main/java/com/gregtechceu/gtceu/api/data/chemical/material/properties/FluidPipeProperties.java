package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties> {
    @Getter @Setter
    private long throughput;
    @Getter @Setter
    private int channels;
    @Getter @Setter
    private int maxFluidTemperature;
    @Getter @Setter
    private boolean gasProof;
    @Getter @Setter
    private boolean acidProof;
    @Getter @Setter
    private boolean cryoProof;
    @Getter @Setter
    private boolean plasmaProof;

    public FluidPipeProperties(int maxFluidTemperature, long throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, int channels) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.channels = channels;
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties(int maxFluidTemperature, long throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    public boolean acceptFluid(FluidStack fluidStack) {
        var fluid = fluidStack.getFluid();
        var fluidProperty = GTFluids.MATERIAL_FLUIDS.get(fluid);
        var plasmaProperty = GTFluids.PLASMA_FLUIDS.get(fluid);
        var fluidType = fluidProperty != null ? fluidProperty.getFluidType() : plasmaProperty != null ? FluidTypes.PLASMA : null;
        var temp = FluidHelper.getTemperature(fluidStack);

        if (temp <= 120 && !isCryoProof()) return false;
        if (temp > getMaxFluidTemperature()) return false;

        if (fluidType == FluidTypes.GAS) return isGasProof();
        if (fluidType == FluidTypes.PLASMA) return isPlasmaProof();
        if (fluidType == FluidTypes.ACID) return isAcidProof();

        return true;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

        if (properties.hasProperty(PropertyKey.ITEM_PIPE)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidPipeProperties that)) return false;
        return maxFluidTemperature == that.maxFluidTemperature &&
            throughput == that.throughput && gasProof == that.gasProof && channels == that.channels;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFluidTemperature, throughput, gasProof, channels);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "maxFluidTemperature=" + maxFluidTemperature +
                ", throughput=" + throughput +
                ", gasProof=" + gasProof +
                ", acidProof=" + acidProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", channels=" + channels +
                '}';
    }
}
