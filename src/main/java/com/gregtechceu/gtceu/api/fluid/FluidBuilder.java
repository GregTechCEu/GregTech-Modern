package com.gregtechceu.gtceu.api.fluid;

import com.gregtechceu.gtceu.api.fluid.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.GTBucketItem;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.material.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.fluid.FluidConstants.*;

@Accessors(fluent = true, chain = true)
public class FluidBuilder {

    private static final int INFER_TEMPERATURE = -1;
    private static final int INFER_COLOR = 0xFFFFFFFF;
    private static final int INFER_DENSITY = -1;
    private static final int INFER_LUMINOSITY = -1;
    private static final int INFER_VISCOSITY = -1;

    @Setter
    @Nullable
    private String name = null;
    @Setter
    @Nullable
    private String translation = null;

    private final Collection<FluidAttribute> attributes = new ArrayList<>();

    @Setter
    private FluidState state = FluidState.LIQUID;
    private int temperature = INFER_TEMPERATURE;
    private int color = INFER_COLOR;
    private boolean isColorEnabled = true;
    @Setter
    private int density = INFER_DENSITY;
    private int luminosity = INFER_LUMINOSITY;
    private int viscosity = INFER_VISCOSITY;
    @Setter
    private int burnTime = 0;

    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    @Nullable
    private ResourceLocation still = null;
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    @Nullable
    private ResourceLocation flowing = null;
    @Getter
    private boolean hasCustomStill = false;
    @Getter
    private boolean hasCustomFlowing = false;

    private boolean hasFluidBlock = false;
    private boolean hasBucket = true;

    public FluidBuilder() {}

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
     * @param density the density in g/cm^3
     * @return this
     */
    @Tolerate
    public @NotNull FluidBuilder density(double density) {
        return density(convertToMCDensity(density));
    }

