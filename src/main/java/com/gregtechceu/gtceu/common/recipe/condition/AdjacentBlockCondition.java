package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.recipe.condition.ConditionSerializeUtils.decodeHolderSets;
import static com.gregtechceu.gtceu.api.recipe.condition.ConditionSerializeUtils.encodeHolderSets;

@NoArgsConstructor
public class AdjacentBlockCondition extends RecipeCondition {

    // spotless:off
    public static final Codec<AdjacentBlockCondition> CODEC =
            RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
                    Codec.STRING.fieldOf("blockString").forGetter(AdjacentBlockCondition::getBlockString)
            ).apply(instance, AdjacentBlockCondition::new));
    // spotless:on

    @Getter
    private @NotNull String blockString = "";

    private @Nullable List<HolderSet<Block>> blocks = null;

    public void setBlocks(@NotNull List<HolderSet<Block>> blocks) {
        this.blocks = blocks;
        this.blockString = encodeHolderSets(blocks);
    }

    public List<HolderSet<Block>> getBlocks() {
        if (blocks == null) {
            blocks = decodeHolderSets(getBlockString(), Registries.BLOCK);
        }
        return blocks;
    }

    public AdjacentBlockCondition(@NotNull List<HolderSet<Block>> blocks) {
        setBlocks(blocks);
    }

    public AdjacentBlockCondition(boolean isReverse, @NotNull List<HolderSet<Block>> blocks) {
        super(isReverse);
        setBlocks(blocks);
    }

    public AdjacentBlockCondition(boolean isReverse, String blockString) {
        super(isReverse);
        this.blockString = blockString;
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
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.ADJACENT_BLOCK;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.adjacent_block.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getMachine().getLevel();
        BlockPos pos = recipeLogic.getMachine().getPos();
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

    public @NotNull List<HolderSet<Block>> getOrInitBlocks(@NotNull GTRecipe recipe) {
        if (getBlocks().isEmpty() || (recipe.data.contains("blockA") && recipe.data.contains("blockB"))) {
            List<HolderSet<Block>> blocks = new ArrayList<>();

            Block blockA = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockA")));
            if (!blockA.defaultBlockState().isAir()) {
                blocks.add(HolderSet.direct(blockA.builtInRegistryHolder()));
            }
            Block blockB = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockB")));
            if (!blockB.defaultBlockState().isAir()) {
                blocks.add(HolderSet.direct(blockB.builtInRegistryHolder()));
            }
            setBlocks(blocks);
        }
        return getBlocks();
    }

    @Override
    public RecipeCondition createTemplate() {
        return new AdjacentBlockCondition();
    }
}
