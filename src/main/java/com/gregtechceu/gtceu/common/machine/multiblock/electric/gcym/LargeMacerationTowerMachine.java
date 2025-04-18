package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LargeMacerationTowerMachine extends WorkableElectricMultiblockMachine {

    private AABB grindBound;
    @NotNull
    private List<NotifiableItemStackHandler> holders = List.of();

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
                .requireNonNullElseGet(getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .filter(NotifiableItemStackHandler.class::isInstance)
                .map(NotifiableItemStackHandler.class::cast)
                .toList();

        subscribeServerTick(this::hurtEntities);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateBounds();

        subscribeServerTick(this::hurtEntities);
    }

    private void updateBounds() {
        var fl = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, 1, -1);
        var br = RelativeDirection.offsetPos(getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 2, -2, -4);
        grindBound = new AABB(fl, br);
    }

    private void hurtEntities() {
        if (isRemote() || getLevel() == null) return;

        if (recipeLogic.getStatus() == RecipeLogic.Status.WORKING) {
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
