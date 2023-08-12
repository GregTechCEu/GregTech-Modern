package com.gregtechceu.gtceu.api.registry.registrate.fabric;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.fabric.GTBucketItem;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.*;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.FluidHelper;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
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
@Accessors(chain = true, fluent = true)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTFluidBuilder<T extends SimpleFlowableFluid, P> extends AbstractBuilder<Fluid, T, P, GTFluidBuilder<T, P>> implements IGTFluidBuilder {

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

    private final String sourceName, bucketName;
    private final Material material;
    private final String langKey;

    private final ResourceLocation stillTexture, flowingTexture;
    private final NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory;

    @Nullable
    private AttributeHandlerProvider attributeHandler = null;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<SimpleFlowableFluid.Properties> fluidProperties;

    private @Nullable Supplier<Supplier<RenderType>> layer = null;

    @Nullable
    private NonNullSupplier<? extends SimpleFlowableFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public GTFluidBuilder(AbstractRegistrate<?> owner, P parent, Material material, String name, String langKey, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.material = material;
        this.langKey = langKey;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
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
        this.fluidProperties = p -> {};
        defaultBucket();

//        String bucketName = this.bucketName;
//        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registry.ITEM_REGISTRY).get())
//                .block(() -> owner.<Block, LiquidBlock>get(name, Registry.BLOCK_REGISTRY).get());
    }

    public GTFluidBuilder<T, P> fluidProperties(NonNullConsumer<SimpleFlowableFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return this;
    }

    public GTFluidBuilder<T, P> defaultLang() {
        return lang(langKey != null ? langKey : material.getUnlocalizedName());
    }

    public GTFluidBuilder<T, P> lang(String name) {
        return lang(flowing -> FluidHelper.getDescriptionId(flowing.getSource()), name);
    }

    public GTFluidBuilder<T, P> renderType(Supplier<Supplier<RenderType>> layer) {
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    protected void registerRenderType(T entry) {
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
            if (this.layer != null) {
                RenderType layer = this.layer.get().get();
                BlockRenderLayerMap.INSTANCE.putFluids(layer, entry, getSource());
            }
        });
    }

    public GTFluidBuilder<T, P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return this;
    }

    public GTFluidBuilder<T, P> source(NonNullFunction<SimpleFlowableFluid.Properties, ? extends SimpleFlowableFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
        return this;
    }

    public GTFluidBuilder<T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    public BlockBuilder<LiquidBlock, GTFluidBuilder<T, P>> block() {
        return block1(LiquidBlock::new);
    }

    public <B extends LiquidBlock> BlockBuilder<B, GTFluidBuilder<T, P>> block(NonNullBiFunction<NonNullSupplier<? extends T>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        NonNullSupplier<T> supplier = asSupplier();

        this.fluidProperties.andThen(p -> p.block(() -> getOwner().<Block, LiquidBlock>get(getName(), Registries.BLOCK).get()));
        return getOwner().<B, GTFluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier, p))
                .properties(p -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                // fabric: luminance is fluid-sensitive, can't do this easily.
                // default impl will try to get it from the fluid's block, thus causing a loop.
                // if you want to do this, override getLuminance in FluidVariantAttributeHandler
                //.properties(p -> p.lightLevel(blockState -> fluidType.get().getLightLevel()))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                        .texture("particle", stillTexture)));
    }

    @SuppressWarnings("unchecked")
    public <B extends LiquidBlock> BlockBuilder<B, GTFluidBuilder<T, P>> block1(NonNullBiFunction<? extends T, BlockBehaviour.Properties, ? extends B> factory) {
        return block((supplier, settings) -> ((NonNullBiFunction<T, BlockBehaviour.Properties, ? extends B>) factory).apply(supplier.get(), settings));
    }

    @Beta
    public GTFluidBuilder<T, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    public GTFluidBuilder<T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    public ItemBuilder<GTBucketItem, GTFluidBuilder<T, P>> bucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        NonNullSupplier<? extends SimpleFlowableFluid> source = this.source;
        // TODO: Can we find a way to circumvent this limitation?
        if (source == null) {
            throw new IllegalStateException("Cannot create a bucket before creating a source block");
        }
        this.fluidProperties = p -> p.bucket(() -> getOwner().get(bucketName, Registries.ITEM).get());
        return getOwner().item(this, bucketName, p -> new GTBucketItem(this.source, p, this.material))
                .onRegister(GTBucketItem::onRegister)
                .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                .color(() -> () -> GTBucketItem::color)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model(NonNullBiConsumer.noop());
    }

    @SafeVarargs
    public final GTFluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
        GTFluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                    prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    private SimpleFlowableFluid getSource() {
        NonNullSupplier<? extends SimpleFlowableFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private SimpleFlowableFluid.Properties makeProperties() {
        NonNullSupplier<? extends SimpleFlowableFluid> source = this.source;
        SimpleFlowableFluid.Properties ret = new SimpleFlowableFluid.Properties(source == null ? null : source::get, asSupplier());
        fluidProperties.accept(ret);
        return ret;
    }

    @Override
    protected T createEntry() {
        return fluidFactory.apply(makeProperties());
    }

    @Environment(EnvType.CLIENT)
    protected void registerDefaultRenderer(T flowing) {
        FluidRenderHandlerRegistry.INSTANCE.register(getSource(), flowing, new SimpleFluidRenderHandler(stillTexture, flowingTexture, color));
    }

    @Override
    public GTFluidBuilder<T, P> hasBlock(boolean hasBlock) {
        if (hasBlock && defaultBlock == null) {
            defaultBlock();
        }
        if (!hasBlock && defaultBlock != null) {
            noBlock();
        }
        return this;
    }

    @Override
    public IGTFluidBuilder onFluidRegister(Consumer<Fluid> fluidConsumer) {
        return onRegister(fluidConsumer::accept);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public FluidEntry<T> register() {
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
            source(SimpleFlowableFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends SimpleFlowableFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Registries.FLUID, (GTFluidBuilder) this, source::get);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        return (FluidEntry<T>) super.register();
    }


    @Override
    public Supplier<? extends Fluid> registerFluid() {
        register();
        return this.source;
    }

    @Override
    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new FluidEntry<>(getOwner(), delegate);
    }
}
