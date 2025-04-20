package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LargeMacerationTowerMachine extends WorkableElectricMultiblockMachine {

    @NotNull
    private AABB grindBound = new AABB(BlockPos.ZERO);
    @NotNull
    private List<NotifiableItemStackHandler> holders = List.of();

    private TickableSubscription hurtSub;

    public LargeMacerationTowerMachine(IMachineBlockEntity holder) {
        super(holder);
        updateBounds();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        updateBounds();
        for (var holder : getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (holder instanceof NotifiableItemStackHandler nish) {
                holders.add(nish);
            }
        }
        hurtSub = subscribeServerTick(this::hurtEntities);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribe(hurtSub);
        hurtSub = null;
    }

    @Override
    public void onUnload() {
        unsubscribe(hurtSub);
        hurtSub = null;
    }

    private void updateBounds() {
        var fl = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, 1, -1);
        var br = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 2, -2, -4);
        grindBound = new AABB(fl, br);
    }

    private void hurtEntities() {
        if (isRemote() || getLevel() == null) return;

        if (recipeLogic.isWorking()) {
            getLevel().getEntities(null, grindBound).forEach(
                    e -> e.hurt(e.damageSources().cramming(), 2.0f));
        }

        if (holders.isEmpty()) return;

        List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, grindBound);
        for (ItemEntity item : items) {
            if (item.isRemoved()) continue;
            for (var holder : holders) {
                item.setItem(ItemHandlerHelper.insertItem(holder, item.getItem(), false));
                if (item.getItem().isEmpty()) {
                    item.discard();
                }
            }
        }
    }
}
