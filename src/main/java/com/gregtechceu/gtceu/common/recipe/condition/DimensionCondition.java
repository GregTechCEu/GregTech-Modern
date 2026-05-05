package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.widgets.ItemDisplayWidget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class DimensionCondition extends RecipeCondition<DimensionCondition> {

    // spotless:off
    public static final Codec<DimensionCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionCondition::getDimension)
    ).apply(instance, DimensionCondition::new));
    // spotless:on

    @Getter
    private ResourceKey<Level> dimension;

    public DimensionCondition(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public DimensionCondition(boolean isReverse, ResourceKey<Level> dimension) {
        super(isReverse);
        this.dimension = dimension;
    }

    @Override
    public RecipeConditionType<DimensionCondition> getType() {
        return GTRecipeConditions.DIMENSION;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.dimension.tooltip", dimension);
    }

    @Override
    public RecipeUIModifier modifyUI() {
        return (recipe, widget) -> {
                    DimensionMarker dimMarker = GTRegistries.DIMENSION_MARKERS.getOrDefault(this.dimension.location(),
                            new DimensionMarker(DimensionMarker.MAX_TIER, () -> Blocks.BARRIER,
                                    this.dimension.toString()));
                    ItemStack icon = dimMarker.getIcon();
                    String dimTier = "T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier);

                    ItemDisplayWidget displayWidget = new ItemDisplayWidget()
                            .item(icon)
                            .recipeSlotRole(RecipeSlotRole.CATALYST)
                            .posRel(0.75f, 0.75f);

                    if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
                        displayWidget.overlay(Text.str(dimTier).scale(0.75f));
                    }
                    widget.child(displayWidget);
                };
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && dimension.location().equals(level.dimension().location());
    }

    @Override
    public DimensionCondition createTemplate() {
        return new DimensionCondition();
    }
}
