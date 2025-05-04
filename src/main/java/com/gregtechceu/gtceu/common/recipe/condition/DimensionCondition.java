package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.worldgen.DimensionMarker;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class DimensionCondition extends RecipeCondition<DimensionCondition> {

    // spotless:off
    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance)
            .and(ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionCondition::getDimension)
            ).apply(instance, DimensionCondition::new));
    // spotless:on

    @Getter
    private ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.withDefaultNamespace("dummy"));

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
        return Component.translatableEscape("recipe.condition.dimension.tooltip", dimension.location());
    }

    public SlotWidget setupDimensionMarkers(int xOffset, int yOffset) {
        DimensionMarker dimMarker = GTRegistries.DIMENSION_MARKERS.getOptional(this.dimension.location())
                .orElse(new DimensionMarker(DimensionMarker.MAX_TIER,
                        () -> Blocks.BARRIER, this.dimension.location().toString()));
        ItemStack icon = dimMarker.getIcon();
        CustomItemStackHandler handler = new CustomItemStackHandler(1);
        SlotWidget dimSlot = new SlotWidget(handler, 0, xOffset, yOffset, false, false)
                .setIngredientIO(IngredientIO.INPUT);
        handler.setStackInSlot(0, icon);
        if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
            dimSlot.setOverlay(
                    new TextTexture("T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier))
                            .scale(0.75f).transform(-3.0f, 5.0f));
        }
        return dimSlot;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && dimension == level.dimension();
    }

    @Override
    public DimensionCondition createTemplate() {
        return new DimensionCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("dimension", dimension.toString());
        return config;
    }

    @Override
    public DimensionCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        dimension = buf.readResourceKey(Registries.DIMENSION);
        return this;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeResourceKey(dimension);
    }
}
