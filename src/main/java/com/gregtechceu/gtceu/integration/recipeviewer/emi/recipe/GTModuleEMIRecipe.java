package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.value.IntValue;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class GTModuleEMIRecipe extends ModularUIEmiRecipe implements EmiRecipe {

    private static final ItemStack NO_ITEM = Items.BARRIER.getDefaultInstance()
            .setHoverName(Component.translatable("gtceu.equipment_foundry.gui.tier_too_high"));

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTCEu.id("equipment_foundry"),
            EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));

    private final EquipmentFoundryRecipe recipe;

    public GTModuleEMIRecipe(EquipmentFoundryRecipe recipe) {
        super(recipe.getId(), () -> createWidget(recipe));
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Stream
                .concat(Arrays.stream(recipe.getEquipment().getItems()),
                        Arrays.stream(recipe.getIngredient().getItems()))
                .filter(GTModuleEMIRecipe::isModular)
                .map(this::fromStack)
                .toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));
    }

    public static IWidget createWidget(EquipmentFoundryRecipe recipe) {
        IntValue actualValue = new IntValue(0);
        DynamicHandler handler = new DynamicHandler()
                .widgetProvider(() -> {
                    ItemModule module = recipe.getModules()[actualValue.getValue()];
                    ItemStack[] moduleItems = getModuleItems(recipe, module);
                    ItemStack[] allEquipment = getEquipment(recipe, module);
                    List<ItemStack> allResults = Arrays.stream(allEquipment)
                            .map(equipment -> getResult(recipe, equipment, moduleItems[0], module))
                            .toList();
                    return Flow.col()
                            .coverChildren()
                            .horizontalCenter()
                            .child(Flow.row()
                                    .coverChildren()
                                    .child(RecipeViewerSlotWidget.create()
                                            .value(ItemStackList.of(List.of(allEquipment)))
                                            .recipeSlotRole(RecipeSlotRole.INPUT))
                                    .child(GuiTextures.ADD.asWidget())
                                    .child(RecipeViewerSlotWidget.create()
                                            .value(ItemStackList.of(List.of(moduleItems)))
                                            .recipeSlotRole(RecipeSlotRole.INPUT))
                                    .child(GuiTextures.RIGHTLOAD.asWidget())
                                    .child(RecipeViewerSlotWidget.create()
                                            .value(ItemStackList.of(allResults))
                                            .recipeSlotRole(RecipeSlotRole.OUTPUT)))
                            .child(new TextWidget<>(module.getInfo()));
                });
        IntValue.Dynamic selectedIndex = new IntValue.Dynamic(actualValue::getValue, x -> {
            actualValue.setValue(x);
            handler.notifyUpdate();
        });
        ItemModule[] modules = recipe.getModules();
        ItemStack[] moduleItems = getModuleItems(recipe);
        return Flow.col()
                .coverChildrenWidth(150)
                .coverChildrenHeight()
                .horizontalCenter()
                .child(Flow.row()
                        .coverChildren()
                        .horizontalCenter()
                        .child(RecipeViewerSlotWidget.create()
                                .value(ItemStackList.of(List.of(moduleItems)))))
                .child(new DynamicWidget<>()
                        .clientOnlyHandler(handler))
                .childIf(modules.length > 0, () -> new CycleButtonWidget()
                        .length(modules.length)
                        .background(IDrawable.NONE)
                        .hoverBackground(IDrawable.NONE)
                        .value(selectedIndex)
                        .overlay(Text.dynamic(
                                () -> Component.literal(GTValues.VNF[getTier(modules[selectedIndex.getValue()])])))
                        .right(0));
    }

    private static int getTier(ItemModule module) {
        if (module instanceof ITieredItemModule tiered) return tiered.getTier();
        return 0;
    }

    private static boolean isModular(ItemStack stack) {
        return stack.getCapability(GTCapability.CAPABILITY_MODULAR_ITEM).isPresent();
    }

    private EmiIngredient fromStack(ItemStack stack) {
        return EmiIngredient.of(Ingredient.of(stack));
    }

    private static ItemStack[] getModuleItems(EquipmentFoundryRecipe recipe) {
        ItemStack[] stacks = recipe.getIngredient().getItems();
        return stacks.length == 0 ? new ItemStack[] { NO_ITEM } : stacks;
    }

    private static ItemStack[] getModuleItems(EquipmentFoundryRecipe recipe, ItemModule module) {
        ItemStack[] stacks = recipe.getIngredient().getItems();
        stacks = Arrays.stream(stacks)
                .filter(stack -> recipe.getModule(stack) == module)
                .toArray(ItemStack[]::new);
        return stacks.length == 0 ? new ItemStack[] { NO_ITEM } : stacks;
    }

    private static ItemStack[] getModuleItems(EquipmentFoundryRecipe recipe, ItemModule module, ItemStack equipment) {
        return Arrays.stream(getModuleItems(recipe, module))
                .filter(stack -> recipe.matches(new RecipeWrapper(new CombinedInvWrapper(
                        new CustomItemStackHandler(equipment),
                        new CustomItemStackHandler(stack))), 0))
                .toArray(ItemStack[]::new);
    }

    private static ItemStack[] getEquipment(EquipmentFoundryRecipe recipe, ItemModule module) {
        return Arrays.stream(recipe.getEquipment().getItems())
                .filter(stack -> stack.getCapability(GTCapability.CAPABILITY_MODULAR_ITEM).map(
                        modularItem -> modularItem.attach(module, true) != null).orElse(false))
                .toArray(ItemStack[]::new);
    }

    private static ItemStack getResult(EquipmentFoundryRecipe recipe, ItemStack equipment, ItemStack moduleItem,
                                       ItemModule module) {
        ItemStack copy = equipment.copy();
        if (moduleItem == NO_ITEM) {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(copy);
            if (modularItem != null) modularItem.attach(module, false);
            return copy;
        }
        RecipeWrapper wrapper = new RecipeWrapper(new CombinedInvWrapper(
                new CustomItemStackHandler(copy),
                new CustomItemStackHandler(moduleItem)));
        return recipe.assemble(wrapper, 0);
    }

    public static void addRecipes(EmiRegistry registry) {
        registry.getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get())
                .stream()
                .map(GTModuleEMIRecipe::new)
                .forEach(registry::addRecipe);
    }
}
