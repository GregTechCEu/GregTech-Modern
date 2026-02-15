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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@NoArgsConstructor
public class AdjacentFluidCondition extends RecipeCondition<AdjacentFluidCondition> {

    // spotless:off
    public static final Codec<AdjacentFluidCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            GTCodecUtils.lazyParsingCodec(RegistryCodecs.homogeneousList(Registries.FLUID)).listOf()
                    .fieldOf("fluids").forGetter(AdjacentFluidCondition::getFluidSuppliers)
    ).apply(instance, AdjacentFluidCondition::new));
    // spotless:on

    private final List<Supplier<HolderSet<Fluid>>> fluids = new ArrayList<>();

    private final List<HolderSet<Fluid>> resolvedFluids = new ArrayList<>();

    private AdjacentFluidCondition(@NotNull List<Supplier<HolderSet<Fluid>>> fluids) {
        this(false, fluids);
    }

    private AdjacentFluidCondition(boolean isReverse, @NotNull List<Supplier<HolderSet<Fluid>>> fluids) {
        super(isReverse);
        this.fluids.addAll(fluids);
    }

    public AdjacentFluidCondition(@NotNull Collection<HolderSet<Fluid>> fluids) {
        this(false, fluids);
    }

    public AdjacentFluidCondition(boolean isReverse, @NotNull Collection<HolderSet<Fluid>> fluids) {
        super(isReverse);
        this.resolvedFluids.addAll(fluids);
        fluids.stream()
                .<Supplier<HolderSet<Fluid>>>map(holderSet -> () -> holderSet)
                .forEachOrdered(this.fluids::add);
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
    public RecipeConditionType<AdjacentFluidCondition> getType() {
        return GTRecipeConditions.ADJACENT_FLUID;
    }

    @Override
    public Component getTooltips() {
        var tooltips = Component.translatable("recipe.condition.adjacent_fluid.tooltip");
        fluids.forEach(set -> {
            var id = set.get().get(0).get().getFluidType().getDescription();
            tooltips.append(" ").append(id);
        });
        return tooltips;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getMachine().getLevel();
        BlockPos pos = recipeLogic.getMachine().getBlockPos();
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

    public @NotNull List<HolderSet<Fluid>> getOrInitFluids(@Nullable GTRecipe recipe) {
        if (resolvedFluids.isEmpty() && !fluids.isEmpty()) {
            for (var holderSetSupplier : this.fluids) {
                this.resolvedFluids.add(holderSetSupplier.get());
            }
        }
        if (!resolvedFluids.isEmpty()) {
            return resolvedFluids;
        }

        if (recipe != null && recipe.data.contains("fluidA") && recipe.data.contains("fluidB")) {
            this.resolvedFluids.clear();

            Fluid fluidA = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidA")));
            if (!fluidA.defaultFluidState().isEmpty()) {
                this.resolvedFluids.add(HolderSet.direct(fluidA.builtInRegistryHolder()));
            }
            Fluid fluidB = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidB")));
            if (!fluidB.defaultFluidState().isEmpty()) {
                this.resolvedFluids.add(HolderSet.direct(fluidB.builtInRegistryHolder()));
            }
            // init the fluid supplier list, just to be safe
            getFluidSuppliers();
        }
        return this.resolvedFluids;
    }

    private @NotNull List<Supplier<HolderSet<Fluid>>> getFluidSuppliers() {
        if (!this.fluids.isEmpty() || this.resolvedFluids.isEmpty()) {
            return this.fluids;
        }

        for (var holderSet : this.resolvedFluids) {
            this.fluids.add(() -> holderSet);
        }
        return this.fluids;
    }

    @Override
    public AdjacentFluidCondition createTemplate() {
        return new AdjacentFluidCondition();
    }
}
