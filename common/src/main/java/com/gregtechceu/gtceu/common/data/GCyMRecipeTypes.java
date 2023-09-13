package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.common.recipe.RPMCondition;
import com.gregtechceu.gtceu.common.recipe.RockBreakerCondition;
import com.gregtechceu.gtceu.data.recipe.RecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.*;

/**
 * @author Rundas
 * @implNote Gregicality Multiblocks Recipe Types
 */
public class GCyMRecipeTypes {
    public static final String ELECTRIC = "electric";
    public static final String GENERATOR = "generator";
    public static final String MULTIBLOCK = "multiblock";

    public final static GTRecipeType LARGE_MIXER_RECIPES = register("large_mixer_recipes", ELECTRIC).setMaxIOSize(9, 1, 6, 1).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MIXER);

    public final static GTRecipeType LARGE_ENGRAVER_RECIPES = register("large_engraver_recipes", ELECTRIC).setMaxIOSize(2, 1, 1, 1).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.LENS_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ELECTROLYZER);

    public final static GTRecipeType LARGE_CENTRIFUGE_RECIPES = register("large_centrifuge_recipes", ELECTRIC).setMaxIOSize(2, 6, 2, 6).setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType BLAST_ALLOY_RECIPES = register("blast_alloy_smelter_recipes", ELECTRIC).setMaxIOSize(9, 0, 3, 1).setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setSound(GTSoundEntries.ARC);

    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTRecipeType(GTCEu.id(name), group, proxyRecipes);
        GTRegistries.register(Registry.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(Registry.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public static void init() {
        MIXER_RECIPES.onRecipeBuild((builder, provider) -> {
            assert LARGE_MIXER_RECIPES != null;
            LARGE_MIXER_RECIPES.copyFrom(builder).save(provider);
        });

        LASER_ENGRAVER_RECIPES.onRecipeBuild((builder, provider) -> {
            assert LARGE_ENGRAVER_RECIPES != null;
            LARGE_ENGRAVER_RECIPES.copyFrom(builder).save(provider);
        });

        CENTRIFUGE_RECIPES.onRecipeBuild((builder, provider) -> {
            assert LARGE_CENTRIFUGE_RECIPES != null;
            LARGE_CENTRIFUGE_RECIPES.copyFrom(builder).save(provider);
        });
    }
}
