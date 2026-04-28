package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamSolidBoilerMachine extends SteamBoilerMachine {

    public static final Object2BooleanMap<net.minecraft.world.item.Item> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

    @SaveField
    public final NotifiableItemStackHandler fuelHandler, ashHandler;

    public SteamSolidBoilerMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure);
        this.fuelHandler = createFuelHandler().setFilter(itemStack -> {
            if (FluidUtil.getFluidContained(itemStack).isPresent()) {
                return false;
            }
            return FUEL_CACHE.computeIfAbsent(itemStack.getItem(), item -> {
                if (isRemote()) return true;
                return recipeLogic.getRecipeManager().recipeMap().byType(getRecipeType()).stream().anyMatch(recipe -> {
                    var list = recipe.value().inputs.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
                    if (!list.isEmpty()) {
                        return Arrays.stream(SizedIngredientExtensions.getItems(
                                ItemRecipeCapability.CAP.of(list.getFirst().content)))
                                .map(ItemStack::getItem).anyMatch(i -> i == item);
                    }
                    return false;
                });
            });
        });
        this.ashHandler = createAshHandler();
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableItemStackHandler createFuelHandler() {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
    }

    protected NotifiableItemStackHandler createAshHandler() {
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
                var input = SizedIngredientExtensions.getItems(ItemRecipeCapability.CAP.of(inputs.get(0).content));
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
    public ModularUI createUI(Player entityPlayer) {
        return SteamSolidBoilerMachineUI.create(this, entityPlayer);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        fuelHandler.dropInventoryInWorld();
        ashHandler.dropInventoryInWorld();
    }
}
