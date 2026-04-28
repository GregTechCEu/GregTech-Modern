package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.SurfaceRockBlockItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.common.block.SurfaceRockBlock;
import com.gregtechceu.gtceu.common.item.GTBucketItem;
import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;
import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import java.util.stream.Stream;

public class GTModelProvider extends ModelProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;
    private final PackOutput output;

    public GTModelProvider(AbstractRegistrate<?> parent, PackOutput output) {
        super(output, parent.getModid());
        this.parent = parent;
        this.output = output;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        new GTBlockstateProvider(parent, output, blockModels.blockStateOutput, blockModels.itemModelOutput,
                blockModels.modelOutput, new ExistingFileHelper()).run();
        new RegistrateItemModelGenerator(parent, itemModels.itemModelOutput, itemModels.modelOutput).run();
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> !isRuntimeGeneratedBlock(holder.value()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return super.getKnownItems().filter(holder -> !isRuntimeGeneratedItem(holder.value()));
    }

    private static boolean isRuntimeGeneratedBlock(Block block) {
        return block instanceof MaterialBlock ||
                block instanceof PipeBlock<?, ?, ?> ||
                block instanceof SurfaceRockBlock;
    }

    private static boolean isRuntimeGeneratedItem(Item item) {
        return item instanceof TagPrefixItem ||
                item instanceof GTToolItem ||
                item instanceof GTArmorItem ||
                item instanceof GTBucketItem ||
                item instanceof MaterialBlockItem ||
                item instanceof MaterialPipeBlockItem ||
                item instanceof SurfaceRockBlockItem;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
