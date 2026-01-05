package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamSolidBoilerMachine extends SteamBoilerMachine implements IMachineLife {

    public static final Object2BooleanMap<Item> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @SaveField
    public final NotifiableItemStackHandler fuelHandler, ashHandler;

    public SteamSolidBoilerMachine(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
        this.fuelHandler = createFuelHandler(args).setFilter(itemStack -> {
            if (FluidUtil.getFluidContained(itemStack).isPresent()) {
                return false;
            }
            return FUEL_CACHE.computeIfAbsent(itemStack.getItem(), item -> {
                if (isRemote()) return true;
                return recipeLogic.getRecipeManager().getAllRecipesFor(getRecipeType()).stream().anyMatch(recipe -> {
                    var list = recipe.inputs.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
                    if (!list.isEmpty()) {
                        return Arrays.stream(ItemRecipeCapability.CAP.of(list.get(0).content).getItems())
                                .map(ItemStack::getItem).anyMatch(i -> i == item);
                    }
                    return false;
                });
            });
        });
        this.ashHandler = createAshHandler(args);
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableItemStackHandler createFuelHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.BOTH);
    }

    protected NotifiableItemStackHandler createAshHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.OUT, IO.OUT);
    }

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
                var input = ItemRecipeCapability.CAP.of(inputs.get(0).content).getItems();
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
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_STEEL :
                GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_BRONZE;

        return super.buildUI(data, syncManager, settings)
                .child(new Column()
                        .coverChildren()
                        .right(18).top(7)
                        .childPadding(4)
                        .reverseLayout(true)
                        .child(new ItemSlot()
                                .slot(new ModularSlot(this.fuelHandler, 0)))
                        .child(new ProgressWidget()
                                .size(18)
                                .texture(progressTexture, 18)
                                .progress(recipeLogic::getProgressPercent)
                                .direction(ProgressWidget.Direction.UP))
                        .child(new ItemSlot()
                                .slot(new ModularSlot(this.ashHandler, 0))));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * return super.createUI(entityPlayer)
     * .widget(new SlotWidget(this.fuelHandler.storage, 0, 115, 62)
     * .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT_STEAM.get(isHighPressure),
     * GuiTextures.COAL_OVERLAY_STEAM.get(isHighPressure))))
     * .widget(new SlotWidget(this.ashHandler.storage, 0, 115, 26, true, false)
     * .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT_STEAM.get(isHighPressure),
     * GuiTextures.DUST_OVERLAY_STEAM.get(isHighPressure))))
     * .widget(new ProgressWidget(recipeLogic::getProgressPercent, 115, 44, 18, 18)
     * .setProgressTexture(
     * GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
     * GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5))
     * .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP));
     * }
     */

    @Override
    public void onMachineRemoved() {
        clearInventory(fuelHandler.storage);
        clearInventory(ashHandler.storage);
    }
}
