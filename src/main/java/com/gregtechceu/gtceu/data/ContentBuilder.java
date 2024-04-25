package com.gregtechceu.gtceu.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

import java.util.*;

public class ContentBuilder {

    public final Map<RecipeCapability<?>, List<Content>> content = new HashMap<>();
    @Setter
    public float chance = 1;
    @Setter
    public float tierChanceBoost = 0;
    @Setter
    public String slotName=null;
    @Setter
    public String uiName=null;
    public ResourceLocation id=new ResourceLocation("content_builder");
    

    @SafeVarargs
    public final <T> ContentBuilder addContent(RecipeCapability<T> capability, T... obj) {
        content.computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
            .map(capability::of)
            .map(o -> new Content(o, chance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }
    
    public ContentBuilder eu(long eu) {
        return addContent(EURecipeCapability.CAP, eu);
    }

    public ContentBuilder cwu(int cwu) {
        return addContent(CWURecipeCapability.CAP, cwu);
    }
    

    public ContentBuilder items(Ingredient... inputs) {
        return addContent(ItemRecipeCapability.CAP, inputs);
    }

    public ContentBuilder itemsIS(ItemStack... inputs) {
        for (ItemStack itemStack : inputs) {
            if (itemStack.isEmpty()) {
                GTCEu.LOGGER.error("gt recipe {} input items is empty", id);
                throw new IllegalArgumentException(id + ": input items is empty");
            }
        }
        return addContent(ItemRecipeCapability.CAP, Arrays.stream(inputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public ContentBuilder item(TagKey<Item> tag, int amount) {
        return items(SizedIngredient.create(tag, amount));
    }

    public ContentBuilder item(TagKey<Item> tag) {
        return item(tag, 1);
    }

    public ContentBuilder item(Item input, int amount) {
        return itemsIS(new ItemStack(input, amount));
    }

    public ContentBuilder item(Item input) {
        return items(SizedIngredient.create(new ItemStack(input)));
    }

//    public ContentBuilder item(Supplier<? extends Item> input) {
//        return item(input.get());
//    }
//
//    public ContentBuilder item(Supplier<? extends Item> input, int amount) {
//        return itemsIS(new ItemStack(input.get(), amount));
//    }

    public ContentBuilder item(TagPrefix orePrefix, Material material) {
        return tagItem(orePrefix, material, 1);
    }

    public ContentBuilder item(UnificationEntry input) {
        return tagItem(input.tagPrefix, input.material, 1);
    }

    public ContentBuilder item(UnificationEntry input, int count) {
        return tagItem(input.tagPrefix, input.material, count);
    }

    public ContentBuilder tagItem(TagPrefix orePrefix, Material material, int count) {
        TagKey<Item> tag = ChemicalHelper.getTag(orePrefix, material);
        if (tag == null) {
            return itemsIS(ChemicalHelper.get(orePrefix, material, count));
        }
        return item(tag, count);
    }

    public ContentBuilder machineItems(MachineDefinition machine) {
        return machineItems(machine, 1);
    }

    public ContentBuilder machineItems(MachineDefinition machine, int count) {
        return itemsIS(machine.asStack(count));
    }
    

    public ContentBuilder notConsumableI(ItemStack itemStack) {
        float lastChance = this.chance;
        this.chance = 0;
        itemsIS(itemStack);
        this.chance = lastChance;
        return this;
    }

    public ContentBuilder notConsumable(Ingredient ingredient) {
        float lastChance = this.chance;
        this.chance = 0;
        items(ingredient);
        this.chance = lastChance;
        return this;
    }

//    public ContentBuilder notConsumable(Item item) {
//        float lastChance = this.chance;
//        this.chance = 0;
//        item(item);
//        this.chance = lastChance;
//        return this;
//    }

//    public ContentBuilder notConsumable(Supplier<? extends Item> item) {
//        float lastChance = this.chance;
//        this.chance = 0;
//        item(item);
//        this.chance = lastChance;
//        return this;
//    }

    public ContentBuilder notConsumable(TagPrefix orePrefix, Material material) {
        float lastChance = this.chance;
        this.chance = 0;
        item(orePrefix, material);
        this.chance = lastChance;
        return this;
    }

    public ContentBuilder notConsumableFluidS(FluidStack fluid) {
        chance(fluid, 0, 0);
        return this;
    }

    public ContentBuilder notConsumableFluid(FluidIngredient ingredient) {
        float lastChance = this.chance;
        this.chance = 0;
        fluids(ingredient);
        this.chance = lastChance;
        return this;
    }

    public ContentBuilder circuitMeta(int configuration) {
        return notConsumable(IntCircuitIngredient.circuitInput(configuration));
    }
    public ContentBuilder circuit(int cfg){
        return circuitMeta(cfg);
    }

    public ContentBuilder chance(ItemStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        itemsIS(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public ContentBuilder chance(FluidStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        fluid(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public ContentBuilder fluid(FluidStack input) {
        return addContent(FluidRecipeCapability.CAP, FluidIngredient.of(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(input.getFluid()).getPath()), input.getAmount()));
    }

    public ContentBuilder fluids(FluidStack... inputs) {
        return addContent(FluidRecipeCapability.CAP, Arrays.stream(inputs).map(fluid -> {
            if (!Platform.isForge() && fluid.getFluid() == Fluids.WATER) { // Special case for fabric, because there all fluids have to be tagged as water to function as water when placed.
                return FluidIngredient.of(fluid);
            } else {
                return FluidIngredient.of(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()), fluid.getAmount());
            }
        }).toArray(FluidIngredient[]::new));
    }

    public ContentBuilder fluids(FluidIngredient... inputs) {
        return addContent(FluidRecipeCapability.CAP, inputs);
    }

    public ContentBuilder stress(float stress) {
        return addContent(StressRecipeCapability.CAP, stress);
    }

    public JsonObject capabilitiesToJson(Map<RecipeCapability<?>, List<Content>> contents) {
        JsonObject jsonObject = new JsonObject();
        contents.forEach((cap, list) -> {
            JsonArray contentsJson = new JsonArray();
            for (Content content : list) {
                contentsJson.add(cap.serializer.toJsonContent(content));
            }
            jsonObject.add(GTRegistries.RECIPE_CAPABILITIES.getKey(cap), contentsJson);
        });
        return jsonObject;
    }
    public Map<RecipeCapability<?>, List<Content>> build(){
        return content;
    }

}

