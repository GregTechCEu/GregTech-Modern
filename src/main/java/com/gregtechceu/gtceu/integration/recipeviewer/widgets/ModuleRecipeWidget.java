package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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

public class ModuleRecipeWidget extends Flow {

    private final EquipmentFoundryRecipe recipe;

    public ModuleRecipeWidget(EquipmentFoundryRecipe recipe) {
        super(GuiAxis.Y);
        this.recipe = recipe;
        this.width(150)
                .coverChildrenHeight()
                .horizontalCenter()
                .padding(3);

        IntValue tier = new IntValue(0);

        var cycleWidget = new CycleButtonWidget()
                .length(recipe.getEntries().size())
                .background(IDrawable.NONE)
                .hoverBackground(IDrawable.NONE)
                .value(tier)
                .coverChildrenHeight()
                .fullWidth();
        for (int i = 0; i < recipe.getEntries().size(); i++) {
            cycleWidget.stateChild(i, getUIForTier(recipe.getEntries().get(i)));
        }
        this.child(cycleWidget);
    }

    private IWidget getUIForTier(EquipmentFoundryRecipe.TierEntry entry) {
        ItemModule module = entry.moduleForTier();

        ItemStack[] moduleItems = entry.ingredient().getItems();
        ItemStack[] allEquipment = getEquipment(recipe, module);

        List<ItemStack> allResults = Arrays.stream(allEquipment)
                .map(equipment -> getResult(recipe, equipment, moduleItems[0]))
                .toList();

        IModularItem defaultModularItem = GTCapabilityHelper.getModularItem(allResults.get(0));
        assert defaultModularItem != null;
        defaultModularItem.attach(module, moduleItems[0], false);
        ModuleData defaultAppliedModule = defaultModularItem.getModuleData(module);
        if (defaultAppliedModule == null) {
            GTCEu.LOGGER.error("Failed to attach default module to modular item preview in EMI for module {}. " +
                    "This means that the module's attachment predicate doesn't allow it to be attached to a completely empty modular item.", module.getId());
            return Flow.col();
        };
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
                                .value(ItemStackList.of(List.of(moduleItems))))
                        .child(new TextWidget<>(module.getDisplayName(defaultAppliedModule))
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
                .child(new TextWidget<>(module.getInfo()).horizontalCenter())
                .childIf(getTier(module) != -1, () -> new TextWidget<>(Component.literal(GTValues.VNF[getTier(module)]))
                        .right(3));
    }

    private static int getTier(ItemModule module) {
        if (module instanceof ITieredItemModule tiered) return tiered.getTier();
        return -1;
    }

    private static ItemStack[] getEquipment(EquipmentFoundryRecipe recipe, ItemModule module) {
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
