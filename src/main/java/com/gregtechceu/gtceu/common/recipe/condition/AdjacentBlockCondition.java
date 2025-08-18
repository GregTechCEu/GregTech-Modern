package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@NoArgsConstructor
public class AdjacentBlockCondition extends RecipeCondition {

    // spotless:off
    private static final Codec<List<HolderSet<Block>>> BLOCK_CODEC = ExtraCodecs.lazyInitializedCodec(
            () -> RegistryCodecs.homogeneousList(Registries.BLOCK).listOf()
    );

    public static final Codec<AdjacentBlockCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            BLOCK_CODEC.fieldOf("blocks").forGetter(AdjacentBlockCondition::getBlocks)
    ).apply(instance, AdjacentBlockCondition::new));
    // spotless:on

    @Getter
    @Setter
    private @NotNull List<HolderSet<Block>> blocks = new ArrayList<>();

    public AdjacentBlockCondition(@NotNull List<HolderSet<Block>> blocks) {
        this.blocks.addAll(blocks);
    }

    public AdjacentBlockCondition(boolean isReverse, @NotNull List<HolderSet<Block>> blocks) {
        super(isReverse);
        this.blocks.addAll(blocks);
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
        if (this.blocks.isEmpty() || (recipe.data.contains("blockA") && recipe.data.contains("blockB"))) {
            List<HolderSet<Block>> blocks = new ArrayList<>();

            Block blockA = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockA")));
            if (!blockA.defaultBlockState().isAir()) {
                blocks.add(HolderSet.direct(blockA.builtInRegistryHolder()));
            }
            Block blockB = BuiltInRegistries.BLOCK.get(new ResourceLocation(recipe.data.getString("blockB")));
            if (!blockB.defaultBlockState().isAir()) {
                blocks.add(HolderSet.direct(blockB.builtInRegistryHolder()));
            }
            this.blocks = blocks;
        }
        return this.blocks;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new AdjacentBlockCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();

        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        JsonElement blocksJson = Util.getOrThrow(BLOCK_CODEC.encodeStart(ops, this.blocks), IllegalStateException::new);
        config.add("blocks", blocksJson);

        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        this.blocks = BLOCK_CODEC.parse(ops, config.get("blocks")).result().orElse(new ArrayList<>());
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        this.blocks = buf.readWithCodec(ops, BLOCK_CODEC);
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        var ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        buf.writeWithCodec(ops, BLOCK_CODEC, this.blocks);
    }
}
