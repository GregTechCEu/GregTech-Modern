package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LargeMacerationTowerMachine extends WorkableElectricMultiblockMachine {

    private AABB grindBound;
    @Nullable
    private List<NotifiableItemStackHandler> holders = null;

    public LargeMacerationTowerMachine(IMachineBlockEntity holder) {
        super(holder);
        grindBound = new AABB(getPos());
        updateBounds();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        updateBounds();
        holders = Objects
                .requireNonNullElseGet(getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .filter(handler -> !handler.isProxy())
                .filter(NotifiableItemStackHandler.class::isInstance)
                .map(NotifiableItemStackHandler.class::cast)
                .toList();
    }

    @Override
    public boolean onWorking() {
        hurtEntities();
        return super.onWorking();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateBounds();
    }

    private void updateBounds() {
        Direction up = RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
        Direction back = getFrontFacing().getOpposite();
        Direction clockWise = getFrontFacing().getClockWise(up.getAxis());
        Direction counterClockWise = getFrontFacing().getCounterClockWise(up.getAxis());

        BlockPos middlePos = self().getPos()
                .offset(back.getNormal().multiply(2))
                .offset(up.getNormal().multiply(2));
        var fl = middlePos.offset(getFrontFacing().getNormal()).offset(clockWise.getNormal().multiply(2))
                .offset(up.getOpposite().getNormal());
        var br = middlePos.offset(back.getNormal().multiply(2)).offset(counterClockWise.getNormal());
        grindBound = new AABB(fl, br);
    }

    private void hurtEntities() {
        if (isRemote() || getLevel() == null) return;
        getLevel().getEntities(null, grindBound).forEach(
                e -> e.hurt(e.damageSources().cramming(), 2.0f));


        List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, grindBound);
        for (ItemEntity item : items) {
            if (item.isRemoved() || holder == null) continue;
            assert holders != null;
            for (var holder : holders) {
                item.setItem(ItemHandlerHelper.insertItem(holder, item.getItem(), false));
                if (item.getItem().isEmpty()) {
                    item.discard();
                }
            }
        }
    }
}
