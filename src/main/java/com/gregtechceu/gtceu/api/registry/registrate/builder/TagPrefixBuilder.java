package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagType;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.TagPrefixEntry;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.google.common.collect.Table;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(fluent = true)
@SuppressWarnings("unused")
public class TagPrefixBuilder extends AbstractBuilder<TagPrefix, TagPrefix, GTRegistrate, TagPrefixBuilder> {

    @Getter
    @Setter
    private String idPattern = "%s_" + getName();

    @Getter
    @Setter
    private long materialAmount = -1;

    @Setter
    @Getter
    private boolean unificationEnabled = true;
    @Setter
    @Getter
    private boolean generateRecycling = false;
    @Setter
    private boolean generateItem = false;
    @Getter
    @Setter
    private TagPrefix.ItemConstructor itemConstructor = TagPrefixItem::new;
    @Setter
    private boolean generateBlock = false;
    @Getter
    @Setter
    private TagPrefix.BlockConstructor blockConstructor = MaterialBlock::createAndAddModel;
    @Getter
    @Setter
    private TagPrefix.BlockItemConstructor blockItemConstructor = MaterialBlockItem::new;
    @Getter
    private TagPrefix.BlockProperties blockProperties = new TagPrefix.BlockProperties(() -> RenderType::translucent,
            UnaryOperator.identity());

    @Getter
    @Setter
    private @Nullable Predicate<Material> generationCondition;

    @Nullable
    @Getter
    @Setter
    private MaterialIconType materialIconType;

    @Setter
    private @Nullable Supplier<Table<TagPrefix, Material, ? extends Supplier<? extends ItemLike>>> customLookupTable = null;

    @Nullable
    @Getter
    @Setter
    private BiConsumer<Material, List<Component>> tooltip;

    @Getter
    @Setter
    private int maxStackSize = Item.DEFAULT_MAX_STACK_SIZE;

    @Getter
    protected final Set<TagKey<Block>> miningToolTag = new HashSet<>();
    protected final List<TagType> tags = new ArrayList<>();

    protected @Nullable TagPrefix.OreType oreType = null;
    protected boolean shouldDropAsItem = false;

    public TagPrefixBuilder(GTRegistrate owner, String name, BuilderCallback callback) {
        super(owner, owner, name, callback, GTRegistries.Keys.TAG_PREFIX);
        this.defaultLang();
    }

    public TagPrefixBuilder defaultLang() {
        return lang("%s " + FormattingUtil.toEnglishName(getName()));
    }

    public TagPrefixBuilder lang(String name) {
        return lang(TagPrefix::getUnlocalizedName, name);
    }

    public TagPrefixBuilder enableRecycling() {
        this.generateRecycling = true;
        return this;
    }

    /**
     * Create a tag with a specified path with the "default" formatter, meaning there should be one "%s" format
     * specifier in the path, intended for the material name.
     */
    public TagPrefixBuilder defaultTag(String path) {
        return defaultTag(path, false);
    }

    /**
     * Create a tag with a specified path with the "default" formatter, meaning there should be one "%s" format
     * specifier in the path, intended for the material name.
     */
    public TagPrefixBuilder defaultTag(String path, boolean isVanilla) {
        this.tags.add(TagType.withDefaultFormatter(path, isVanilla));
        return this;
    }

    /**
     * Create a tag with a specified path with the "prefix" formatter, meaning there should be two "%s" format
     * specifiers in the path, with the first being the prefix name and the second being the material name.
     */
    public TagPrefixBuilder prefixTag(String path) {
        this.tags.add(TagType.withPrefixFormatter(path));
        return this;
    }

    /**
     * Create a tag with a specified path with the "prefix only" formatter, meaning there should be one "%s" format
     * specifier in the path, intended for the prefix name.
     */
    public TagPrefixBuilder prefixOnlyTag(String path) {
        this.tags.add(TagType.withPrefixOnlyFormatter(path));
        return this;
    }

    /**
     * Create a tag with a specified path with no formatter, meaning there should be <b>no</b> "%s" format specifiers in
     * the path and that it's usable as a tag key's path as is.
     */
    public TagPrefixBuilder unformattedTag(String path) {
        return unformattedTag(path, false);
    }

    /**
     * Create a tag with a specified path with no formatter, meaning there should be <b>no</b> "%s" format specifiers in
     * the path and that it's usable as a tag key's path as is.
     */
    public TagPrefixBuilder unformattedTag(String path, boolean isVanilla) {
        this.tags.add(TagType.withNoFormatter(path, isVanilla));
        return this;
    }

    /**
     * Tag all generated items and blocks with the given tag key as it is.
     */
    public TagPrefixBuilder tag(TagKey<?> tag) {
        this.tags.add(TagType.ofTagKey(tag));
        return this;
    }

    /**
     * Create a tag with a specified path with a custom formatter.
     */
    public TagPrefixBuilder customFormattedTag(String path, BiFunction<String, Material, TagKey<Item>> formatter) {
        this.tags.add(TagType.withCustomFormatter((self, mat) -> formatter.apply(path, mat)));
        return this;
    }

