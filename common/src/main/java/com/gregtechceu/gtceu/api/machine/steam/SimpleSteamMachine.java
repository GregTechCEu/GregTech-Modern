package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.recipe.VentCondition;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import it.unimi.dsi.fastutil.longs.LongIntMutablePair;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
    public boolean needsVenting() {
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
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof SimpleSteamMachine steamMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV || !steamMachine.checkVenting()) {
                return null;
            }

            var modified = RecipeHelper.applyOverclock(new OverclockingLogic((_recipe, recipeEUt, maxVoltage, duration, amountOC) ->
                            LongIntMutablePair.of(steamMachine.isHighPressure ? recipeEUt * 2 : recipeEUt, steamMachine.isHighPressure ? duration : duration * 2)),
                    recipe, GTValues.V[GTValues.LV]);

            if (modified == recipe) {
                modified = recipe.copy();
            }

            modified.conditions.add(VentCondition.INSTANCE);
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
        var group = recipeType.createUITemplate(recipeLogic::getProgressPercent, importItems.storage, exportItems.storage, new IFluidStorage[0], new IFluidStorage[0], true, isHighPressure);
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
