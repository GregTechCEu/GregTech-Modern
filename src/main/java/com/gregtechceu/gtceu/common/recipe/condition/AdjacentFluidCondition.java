package com.gregtechceu.gtceu.common.recipe.condition;

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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@NoArgsConstructor
public class AdjacentFluidCondition extends RecipeCondition {

    // spotless:off
    public static final Codec<List<HolderSet<Fluid>>> FLUID_CODEC = ExtraCodecs.lazyInitializedCodec(
            () -> RegistryCodecs.homogeneousList(Registries.FLUID).listOf()
    );

    public static final Codec<AdjacentFluidCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            FLUID_CODEC.fieldOf("fluids").forGetter(AdjacentFluidCondition::getFluids)
    ).apply(instance, AdjacentFluidCondition::new));
    // spotless:on

    @Getter
    @Setter
    private @NotNull List<HolderSet<Fluid>> fluids = new ArrayList<>();

    public AdjacentFluidCondition(@NotNull List<HolderSet<Fluid>> fluids) {
        this.fluids.addAll(fluids);
    }

    public AdjacentFluidCondition(boolean isReverse, @NotNull List<HolderSet<Fluid>> fluids) {
        super(isReverse);
        this.fluids.addAll(fluids);
    }

    public static AdjacentFluidCondition fromFluids(Collection<Fluid> fluids) {
        return new AdjacentFluidCondition(fluids.stream()
                .map(Fluid::builtInRegistryHolder)
                .<HolderSet<Fluid>>map(HolderSet::direct)
                .toList());
    }

    public static AdjacentFluidCondition fromFluids(Fluid... fluids) {
        return fromFluids(Arrays.asList(fluids));
    }

    public static AdjacentFluidCondition fromTags(Collection<TagKey<Fluid>> tags) {
        return new AdjacentFluidCondition(tags.stream()
                .<HolderSet<Fluid>>map(BuiltInRegistries.FLUID::getOrCreateTag)
                .toList());
    }

    @SafeVarargs
    public static AdjacentFluidCondition fromTags(TagKey<Fluid>... tags) {
        return fromTags(Arrays.asList(tags));
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.ADJACENT_FLUID;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.adjacent_fluid.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getMachine().getLevel();
        BlockPos pos = recipeLogic.getMachine().getPos();
        if (level == null) {
            return false;
        }
        Set<HolderSet<Fluid>> remainingFluids = new HashSet<>(getOrInitFluids(recipe));
        if (remainingFluids.isEmpty()) {
            return true;
        }

        for (BlockPos offset : GTUtil.NON_CORNER_NEIGHBOURS) {
            FluidState fluid = level.getFluidState(pos.offset(offset));
            if (!fluid.isSource()) continue;
            for (var it = remainingFluids.iterator(); it.hasNext();) {
                if (fluid.is(it.next())) {
                    it.remove();
                    break;
                }
            }
            if (remainingFluids.isEmpty()) return true;
        }
        return false;
    }

    public @NotNull List<HolderSet<Fluid>> getOrInitFluids(@NotNull GTRecipe recipe) {
        if (this.fluids.isEmpty() || (recipe.data.contains("fluidA") && recipe.data.contains("fluidB"))) {
            List<HolderSet<Fluid>> fluids = new ArrayList<>();

            Fluid fluidA = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidA")));
            if (!fluidA.defaultFluidState().isEmpty()) {
                fluids.add(HolderSet.direct(fluidA.builtInRegistryHolder()));
            }
            Fluid fluidB = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidB")));
            if (!fluidB.defaultFluidState().isEmpty()) {
                fluids.add(HolderSet.direct(fluidB.builtInRegistryHolder()));
            }

            this.fluids = fluids;
        }
        return this.fluids;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new AdjacentFluidCondition();
    }
}
