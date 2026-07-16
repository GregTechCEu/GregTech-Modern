package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagType;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.holder.HolderBuilder;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.builders.BuilderCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class TagPrefixBuilder<P> extends HolderBuilder<TagPrefix, TagPrefix, P, TagPrefixBuilder<P>> {

    @Setter
    private String idPattern;

    protected final List<TagType> tags = new ArrayList<>();
    @Setter
    public String langValue;

    @Setter
    private long materialAmount = -1;

    @Setter
    private boolean unificationEnabled;
    @Getter
    private boolean generateRecycling = false;
    @Setter
    private boolean generateItem;
    @Setter
    private TagPrefix.ItemConstructor itemConstructor = TagPrefixItem::new;
    @Setter
    private boolean generateBlock;
    @Setter
    private TagPrefix.BlockConstructor blockConstructor = MaterialBlock::new;
    @Setter
    private TagPrefix.BlockItemConstructor blockItemConstructor = MaterialBlockItem::new;
    @Setter
    private TagPrefix.BlockProperties blockProperties = new TagPrefix.BlockProperties(() -> RenderType::translucent,
            UnaryOperator.identity());

    @Setter
    private @Nullable Predicate<Material> generationCondition;

    @Setter
    private @Nullable MaterialIconType materialIconType;

    @Setter
    private Supplier<Table<Holder<TagPrefix>, Holder<Material>, ? extends Supplier<? extends ItemLike>>> itemTable;

    @Setter
    private @Nullable BiConsumer<Material, List<Component>> tooltip;

    @Setter
    private int maxStackSize = 64;

    protected final Set<TagKey<Block>> miningToolTags = new HashSet<>();

    public TagPrefixBuilder(GTRegistrate owner, P parent, String name, BuilderCallback callback) {
        super(owner, parent, name, callback, GTRegistries.Keys.TAG_PREFIX);

        this.idPattern = "%s_" + name;
        this.langValue = "%s " + FormattingUtil.toEnglishName(name);
    }

    public TagPrefixBuilder<P> registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                           BlockBehaviour.Properties properties, ResourceLocation baseModelLocation) {
        return registerOre(stoneType, material, properties, baseModelLocation, false);
    }

    public TagPrefixBuilder<P> registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                           BlockBehaviour.Properties properties, ResourceLocation baseModelLocation,
                                           boolean doubleDrops) {
        return registerOre(stoneType, material, properties, baseModelLocation, doubleDrops, false, false);
    }

    public TagPrefixBuilder<P> registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                           BlockBehaviour.Properties properties, ResourceLocation baseModelLocation,
                                           boolean doubleDrops, boolean isSand, boolean shouldDropAsItem) {
        return registerOre(stoneType, material, () -> properties, baseModelLocation, doubleDrops, isSand,
                shouldDropAsItem);
    }

    public TagPrefixBuilder<P> registerOre(Supplier<BlockState> stoneType, Supplier<Material> material,
                                           Supplier<BlockBehaviour.Properties> properties,
                                           ResourceLocation baseModelLocation, boolean doubleDrops, boolean isSand,
                                           boolean shouldDropAsItem) {
        onRegister(prefix -> {
            TagPrefix.ORES.put(prefix, new TagPrefix.OreType(stoneType, material, properties, baseModelLocation,
                    doubleDrops, isSand, shouldDropAsItem));
            if (shouldDropAsItem) {
                GTOreByProduct.addOreByProductPrefix(prefix);
            }
        });
        return this;
    }

    public TagPrefixBuilder<P> defaultTagPath(String path) {
        return this.defaultTagPath(path, false);
    }

    public TagPrefixBuilder<P> defaultTagPath(String path, boolean isVanilla) {
        this.tags.add(TagType.withDefaultFormatter(path, isVanilla));
        return this;
    }

    public TagPrefixBuilder<P> prefixTagPath(String path) {
        this.tags.add(TagType.withPrefixFormatter(path));
        return this;
    }

    public TagPrefixBuilder<P> prefixOnlyTagPath(String path) {
        this.tags.add(TagType.withPrefixOnlyFormatter(path));
        return this;
    }

    public TagPrefixBuilder<P> unformattedTagPath(String path) {
        return unformattedTagPath(path, false);
    }

    public TagPrefixBuilder<P> unformattedTagPath(String path, boolean isVanilla) {
        this.tags.add(TagType.withNoFormatter(path, isVanilla));
        return this;
    }

    public TagPrefixBuilder<P> customTagPath(String path, BiFunction<TagPrefix, Material, TagKey<Item>> formatter) {
        this.tags.add(TagType.withCustomFormatter(path, formatter));
        return this;
    }

    public TagPrefixBuilder<P> customTagPredicate(String path, boolean isVanilla, Predicate<Material> materialPredicate) {
        this.tags.add(TagType.withCustomFilter(path, isVanilla, materialPredicate));
        return this;
    }

    public TagPrefixBuilder<P> miningToolTag(TagKey<Block> tag) {
        this.miningToolTags.add(tag);
        return this;
    }

    public TagPrefixBuilder<P> blockProperties(Supplier<Supplier<RenderType>> renderType,
                                     UnaryOperator<BlockBehaviour.Properties> properties) {
        return this.blockProperties(new TagPrefix.BlockProperties(renderType, properties));
    }

    public TagPrefixBuilder<P> enableRecycling() {
        this.generateRecycling = true;
        return this;
    }

    @Override
    protected TagPrefix createEntry() {
        TagPrefix prefix = new TagPrefix(getOwner().makeResourceLocation(getName()), idPattern, langValue);
        prefix.tags().addAll(this.tags);
        prefix.materialAmount(this.materialAmount);
        prefix.unificationEnabled(this.unificationEnabled);
        prefix.generateRecycling(this.generateRecycling);

        prefix.generateItem(this.generateItem);
        prefix.itemConstructor(this.itemConstructor);
        prefix.generateBlock(this.generateBlock);
        prefix.blockConstructor(this.blockConstructor);
        prefix.blockItemConstructor(this.blockItemConstructor);
        prefix.blockProperties(this.blockProperties);
        prefix.generationCondition(this.generationCondition);

        prefix.materialIconType(this.materialIconType);
        prefix.itemTable(this.itemTable);
        prefix.tooltip(this.tooltip);
        prefix.maxStackSize(this.maxStackSize);

        prefix.miningToolTags().addAll(this.miningToolTags);

        return prefix;
    }
}
