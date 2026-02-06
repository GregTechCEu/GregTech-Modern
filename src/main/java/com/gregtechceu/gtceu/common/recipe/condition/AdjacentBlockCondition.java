package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.codec.GTCodecUtils;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@NoArgsConstructor
public class AdjacentBlockCondition extends RecipeCondition<AdjacentBlockCondition> {

    // spotless:off
    public static final Codec<AdjacentBlockCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            GTCodecUtils.lazyParsingCodec(RegistryCodecs.homogeneousList(Registries.BLOCK)).listOf()
                    .fieldOf("blocks").forGetter(AdjacentBlockCondition::getBlockSuppliers)
    ).apply(instance, AdjacentBlockCondition::new));
    // spotless:on

    private final List<Supplier<HolderSet<Block>>> blocks = new ArrayList<>();

    private final List<HolderSet<Block>> resolvedBlocks = new ArrayList<>();

    private AdjacentBlockCondition(@NotNull List<Supplier<HolderSet<Block>>> blocks) {
        this(false, blocks);
    }

    private AdjacentBlockCondition(boolean isReverse, @NotNull List<Supplier<HolderSet<Block>>> blocks) {
        super(isReverse);
        this.blocks.addAll(blocks);
    }

    public AdjacentBlockCondition(@NotNull Collection<HolderSet<Block>> blocks) {
        this(false, blocks);
    }

    public AdjacentBlockCondition(boolean isReverse, @NotNull Collection<HolderSet<Block>> blocks) {
        super(isReverse);
        this.resolvedBlocks.addAll(blocks);
    }

    public static AdjacentBlockCondition fromBlocks(Collection<Block> blocks) {
        return new AdjacentBlockCondition(blocks.stream()
                .map(Block::builtInRegistryHolder)
                .<HolderSet<Block>>map(HolderSet::direct)
                .toList());
    }

    public static AdjacentBlockCondition fromBlocks(Block... blocks) {
        return fromBlocks(Arrays.asList(blocks));
    }

    public static AdjacentBlockCondition fromTags(Collection<TagKey<Block>> tags) {
        return new AdjacentBlockCondition(tags.stream()
                .<HolderSet<Block>>map(BuiltInRegistries.BLOCK::getOrCreateTag)
                .toList());
    }

    @SafeVarargs
    public static AdjacentBlockCondition fromTags(TagKey<Block>... tags) {
        return fromTags(Arrays.asList(tags));
    }

    @Override
    public RecipeConditionType<AdjacentBlockCondition> getType() {
        return GTRecipeConditions.ADJACENT_BLOCK;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.adjacent_block.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getMachine().getLevel();
        BlockPos pos = recipeLogic.getMachine().getBlockPos();
        if (level == null) {
            return false;
        }
        Set<HolderSet<Block>> remainingBlocks = new HashSet<>(getOrInitBlocks(recipe));
        if (remainingBlocks.isEmpty()) {
            return true;
        }

        for (BlockPos offset : GTUtil.NON_CORNER_NEIGHBOURS) {
            BlockState block = level.getBlockState(pos.offset(offset));
            for (var it = remainingBlocks.iterator(); it.hasNext();) {
                if (block.is(it.next())) {
                    it.remove();
                    break;
                }
            }
            if (remainingBlocks.isEmpty()) return true;
        }
        return false;
    }

    public @NotNull List<HolderSet<Block>> getOrInitBlocks(@Nullable GTRecipe recipe) {
        if (resolvedBlocks.isEmpty() && !blocks.isEmpty()) {
            for (var holderSetSupplier : this.blocks) {
                this.resolvedBlocks.add(holderSetSupplier.get());
            }
        }
        if (!resolvedBlocks.isEmpty()) {
            return resolvedBlocks;
        }

        if (recipe != null && recipe.data.contains("blockA") && recipe.data.contains("blockB")) {
            this.resolvedBlocks.clear();

            Block blockA = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockA")));
            if (!blockA.defaultBlockState().isAir()) {
                this.resolvedBlocks.add(HolderSet.direct(blockA.builtInRegistryHolder()));
            }
            Block blockB = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockB")));
            if (!blockB.defaultBlockState().isAir()) {
                this.resolvedBlocks.add(HolderSet.direct(blockB.builtInRegistryHolder()));
            }
            // init the block supplier list, just to be safe
            getBlockSuppliers();
        }
        return this.resolvedBlocks;
    }

    private @NotNull List<Supplier<HolderSet<Block>>> getBlockSuppliers() {
        if (!this.blocks.isEmpty() || this.resolvedBlocks.isEmpty()) {
            return this.blocks;
        }

        for (var holderSet : this.resolvedBlocks) {
            this.blocks.add(() -> holderSet);
        }
        return this.blocks;
    }

    @Override
    public AdjacentBlockCondition createTemplate() {
        return new AdjacentBlockCondition();
    }
}
