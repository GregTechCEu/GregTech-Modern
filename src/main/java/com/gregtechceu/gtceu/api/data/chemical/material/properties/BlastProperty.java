package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@AllArgsConstructor
public class BlastProperty implements IMaterialProperty<BlastProperty> {
    public static final Codec<BlastProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.POSITIVE_INT.fieldOf("blast_temperature").forGetter(val -> val.blastTemperature),
        GasTier.CODEC.fieldOf("gas_tier").forGetter(val -> val.gasTier),
        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("duration_override", -1).forGetter(val -> val.durationOverride),
        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("eut_override", -1).forGetter(val -> val.EUtOverride)
    ).apply(instance, BlastProperty::new));

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    @Getter
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     * <p>
     * Default: null, meaning no Gas EBF recipes.
     */
    @Getter @Setter
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    @Getter @Setter
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120.
     */
    @Getter @Setter
    private int EUtOverride = -1;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    /**
     * Default property constructor.
     */
    public BlastProperty() {
        this(0);
    }

    public void setBlastTemperature(int blastTemp) {
        if (blastTemp <= 0) throw new IllegalArgumentException("Blast Temperature must be greater than zero!");
        this.blastTemperature = blastTemp;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public enum GasTier implements StringRepresentable {
        // Tiers used by GTCEu
        LOW,
        MID,
        HIGH,

        // Tiers reserved for addons
        HIGHER,
        HIGHEST;

        public static final GasTier[] VALUES = values();
        public static final Codec<GasTier> CODEC = StringRepresentable.fromEnum(GasTier::values);

        @Override
        public String getSerializedName() {
            return name().toUpperCase(Locale.ROOT);
        }
    }
}
