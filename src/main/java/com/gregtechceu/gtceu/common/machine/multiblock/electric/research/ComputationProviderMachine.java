package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputationProviderMachine extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider, IControllable {
    public int allocatedCWUt = 0;
    @Persisted
    public long totalCWU = 0;
    public Boolean canBridge = true;
    public int maxCWUt = 0;
    @Nullable
    protected TickableSubscription tickSubs;

    String lastAllocatedCWUt = "";
    boolean canProvideCWUt = true;

    public ComputationProviderMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (!canProvideCWUt) return 0;
        return allocatedCWUt(cwut, simulate);
    }

    private int allocatedCWUt(int cwut, boolean simulate) {
        int maxCWUt = getMaxCWUt();
        int availableCWUt = maxCWUt - this.allocatedCWUt;
        int toAllocate = Math.min(cwut, (int) Math.min(availableCWUt, totalCWU));
        if (!simulate) {
            this.allocatedCWUt += toAllocate;
        }
        return toAllocate;
    }


    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (maxCWUt == 0)
            return maxCWUt = customCallback("getMaxCWUt", true, 0);
        else return maxCWUt;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (!isFormed()) return true;
        if (canBridge == null) return canBridge = customCallback("canBridge", null, true);
        return canBridge;
    }

    public void tick() {
        totalCWU -= allocatedCWUt;
        lastAllocatedCWUt = String.valueOf(allocatedCWUt);
        if(getRecipeLogic().isSuspend()){
            allocatedCWUt = 0;
            canProvideCWUt=false;
            return;
        }else{
            canProvideCWUt=true;
        }
        if(allocatedCWUt!=0){
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
            allocatedCWUt = 0;
        }else{
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
        }
        if (totalCWU < getMaxCWUt()) {
            totalCWU += customCallback("requestCWUt", null, 0);
            maxCWUt = 0;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    protected void updateTickSubscription() {
        if (isFormed) {
            tickSubs = subscribeServerTick(tickSubs, this::tick);
        } else if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
        }
    }

    @Override
    public void onChanged() {
        super.onChanged();
        maxCWUt = 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(true, allocatedCWUt > 0) // transform into two-state system for
            // display
            .setWorkingStatusKeys(
                "gtceu.multiblock.idling",
                "gtceu.multiblock.idling",
                "gtceu.multiblock.data_bank.providing")
            .addCustom(tl -> {
                if (isFormed()) {
                    // Provided Computation

                    Component cwutInfo = Component.literal(
                            lastAllocatedCWUt + " / " + getMaxCWUt() + " CWU/t")
                        .withStyle(ChatFormatting.AQUA);
                    Component totalCwu = Component.literal(
                            totalCWU + " CWU")
                        .withStyle(ChatFormatting.AQUA);
                    tl.add(Component.translatable(
                        "gtceu.multiblock.hpca.computation",
                        cwutInfo).withStyle(ChatFormatting.GRAY));
                    tl.add(Component.translatable(
                        "gtceu.multiblock.hpca.total",
                        totalCwu).withStyle(ChatFormatting.GRAY));
                }
            })
            .addWorkingStatusLine();
    }
}
