package com.gregtechceu.gtceu.api.machines.steam;

import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.guis.UITemplate;
import com.gregtechceu.gtceu.api.guis.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.gregtechceu.gtceu.api.machines.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machines.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machines.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machines.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machines.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.RecipeHelper;
import com.gregtechceu.gtceu.common.recipe.VentCondition;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleSteamMachine extends SteamWorkableMachine implements IExhaustVentMachine, IMachineModifyDrops, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SimpleSteamMachine.class, SteamWorkableMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableItemStackHandler importItems;
    @Persisted
    public final NotifiableItemStackHandler exportItems;
    @Setter
    @Persisted
    private boolean needsVenting;

    public SimpleSteamMachine(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
        this.importItems = createImportItemHandler(args);
        this.exportItems = createExportItemHandler(args);
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableFluidTank createSteamTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidHelper.getBucket(), IO.IN);
    }

    protected NotifiableItemStackHandler createImportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // Fine, we use it to provide eu cap for recipe, simulating an EU machine.
        capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP,
                List.of(new SteamEnergyRecipeHandler(steamTank, FluidHelper.getBucket() / 1000d)));
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, importItems.storage);
        clearInventory(drops, exportItems.storage);
    }

    //////////////////////////////////////
    //******     Venting Logic    ******//
    //////////////////////////////////////

    @Override
    public float getVentingDamage() {
        return isHighPressure() ? 12F : 6F;
    }

    @Override
    public @NotNull Direction getVentingDirection() {
        return getOutputFacing();
    }

    @Override
    public boolean isNeedsVenting() {
        return this.needsVenting;
    }

    @Override
    public void markVentingComplete() {
        this.needsVenting = false;
    }

    //////////////////////////////////////
    //******     Recipe Logic     ******//
    //////////////////////////////////////

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof SimpleSteamMachine steamMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV || !steamMachine.checkVenting()) {
                return null;
            }

            var modified = recipe.copy();
            modified.conditions.add(VentCondition.INSTANCE);

            if (!steamMachine.isHighPressure) {
                modified.duration *= 2;
                RecipeHelper.setInputEUt(modified, RecipeHelper.getInputEUt(recipe) / 2);
            }

            return modified;
        }
        return null;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        needsVenting = true;
        checkVenting();
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap<RecipeCapability<?>, Object>::new);
        storages.put(IO.IN, ItemRecipeCapability.CAP, importItems.storage);
        storages.put(IO.OUT, ItemRecipeCapability.CAP, exportItems.storage);

        var group = getRecipeType().getRecipeUI().createUITemplate(recipeLogic::getProgressPercent,
            storages,
            new CompoundTag(),
            Collections.emptyList(),
            true,
            isHighPressure);
        Position pos = new Position((Math.max(group.getSize().width + 4 + 8, 176) - 4 - group.getSize().width) / 2 + 4, 32);
        group.setSelfPosition(pos);
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isHighPressure))
                .widget(group)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new PredicatedImageWidget(pos.x + group.getSize().width / 2 - 9, pos.y + group.getSize().height / 2 - 9, 18, 18, GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure))
                        .setPredicate(recipeLogic::isWaiting))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(isHighPressure), 7, 84, true));
    }
}
