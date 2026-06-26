package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamSolidBoilerMachine extends SteamBoilerMachine {

    public static final Object2BooleanMap<Item> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @SaveField
    public final NotifiableItemStackHandler fuelHandler, ashHandler;

    public SteamSolidBoilerMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure);
        this.fuelHandler = attachTrait(new NotifiableItemStackHandler(1, IO.IN, IO.IN));
        fuelHandler.setFilter(itemStack -> {
            if (FluidUtil.getFluidContained(itemStack).isPresent()) {
                return false;
            }
            return FUEL_CACHE.computeIfAbsent(itemStack.getItem(), item -> {
                if (isRemote()) return true;
                return recipeLogic.getRecipeManager().getAllRecipesFor(getRecipeType()).stream().anyMatch(recipe -> {
                    var list = recipe.value().inputs.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
                    if (!list.isEmpty()) {
                        return Arrays.stream(ItemRecipeCapability.CAP.of(list.getFirst().content()).getItems())
                                .map(ItemStack::getItem).anyMatch(i -> i == item);
                    }
                    return false;
                });
            });
        });
        this.ashHandler = attachTrait(new NotifiableItemStackHandler(1, IO.OUT, IO.OUT));
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput :
                ConfigHolder.INSTANCE.machines.smallBoilers.solidBoilerBaseOutput;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (recipeLogic.getLastRecipe() != null) {
            var inputs = recipeLogic.getLastRecipe().inputs.getOrDefault(ItemRecipeCapability.CAP,
                    Collections.emptyList());
            if (!inputs.isEmpty()) {
                var input = ItemRecipeCapability.CAP.of(inputs.getFirst().content()).getItems();
                if (input.length > 0) {
                    var remaining = getBurningFuelRemainder(input[0]);
                    if (!remaining.isEmpty()) {
                        ashHandler.insertItem(0, remaining, false);
                    }
                }
            }
        }
    }

    public static ItemStack getBurningFuelRemainder(ItemStack fuelStack) {
        float remainderChance;
        ItemStack remainder;
        var materialStack = ChemicalHelper.getMaterialStack(fuelStack);
        if (materialStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (materialStack.material() == GTMaterials.Charcoal) {
            remainder = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash);
            remainderChance = 0.3f;
        } else if (materialStack.material() == GTMaterials.Coal) {
            remainder = ChemicalHelper.get(TagPrefix.dust, GTMaterials.DarkAsh);
            remainderChance = 0.35f;
        } else if (materialStack.material() == GTMaterials.Coke) {
            remainder = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash);
            remainderChance = 0.5f;
        } else {
            return ItemStack.EMPTY;
        }
        return GTValues.RNG.nextFloat() <= remainderChance ? remainder : ItemStack.EMPTY;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        super.buildMainUI(mainWidget, guiData, syncManager, settings);

        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_STEEL :
                GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_BRONZE;

        DoubleSyncValue progressPercent = syncManager.getOrCreateSyncHandler("progressPercent", DoubleSyncValue.class,
                () -> new DoubleSyncValue(() -> {
                    if (recipeLogic == null) return -1f;
                    return recipeLogic.getProgressPercent();
                }));

        mainWidget.child(Flow.col()
                .coverChildren()
                .right(18).top(7)
                .childPadding(4)
                .reverseLayout(true)
                .child(new ItemSlot()
                        .slot(new ModularSlot(this.fuelHandler, 0)))
                .child(new ProgressWidget()
                        .size(18)
                        .texture(progressTexture, ProgressDrawable.Direction.UP)
                        .value(progressPercent))
                .child(new ItemSlot()
                        .slot(new ModularSlot(this.ashHandler, 0))));
    }
}
