package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ITieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
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

    private static final ItemStack NO_ITEM = Items.BARRIER.getDefaultInstance()
            .setHoverName(Component.translatable("gtceu.equipment_foundry.gui.tier_too_high"));

    private final EquipmentFoundryRecipe recipe;
    private IntValue tier;

    public ModuleRecipeWidget(EquipmentFoundryRecipe recipe) {
        super(GuiAxis.Y);
        ItemModule[] modules = recipe.getModules();
        this.recipe = recipe;
        this.tier = new IntValue(0);
        this.width(150)
                .coverChildrenHeight()
                .horizontalCenter();
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
        ItemModule module = recipe.getModules()[tier];
        ItemStack[] allModuleItems = getModuleItems(recipe);
        ItemStack[] moduleItems = getModuleItems(recipe, module);
        ItemStack[] allEquipment = getEquipment(recipe, module);
        List<ItemStack> allResults = Arrays.stream(allEquipment)
                .map(equipment -> getResult(recipe, equipment, moduleItems[0], module))
                .toList();

        IModularItem defaultModularItem = GTCapabilityHelper.getModularItem(allResults.get(0));
        assert defaultModularItem != null;
        AppliedItemModule defaultAppliedModule = defaultModularItem.getModule(module);
        // noinspection UnstableApiUsage
        return Flow.col()
                .coverChildrenHeight()
                .horizontalCenter()
                .child(Flow.row()
                        .coverChildrenHeight()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(RecipeViewerSlotWidget.create()
                                .value(ItemStackList.of(List.of(allModuleItems))))
                        .child(new TextWidget<>(module.getDisplayName(defaultAppliedModule))
                                .scale(0.75f)
                                .width(100)))
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
                .child(new TextWidget<>(module.getInfo()))
                .childIf(getTier(module) != -1, () -> new TextWidget<>(Text.dynamic(
                        () -> Component.literal(GTValues.VNF[getTier(module)])))
                        .right(3));
    }

    private static int getTier(ItemModule module) {
        if (module instanceof ITieredItemModule tiered) return tiered.getTier();
        return -1;
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
}
