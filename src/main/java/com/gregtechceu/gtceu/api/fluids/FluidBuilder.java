package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.registrate.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.block.GTLiquidBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.item.GTBucketItem;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
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

import static com.gregtechceu.gtceu.api.fluids.FluidConstants.*;

@SuppressWarnings("unused")
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
    @Setter
    private @Nullable MaterialIconType customIconType = null;
    @Setter
    private @Nullable MaterialIconSet customIconSet = null;

    private boolean hasFluidBlock = true;
    private boolean hasBucket = true;

    public FluidBuilder() {}

    /**
     * @param temperature the temperature of the fluid in Kelvin
     * @return this;
     */
    public FluidBuilder temperature(int temperature) {
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
    public FluidBuilder color(int color) {
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
    public FluidBuilder disableColor() {
        this.isColorEnabled = false;
        return this;
    }

    /**
     * Forcibly enables coloring the fluid. Use when you want to use a custom fluid texture and also tint it.
     *
     * @return this
     */
    public FluidBuilder forceEnableColor() {
        this.isColorEnabled = true;
        return this;
    }

    /**
     * @param density the density in g/cm^3
     * @return this
     */
    @Tolerate
    public FluidBuilder density(double density) {
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
    public FluidBuilder luminosity(int luminosity) {
        Preconditions.checkArgument(luminosity >= 0 && luminosity < 16, "luminosity must be >= 0 and < 16");
        this.luminosity = luminosity;
        return this;
    }

    /**
     * @param mcViscosity the MC viscosity of the fluid
     * @return this
     */
    public FluidBuilder viscosity(int mcViscosity) {
        Preconditions.checkArgument(mcViscosity >= 0, "viscosity must be >= 0");
        this.viscosity = mcViscosity;
        return this;
    }

    /**
     * @param viscosity the viscosity of the fluid in Poise
     * @return this
     */
    public FluidBuilder viscosity(double viscosity) {
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
    public FluidBuilder attribute(FluidAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    /**
     * @param attributes the attributes to add
     * @return this
     */
    public FluidBuilder attributes(FluidAttribute @NotNull... attributes) {
        Collections.addAll(this.attributes, attributes);
        return this;
    }

    /**
     * Mark this fluid as having a custom still texture
     * 
     * @return this
     */
    public FluidBuilder customStill() {
        return textures(true);
    }

    /**
     * @param hasCustomStill if the fluid has a custom still texture
     * @return this
     */
    public FluidBuilder textures(boolean hasCustomStill) {
        this.hasCustomStill = hasCustomStill;
        this.isColorEnabled = false;
        return this;
    }

    /**
     * @param hasCustomStill   if the fluid has a custom still texture
     * @param hasCustomFlowing if the fluid has a custom flowing texture
     * @return this
     */
    public FluidBuilder textures(boolean hasCustomStill, boolean hasCustomFlowing) {
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
    public FluidBuilder block() {
        this.hasFluidBlock = true;
        return this;
    }

    /**
     * Disables the auto-generated fluid bucket for the fluid
     *
     * @return this
     */
    public FluidBuilder disableBucket() {
        this.hasBucket = false;
        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Supplier<? extends Fluid> build(Material material, FluidStorageKey key, GTRegistrate registrate) {
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

        // noinspection DataFlowIssue
        var builder = registrate.fluid(this.name, this.still, this.flowing,
                (p, $1, $2) -> makeFluidType(registrate, p, material, key),
                (p) -> new GTFluid.Flowing(this.state, this.burnTime, p))
                .source((p) -> new GTFluid.Source(this.state, this.burnTime, p))
                .properties(p -> this.setupFluidTypeProperties(p, material))
                .fluidProperties(p -> this.setupFluidProperties(p, material))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .renderType(() -> RenderType::translucent);
        if (this.hasFluidBlock) {
            // noinspection Convert2MethodRef
            builder.block(GTLiquidBlock::new)
                    .properties(p -> p.liquid())
                    .color(() -> () -> (state, level, pos, index) -> {
                        if (this.isColorEnabled) {
                            return index == 0 ? this.color : material.getMaterialARGB(index);
                        } else {
                            return INFER_COLOR;
                        }
                    })
                    .register();
        } else {
            builder.noBlock();
        }
        if (this.hasBucket) {
            builder.bucket((fluid, properties) -> new GTBucketItem(fluid, properties, material))
                    .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                    .color(() -> () -> GTBucketItem::color)
                    .register();
        } else {
            builder.noBucket();
        }

        builder.onRegister(fluid -> {
            if (fluid.getSource() instanceof GTFluid gtSource) attributes.forEach(gtSource::addAttribute);
            if (fluid.getFlowing() instanceof GTFluid gtFlowing) attributes.forEach(gtFlowing::addAttribute);
        });
        return builder.register()::getSource;
    }

    private void determineName(Material material, @Nullable FluidStorageKey key) {
        if (name != null) return;
        if (material.isNull() || key == null) throw new IllegalArgumentException("Fluid must have a name");
        name = key.getRegistryNameFor(material);
    }

    @ApiStatus.Internal
    public void determineTextures(Material material, FluidStorageKey key) {
        MaterialIconType iconType = customIconType != null ? customIconType : key.getIconType();
        MaterialIconSet iconSet = customIconSet != null ? customIconSet : material.getMaterialIconSet();

        boolean usesSameTextureForStillAndFlowing = false;
        if (hasCustomStill || material.isNull()) {
            still = ResourceLocation.fromNamespaceAndPath(material.getModid(), "block/fluids/fluid." + name + "_still");
            if (!GTUtil.resourceExists(MaterialIconType.TEXTURE_ID_CONVERTER.idToFile(still))) {
                still = ResourceLocation.fromNamespaceAndPath(material.getModid(), "block/fluids/fluid." + name);
                usesSameTextureForStillAndFlowing = true;
            }
        } else {
            still = iconType.getBlockTexturePath(iconSet, "still", true);
            if (still.equals(GTModels.BLANK_TEXTURE)) {
                still = iconType.getBlockTexturePath(iconSet, null, true);
                usesSameTextureForStillAndFlowing = true;
            }
        }

        if (hasCustomFlowing) {
            flowing = ResourceLocation.fromNamespaceAndPath(material.getModid(),
                    "block/fluids/fluid." + name + "_flow");

            if (!GTUtil.resourceExists(MaterialIconType.TEXTURE_ID_CONVERTER.idToFile(flowing))) {
                // this is technically wrong, flowing fluids should have 32x32 textures (double the size of still ones)
                // it'll look weird if the still texture isn't a single color (note: gases are, so those are fine)
                flowing = still;
            }
        } else if (usesSameTextureForStillAndFlowing) {
            // same note as above
            flowing = still;
        } else {
            flowing = iconType.getBlockTexturePath(iconSet, "flow", true);
            if (flowing.equals(GTModels.BLANK_TEXTURE)) {
                // same note as above
                flowing = still;
            }
        }
    }

    private void determineTemperature(Material material) {
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

    private void determineColor(Material material) {
        if (color != INFER_COLOR) return;
        if (isColorEnabled && !material.isNull() && material.hasFluidColor()) {
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

    private void determineLuminosity(Material material) {
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

    private void determineViscosity(Material material) {
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

    private FluidType.Properties setupFluidTypeProperties(FluidType.Properties properties, Material material) {
        final boolean canSwim = this.density >= MIN_SWIMMABLE_DENSITY && this.viscosity <= MAX_SWIMMABLE_VISCOSITY;
        return properties.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                .temperature(this.temperature)
                .density(this.density)
                .lightLevel(this.luminosity)
                .viscosity(this.viscosity)
                .motionScale(0.014D * ((double) DEFAULT_LIQUID_VISCOSITY / this.viscosity))
                .canSwim(canSwim)
                .supportsBoating(canSwim);
    }

    @SuppressWarnings("DataFlowIssue")
    private BaseFlowingFluid.Properties setupFluidProperties(BaseFlowingFluid.Properties properties, Material material) {
        if (!this.hasFluidBlock) properties.block(null);
        if (!this.hasBucket) properties.bucket(null);

        // limit flow rate a maximum to 1 tick per step
        // (0 or negative values don't cause issues AFAIK, but they aren't good either.)
        return properties.tickRate(Math.max(this.viscosity / 200, 1));
    }

    private FluidType makeFluidType(AbstractRegistrate<?> owner, FluidType.Properties properties,
                                    Material material, FluidStorageKey key) {
        // sadly, we have to apply this here instead of in setupFluidTypeProperties because Registrate overwrites
        // whatever we assign there.
        final String langKey = this.translation != null ? this.translation : key.getTranslationKeyFor(material);
        properties.descriptionId(langKey);

        FluidType type = new GTFluidType(properties, material);
        OneTimeEventReceiver.addModListener(owner, RegisterClientExtensionsEvent.class, event -> {
            final int color = isColorEnabled ? this.color : INFER_COLOR;
            if (still == null || flowing == null) {
                this.determineTextures(material, key);
            }
            event.registerFluidType(new GTClientFluidTypeExtensions(type, still, flowing, color), type);
        });
        return type;
    }
}
