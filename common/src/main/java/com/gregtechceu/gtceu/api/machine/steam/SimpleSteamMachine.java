package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.widget.LabelWidget;
import com.gregtechceu.gtlib.side.fluid.FluidHelper;
import com.gregtechceu.gtlib.side.fluid.IFluidStorage;
import com.gregtechceu.gtlib.syncdata.annotation.Persisted;
import com.gregtechceu.gtlib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        subscribeServerTick(this::tryDoVenting);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, importItems.storage);
        clearInventory(drops, exportItems.storage);
    }

    //////////////////////////////////////
    //******     Venting Logic    ******//
    //////////////////////////////////////

    public void tryDoVenting() {
        // check venting every 10 ticks
        if (getOffsetTimer() % 10 == 0) {
            checkVenting();
        }
    }

    @Override
    public float getVentingDamage() {
        return isHighPressure() ? 12F : 6F;
    }

    /**
     * Checks the venting state. Performs venting only if required.
     * <strong>Server-Side Only.</strong>
     *
     * @return if the machine does not need venting
     */
    protected boolean checkVenting() {
        if (needsVenting()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                tryDoVenting(serverLevel, getPos());
            } else {
                throw new IllegalStateException("Must be Sever-Side to check steam venting");
            }
        }

        return !needsVenting();
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
    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV || !checkVenting()) {
            return null;
        }

        return RecipeHelper.applyOverclock(new OverclockingLogic(false) {
            @Override
            protected LongIntPair runOverclockingLogic(@NotNull GTRecipe recipe, long recipeEUt, long maxVoltage, int duration, int amountOC) {
                return LongIntPair.of(isHighPressure ? recipeEUt * 2 : recipeEUt, isHighPressure ? duration : duration * 2);
            }
        }, recipe, GTValues.V[GTValues.LV]);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (!getLevel().isClientSide()) {
            needsVenting = true;
            checkVenting();
        }
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var group = recipeType.createUITemplate(recipeLogic::getProgressPercent, importItems.storage, exportItems.storage, new IFluidStorage[0], new IFluidStorage[0], true, isHighPressure);
        group.addSelfPosition(0, 20);
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isHighPressure))
                .widget(group)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new PredicatedImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure))
                        .setPredicate(recipeLogic::isHasNotEnoughEnergy))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(isHighPressure), 7, 84, true));
    }
}
