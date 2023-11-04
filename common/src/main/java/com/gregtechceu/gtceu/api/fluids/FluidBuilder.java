package com.gregtechceu.gtceu.api.fluids;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.fluids.FluidConstants.*;

public class FluidBuilder {

    private static final int INFER_TEMPERATURE = -1;
    private static final int INFER_COLOR = 0xFFFFFFFF;
    private static final int INFER_DENSITY = -1;
    private static final int INFER_LUMINOSITY = -1;
    private static final int INFER_VISCOSITY = -1;

    private String name = null;
    private String translationKey = null;

    private final Collection<FluidAttribute> attributes = new ArrayList<>();

    private FluidState state = FluidState.LIQUID;
    private int temperature = INFER_TEMPERATURE;
    private int color = INFER_COLOR;
    private boolean isColorEnabled = true;
    private int density = INFER_DENSITY;
    private int luminosity = INFER_LUMINOSITY;
    private int viscosity = INFER_VISCOSITY;
    @Setter
    private int burnTime = -1;

    @Getter
    private ResourceLocation still = null;
    @Getter
    private ResourceLocation flowing = null;
    private boolean hasCustomStill = false;
    private boolean hasCustomFlowing = false;

    private boolean hasFluidBlock = false;
    private boolean hasBucket = true;

    public FluidBuilder() {}

    /**
     * @param name the name of the fluid
     * @return this
     */
    public @NotNull FluidBuilder name(@NotNull String name) {
        this.name = name;
        return this;
    }

