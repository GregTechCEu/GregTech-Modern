package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.forge.GTFluidImpl;
import com.gregtechceu.gtceu.api.item.GTBucketItem;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Accessors(chain = true, fluent = true)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTFluidBuilder<P> extends AbstractBuilder<Fluid, GTFluidImpl.Flowing, P, GTFluidBuilder<P>>
                           implements IGTFluidBuilder {

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

    @FunctionalInterface
    public interface FluidTypeFactory {

        FluidType create(String langKey, Material material, FluidType.Properties properties,
                         ResourceLocation stillTexture, ResourceLocation flowingTexture, int color);
    }

    private final String sourceName, bucketName;
    private final Material material;
    private final String langKey;

    private final ResourceLocation stillTexture, flowingTexture;

    @Nullable
    private final NonNullSupplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private @Nullable Supplier<RenderType> layer = null;

    private boolean registerType;

    @Nullable
    private NonNullSupplier<? extends GTFluid> source;
    @Nullable
    private NonNullSupplier<? extends LiquidBlock> block;
    @Nullable
    private NonNullSupplier<? extends BucketItem> bucket;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public GTFluidBuilder(AbstractRegistrate<?> owner, P parent, Material material, String name, String langKey,
                          BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
                          GTFluidBuilder.FluidTypeFactory typeFactory) {
        super(owner, parent, "flowing_" + name, callback, ForgeRegistries.Keys.FLUIDS);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.material = material;
        this.langKey = langKey;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(langKey, material, makeTypeProperties(),
                this.stillTexture, this.flowingTexture, this.color));
        this.registerType = true;
        defaultBucket();
    }

    public GTFluidBuilder<P> defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), langKey);
    }

    public GTFluidBuilder<P> lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }

    @SuppressWarnings("deprecation")
    public GTFluidBuilder<P> renderType(Supplier<RenderType> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get()),
                    "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    @SuppressWarnings("deprecation")
    protected void registerRenderType(GTFluidImpl.Flowing entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
                if (this.layer != null) {
                    RenderType layer = this.layer.get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                    ItemBlockRenderTypes.setRenderLayer(getSource(), layer);
                }
            });
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
        return block(LiquidBlock::new);
    }

    public <B extends LiquidBlock> BlockBuilder<B, GTFluidBuilder<P>> block(NonNullBiFunction<NonNullSupplier<GTFluidImpl.Flowing>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        NonNullSupplier<GTFluidImpl.Flowing> supplier = asSupplier();

        return getOwner().<B, GTFluidBuilder<P>>block(this, sourceName, p -> factory.apply(supplier, p))
                .properties(p -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())
                .properties(p -> p.lightLevel(blockState -> fluidType.get().getLightLevel()))
                .properties(p -> p.mapColor(GTUtil.determineMapColor(material.getMaterialRGB())))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                        .texture("particle", stillTexture)))
                .onRegister(block -> this.block = () -> block);
    }

    public GTFluidBuilder<P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    public GTFluidBuilder<P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return this;
    }

    public GTFluidBuilder<P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    public ItemBuilder<GTBucketItem, GTFluidBuilder<P>> bucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }

        return getOwner().item(this, bucketName, p -> new GTBucketItem(this.source, p, this.material, this.langKey))
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
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(),
                    ProviderType.FLUID_TAGS,
                    prov -> this.tags.stream().map(prov::addTag)
                            .forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    private GTFluid getSource() {
        NonNullSupplier<? extends GTFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        RegistryEntry<Block> block = getOwner().getOptional(sourceName, ForgeRegistries.Keys.BLOCKS);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent()) {
            properties.descriptionId(block.get().getDescriptionId());
        } else {
            // Fallback to material's name
            properties.descriptionId(langKey);
        }
        setData(ProviderType.LANG, NonNullBiConsumer.noop());

        return properties.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).temperature(temperature).density(density)
                .viscosity(viscosity).lightLevel(luminance);
    }

    @Override
    protected GTFluidImpl.Flowing createEntry() {
        return new GTFluidImpl.Flowing(this.state, () -> this.source.get(), () -> this.get().get(),
                (() -> this.block != null ? this.block.get() : null),
                (() -> this.bucket != null ? this.bucket.get() : null), this.burnTime, this.fluidType);
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
        if (this.fluidType != null) {
            // Register the type.
            if (this.registerType) {
                getOwner().simple(this, this.sourceName, ForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultSource == Boolean.TRUE) {
            source(() -> new GTFluidImpl.Source(this.state, () -> this.source.get(), () -> this.get().get(),
                    (() -> this.block != null ? this.block.get() : null),
                    (() -> this.bucket != null ? this.bucket.get() : null), this.burnTime, this.fluidType));
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends GTFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, ForgeRegistries.Keys.FLUIDS, (GTFluidBuilder) this, source::get);
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

    public static FluidType defaultFluidType(String langKey, Material material, FluidType.Properties properties,
                                             ResourceLocation stillTexture, ResourceLocation flowingTexture,
                                             int color) {
        return new FluidType(properties) {

            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new GTClientFluidTypeExtensions(stillTexture, flowingTexture, color));
            }

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
    }
}