    /**
     * Create a tag with a specified path with no formatter, meaning there should be <b>no</b> "%s" format specifiers in
     * the path and that it's usable as a tag key's path as is.
     * <p>
     * This variant of the method accepts an additional argument for a filter that'll be used to decide whether a
     * specific material should be added to the tag.
     */
    public TagPrefixBuilder filteredUnformattedTag(String path, boolean isVanilla,
                                                   Predicate<Material> materialPredicate) {
        TagType entry = TagType.withNoFormatter(path, isVanilla);
        entry.setFilter(materialPredicate);
        this.tags.add(entry);
        return this;
    }

    /**
     * Create a tag with a specified path with a custom formatter.
     * <p>
     * This variant of the method accepts an additional argument for a filter that'll be used to decide whether a
     * specific material should be added to the tag.
     */
    public TagPrefixBuilder filteredCustomTag(String path, Predicate<Material> materialPredicate,
                                              BiFunction<String, Material, TagKey<Item>> formatter) {
        TagType entry = TagType.withCustomFormatter((self, mat) -> formatter.apply(path, mat));
        entry.setFilter(materialPredicate);
        this.tags.add(entry);
        return this;
    }

    public TagPrefixBuilder miningToolTag(TagKey<Block> tag) {
        this.miningToolTag.add(tag);
        return this;
    }

    public TagPrefixBuilder blockProperties(TagPrefix.BlockProperties blockProperties) {
        Objects.requireNonNull(blockProperties.renderType(),
                "Could not set blockProperties with null renderType in TagPrefix " +
                        getOwner().makeResourceLocation(getName()));
        Objects.requireNonNull(blockProperties.properties(),
                "Could not set blockProperties with null properties in TagPrefix " +
                        getOwner().makeResourceLocation(getName()));
        this.blockProperties = blockProperties;
        return this;
    }

    public TagPrefixBuilder blockProperties(Supplier<Supplier<RenderType>> renderType,
                                            UnaryOperator<BlockBehaviour.Properties> properties) {
        return this.blockProperties(new TagPrefix.BlockProperties(renderType, properties));
    }

    public TagPrefixBuilder registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                        BlockBehaviour.Properties properties, ResourceLocation baseModelLocation) {
        return registerOre(stoneType, material, properties, baseModelLocation, false);
    }

    public TagPrefixBuilder registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                        BlockBehaviour.Properties properties, ResourceLocation baseModelLocation,
                                        boolean doubleDrops) {
        return registerOre(stoneType, material, properties, baseModelLocation, doubleDrops, false, false);
    }

    public TagPrefixBuilder registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                        BlockBehaviour.Properties properties, ResourceLocation baseModelLocation,
                                        boolean doubleDrops, boolean isSand, boolean shouldDropAsItem) {
        return registerOre(stoneType, material, () -> properties, baseModelLocation, doubleDrops, isSand,
                shouldDropAsItem);
    }

    public TagPrefixBuilder registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                        Supplier<BlockBehaviour.Properties> properties,
                                        ResourceLocation baseModelLocation,
                                        boolean doubleDrops, boolean isSand, boolean shouldDropAsItem) {
        oreType = new TagPrefix.OreType(stoneType, material, properties, baseModelLocation, doubleDrops, isSand,
                shouldDropAsItem);
        this.shouldDropAsItem = shouldDropAsItem;
        return this;
    }

    @Override
    protected TagPrefix createEntry() {
        TagPrefix newPrefix = new TagPrefix(getOwner().makeResourceLocation(getName()))
                .idPattern(idPattern)
                .materialAmount(materialAmount)
                .unificationEnabled(unificationEnabled)
                .generateRecycling(generateRecycling)
                .generateItem(generateItem)
                .itemConstructor(itemConstructor)
                .generateBlock(generateBlock)
                .blockConstructor(blockConstructor)
                .blockItemConstructor(blockItemConstructor)
                .blockProperties(blockProperties)
                .generationCondition(generationCondition)
                .materialIconType(materialIconType)
                .customLookupTable(customLookupTable)
                .tooltip(tooltip)
                .maxStackSize(maxStackSize);

        newPrefix.setTags(tags);
        newPrefix.miningToolTag().addAll(miningToolTag);

        if (oreType != null) TagPrefix.ORES.put(newPrefix, oreType);
        if (shouldDropAsItem) {
            GTOreByProduct.addOreByProductPrefix(newPrefix);
        }

        return newPrefix;
    }

    @Override
    public GTRegistrate getOwner() {
        return (GTRegistrate) super.getOwner();
    }

    @Override
    protected RegistryEntry<TagPrefix, TagPrefix> createEntryWrapper(DeferredHolder<TagPrefix, TagPrefix> delegate) {
        return new TagPrefixEntry(getOwner(), delegate);
    }

    @Override
    public TagPrefixEntry register() {
        return (TagPrefixEntry) super.register();
    }
}