    /**
     * @param translationKey the translation key of the fluid
     * @return this
     */
    public @NotNull FluidBuilder translation(@NotNull String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    /**
     * @param state the fluid's state of matter
     * @return this
     */
    public @NotNull FluidBuilder state(@NotNull FluidState state) {
        this.state = state;
        return this;
    }

    /**
     * @param temperature the temperature of the fluid in Kelvin
     * @return this;
     */
    public @NotNull FluidBuilder temperature(int temperature) {
        Preconditions.checkArgument(temperature > 0, "temperature must be > 0");
        this.temperature = temperature;
        return this;
    }

    /**
     * The color may be in either {@code RGB} or {@code ARGB} format.
     * RGB format will assume an alpha of {@code 0xFF}.
     *
     * @param color the color
     * @return this
     */
    public @NotNull FluidBuilder color(int color) {
        this.color = GTUtil.convertRGBtoARGB(color);
        if (this.color == INFER_COLOR) {
            return disableColor();
        }
        return this;
    }

    /**
     * Disables coloring the fluid. A color should still be specified.
     *
     * @return this
     */
    public @NotNull FluidBuilder disableColor() {
        this.isColorEnabled = false;
        return this;
    }

    /**
     * @param mcDensity the density in MC's units
     * @return this
     */
    public @NotNull FluidBuilder density(int mcDensity) {
        this.density = mcDensity;
        return this;
    }

    /**
     * @param density the density in g/cm^3
     * @return this
     */
    public @NotNull FluidBuilder density(double density) {
        return density(convertToMCDensity(density));
    }

    /**
     * Converts a density value in g/cm^3 to an MC fluid density by comparison to air's density.
     * @param density the density to convert
     * @return the MC integer density
     */
    private static int convertToMCDensity(double density) {
        // conversion formula from GT6
        if (density > 0.001225) {
            return (int) (1000 * density);
        } else if (density < 0.001225) {
            return (int) (-0.1 / density);
        }
        return 0;
    }

    /**
     * @param luminosity of the fluid from [0, 16)
     * @return this
     */
    public @NotNull FluidBuilder luminosity(int luminosity) {
        Preconditions.checkArgument(luminosity >= 0 && luminosity < 16, "luminosity must be >= 0 and < 16");
        this.luminosity = luminosity;
        return this;
    }

    /**
     * @param mcViscosity the MC viscosity of the fluid
     * @return this
     */
    public @NotNull FluidBuilder viscosity(int mcViscosity) {
        Preconditions.checkArgument(mcViscosity >= 0, "viscosity must be >= 0");
        this.viscosity = mcViscosity;
        return this;
    }

    /**
     * @param viscosity the viscosity of the fluid in Poise
     * @return this
     */
    public @NotNull FluidBuilder viscosity(double viscosity) {
        return viscosity(convertViscosity(viscosity));
    }

    /**
     * Converts viscosity in Poise to MC viscosity
     * @param viscosity the viscosity to convert
     * @return the converted value
     */
    private static int convertViscosity(double viscosity) {
        return (int) (viscosity * 10000);
    }

    /**
     * @param attribute the attribute to add
     * @return this
     */
    public @NotNull FluidBuilder attribute(@NotNull FluidAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    /**
     * @param attributes the attributes to add
     * @return this
     */
    public @NotNull FluidBuilder attributes(@NotNull FluidAttribute @NotNull ... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    /**
     * Mark this fluid as having a custom still texture
     * @return this
     */
    public @NotNull FluidBuilder customStill() {
        return textures(true);
    }

    /**
     * @param hasCustomStill if the fluid has a custom still texture
     * @return this
     */
    public @NotNull FluidBuilder textures(boolean hasCustomStill) {
        this.hasCustomStill = hasCustomStill;
        this.isColorEnabled = false;
        return this;
    }

    /**
     * @param hasCustomStill if the fluid has a custom still texture
     * @param hasCustomFlowing if the fluid has a custom flowing texture
     * @return this
     */
    public @NotNull FluidBuilder textures(boolean hasCustomStill, boolean hasCustomFlowing) {
        this.hasCustomStill = hasCustomStill;
        this.hasCustomFlowing = hasCustomFlowing;
        this.isColorEnabled = false;
        return this;
    }

    /**
     * Generate a fluid block for the fluid
     *
     * @return this
     */
    public @NotNull FluidBuilder block() {
        this.hasFluidBlock = true;
        return this;
    }

    /**
     * Disables the auto-generated fluid bucket for the fluid
     *
     * @return this
     */
    public @NotNull FluidBuilder disableBucket() {
        this.hasBucket = false;
        return this;
    }

    public @NotNull Supplier<? extends Fluid> build(@NotNull String modid, Material material, FluidStorageKey key) {
        determineName(material, key);
        determineTextures(material, key, modid);

        if (name == null) {
            throw new IllegalStateException("Could not determine fluid name");
        }

        determineTemperature(material);
        determineColor(material);
        determineDensity();
        determineLuminosity(material);
        determineViscosity(material);
        IGTFluidBuilder builder = GTRegistries.REGISTRATE.createFluid(name, this.translationKey != null ? this.translationKey : key.getTranslationKeyFor(material), material, this.still, this.flowing)
                .temperature(this.temperature)
                .density(this.density)
                .luminance(this.luminosity)
                .viscosity(this.viscosity)
                .hasBlock(this.hasFluidBlock)
                .hasBucket(this.hasBucket)
                .burnTime(this.burnTime)
                .state(this.state);

        if (isColorEnabled) {
            builder.color(this.color);
        }

        builder.onFluidRegister(fluid -> {
            if (fluid instanceof GTFluid gtFluid) {
                attributes.forEach(gtFluid::addAttribute);
            }
        });

        return builder.registerFluid();
    }

    private void determineName(@Nullable Material material, @Nullable FluidStorageKey key) {
        if (name != null) return;
        if (material == null || key == null) throw new IllegalArgumentException("Fluid must have a name");
        name = key.getRegistryNameFor(material.getName());
    }

    private void determineTextures(@Nullable Material material, @Nullable FluidStorageKey key, @NotNull String modid) {
        if (material != null && key != null) {
            if (hasCustomStill) {
                still = new ResourceLocation(modid, "block/fluids/fluid." + name);
            } else {
                still = key.getIconType().getBlockTexturePath(material.getMaterialIconSet(), true);
            }
        } else {
            still = new ResourceLocation(modid, "block/fluids/fluid." + name);
        }

        if (hasCustomFlowing) {
            flowing = new ResourceLocation(modid, "block/fluids/fluid." + name + "_flow");
        } else {
            flowing = still;
        }
    }

    private void determineTemperature(@Nullable Material material) {
        if (temperature != INFER_TEMPERATURE) return;
        if (material == null) {
            temperature = ROOM_TEMPERATURE;
        } else {
            BlastProperty property = material.getProperty(PropertyKey.BLAST);
            if (property == null) {
                temperature = switch (state) {
                    case LIQUID -> {
                        if (material.hasProperty(PropertyKey.DUST)) {
                            yield SOLID_LIQUID_TEMPERATURE;
                        }
                        yield ROOM_TEMPERATURE;
                    }
                    case GAS -> ROOM_TEMPERATURE;
                    case PLASMA -> BASE_PLASMA_TEMPERATURE;
                    case MOLTEN -> SOLID_LIQUID_TEMPERATURE;
                };
            } else {
                temperature = property.getBlastTemperature() + switch (state) {
                    case LIQUID -> LIQUID_TEMPERATURE_OFFSET;
                    case GAS -> GAS_TEMPERATURE_OFFSET;
                    case PLASMA -> BASE_PLASMA_TEMPERATURE;
                    case MOLTEN -> SOLID_LIQUID_TEMPERATURE;
                };
            }
        }
    }

    private void determineColor(@Nullable Material material) {
        if (color != INFER_COLOR) return;
        if (isColorEnabled && material != null) {
            color = Platform.isForge() ? GTUtil.convertRGBtoARGB(material.getMaterialRGB()) : material.getMaterialRGB();
        }
    }

    private void determineDensity() {
        if (density != INFER_DENSITY) return;
        density = switch (state) {
            case LIQUID -> DEFAULT_LIQUID_DENSITY;
            case GAS -> DEFAULT_GAS_DENSITY;
            case PLASMA -> DEFAULT_PLASMA_DENSITY;
            case MOLTEN -> DEFAULT_MOLTEN_DENSITY;
        };
    }

    private void determineLuminosity(@Nullable Material material) {
        if (luminosity != INFER_LUMINOSITY) return;
        if (state == FluidState.PLASMA) {
            luminosity = 15;
        } else if (material != null) {
            if (material.hasFlag(MaterialFlags.PHOSPHORESCENT)) {
                luminosity = 15;
            } else if (state == FluidState.LIQUID && material.hasProperty(PropertyKey.DUST)) {
                // liquids only glow if not phosphorescent
                luminosity = 10;
            } else {
                luminosity = 0;
            }
        } else {
            luminosity = 0;
        }
    }

    private void determineViscosity(@Nullable Material material) {
        if (viscosity != INFER_VISCOSITY) return;
        viscosity = switch (state) {
            case LIQUID -> {
                if (material != null && material.hasFlag(MaterialFlags.STICKY)) {
                    yield STICKY_LIQUID_VISCOSITY;
                }
                yield DEFAULT_LIQUID_VISCOSITY;
            }
            case GAS -> DEFAULT_GAS_VISCOSITY;
            case PLASMA -> DEFAULT_PLASMA_VISCOSITY;
            case MOLTEN -> DEFAULT_MOLTEN_VISCOSITY;
        };
    }
}
