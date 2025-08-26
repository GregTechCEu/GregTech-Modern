package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonObject;

import java.util.List;

import static com.gregtechceu.gtceu.api.recipe.OverclockLogicTest.equalFluidSets;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class GTRecipeSerializerTest {

    @GameTest(template = "empty_5x5")
    public static void serializeTest(GameTestHelper helper) {
        // Direct: water
        HolderSet<Fluid> waterSet = HolderSet.direct(Fluids.WATER.builtInRegistryHolder());

        // Tag: forge:lava
        TagKey<Fluid> lavaTag = TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava"));
        HolderSet<Fluid> lavaSet = GTRegistries.builtinRegistry()
                .registryOrThrow(Registries.FLUID)
                .getOrCreateTag(lavaTag);

        List<HolderSet<Fluid>> list = List.of(waterSet, lavaSet);

        var condition = new AdjacentFluidCondition();
        condition.setFluids(list);

        var jsonObject = condition.serialize();

        var back_to_condition = (AdjacentFluidCondition) RecipeCondition.deserialize(jsonObject);

        helper.assertTrue(equalFluidSets(condition.getFluids(), (back_to_condition).getFluids()),
                "Condition did not deserialize properly");
        JsonObject AFConditionJSON = new JsonObject();
        GTRecipeBuilder.ofRaw().addCondition(condition).toJson(AFConditionJSON);

        GTRecipe recipe = GTRecipeSerializer.SERIALIZER.fromJson(GTCEu.id("test"), AFConditionJSON);

        AFConditionJSON.get("config");

        helper.assertTrue(recipe.conditions.stream().anyMatch(x -> x instanceof AdjacentFluidCondition),
                "Fluid recipe condition did not deserialize properly");
        helper.succeed();
    }
}
