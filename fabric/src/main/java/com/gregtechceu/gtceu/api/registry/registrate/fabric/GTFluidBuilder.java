package com.gregtechceu.gtceu.api.registry.registrate.fabric;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.fabric.GTFluidImpl;
import com.gregtechceu.gtceu.api.item.fabric.GTBucketItem;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.FluidHelper;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTFluidBuilderImpl
 */
@SuppressWarnings("UnstableApiUsage")
@Accessors(chain = true, fluent = true)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTFluidBuilder<P> extends AbstractBuilder<Fluid, GTFluidImpl.Flowing, P, GTFluidBuilder<P>> implements IGTFluidBuilder {

    @FunctionalInterface
    interface AttributeHandlerProvider {
        FluidVariantAttributeHandler create(int temperature, int density, int luminance, int viscosity);
    }

    @Setter
    public int temperature = 300;
    @Setter
    public int density = 1000;
    @Setter
    public int luminance = 0;
    @Setter
    public int viscosity = 1000;
    @Setter
    public int color = -1;
    @Setter
    public int burnTime = -1;
    @Setter
    public FluidState state;

    private final String sourceName, bucketName;
    private final Material material;
    private final String langKey;

    private final ResourceLocation stillTexture, flowingTexture;

    @Nullable
    private AttributeHandlerProvider attributeHandler;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private @Nullable Supplier<Supplier<RenderType>> layer = null;

    @Nullable
    private NonNullSupplier<? extends GTFluid> source;
    @Nullable
    private NonNullSupplier<? extends LiquidBlock> block;
    @Nullable
    private NonNullSupplier<? extends BucketItem> bucket;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public GTFluidBuilder(AbstractRegistrate<?> owner, P parent, Material material, String name, String langKey, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.material = material;
        this.langKey = langKey;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.attributeHandler = (temperature, density, luminance, viscosity) -> new FluidVariantAttributeHandler() {

            @Override
            public Component getName(FluidVariant fluidVariant) {
                String key = "fluid." + BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid()).toLanguageKey();
                return I18n.exists(key) ? Component.translatable(key) : Component.translatable(langKey, material.getLocalizedName());
            }

            @Override
            public int getLuminance(FluidVariant variant) {
                return luminance;
            }

            @Override
            public int getTemperature(FluidVariant variant) {
                return temperature;
            }

            @Override
            public int getViscosity(FluidVariant variant, @org.jetbrains.annotations.Nullable Level world) {
                return viscosity;
            }

            @Override
            public boolean isLighterThanAir(FluidVariant variant) {
                return density < 1000;
            }
        };
        defaultBucket();
    }

    public GTFluidBuilder<P> defaultLang() {
        return lang(langKey != null ? langKey : material.getUnlocalizedName());
    }

    public GTFluidBuilder<P> lang(String name) {
        return lang(flowing -> FluidHelper.getDescriptionId(flowing.getSource()), name);
    }

    public GTFluidBuilder<P> renderType(Supplier<Supplier<RenderType>> layer) {
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    protected void registerRenderType(GTFluidImpl.Flowing entry) {
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
            if (this.layer != null) {
                RenderType layer = this.layer.get().get();
                BlockRenderLayerMap.INSTANCE.putFluids(layer, entry, getSource());
            }
        });
    }

    public GTFluidBuilder<P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return this;
    }

    public GTFluidBuilder<P> source(NonNullSupplier<? extends GTFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(factory::get);
        return this;
    }

    public GTFluidBuilder<P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    public BlockBuilder<LiquidBlock, GTFluidBuilder<P>> block() {
        return block1(LiquidBlock::new);
    }

    public <B extends LiquidBlock> BlockBuilder<B, GTFluidBuilder<P>> block(NonNullBiFunction<NonNullSupplier<GTFluidImpl.Flowing>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        NonNullSupplier<GTFluidImpl.Flowing> supplier = asSupplier();

        return getOwner().<B, GTFluidBuilder<P>>block(this, sourceName, p -> factory.apply(supplier, p))
                .properties(p -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                // fabric: luminance is fluid-sensitive, can't do this easily.
                // default impl will try to get it from the fluid's block, thus causing a loop.
                // if you want to do this, override getLuminance in FluidVariantAttributeHandler
                //.properties(p -> p.lightLevel(blockState -> fluidType.get().getLightLevel()))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                        .texture("particle", stillTexture)))
                .onRegister(block -> this.block = () -> block);
    }

    @SuppressWarnings("unchecked")
    public <B extends LiquidBlock> BlockBuilder<B, GTFluidBuilder<P>> block1(NonNullBiFunction<? extends GTFluidImpl.Flowing, BlockBehaviour.Properties, ? extends B> factory) {
        return block((supplier, settings) -> ((NonNullBiFunction<GTFluidImpl.Flowing, BlockBehaviour.Properties, ? extends B>) factory).apply(supplier.get(), settings));
    }

    public GTFluidBuilder<P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    public GTFluidBuilder<P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    public GTFluidBuilder<P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return this;
    }

    public ItemBuilder<GTBucketItem, GTFluidBuilder<P>> bucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return getOwner().item(this, bucketName, p -> new GTBucketItem(this.source, p, this.material))
                .onRegister(GTBucketItem::onRegister)
                .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                .color(() -> () -> GTBucketItem::color)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model(NonNullBiConsumer.noop())
                .onRegister(bucket -> this.bucket = () -> bucket);
    }

    @SafeVarargs
    public final GTFluidBuilder<P> tag(TagKey<Fluid>... tags) {
        GTFluidBuilder<P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                    prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    private GTFluid getSource() {
        NonNullSupplier<? extends GTFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    @Override
    protected GTFluidImpl.Flowing createEntry() {
        return new GTFluidImpl.Flowing(this.state, () -> this.source.get(), ()  -> this.get().get(), (() -> this.block != null ? this.block.get() : null), (() -> this.bucket != null ? this.bucket.get() : null), this.burnTime);
    }

    @Environment(EnvType.CLIENT)
    protected void registerDefaultRenderer(GTFluidImpl.Flowing flowing) {
        FluidRenderHandlerRegistry.INSTANCE.register(getSource(), flowing, new SimpleFluidRenderHandler(stillTexture, flowingTexture, color));
    }

    @Override
    public GTFluidBuilder<P> hasBlock(boolean hasBlock) {
        if (hasBlock && defaultBlock == null) {
            defaultBlock();
        }
        if (!hasBlock && defaultBlock != null) {
            noBlock();
        }
        return this;
    }

    @Override
    public IGTFluidBuilder hasBucket(boolean hasBucket) {
        if (hasBucket && defaultBucket == null) {
            defaultBucket();
        }
        if (!hasBucket && defaultBucket != null) {
            noBucket();
        }
        return this;
    }

    @Override
    public IGTFluidBuilder onFluidRegister(Consumer<Fluid> fluidConsumer) {
        return onRegister(fluidConsumer::accept);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RegistryEntry<GTFluidImpl.Flowing> register() {
        // Check the fluid has a type.
        if (this.attributeHandler != null) {
            // Register the type.
            onRegister(entry -> {
                FluidVariantAttributeHandler handler = attributeHandler.create(temperature, density, luminance, viscosity);
                FluidVariantAttributes.register(entry, handler);
                FluidVariantAttributes.register(getSource(), handler);
            });
        }

        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> onRegister(this::registerDefaultRenderer));

        if (defaultSource == Boolean.TRUE) {
            source(() -> new GTFluidImpl.Source(this.state, () -> this.source.get(), () -> this.get().get(), (() -> this.block != null ? this.block.get() : null), (() -> this.bucket != null ? this.bucket.get() : null), this.burnTime));
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends GTFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Registries.FLUID, (GTFluidBuilder) this, source::get);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        return super.register();
    }


    @Override
    public Supplier<? extends Fluid> registerFluid() {
        register();
        return this.source;
    }
}
