package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.value.IntValue;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ModuleRecipeWidget extends Flow {

    private static final ItemStack NO_ITEM = Items.BARRIER.getDefaultInstance()
            .setHoverName(Component.translatable("gtceu.equipment_foundry.gui.tier_too_high"));

    private final EquipmentFoundryRecipe recipe;
    private IntValue tier;

    public ModuleRecipeWidget(EquipmentFoundryRecipe recipe) {
        super(GuiAxis.Y);
        ItemModuleType<?>[] modules = recipe.getModules();
        this.recipe = recipe;
        this.tier = new IntValue(0);
        this.width(150)
                .coverChildrenHeight()
                .horizontalCenter()
                .padding(3);
        var cycleWidget = new CycleButtonWidget()
                .length(modules.length)
                .background(IDrawable.NONE)
                .hoverBackground(IDrawable.NONE)
                .value(tier)
                .coverChildrenHeight()
                .fullWidth();
        for (int i = 0; i < modules.length; i++) {
            cycleWidget.stateChild(i, getUIForTier(i));
        }
        this.child(cycleWidget);
    }

    private IWidget getUIForTier(int tier) {
        ItemModuleType<?> module = recipe.getModules()[tier];
        ItemStack[] allModuleItems = getModuleItems(recipe);
        ItemStack[] moduleItems = getModuleItems(recipe, module);
        ItemStack[] allEquipment = getEquipment(recipe, module);
        List<ItemStack> allResults = Arrays.stream(allEquipment)
                .map(equipment -> getResult(recipe, equipment, moduleItems[0]))
                .toList();

        IModularItem defaultModularItem = GTCapabilityHelper.getModularItem(allResults.get(0));
        assert defaultModularItem != null;
        ItemModule defaultModule = module.defaultInstance().apply(ItemStack.EMPTY);

        // noinspection UnstableApiUsage
        return Flow.col()
                .coverChildrenHeight()
                .horizontalCenter()
                .childPadding(2)
                .child(Flow.row()
                        .coverChildrenHeight()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(RecipeViewerSlotWidget.create(ItemStack.class)
                                .value(ItemStackList.of(List.of(allModuleItems))))
                        .child(new TextWidget<>(defaultModule.getDisplayName())
                                .scale(0.75f)
                                .width(100)))
                .child(Flow.row()
                        .coverChildren()
                        .child(RecipeViewerSlotWidget.create(ItemStack.class)
                                .value(ItemStackList.of(List.of(allEquipment)))
                                .recipeSlotRole(RecipeSlotRole.INPUT))
                        .child(GuiTextures.ADD.asWidget())
                        .child(RecipeViewerSlotWidget.create(ItemStack.class)
                                .value(ItemStackList.of(List.of(moduleItems)))
                                .recipeSlotRole(RecipeSlotRole.INPUT))
                        .child(GuiTextures.RIGHTLOAD.asWidget())
                        .child(RecipeViewerSlotWidget.create(ItemStack.class)
                                .value(ItemStackList.of(allResults))
                                .recipeSlotRole(RecipeSlotRole.OUTPUT)))
                .child(new TextWidget<>(defaultModule.getInfo()).horizontalCenter())
                .childIf(getTier(defaultModule) != -1,
                        () -> new TextWidget<>(Component.literal(GTValues.VNF[getTier(defaultModule)]))
                                .right(3));
    }

    private static int getTier(ItemModule module) {
        if (module instanceof ITieredItemModule tiered) return tiered.getTier();
        return -1;
    }

    private static ItemStack[] getModuleItems(EquipmentFoundryRecipe recipe, int tier) {
        ItemStack[] stacks = Objects.requireNonNull(recipe.getIngredients()[tier]).getItems();
        return stacks.length == 0 ? new ItemStack[] { NO_ITEM } : stacks;
    }

    private static ItemStack[] getModuleItems(EquipmentFoundryRecipe recipe, ItemModuleType<?> module, int tier) {
        ItemStack[] stacks = Objects.requireNonNull(recipe.getIngredients()[tier]).getItems();
        stacks = Arrays.stream(stacks)
                .filter(stack -> recipe.getModules()[tier] == module)
                .toArray(ItemStack[]::new);
        return stacks.length == 0 ? new ItemStack[] { NO_ITEM } : stacks;
    }

    private static ItemStack[] getEquipment(EquipmentFoundryRecipe recipe, ItemModuleType<?> module) {
        return Arrays.stream(recipe.getEquipment().getItems())
                .filter(stack -> stack.getCapability(GTCapability.CAPABILITY_MODULAR_ITEM).map(
                        modularItem -> modularItem.attach(module, ItemStack.EMPTY, true) != null).orElse(false))
                .toArray(ItemStack[]::new);
    }

    private static ItemStack getResult(EquipmentFoundryRecipe recipe, ItemStack equipment, ItemStack moduleItem) {
        ItemStack copy = equipment.copy();
        recipe.applyToItem(copy, moduleItem, 0);
        return copy;
    }
}