    /**
     * Converts a density value in g/cm^3 to an MC fluid density by comparison to air's density.
     * 
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
     * 
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
    public @NotNull FluidBuilder attributes(@NotNull FluidAttribute @NotNull... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    /**
     * Mark this fluid as having a custom still texture
     * 
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
     * @param hasCustomStill   if the fluid has a custom still texture
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

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Supplier<? extends Fluid> build(Material material, FluidStorageKey key,
                                                    GTRegistrate registrate) {
        determineName(material, key);
        determineTextures(material, key);

        if (name == null) {
            throw new IllegalStateException("Could not determine fluid name");
        }

        // noinspection ConstantValue: in case of mistakes
        if (state == null) {
            if (key.getDefaultFluidState() != null) {
                state = key.getDefaultFluidState();
            } else {
                state = FluidState.LIQUID; // default fallback
            }
        }

        determineTemperature(material);
        determineColor(material);
        determineDensity();
        determineLuminosity(material);
        determineViscosity(material);

        final String langKey = this.translation != null ? this.translation : key.getTranslationKeyFor(material);
        // noinspection DataFlowIssue
        var builder = registrate.fluid(this.name, this.still, this.flowing,
                (p, $1, $2) -> makeFluidType(registrate, p, material, key, langKey),
                (p) -> new GTFluid.Flowing(this.state, this.burnTime, p))
                .source((p) -> new GTFluid.Source(this.state, this.burnTime, p))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop());
        if (this.hasFluidBlock) {
            builder.block()
                    .blockstate((ctx, prov) -> prov
                            .simpleBlock(ctx.getEntry(), prov.models().getBuilder(this.name)
                                    .texture("particle", this.still)))
                    .color(() -> () -> (state, level, pos, index) -> {
                        return IClientFluidTypeExtensions.of(state.getFluidState())
                                .getTintColor(state.getFluidState(), level, pos);
                    })
                    .register();
        } else {
            // noinspection DataFlowIssue
            builder.noBlock().fluidProperties(p -> p.block(null));
        }
        if (this.hasBucket) {
            builder.bucket((fluid, properties) -> new GTBucketItem(fluid, properties, material, langKey))
                    .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                    .color(() -> () -> GTBucketItem::color)
                    .register();
        } else {
            // noinspection DataFlowIssue
            builder.noBucket().fluidProperties(p -> p.bucket(null));
        }

        builder.onRegister(fluid -> {
            if (fluid.getSource() instanceof GTFluid gtSource) attributes.forEach(gtSource::addAttribute);
            if (fluid.getFlowing() instanceof GTFluid gtFlowing) attributes.forEach(gtFlowing::addAttribute);
        });
        return builder.register()::getSource;
    }

    private void determineName(@NotNull Material material, @Nullable FluidStorageKey key) {
        if (name != null) return;
        if (material.isNull() || key == null) throw new IllegalArgumentException("Fluid must have a name");
        name = key.getRegistryNameFor(material);
    }

    @ApiStatus.Internal
    public void determineTextures(@NotNull Material material, @Nullable FluidStorageKey key) {
        if (hasCustomStill || material.isNull() || key == null) {
            still = ResourceLocation.fromNamespaceAndPath(material.getModid(), "block/fluids/fluid." + name);
        } else {
            still = key.getIconType().getBlockTexturePath(material.getMaterialIconSet(), true);
        }

        if (hasCustomFlowing) {
            flowing = ResourceLocation.fromNamespaceAndPath(material.getModid(),
                    "block/fluids/fluid." + name + "_flow");
        } else {
            // FIXME this is actually wrong, flowing fluids should have 32x32 textures (double the size of still ones).
            flowing = still;
        }
    }

    private void determineTemperature(@NotNull Material material) {
        if (temperature != INFER_TEMPERATURE) return;
        if (material.isNull()) {
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
                    case PLASMA -> {
                        if (material.hasFluid() && material.getFluidBuilder() != null &&
                                material.getFluidBuilder() != material.getFluidBuilder(FluidStorageKeys.PLASMA)) {
                            yield BASE_PLASMA_TEMPERATURE + material.getFluidBuilder().temperature;
                        }
                        yield BASE_PLASMA_TEMPERATURE;
                    }
                };
            } else {
                temperature = property.getBlastTemperature() + switch (state) {
                    case LIQUID -> LIQUID_TEMPERATURE_OFFSET;
                    case GAS -> GAS_TEMPERATURE_OFFSET;
                    case PLASMA -> BASE_PLASMA_TEMPERATURE;
                };
            }
        }
    }

    private void determineColor(@NotNull Material material) {
        if (color != INFER_COLOR) return;
        if (isColorEnabled && !material.isNull()) {
            color = GTUtil.convertRGBtoARGB(material.getMaterialRGB());
        }
    }

    private void determineDensity() {
        if (density != INFER_DENSITY) return;
        density = switch (state) {
            case LIQUID -> DEFAULT_LIQUID_DENSITY;
            case GAS -> DEFAULT_GAS_DENSITY;
            case PLASMA -> DEFAULT_PLASMA_DENSITY;
        };
    }

    private void determineLuminosity(@NotNull Material material) {
        if (luminosity != INFER_LUMINOSITY) return;
        if (state == FluidState.PLASMA) {
            luminosity = 15;
        } else if (!material.isNull()) {
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

    private void determineViscosity(@NotNull Material material) {
        if (viscosity != INFER_VISCOSITY) return;
        viscosity = switch (state) {
            case LIQUID -> {
                if (!material.isNull() && material.hasFlag(MaterialFlags.STICKY)) {
                    yield STICKY_LIQUID_VISCOSITY;
                }
                yield DEFAULT_LIQUID_VISCOSITY;
            }
            case GAS -> DEFAULT_GAS_VISCOSITY;
            case PLASMA -> DEFAULT_PLASMA_VISCOSITY;
        };
    }

    private FluidType makeFluidType(AbstractRegistrate<?> owner, FluidType.Properties properties,
                                    Material material, FluidStorageKey key, String langKey) {
        properties.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .temperature(this.temperature)
                .density(this.density)
                .lightLevel(this.luminosity)
                .viscosity(this.viscosity);
        FluidType type = new FluidType(properties) {

            @Override
            public String getDescriptionId() {
                return material.getUnlocalizedName();
            }

            @Override
            public Component getDescription() {
                return Component.translatable(langKey, material.getLocalizedName());
            }

            @Override
            public Component getDescription(FluidStack stack) {
                return this.getDescription();
            }
        };
        OneTimeEventReceiver.addModListener(owner, RegisterClientExtensionsEvent.class, event -> {
            final int color = isColorEnabled ? this.color : INFER_COLOR;
            if (still == null || flowing == null) {
                this.determineTextures(material, key);
            }
            event.registerFluidType(new GTClientFluidTypeExtensions(still, flowing, color), type);
        });
        return type;
    }
}
