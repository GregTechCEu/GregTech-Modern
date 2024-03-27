package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.capability.IPropertyFluidFilter;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor
public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties>, IPropertyFluidFilter {

    /**
     * The maximum number of channels any fluid pipe can have
     */
    public static final int MAX_PIPE_CHANNELS = 9;
    public static final Codec<FluidPipeProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("max_temperature").forGetter(val -> val.maxFluidTemperature),
        Codec.LONG.fieldOf("throughput").forGetter(val -> val.throughput),
        Codec.BOOL.optionalFieldOf("gas_proof", true).forGetter(val -> val.gasProof),
        Codec.BOOL.optionalFieldOf("cryo_proof", false).forGetter(val -> val.cryoProof),
        Codec.BOOL.optionalFieldOf("plasma_proof", false).forGetter(val -> val.plasmaProof),
        Codec.simpleMap(FluidAttribute.CODEC, Codec.BOOL, FluidAttribute.CODEC_KEYS)
            .codec()
            .optionalFieldOf("can_contain", Map.of())
            .forGetter(val -> val.containmentPredicate)
    ).apply(instance, FluidPipeProperties::new));

    @Getter
    @Setter
    private long throughput;
    @Getter
    @Setter
    private int channels;
    @Getter
    @Setter
    private int maxFluidTemperature;
    @Getter
    @Setter
    private boolean gasProof;
    @Getter
    @Setter
    private boolean cryoProof;
    @Getter
    @Setter
    private boolean plasmaProof;

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    public FluidPipeProperties(int maxFluidTemperature, long throughput, boolean gasProof, boolean cryoProof, boolean plasmaProof, Map<FluidAttribute, Boolean> canContain) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.channels = 1;
        this.containmentPredicate.putAll(canContain);
    }

    public FluidPipeProperties(int maxFluidTemperature, long throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int channels) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.channels = channels;
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties(int maxFluidTemperature, long throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
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
                ", acidProof=" + isAcidProof() +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", channels=" + channels +
                '}';
    }

    public long getPlatformThroughput() {
        return getThroughput() * FluidHelper.getBucket() / 1000;
    }

    @Override
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }
}
