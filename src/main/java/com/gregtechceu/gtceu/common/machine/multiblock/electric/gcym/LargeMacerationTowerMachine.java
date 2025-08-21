package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LargeMacerationTowerMachine extends WorkableElectricMultiblockMachine {

    @NotNull
    private AABB grindBound = new AABB(BlockPos.ZERO);
    @NotNull
    private final List<IItemHandler> handlers = new ArrayList<>();

    private TickableSubscription hurtSub;

    public LargeMacerationTowerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        updateBounds();
        for (var holder : getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (holder instanceof IItemHandler ih) {
                handlers.add(ih);
            }
        }
        hurtSub = subscribeServerTick(this::spinWheels);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribe(hurtSub);
        hurtSub = null;
        handlers.clear();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribe(hurtSub);
        hurtSub = null;
        handlers.clear();
    }

    private void updateBounds() {
        var fl = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, 1, -1);
        var br = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, -2, -4);
        grindBound = AABB.of(BoundingBox.fromCorners(fl, br));
    }

    private void spinWheels() {
        if (isRemote() || getLevel() == null) return;
        if (getOffsetTimer() % 10 != 0) return;

        List<ItemEntity> itemEntities = new ArrayList<>();
        for (var entity : getLevel().getEntities(null, grindBound)) {
            if (entity instanceof ItemEntity ie) {
                itemEntities.add(ie);
            } else {
                if (recipeLogic.isWorking()) {
                    entity.hurt(entity.damageSources().cramming(), 2.0f);
                }
            }
        }

        if (handlers.isEmpty()) return;

        for (ItemEntity item : itemEntities) {
            if (item.isRemoved()) continue;
            for (var holder : handlers) {
                item.setItem(ItemHandlerHelper.insertItem(holder, item.getItem(), false));
                if (item.getItem().isEmpty()) {
                    item.discard();
                    break;
                }
            }
        }
    }
}
